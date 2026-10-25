
import { useEffect, useState } from 'react'
import './App.css'

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

function App() {
  const [activePage, setActivePage] = useState('Overview')
  const [heartRate, setHeartRate] = useState(78)
  const [oxygen, setOxygen] = useState(98)
  const [steps, setSteps] = useState(6842)
  const [battery, setBattery] = useState(86)
  const [status, setStatus] = useState('NORMAL')
  const [backend, setBackend] = useState('Checking...')
  const [message, setMessage] = useState('Your readings are within the configured demonstration limits.')
  const [loading, setLoading] = useState(false)
  const [lastUpdated, setLastUpdated] = useState(new Date())

  useEffect(() => {
    fetch(`${API}/api/health`)
      .then((response) => {
        if (!response.ok) throw new Error('Backend unavailable')
        return response.json()
      })
      .then(() => setBackend('Connected'))
      .catch(() => setBackend('Demo mode'))
  }, [])

  async function assess(sos = false) {
    setLoading(true)

    const recordedAt = new Date()
    const payload = {
      heartRateBpm: heartRate,
      oxygenSaturationPercent: oxygen,
      recordedAt: recordedAt.toISOString(),
      sosTriggered: sos,
    }

    try {
      const response = await fetch(`${API}/api/assessment`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })

      if (!response.ok) throw new Error('Assessment request failed')

      const result = await response.json()
      setStatus(result.status)
      setMessage(result.message)
      setBackend('Connected')
      setLastUpdated(new Date())
    } catch {
      setBackend('Demo mode')
      const localStatus = sos
        ? 'SOS'
        : heartRate < 50 || heartRate > 120 || oxygen < 92
          ? 'ALERT'
          : 'NORMAL'

      setStatus(localStatus)
      setMessage(
        sos
          ? 'Demo SOS activated. No emergency contact has been notified.'
          : localStatus === 'ALERT'
            ? 'A configured demonstration safety threshold has been crossed.'
            : 'Demo assessment complete. No configured demonstration rule was triggered.'
      )
      setLastUpdated(new Date())
    } finally {
      setLoading(false)
    }
  }

  const statusInfo = {
    NORMAL: { label: 'All systems normal', color: 'safe', icon: '✓' },
    ALERT: { label: 'Attention required', color: 'warning', icon: '!' },
    SOS: { label: 'SOS activated', color: 'danger', icon: '!' },
    MONITORING_UNAVAILABLE: { label: 'Monitoring unavailable', color: 'warning', icon: '!' },
  }

  const currentStatus = statusInfo[status] || statusInfo.MONITORING_UNAVAILABLE

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">♥</div>
          <div>
            <h1>SafeBeat</h1>
            <span>SAFETY MONITORING</span>
          </div>
        </div>

        <div className="nav-label">WORKSPACE</div>
        <nav className="navigation">
          {[
            ['Overview', '◫'],
            ['Activity', '⌁'],
            ['Alerts', '♧'],
          ].map(([name, icon]) => (
            <button
              key={name}
              className={`nav-item ${activePage === name ? 'active' : ''}`}
              onClick={() => setActivePage(name)}
            >
              <span>{icon}</span>
              {name}
              {name === 'Alerts' && status !== 'NORMAL' && (
                <i className="nav-dot" />
              )}
            </button>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="device-card">
            <div className="device-top">
              <span className="device-symbol">⌚</span>
              <span className="connected-dot" />
            </div>
            <strong>SafeBeat Wearable</strong>
            <span className="muted small">Software simulator</span>
            <div className="device-battery">
              <span>Battery</span>
              <strong>{battery}%</strong>
            </div>
            <div className="battery-track">
              <div style={{ width: `${battery}%` }} />
            </div>
          </div>

          <div className="profile">
            <div className="avatar">Z</div>
            <div>
              <strong>My Dashboard</strong>
              <span>Personal account</span>
            </div>
            <span className="profile-menu">···</span>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">
            Workspace <span>/</span> <strong>{activePage}</strong>
          </div>
          <div className="topbar-right">
            <span className={`connection ${backend === 'Connected' ? 'online' : ''}`}>
              <i /> {backend}
            </span>
            <div className="top-avatar">Z</div>
          </div>
        </header>

        <div className="dashboard">
          <section className="welcome-row">
            <div>
              <div className="eyebrow">SUNDAY, OCTOBER 25, 2026</div>
              <h2>{activePage === 'Overview' ? 'Good evening 👋' : activePage}</h2>
              <p className="subtitle">
                {activePage === 'Overview'
                  ? 'Here’s your personal safety overview for today.'
                  : `Your SafeBeat ${activePage.toLowerCase()} at a glance.`}
              </p>
            </div>
            <button className="refresh-button" onClick={() => assess()} disabled={loading}>
              <span>↻</span> {loading ? 'Checking...' : 'Run assessment'}
            </button>
          </section>

          <section className={`status-banner ${currentStatus.color}`}>
            <div className="status-icon">{currentStatus.icon}</div>
            <div className="status-copy">
              <strong>{currentStatus.label}</strong>
              <span>{message}</span>
            </div>
            <div className="status-time">
              <span>LAST ASSESSED</span>
              <strong>{lastUpdated.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</strong>
            </div>
          </section>

          {activePage === 'Overview' && (
            <>
              <div className="section-heading">
                <div>
                  <h3>Live health readings</h3>
                  <p>Simulated wearable measurements</p>
                </div>
                <span className="live-label"><i /> LIVE SIMULATION</span>
              </div>

              <section className="metrics-grid">
                <article className="metric-card">
                  <div className="metric-heading">
                    <span className="metric-icon heart">♥</span>
                    <span className="metric-tag">HEART RATE</span>
                    <span className="metric-menu">···</span>
                  </div>
                  <div className="metric-value">{heartRate}<span>BPM</span></div>
                  <div className="metric-caption">Simulated heart rate</div>
                  <div className="metric-footer">
                    <span className="metric-line heart-line">〰 〰 〰 〰</span>
                    <span>Current reading</span>
                  </div>
                </article>

                <article className="metric-card">
                  <div className="metric-heading">
                    <span className="metric-icon oxygen">◉</span>
                    <span className="metric-tag">OXYGEN SATURATION</span>
                    <span className="metric-menu">···</span>
                  </div>
                  <div className="metric-value">{oxygen}<span>%</span></div>
                  <div className="metric-caption">Simulated SpO₂ reading</div>
                  <div className="metric-footer">
                    <span className="metric-line oxygen-line">▂ ▅ ▃ ▇ ▄ ▆</span>
                    <span>Current reading</span>
                  </div>
                </article>

                <article className="metric-card">
                  <div className="metric-heading">
                    <span className="metric-icon activity">⌁</span>
                    <span className="metric-tag">DAILY ACTIVITY</span>
                    <span className="metric-menu">···</span>
                  </div>
                  <div className="metric-value">{steps.toLocaleString()}<span>steps</span></div>
                  <div className="metric-caption">Daily simulated movement</div>
                  <div className="progress-track">
                    <div style={{ width: `${Math.min((steps / 10000) * 100, 100)}%` }} />
                  </div>
                  <div className="metric-footer">
                    <span>Daily goal</span>
                    <strong>{Math.round((steps / 10000) * 100)}%</strong>
                  </div>
                </article>

                <article className="metric-card">
                  <div className="metric-heading">
                    <span className="metric-icon battery">▰</span>
                    <span className="metric-tag">DEVICE BATTERY</span>
                    <span className="metric-menu">···</span>
                  </div>
                  <div className="metric-value">{battery}<span>%</span></div>
                  <div className="metric-caption">Wearable simulator battery</div>
                  <div className="progress-track battery-progress">
                    <div style={{ width: `${battery}%` }} />
                  </div>
                  <div className="metric-footer">
                    <span>Device status</span>
                    <strong>Connected</strong>
                  </div>
                </article>
              </section>

              <div className="content-grid">
                <section className="panel simulator-panel">
                  <div className="panel-heading">
                    <div>
                      <h3>Wearable simulator</h3>
                      <p>Adjust readings to test safety assessment</p>
                    </div>
                    <span className="simulator-chip">DEMO</span>
                  </div>

                  <div className="slider-control">
                    <div className="slider-label">
                      <label htmlFor="heart-rate">Heart rate</label>
                      <strong>{heartRate} <small>BPM</small></strong>
                    </div>
                    <input
                      id="heart-rate"
                      type="range"
                      min="30"
                      max="180"
                      value={heartRate}
                      onChange={(event) => setHeartRate(Number(event.target.value))}
                    />
                    <div className="range-labels"><span>30 BPM</span><span>180 BPM</span></div>
                  </div>

                  <div className="slider-control">
                    <div className="slider-label">
                      <label htmlFor="oxygen">Oxygen saturation</label>
                      <strong>{oxygen}<small>%</small></strong>
                    </div>
                    <input
                      id="oxygen"
                      type="range"
                      min="70"
                      max="100"
                      value={oxygen}
                      onChange={(event) => setOxygen(Number(event.target.value))}
                    />
                    <div className="range-labels"><span>70%</span><span>100%</span></div>
                  </div>

                  <div className="slider-control">
                    <div className="slider-label">
                      <label htmlFor="steps">Daily steps</label>
                      <strong>{steps.toLocaleString()}</strong>
                    </div>
                    <input
                      id="steps"
                      type="range"
                      min="0"
                      max="15000"
                      step="100"
                      value={steps}
                      onChange={(event) => setSteps(Number(event.target.value))}
                    />
                    <div className="range-labels"><span>0 steps</span><span>15,000 steps</span></div>
                  </div>

                  <button className="primary-button" onClick={() => assess()} disabled={loading}>
                    {loading ? 'Assessing...' : '↗  Evaluate readings'}
                  </button>
                </section>

                <section className="panel emergency-panel">
                  <div className="emergency-decoration">✳</div>
                  <div className="emergency-content">
                    <span className="emergency-kicker">EMERGENCY SUPPORT</span>
                    <h3>Need immediate help?</h3>
                    <p>Activate the SOS assessment to mark an emergency request in SafeBeat.</p>
                    <button
                      className="sos-button"
                      onClick={() => {
                        if (window.confirm('Activate the SafeBeat SOS assessment?')) assess(true)
                      }}
                      disabled={loading}
                    >
                      <span>✳</span> {loading ? 'Sending...' : 'Activate SOS'}
                    </button>
                    <span className="emergency-note">Software demonstration · No real emergency call is placed</span>
                  </div>
                </section>
              </div>
            </>
          )}

          {activePage === 'Activity' && (
            <section className="panel simple-panel">
              <h3>Daily activity</h3>
              <p>Your simulated daily movement is currently <strong>{steps.toLocaleString()} steps</strong>.</p>
              <div className="progress-track activity-progress">
                <div style={{ width: `${Math.min((steps / 10000) * 100, 100)}%` }} />
              </div>
              <p className="muted">Adjust the daily steps using the simulator on the Overview page.</p>
              <button className="primary-button" onClick={() => setActivePage('Overview')}>Back to overview</button>
            </section>
          )}

          {activePage === 'Alerts' && (
            <section className="panel simple-panel">
              <h3>Current assessment</h3>
              <div className={`alert-record ${currentStatus.color}`}>
                <strong>{status}</strong>
                <p>{message}</p>
                <span>{lastUpdated.toLocaleString()}</span>
              </div>
              <p className="muted">This view displays the latest assessment in this session. Historical alert retrieval is not yet available through the backend API.</p>
              <button className="primary-button" onClick={() => assess()}>Run new assessment</button>
            </section>
          )}

          <footer className="footer">
            <span>© 2026 SafeBeat · Software-simulated safety monitoring</span>
            <span>Not a medical diagnostic device</span>
          </footer>
        </div>
      </main>
    </div>
  )
}

export default App

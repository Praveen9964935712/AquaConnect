import { useState } from 'react';
import apiClient from '../api/client';
import { useAuth } from '../auth/AuthContext';

type SessionState = {
  id: string;
  callerIdentifier: string;
  language?: string;
  state?: string;
  selectedCategory?: string;
  description?: string;
  incidentId?: string;
  latitude?: number | null;
  longitude?: number | null;
  locationSource?: string;
  sessionStatus?: string;
};
type Complaint = { id: string; category: string; description: string; status?: string | null; priority?: string | null; reportedAt?: string; source?: string; latitude?: number | null; longitude?: number | null; locationSource?: string | null };

const callerIdentifier = 'dev-simulator';
const languages = ['ENGLISH', 'KANNADA', 'HINDI'];
const categories = ['PIPE_LEAK', 'PIPE_BURST', 'NO_WATER_SUPPLY', 'LOW_WATER_PRESSURE', 'CONTAMINATED_WATER', 'VALVE_ISSUE', 'OTHER'];
const menuItems = [
  { choice: 1, title: 'Report a water problem', description: 'Create an incident in the shared service workflow.' },
  { choice: 2, title: 'Check existing complaint', description: 'Look up caller-linked incidents for this session.' },
  { choice: 3, title: 'Water-supply information', description: 'No live supply-information endpoint is connected.' },
  { choice: 4, title: 'Request an operator', description: 'Queue a development escalation for operator follow-up.' }
];

export default function IvrSimulatorPage() {
  const { user } = useAuth();
  const [session, setSession] = useState<SessionState | null>(null);
  const [category, setCategory] = useState('PIPE_LEAK');
  const [description, setDescription] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [locationSource, setLocationSource] = useState('UNKNOWN');
  const [escalationReason, setEscalationReason] = useState('');
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busy, setBusy] = useState(false);

  async function run<T>(operation: () => Promise<T>, successMessage = '') {
    setBusy(true);
    setError('');
    setNotice('');
    try {
      await operation();
      if (successMessage) setNotice(successMessage);
    } catch {
      setError('The simulator request could not be completed. Check the session state and try again.');
    } finally {
      setBusy(false);
    }
  }

  async function startSession() {
    setSession(null);
    setComplaints([]);
    setDescription('');
    setLatitude('');
    setLongitude('');
    setLocationSource('UNKNOWN');
    setEscalationReason('');
    await run(async () => {
      const { data } = await apiClient.post<SessionState>('/api/ivr/sessions', { callerIdentifier });
      setSession(data);
    });
  }

  async function selectLanguage(language: string) {
    if (!session) return;
    await run(async () => {
      const { data } = await apiClient.post<SessionState>(`/api/ivr/sessions/${session.id}/language`, { language });
      setSession(data);
    });
  }

  async function chooseMenu(choice: number) {
    if (!session) return;
    await run(async () => {
      const { data } = await apiClient.post<SessionState>(`/api/ivr/sessions/${session.id}/menu`, { choice });
      setSession(data);
      if (data.state === 'CHECK_COMPLAINT') {
        const result = await apiClient.get<Complaint[]>(`/api/ivr/sessions/${session.id}/complaints`);
        setComplaints(result.data);
      }
      if (data.state === 'WATER_SUPPLY_INFO') setNotice('Water-supply information is not connected to a live service in this environment.');
      if (data.state === 'OPERATOR_REQUEST') setNotice('Add a short reason below to queue the operator escalation.');
    });
  }

  async function loadComplaints() {
    if (!session) return;
    await run(async () => {
      const { data } = await apiClient.get<Complaint[]>(`/api/ivr/sessions/${session.id}/complaints`);
      setComplaints(data);
    });
  }

  async function submitDetails(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!session) return;
    await run(async () => {
      const { data } = await apiClient.post<SessionState>(`/api/ivr/sessions/${session.id}/details`, { category, description });
      setSession(data);
      setDescription('');
    }, 'Details recorded in the IVR session.');
  }

  async function submitLocation(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!session) return;
    const hasLatitude = latitude.trim() !== '';
    const hasLongitude = longitude.trim() !== '';
    if (hasLatitude !== hasLongitude) {
      setError('Enter both latitude and longitude, or leave both blank.');
      return;
    }
    await run(async () => {
      const { data } = await apiClient.post<SessionState>(`/api/ivr/sessions/${session.id}/location`, {
        latitude: hasLatitude ? Number(latitude) : null,
        longitude: hasLongitude ? Number(longitude) : null,
        locationSource
      });
      setSession(data);
    }, 'Location metadata saved to this session.');
  }

  async function confirmIncident(confirmed: boolean) {
    if (!session) return;
    await run(async () => {
      const { data } = await apiClient.post<SessionState>(`/api/ivr/sessions/${session.id}/confirmation`, null, { params: { confirmed } });
      setSession(data);
    }, confirmed ? 'Incident created through the shared incident service.' : 'Session cancelled. You can start another simulation.');
  }

  async function escalate(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!session || !session.language) return;
    await run(async () => {
      await apiClient.post(`/api/ivr/sessions/${session.id}/escalate`, null, { params: { callerIdentifier, language: session.language, reason: escalationReason } });
      setEscalationReason('');
    }, 'Escalation queued for operator follow-up.');
  }

  const state = session?.state ?? 'IDLE';
  return <div className="stack">
    <header className="page-heading"><div><p className="eyebrow">Development / testing only</p><h1>IVR simulator</h1><p>This local interface exercises AquaConnect session APIs. It does not place or receive telephone calls.</p></div><span className="role-badge">{user?.roles.join(' · ') ?? 'Operator'}</span></header>
    {error && <p className="feedback error" role="alert">{error}</p>}
    {notice && <p className="feedback info" role="status">{notice}</p>}

    <div className="ivr-console">
      <section className="ivr-screen"><p className="eyebrow">Session console</p><h2>{session ? `Session ${session.id.slice(0, 8).toUpperCase()}` : 'No active session'}</h2><div className="session-facts"><Fact label="State" value={humanize(state)}/><Fact label="Language" value={session?.language ? humanize(session.language) : 'Not selected'}/><Fact label="Status" value={session?.sessionStatus ? humanize(session.sessionStatus) : 'Idle'}/><Fact label="Caller" value={session?.callerIdentifier ?? callerIdentifier}/></div>{session?.incidentId && <p className="ivr-incident">Incident created: <span className="mono">{session.incidentId}</span></p>}</section>
      <section className="panel"><div className="panel-header"><div><h2>Simulation controls</h2><p>Actions follow the current backend session state.</p></div></div><button type="button" disabled={busy} onClick={() => void startSession()}>{busy && !session ? 'Starting…' : session ? 'Start new session' : 'Start session'}</button>{session?.sessionStatus === 'FAILED' && <p className="field-help">This session ended. Start a new session to continue.</p>}</section>

      {session && ['START', 'LANGUAGE_SELECTION'].includes(state) && <section className="panel span-full"><div className="panel-header"><div><h2>Choose a language</h2><p>Language selection for the current session.</p></div></div><div className="control-row">{languages.map((language) => <button className="secondary" key={language} type="button" disabled={busy} onClick={() => void selectLanguage(language)}>{humanize(language)}</button>)}</div></section>}

      {session && state === 'MAIN_MENU' && <section className="panel span-full"><div className="panel-header"><div><h2>Main menu</h2><p>Select a supported action.</p></div></div><div className="menu-grid">{menuItems.map((item) => <button className="menu-choice" key={item.choice} type="button" disabled={busy} onClick={() => void chooseMenu(item.choice)}><span className="menu-number">0{item.choice}</span><strong>{item.title}</strong><span>{item.description}</span></button>)}</div><button className="ghost small" type="button" disabled={busy} onClick={() => void chooseMenu(9)}>Repeat menu</button></section>}

      {session && ['REPORT_PROBLEM', 'COLLECTING_DETAILS'].includes(state) && <section className="panel span-full"><div className="panel-header"><div><h2>Report a water problem</h2><p>Details are submitted to the existing IVR session workflow.</p></div></div><form className="form-row" onSubmit={submitDetails}><label>Issue category<select value={category} onChange={(event) => setCategory(event.target.value)}>{categories.map((item) => <option key={item}>{item}</option>)}</select></label><label className="description-field">Description<textarea required maxLength={2000} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Describe the issue"/></label><div className="form-actions description-actions"><button type="submit" disabled={busy || !description.trim()}>{busy ? 'Saving…' : 'Submit details'}</button></div></form></section>}

      {session && state === 'CONFIRMATION' && <section className="panel span-full">
        <div className="panel-header"><div><h2>Confirm incident creation</h2><p>Confirming creates an incident in the shared AquaConnect incident service.</p></div></div>
        <div className="confirmation-summary"><span className="status-badge">{humanize(session.selectedCategory ?? category)}</span><p>{session.description ?? description}</p><p className="field-help">Location is optional. Coordinates are supplied for this simulation; this does not emulate phone GPS.</p></div>
        <form className="location-form" onSubmit={submitLocation}>
          <label>Location source<select value={locationSource} onChange={(event) => setLocationSource(event.target.value)}><option value="UNKNOWN">Unknown / not provided</option><option value="GPS">GPS (simulated input)</option><option value="NETWORK">Network-provided</option><option value="REGISTERED_ADDRESS">Registered address</option><option value="OPERATOR">Operator-provided</option></select></label>
          <label>Latitude<input type="number" inputMode="decimal" min="-90" max="90" step="any" value={latitude} onChange={(event) => setLatitude(event.target.value)} placeholder="Optional" /></label>
          <label>Longitude<input type="number" inputMode="decimal" min="-180" max="180" step="any" value={longitude} onChange={(event) => setLongitude(event.target.value)} placeholder="Optional" /></label>
          <button className="secondary" type="submit" disabled={busy || (latitude.trim() === '') !== (longitude.trim() === '')}>{busy ? 'Saving…' : 'Save location'}</button>
        </form>
        {(session.latitude != null || session.longitude != null) && <p className="location-line">Session location: {session.latitude}, {session.longitude} · {humanize(session.locationSource ?? 'UNKNOWN')}</p>}
        <div className="form-actions"><button className="secondary" type="button" disabled={busy} onClick={() => void confirmIncident(false)}>Cancel session</button><button type="button" disabled={busy} onClick={() => void confirmIncident(true)}>{busy ? 'Submitting…' : 'Confirm and create incident'}</button></div>
      </section>}

      {session && state === 'CHECK_COMPLAINT' && <section className="panel span-full"><div className="panel-header"><div><h2>Caller complaint status</h2><p>Records returned by the authenticated session API.</p></div><button className="secondary small" type="button" disabled={busy} onClick={() => void loadComplaints()}>Refresh status</button></div>{complaints.length === 0 ? <div className="empty-state"><strong>No linked complaints returned</strong><p>The session service returned no incident records for this caller.</p></div> : <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Category</th><th>Status</th><th>Priority</th><th>Reported</th></tr></thead><tbody>{complaints.map((complaint) => <tr key={complaint.id}><td>{humanize(complaint.category)}</td><td><StatusBadge value={complaint.status ?? 'UNKNOWN'}/></td><td>{complaint.priority ? <PriorityBadge value={complaint.priority}/> : '—'}</td><td>{complaint.reportedAt ? new Date(complaint.reportedAt).toLocaleString() : '—'}</td></tr>)}</tbody></table></div>}</section>}

      {session && state === 'WATER_SUPPLY_INFO' && <section className="panel span-full"><div className="panel-header"><div><h2>Water-supply information</h2><p>No live supply-information API is currently connected.</p></div></div><div className="empty-state"><strong>Information service unavailable</strong><p>This simulator does not create or display fabricated supply schedules.</p></div></section>}

      {session && state === 'OPERATOR_REQUEST' && <section className="panel span-full"><div className="panel-header"><div><h2>Request an operator</h2><p>Queue a provider-neutral escalation through the existing authenticated IVR route.</p></div></div><form className="form-row" onSubmit={escalate}><label className="description-field">Reason<textarea required maxLength={1000} value={escalationReason} onChange={(event) => setEscalationReason(event.target.value)} placeholder="Briefly explain why operator assistance is needed"/></label><div className="form-actions description-actions"><button type="submit" disabled={busy || !escalationReason.trim() || !session.language}>{busy ? 'Requesting…' : 'Queue operator request'}</button></div></form></section>}

      {session && ['COMPLETED', 'FAILED'].includes(session.sessionStatus ?? '') && <section className="panel span-full"><div className="empty-state"><strong>{session.sessionStatus === 'COMPLETED' ? 'Session complete' : 'Session ended'}</strong><p>{session.incidentId ? `Incident reference ${session.incidentId}` : 'No incident was created.'} Start a new session to demonstrate another flow.</p></div></section>}
    </div>
  </div>;
}

function Fact({ label, value }: { label: string; value: string }) { return <div className="fact"><span>{label}</span><strong>{value}</strong></div>; }
function StatusBadge({ value }: { value: string }) { return <span className={`status-badge status-${value.toLowerCase()}`}>{humanize(value)}</span>; }
function PriorityBadge({ value }: { value: string }) { return <span className={`status-badge priority-${value.toLowerCase()}`}>{humanize(value)}</span>; }
function humanize(value: string) { return value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (letter: string) => letter.toUpperCase()); }
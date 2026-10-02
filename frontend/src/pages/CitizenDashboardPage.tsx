import { useEffect, useState } from 'react';
import apiClient from '../api/client';

type Incident = {
  id: string;
  category: string;
  description: string;
  source: string;
  latitude?: number | null;
  longitude?: number | null;
  locationSource?: string | null;
  locationAccuracy?: number | null;
  reportedAt?: string;
  status?: string | null;
  priority?: string | null;
};

const categories = ['PIPE_LEAK', 'PIPE_BURST', 'NO_WATER_SUPPLY', 'LOW_WATER_PRESSURE', 'CONTAMINATED_WATER', 'VALVE_ISSUE', 'OTHER'];
const statuses = ['SUBMITTED', 'UNDER_VERIFICATION', 'VERIFIED', 'ASSIGNED', 'IN_PROGRESS', 'REPAIR_COMPLETED', 'AUTHORITY_VERIFICATION', 'RESOLVED', 'CLOSED'];

export default function CitizenDashboardPage() {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [category, setCategory] = useState('PIPE_LEAK');
  const [description, setDescription] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [locationSource, setLocationSource] = useState('UNKNOWN');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadIncidents() {
    setError('');
    try {
      const { data } = await apiClient.get<Incident[]>('/api/incidents');
      setIncidents(data);
    } catch {
      setError('Unable to load your reports. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void loadIncidents(); }, []);

  async function submitIncident(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setNotice('');
    setSubmitting(true);
    const hasLatitude = latitude.trim() !== '';
    const hasLongitude = longitude.trim() !== '';
    if (hasLatitude !== hasLongitude) {
      setError('Enter both latitude and longitude, or leave both blank.');
      setSubmitting(false);
      return;
    }

    const payload = {
      category,
      description: description.trim(),
      latitude: hasLatitude ? Number(latitude) : null,
      longitude: hasLongitude ? Number(longitude) : null,
      locationSource,
      source: 'WEB'
    };
    try {
      const { data } = await apiClient.post<Incident>('/api/incidents', payload);
      setIncidents((current) => [data, ...current]);
      setDescription('');
      setLatitude('');
      setLongitude('');
      setLocationSource('UNKNOWN');
      setNotice(`Report submitted. Reference: ${data.id}`);
    } catch {
      setError('We could not submit your report. Check the details and try again.');
    } finally {
      setSubmitting(false);
    }
  }

  async function confirmResolved(incidentId: string, stillPresent: boolean) {
    setError('');
    setNotice('');
    try {
      await apiClient.post(`/api/incidents/${incidentId}/resolution-confirmation`, {
        decision: stillPresent ? 'STILL_PRESENT' : 'CONFIRMED'
      });
      setNotice(stillPresent ? 'Your report was sent back for follow-up.' : 'Thank you for confirming the repair.');
      await loadIncidents();
    } catch {
      setError('We could not submit your response. Please try again.');
    }
  }

  return (
    <div className="stack">
      <header className="page-heading">
        <div><p className="eyebrow">Citizen service</p><h1>Your water service</h1><p>Report an issue and follow its progress through the response process.</p></div>
        <a className="button" href="#report-issue">＋ Report a problem</a>
      </header>

      {notice && <p className="feedback success" role="status">{notice}</p>}
      {error && <p className="feedback error" role="alert">{error}</p>}

      <div className="section-grid">
        <section className="panel span-8" aria-labelledby="reports-title">
          <div className="panel-header"><div><h2 id="reports-title">Your reports</h2><p>Only incidents associated with your account appear here.</p></div><span className="chip">{incidents.length} total</span></div>
          {loading ? <div className="loading-state" role="status"><span className="spinner" aria-hidden="true"/><p>Loading your reports…</p></div> : incidents.length === 0 ? <div className="empty-state"><strong>No reports yet</strong><p>When you report a service issue, its status will appear here.</p></div> : (
            <div className="stack compact">
              {incidents.map((incident) => <IncidentCard key={incident.id} incident={incident} onConfirm={confirmResolved} />)}
            </div>
          )}
        </section>

        <section className="panel span-4" id="report-issue" aria-labelledby="report-title">
          <div className="panel-header"><div><h2 id="report-title">Report a problem</h2><p>Share what happened so the service team can review it.</p></div></div>
          <form className="stack" onSubmit={submitIncident}>
            <label>Issue type<select required value={category} onChange={(event) => setCategory(event.target.value)}>{categories.map((item) => <option key={item} value={item}>{humanize(item)}</option>)}</select></label>
            <label>Description<textarea required minLength={1} maxLength={2000} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Describe where you noticed the issue and what is happening." /></label>
            <p className="field-help">Do not include passwords or other sensitive information. Maximum 2,000 characters.</p>
            <details>
              <summary className="muted">Add optional location</summary>
              <div className="stack compact" style={{ marginTop: '0.8rem' }}>
                <label>Location source<select value={locationSource} onChange={(event) => setLocationSource(event.target.value)}><option value="UNKNOWN">Not provided</option><option value="GPS">GPS coordinates</option><option value="NETWORK">Network-provided</option><option value="REGISTERED_ADDRESS">Registered address</option><option value="OPERATOR">Provided to an operator</option></select></label>
                <div className="form-row">
                  <label>Latitude<input inputMode="decimal" type="number" min="-90" max="90" step="any" value={latitude} onChange={(event) => setLatitude(event.target.value)} placeholder="12.9716" /></label>
                  <label>Longitude<input inputMode="decimal" type="number" min="-180" max="180" step="any" value={longitude} onChange={(event) => setLongitude(event.target.value)} placeholder="77.5946" /></label>
                </div>
                <p className="field-help">Coordinates are optional and are not obtained automatically by this form.</p>
              </div>
            </details>
            <button type="submit" disabled={submitting || !description.trim()}>{submitting ? 'Submitting…' : 'Submit report'}</button>
          </form>
        </section>
      </div>
    </div>
  );
}

function IncidentCard({ incident, onConfirm }: { incident: Incident; onConfirm: (id: string, stillPresent: boolean) => void }) {
  const status = incident.status ?? 'SUBMITTED';
  const currentIndex = status === 'REOPENED' ? statuses.indexOf('UNDER_VERIFICATION') : statuses.indexOf(status);
  return (
    <article className="incident-card">
      <div className="incident-card-top">
        <div><p className="eyebrow">{humanize(incident.category)} · {incident.reportedAt ? new Date(incident.reportedAt).toLocaleDateString() : 'Date unavailable'}</p><h3>{incident.description}</h3></div>
        <StatusBadge value={status} />
      </div>
      <div className="chip-row"><span className={`status-badge priority-${(incident.priority ?? 'MEDIUM').toLowerCase()}`}>{humanize(incident.priority ?? 'Priority pending')}</span><span className="source-badge">{humanize(incident.source)}</span></div>
      {incident.latitude != null && incident.longitude != null && <p className="location-line">Location: {incident.latitude}, {incident.longitude} · {humanize(incident.locationSource ?? 'UNKNOWN')}</p>}
      {currentIndex >= 0 && <ol className="timeline" aria-label="Incident lifecycle">{statuses.filter((_, index) => index <= Math.max(currentIndex, 0)).map((step, index) => <li className={index === currentIndex ? 'current' : ''} key={step}>{humanize(step)}</li>)}</ol>}
      {status === 'REOPENED' && <p className="feedback info">Reopened for follow-up and returned to verification.</p>}
      {status === 'RESOLVED' && <div className="confirmation-actions"><span className="status-badge status-resolved">Repair marked resolved</span><button className="small secondary" type="button" onClick={() => onConfirm(incident.id, false)}>Confirm fixed</button><button className="small secondary" type="button" onClick={() => onConfirm(incident.id, true)}>Still needs attention</button></div>}
    </article>
  );
}

function StatusBadge({ value }: { value: string }) {
  return <span className={`status-badge status-${value.toLowerCase()}`}>{humanize(value)}</span>;
}

function humanize(value: string) {
  return value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (letter: string) => letter.toUpperCase());
}
import { useEffect, useState } from 'react';
import apiClient from '../api/client';

type WorkOrder = { id: string; incidentId: string; status: string; priority: string; startedAt?: string | null; inspectionNotes?: string; observedCondition?: string; repairNotes?: string };

type Notification = { id: string; title: string; message: string; read: boolean };

export default function FieldEngineerDashboardPage() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [selected, setSelected] = useState<WorkOrder | null>(null);
  const [findings, setFindings] = useState({ inspectionNotes: '', observedCondition: '', repairNotes: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  async function load() {
    setError('');
    try {
      const [ordersResponse, notificationsResponse] = await Promise.all([apiClient.get<WorkOrder[]>('/api/work-orders'), apiClient.get<Notification[]>('/api/notifications/unread')]);
      setOrders(ordersResponse.data);
      setNotifications(notificationsResponse.data);
    } catch { setError('Unable to load assigned work. Please try again.'); }
    finally { setLoading(false); }
  }
  useEffect(() => { load(); }, []);

  async function action(path: string) { if (!selected) return; setBusy(true); setError(''); try { await apiClient.post(`/api/work-orders/${selected.id}/${path}`); await load(); setSelected(null); } catch { setError('The requested workflow action was rejected. Refresh and try again.'); } finally { setBusy(false); } }
  async function complete(event: React.FormEvent) { event.preventDefault(); if (!selected) return; setBusy(true); setError(''); try { await apiClient.post(`/api/work-orders/${selected.id}/complete`, findings); await load(); setSelected(null); setFindings({ inspectionNotes: '', observedCondition: '', repairNotes: '' }); } catch { setError('Completion could not be submitted. Check your entries and try again.'); } finally { setBusy(false); } }
  async function upload(event: React.ChangeEvent<HTMLInputElement>, type: string) { const file = event.target.files?.[0]; if (!file || !selected) return; if (!file.type.startsWith('image/') || file.size > 10_000_000) { setError('Choose an image smaller than 10 MB.'); return; } const form = new FormData(); form.append('type', type); form.append('file', file); setBusy(true); setError(''); try { await apiClient.post(`/api/work-orders/${selected.id}/evidence`, form, { headers: { 'Content-Type': 'multipart/form-data' } }); } catch { setError('Evidence upload failed. Please retry.'); } finally { setBusy(false); event.target.value = ''; } }

  if (loading) return <div className="loading-state" role="status"><span className="spinner" aria-hidden="true"/><p>Loading assigned work…</p></div>;

  return <div className="stack">
    <header className="page-heading"><div><p className="eyebrow">Field operations</p><h1>My work orders</h1><p>Review assigned work, record findings, and submit repair evidence.</p></div><button className="secondary" type="button" onClick={() => void load()}>Refresh</button></header>
    {notifications.length > 0 && <p className="feedback info" role="status">{notifications.length} unread notifications</p>}
    {error && <p className="feedback error" role="alert">{error}</p>}
    <div className="section-grid">
      <section className="panel span-5"><div className="panel-header"><div><h2>Assigned work</h2><p>Select a work order to view its workflow.</p></div><span className="chip">{orders.length}</span></div>
        {orders.length === 0 ? <div className="empty-state"><strong>No assigned work</strong><p>New work orders assigned to you will appear here.</p></div> : <div className="engineer-order-list">{orders.map((order) => <button className={`engineer-order ${selected?.id === order.id ? 'selected' : ''}`} key={order.id} type="button" aria-pressed={selected?.id === order.id} onClick={() => setSelected(order)}><span className="engineer-order-top"><StatusBadge value={order.status}/><PriorityBadge value={order.priority}/></span><span className="engineer-order-id">Incident {order.incidentId.slice(0, 8).toUpperCase()}</span><span className="muted">Work order {order.id.slice(0, 8).toUpperCase()}</span></button>)}</div>}
      </section>
      <section className="panel span-7">{selected ? <>
        <div className="panel-header"><div><p className="eyebrow">Selected work order</p><h2>{selected.id.slice(0, 8).toUpperCase()}</h2><p>Incident {selected.incidentId}</p></div><StatusBadge value={selected.status}/></div>
        {error && <p className="feedback error" role="alert">{error}</p>}
        <dl className="detail-grid"><div><dt>Priority</dt><dd><PriorityBadge value={selected.priority}/></dd></div><div><dt>Incident reference</dt><dd className="mono">{selected.incidentId}</dd></div><div><dt>Started</dt><dd>{selected.startedAt ? new Date(selected.startedAt).toLocaleString() : 'Not started'}</dd></div></dl>
        <div className="form-actions workflow-actions">{selected.status === 'ASSIGNED' && <button type="button" disabled={busy} onClick={() => void action('accept')}>{busy ? 'Updating…' : 'Accept work'}</button>}{selected.status === 'ACCEPTED' && <button type="button" disabled={busy} onClick={() => void action('start')}>{busy ? 'Updating…' : 'Start inspection'}</button>}</div>
        {selected.status === 'IN_PROGRESS' && <form className="stack findings-form" onSubmit={complete}><h3>Inspection and repair findings</h3><label>Inspection notes<textarea maxLength={2000} value={findings.inspectionNotes} onChange={(event) => setFindings({ ...findings, inspectionNotes: event.target.value })}/></label><label>Observed condition<textarea maxLength={1000} value={findings.observedCondition} onChange={(event) => setFindings({ ...findings, observedCondition: event.target.value })}/></label><label>Repair notes<textarea maxLength={2000} value={findings.repairNotes} onChange={(event) => setFindings({ ...findings, repairNotes: event.target.value })}/></label><div className="form-actions"><button type="submit" disabled={busy}>{busy ? 'Submitting…' : 'Submit repair completion'}</button></div></form>}
        {selected.status !== 'COMPLETED' && selected.status !== 'CANCELLED' && <div className="evidence-grid"><label>Before repair evidence<input type="file" accept="image/*" disabled={busy} onChange={(event) => void upload(event, 'BEFORE_REPAIR')}/><span className="field-help">Image file, up to 10 MB.</span></label><label>After repair evidence<input type="file" accept="image/*" disabled={busy} onChange={(event) => void upload(event, 'AFTER_REPAIR')}/><span className="field-help">Image file, up to 10 MB.</span></label></div>}
        {selected.status === 'COMPLETED' && <p className="feedback info">Submitted for authority verification.</p>}
      </> : <div className="empty-state"><strong>Select a work order</strong><p>Its current status and supported actions will appear here.</p></div>}</section>
    </div>
  </div>;
}

function StatusBadge({ value }: { value: string }) { return <span className={`status-badge status-${value.toLowerCase()}`}>{value.replace(/_/g, ' ')}</span>; }
function PriorityBadge({ value }: { value: string }) { return <span className={`status-badge priority-${value.toLowerCase()}`}>{value}</span>; }

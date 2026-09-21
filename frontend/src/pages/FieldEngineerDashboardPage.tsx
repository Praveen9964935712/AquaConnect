import { useEffect, useState } from 'react';
import apiClient from '../api/client';

type WorkOrder = { id: string; incidentId: string; status: string; priority: string; inspectionNotes?: string; observedCondition?: string; repairNotes?: string };

type Notification = { id: string; title: string; message: string; read: boolean };

export default function FieldEngineerDashboardPage() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [selected, setSelected] = useState<WorkOrder | null>(null);
  const [findings, setFindings] = useState({ inspectionNotes: '', observedCondition: '', repairNotes: '' });
  const [error, setError] = useState('');

  async function load() {
    try {
      const [ordersResponse, notificationsResponse] = await Promise.all([apiClient.get<WorkOrder[]>('/api/work-orders'), apiClient.get<Notification[]>('/api/notifications/unread')]);
      setOrders(ordersResponse.data);
      setNotifications(notificationsResponse.data);
    } catch { setError('Unable to load assigned work.'); }
  }
  useEffect(() => { load(); }, []);

  async function action(path: string) { if (!selected) return; try { await apiClient.post(`/api/work-orders/${selected.id}/${path}`); await load(); setSelected(null); } catch { setError('The requested workflow action was rejected by the backend.'); } }
  async function complete(event: React.FormEvent) { event.preventDefault(); if (!selected) return; try { await apiClient.post(`/api/work-orders/${selected.id}/complete`, findings); await load(); setSelected(null); } catch { setError('Completion could not be submitted.'); } }
  async function upload(event: React.ChangeEvent<HTMLInputElement>, type: string) { const file = event.target.files?.[0]; if (!file || !selected) return; if (!file.type.startsWith('image/') || file.size > 10_000_000) { setError('Choose an image smaller than 10 MB.'); return; } const form = new FormData(); form.append('type', type); form.append('file', file); try { await apiClient.post(`/api/work-orders/${selected.id}/evidence`, form, { headers: { 'Content-Type': 'multipart/form-data' } }); } catch { setError('Evidence upload failed.'); } }

  return <div className="stack"><div className="card"><h1>Field Engineer</h1><p>Assigned jobs and repair workflow.</p>{notifications.length > 0 && <p>{notifications.length} unread notifications</p>}{error && <p className="error">{error}</p>}</div><div className="card"><h2>My work orders</h2><div className="list">{orders.map((order) => <button className="list-row work-order" key={order.id} type="button" onClick={() => setSelected(order)}><strong>{order.status}</strong><span>{order.priority}</span><span>Incident {order.incidentId}</span></button>)}{orders.length === 0 && <p>No assigned work orders.</p>}</div></div>{selected && <div className="card"><h2>Work order {selected.id}</h2><p>Incident: {selected.incidentId}</p><p>Priority: {selected.priority}</p><div className="actions">{selected.status === 'ASSIGNED' && <button type="button" onClick={() => action('accept')}>Accept</button>}{selected.status === 'ACCEPTED' && <button type="button" onClick={() => action('start')}>Start work</button>}</div>{selected.status === 'IN_PROGRESS' && <form className="stack" onSubmit={complete}><textarea placeholder="Inspection notes" value={findings.inspectionNotes} onChange={(event) => setFindings({ ...findings, inspectionNotes: event.target.value })} /><textarea placeholder="Observed condition" value={findings.observedCondition} onChange={(event) => setFindings({ ...findings, observedCondition: event.target.value })} /><textarea placeholder="Repair notes" value={findings.repairNotes} onChange={(event) => setFindings({ ...findings, repairNotes: event.target.value })} /><button type="submit">Complete work</button></form>}<label>Before repair<input type="file" accept="image/*" onChange={(event) => upload(event, 'BEFORE_REPAIR')} /></label><label>After repair<input type="file" accept="image/*" onChange={(event) => upload(event, 'AFTER_REPAIR')} /></label></div>}</div>;
}

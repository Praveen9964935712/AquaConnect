import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/client';

type Analytics = {
  totalIncidents: number;
  openIncidents: number;
  pendingAuthorityVerification?: number;
  incidentsByStatus: Record<string, number>;
  incidentsByPriority: Record<string, number>;
  incidentsBySource: Record<string, number>;
  resolutionSummary?: { approvedResolutions: number; rejectedResolutions: number; citizenConfirmedClosures: number; citizenStillPresentReopenedCases: number };
};

type WorkOrder = { id: string; incidentId: string; status: string; priority: string; createdAt?: string; assignedEngineerId?: string | null };
type Notification = { id: string; title: string; message: string; read: boolean; createdAt?: string };

export default function OperatorDashboardPage() {
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [analyticsResponse, ordersResponse, notificationsResponse] = await Promise.all([
        apiClient.get<Analytics>('/api/analytics/overview'),
        apiClient.get<WorkOrder[]>('/api/work-orders'),
        apiClient.get<Notification[]>('/api/notifications/unread')
      ]);
      setAnalytics(analyticsResponse.data);
      setOrders(ordersResponse.data);
      setNotifications(notificationsResponse.data);
    } catch {
      setError('Unable to load operations data. Check your access and try again.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  if (loading) return <LoadingState message="Loading operations overview…" />;

  return (
    <div className="stack">
      <header className="page-heading">
        <div><p className="eyebrow">Control room</p><h1>Operations overview</h1><p>Current incident and work-order information from AquaConnect.</p></div>
        <div className="heading-actions"><Link className="button secondary" to="/ivr-simulator">Open IVR simulator</Link><button className="secondary" type="button" onClick={() => void load()}>Refresh</button></div>
      </header>
      {error && <div className="feedback error" role="alert">{error} <button className="small secondary" type="button" onClick={() => void load()}>Retry</button></div>}
      {analytics && <>
        <section className="dashboard-grid" aria-label="Incident metrics">
          <Metric title="All incidents" value={analytics.totalIncidents} />
          <Metric title="Open incidents" value={analytics.openIncidents} tone="water" />
          <Metric title="Pending authority review" value={analytics.pendingAuthorityVerification ?? analytics.incidentsByStatus.AUTHORITY_VERIFICATION ?? 0} tone="warning" />
          <Metric title="Reopened" value={analytics.incidentsByStatus.REOPENED ?? 0} />
        </section>
        <div className="section-grid">
          <section className="panel span-6"><div className="panel-header"><div><h2>Incident status</h2><p>Counts reported by the analytics API.</p></div></div><CountList values={analytics.incidentsByStatus} /></section>
          <section className="panel span-6"><div className="panel-header"><div><h2>Priority</h2><p>Current priority distribution.</p></div></div><CountList values={analytics.incidentsByPriority} /></section>
          <section className="panel span-6"><div className="panel-header"><div><h2>Reporting channel</h2><p>Incident source as recorded by the backend.</p></div></div><CountList values={analytics.incidentsBySource} /></section>
          <section className="panel span-6"><div className="panel-header"><div><h2>Resolution activity</h2><p>Recorded verification and citizen decisions.</p></div></div>{analytics.resolutionSummary ? <div className="dashboard-grid compact-grid"><Metric title="Approved" value={analytics.resolutionSummary.approvedResolutions} /><Metric title="Rejected" value={analytics.resolutionSummary.rejectedResolutions} /><Metric title="Citizen confirmed" value={analytics.resolutionSummary.citizenConfirmedClosures} /><Metric title="Still present" value={analytics.resolutionSummary.citizenStillPresentReopenedCases} /></div> : <EmptyState message="Resolution summary is not available." />}</section>
          <section className="panel span-8"><div className="panel-header"><div><h2>Work orders</h2><p>Records returned for the authenticated operator.</p></div><span className="chip">{orders.length} visible</span></div>{orders.length === 0 ? <EmptyState message="No work orders are available." /> : <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Incident</th><th>Status</th><th>Priority</th><th>Assigned</th></tr></thead><tbody>{orders.map((order) => <tr key={order.id}><td className="mono">{shortId(order.incidentId)}</td><td><StatusBadge value={order.status} /></td><td><PriorityBadge value={order.priority} /></td><td>{order.assignedEngineerId ? shortId(order.assignedEngineerId) : 'Unassigned'}</td></tr>)}</tbody></table></div>}</section>
          <section className="panel span-4"><div className="panel-header"><div><h2>Unread notices</h2><p>For your account.</p></div><span className="chip">{notifications.length}</span></div>{notifications.length === 0 ? <EmptyState message="You are all caught up." /> : <div className="stack compact">{notifications.slice(0, 5).map((notice) => <article className="notice-row" key={notice.id}><strong>{notice.title}</strong><span>{notice.message}</span></article>)}</div>}</section>
        </div>
      </>}
    </div>
  );
}

function Metric({ title, value, tone = '' }: { title: string; value: number; tone?: string }) { return <div className={`metric ${tone}`}><span>{title}</span><strong>{value}</strong></div>; }
function CountList({ values }: { values: Record<string, number> }) { const entries = Object.entries(values ?? {}); return entries.length ? <div className="count-list">{entries.map(([name, value]) => <div className="count-row" key={name}><span>{humanize(name)}</span><strong>{value}</strong></div>)}</div> : <EmptyState message="No values are available." />; }
function EmptyState({ message }: { message: string }) { return <div className="empty-state"><p>{message}</p></div>; }
function LoadingState({ message }: { message: string }) { return <div className="loading-state" role="status"><span className="spinner" aria-hidden="true"/><p>{message}</p></div>; }
function StatusBadge({ value }: { value: string }) { return <span className={`status-badge status-${value.toLowerCase()}`}>{humanize(value)}</span>; }
function PriorityBadge({ value }: { value: string }) { return <span className={`status-badge priority-${value.toLowerCase()}`}>{humanize(value)}</span>; }
function humanize(value: string) { return value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (letter: string) => letter.toUpperCase()); }
function shortId(value: string) { return value.slice(0, 8).toUpperCase(); }
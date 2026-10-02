import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import IncidentLocationMap from '../components/IncidentLocationMap';

type Dashboard = {
  incidentsByStatus: Record<string, number>;
  incidentsByPriority: Record<string, number>;
  workOrdersByStatus: Record<string, number>;
  pendingAuthorityVerification: number;
  resolvedIncidents: number;
  closedIncidents: number;
  reopenedIncidents: number;
  engineerWorkloads: { engineerId: string; username: string; assigned: number; accepted: number; inProgress: number; completed: number }[];
  priorityQueue: { id: string; description: string; status: string; priority: string; category: string; source?: string; latitude?: number | null; longitude?: number | null; locationSource?: string | null; reportedAt?: string }[];
  workOrders?: { id: string; incidentId: string; status: string; priority: string; assignedEngineerId?: string | null }[];
};
type Analytics = { totalIncidents: number; openIncidents: number; incidentsBySource: Record<string, number>; incidentsByCategory: Record<string, number>; dailyIncidentTrend: { date: string; count: number }[]; engineerWorkload: { engineerId: string; username: string; assignedWorkOrders: number; activeWorkOrders: number; completedWorkOrders: number }[] };

export default function ManagerDashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [dashboardResponse, analyticsResponse] = await Promise.all([
        apiClient.get<Dashboard>('/api/manager/dashboard'),
        apiClient.get<Analytics>('/api/analytics/overview')
      ]);
      setDashboard(dashboardResponse.data);
      setAnalytics(analyticsResponse.data);
    } catch {
      setError('Unable to load management data. Check your access and try again.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);
  if (loading) return <div className="loading-state" role="status"><span className="spinner" aria-hidden="true"/><p>Loading management overview…</p></div>;

  return <div className="stack">
    <header className="page-heading"><div><p className="eyebrow">Service operations</p><h1>Management overview</h1><p>Monitor verification, repair workload, and incident trends from recorded service data.</p></div><button className="secondary" type="button" onClick={() => void load()}>Refresh data</button></header>
    {error && <div className="feedback error" role="alert">{error} <button className="small secondary" type="button" onClick={() => void load()}>Retry</button></div>}
    {dashboard && <>
      <section className="dashboard-grid" aria-label="Operations metrics">
        <Metric title="All incidents" value={analytics?.totalIncidents ?? sum(dashboard.incidentsByStatus)} />
        <Metric title="Open incidents" value={analytics?.openIncidents ?? 0} tone="water" />
        <Metric title="Pending verification" value={dashboard.pendingAuthorityVerification} tone="warning" />
        <Metric title="Reopened" value={dashboard.reopenedIncidents} />
      </section>
      <div className="section-grid">
        <section className="panel span-4"><PanelHeading title="Incident status" subtitle="Current workflow distribution"/><CountList values={dashboard.incidentsByStatus}/></section>
        <section className="panel span-4"><PanelHeading title="Priority" subtitle="Backend-assigned priorities"/><CountList values={dashboard.incidentsByPriority}/></section>
        <section className="panel span-4"><PanelHeading title="Work-order status" subtitle="Current operational workload"/><CountList values={dashboard.workOrdersByStatus}/></section>
          <section className="panel span-8"><PanelHeading title="Priority queue" subtitle="Incidents returned by the manager dashboard"/>
          {dashboard.priorityQueue.length === 0 ? <EmptyState message="No incidents are in the priority queue."/> : <div className="queue-list">{dashboard.priorityQueue.map((incident) => <article className="queue-item" key={incident.id}><div className="queue-priority"><PriorityBadge value={incident.priority}/><span className="source-badge">{humanize(incident.source ?? 'source unavailable')}</span></div><div className="queue-description"><strong>{humanize(incident.category)}</strong><p>{incident.description}</p><small>{incident.reportedAt ? new Date(incident.reportedAt).toLocaleString() : 'Report time unavailable'}{incident.latitude != null && incident.longitude != null ? ` · ${incident.latitude}, ${incident.longitude} · ${humanize(incident.locationSource ?? 'UNKNOWN')}` : ' · Location not provided'}</small></div><StatusBadge value={incident.status}/></article>)}</div>}
        </section>
        <section className="panel span-4"><PanelHeading title="Incident locations" subtitle="Only returned coordinates are mapped"/><IncidentLocationMap points={dashboard.priorityQueue.filter((incident) => Number.isFinite(incident.latitude) && Number.isFinite(incident.longitude)).map((incident) => ({ id: incident.id, label: `${humanize(incident.category)} · ${humanize(incident.status)}`, latitude: incident.latitude as number, longitude: incident.longitude as number, source: incident.locationSource }))}/></section>
        <section className="panel span-4"><PanelHeading title="Reporting channel" subtitle="Recorded incident source"/><CountList values={analytics?.incidentsBySource ?? {}}/><div className="subsection"><PanelHeading title="Category" subtitle="Recorded incident categories"/><CountList values={analytics?.incidentsByCategory ?? {}}/></div></section>
        <section className="panel span-6"><PanelHeading title="Field workload" subtitle="Counts returned by the analytics endpoint"/>
          {analytics?.engineerWorkload.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Engineer</th><th>Assigned</th><th>Active</th><th>Completed</th></tr></thead><tbody>{analytics.engineerWorkload.map((engineer) => <tr key={engineer.engineerId}><td>{engineer.username}</td><td>{engineer.assignedWorkOrders}</td><td>{engineer.activeWorkOrders}</td><td>{engineer.completedWorkOrders}</td></tr>)}</tbody></table></div> : <EmptyState message="No engineer workload is available."/>}
        </section>
        <section className="panel span-6"><PanelHeading title="Daily incident trend" subtitle="Counts reported by analytics; dates with no recorded incidents may be absent"/>
          {analytics?.dailyIncidentTrend.length ? <div className="trend-list">{analytics.dailyIncidentTrend.map((point) => <div className="trend-row" key={point.date}><time>{new Date(`${point.date}T00:00:00`).toLocaleDateString()}</time><span className="trend-track"><span style={{ width: `${Math.max(4, (point.count / Math.max(...analytics.dailyIncidentTrend.map((value) => value.count))) * 100)}%` }}/></span><strong>{point.count}</strong></div>)}</div> : <EmptyState message="No trend points are available for the current window."/>}
        </section>
        {dashboard.workOrders && <section className="panel span-12"><PanelHeading title="Work orders" subtitle="Current work-order records"/>{dashboard.workOrders.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Incident</th><th>Status</th><th>Priority</th><th>Engineer</th></tr></thead><tbody>{dashboard.workOrders.map((order) => <tr key={order.id}><td className="mono">{order.incidentId.slice(0, 8).toUpperCase()}</td><td><StatusBadge value={order.status}/></td><td><PriorityBadge value={order.priority}/></td><td>{order.assignedEngineerId?.slice(0, 8).toUpperCase() ?? 'Unassigned'}</td></tr>)}</tbody></table></div> : <EmptyState message="No work orders are available."/>}</section>}
      </div>
    </>}
  </div>;
}

function Metric({ title, value, tone = '' }: { title: string; value: number; tone?: string }) { return <div className={`metric ${tone}`}><span>{title}</span><strong>{value}</strong></div>; }
function PanelHeading({ title, subtitle }: { title: string; subtitle: string }) { return <div className="panel-header"><div><h2>{title}</h2><p>{subtitle}</p></div></div>; }
function CountList({ values }: { values: Record<string, number> }) { const items = Object.entries(values ?? {}); return items.length ? <div className="count-list">{items.map(([key, value]) => <div className="count-row" key={key}><span>{humanize(key)}</span><strong>{value}</strong></div>)}</div> : <EmptyState message="No recorded values."/>; }
function EmptyState({ message }: { message: string }) { return <div className="empty-state"><p>{message}</p></div>; }
function StatusBadge({ value }: { value: string }) { return <span className={`status-badge status-${value.toLowerCase()}`}>{humanize(value)}</span>; }
function PriorityBadge({ value }: { value: string }) { return <span className={`status-badge priority-${value.toLowerCase()}`}>{humanize(value)}</span>; }
function humanize(value: string) { return value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (letter: string) => letter.toUpperCase()); }
function sum(values: Record<string, number>) { return Object.values(values ?? {}).reduce((total, value) => total + value, 0); }
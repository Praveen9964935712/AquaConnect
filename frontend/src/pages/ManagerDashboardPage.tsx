import { useEffect, useState } from 'react';
import apiClient from '../api/client';

type Dashboard = {
  incidentsByStatus: Record<string, number>;
  incidentsByPriority: Record<string, number>;
  workOrdersByStatus: Record<string, number>;
  pendingAuthorityVerification: number;
  resolvedIncidents: number;
  closedIncidents: number;
  reopenedIncidents: number;
  engineerWorkloads: { engineerId: string; username: string; assigned: number; accepted: number; inProgress: number; completed: number }[];
  priorityQueue: { id: string; description: string; status: string; priority: string; category: string }[];
};

export default function ManagerDashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    apiClient.get<Dashboard>('/api/manager/dashboard').then(({ data }) => setDashboard(data)).catch(() => setError('Unable to load operational data.'));
  }, []);

  if (error) return <div className="card"><p className="error">{error}</p></div>;
  if (!dashboard) return <div className="card"><p>Loading operational dashboard...</p></div>;

  return <div className="stack">
    <div className="card"><h1>Operations Manager</h1><p>Live operational view from incidents and work orders.</p></div>
    <section className="dashboard-grid">
      <Metric title="Pending verification" value={dashboard.pendingAuthorityVerification} />
      <Metric title="Resolved" value={dashboard.resolvedIncidents} />
      <Metric title="Closed" value={dashboard.closedIncidents} />
      <Metric title="Reopened" value={dashboard.reopenedIncidents} />
    </section>
    <div className="card"><h2>Incident status</h2><StatusList values={dashboard.incidentsByStatus} /></div>
    <div className="card"><h2>Priority queue</h2><div className="list">{dashboard.priorityQueue.map((incident) => <article className="list-row" key={incident.id}><strong>{incident.priority}</strong><span>{incident.category}</span><span>{incident.status}</span><span>{incident.description}</span></article>)}</div></div>
    <div className="card"><h2>Field workload</h2><div className="list">{dashboard.engineerWorkloads.map((engineer) => <article className="list-row" key={engineer.engineerId}><strong>{engineer.username}</strong><span>Assigned {engineer.assigned}</span><span>Accepted {engineer.accepted}</span><span>In progress {engineer.inProgress}</span><span>Completed {engineer.completed}</span></article>)}</div></div>
  </div>;
}

function Metric({ title, value }: { title: string; value: number }) { return <div className="card metric"><span>{title}</span><strong>{value}</strong></div>; }
function StatusList({ values }: { values: Record<string, number> }) { return <div className="status-list">{Object.entries(values).map(([key, value]) => <span key={key}><strong>{value}</strong> {key.replaceAll('_', ' ')}</span>)}</div>; }

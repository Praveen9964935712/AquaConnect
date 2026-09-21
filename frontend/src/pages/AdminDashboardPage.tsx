import { useEffect, useState } from 'react';
import apiClient from '../api/client';

type User = { id: string; username: string; email: string; active: boolean; roles: string[] };
type Role = { id: string; name: string; description?: string };
type Zone = { id: string; code: string; name: string; active: boolean };
type Asset = { id: string; assetType: string; name: string; identifier: string; active: boolean; zoneId?: string };

export default function AdminDashboardPage() {
  const [users, setUsers] = useState<User[]>([]); const [roles, setRoles] = useState<Role[]>([]); const [zones, setZones] = useState<Zone[]>([]); const [assets, setAssets] = useState<Asset[]>([]); const [error, setError] = useState('');
  useEffect(() => { Promise.all([apiClient.get<User[]>('/api/admin/users'), apiClient.get<Role[]>('/api/admin/roles'), apiClient.get<Zone[]>('/api/admin/zones'), apiClient.get<Asset[]>('/api/admin/infrastructure/assets')]).then(([u, r, z, a]) => { setUsers(u.data); setRoles(r.data); setZones(z.data); setAssets(a.data); }).catch(() => setError('Unable to load administrative data.')); }, []);
  return <div className="stack"><div className="card"><h1>Administration</h1><p>System visibility without exposing credentials or database controls.</p>{error && <p className="error">{error}</p>}</div><div className="dashboard-grid"><Metric title="Users" value={users.length} /><Metric title="Roles" value={roles.length} /><Metric title="Zones" value={zones.length} /><Metric title="Assets" value={assets.length} /></div><div className="card"><h2>Users</h2><div className="list">{users.map((user) => <div className="list-row" key={user.id}><strong>{user.username}</strong><span>{user.email}</span><span>{user.roles.join(', ')}</span><span>{user.active ? 'Active' : 'Inactive'}</span></div>)}</div></div><div className="card"><h2>Roles</h2><div className="status-list">{roles.map((role) => <span key={role.id}>{role.name}</span>)}</div></div><div className="card"><h2>Infrastructure</h2><div className="list">{assets.map((asset) => <div className="list-row" key={asset.id}><strong>{asset.name}</strong><span>{asset.assetType}</span><span>{asset.identifier}</span><span>{asset.active ? 'Active' : 'Inactive'}</span></div>)}</div></div></div>;
}
function Metric({ title, value }: { title: string; value: number }) { return <div className="card metric"><span>{title}</span><strong>{value}</strong></div>; }

import { useEffect, useState } from 'react';
import apiClient from '../api/client';

type User = { id: string; username: string; email: string; active: boolean; roles: string[] };
type Role = { id: string; name: string; description?: string };
type Zone = { id: string; code: string; name: string; active: boolean };
type Asset = { id: string; assetType: string; name: string; identifier: string; active: boolean; zoneId?: string };

export default function AdminDashboardPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [zones, setZones] = useState<Zone[]>([]);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [selectedUser, setSelectedUser] = useState('');
  const [selectedRole, setSelectedRole] = useState('OPERATOR');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [userResponse, roleResponse, zoneResponse, assetResponse] = await Promise.all([
        apiClient.get<User[]>('/api/admin/users'),
        apiClient.get<Role[]>('/api/admin/roles'),
        apiClient.get<Zone[]>('/api/admin/zones'),
        apiClient.get<Asset[]>('/api/admin/infrastructure/assets')
      ]);
      setUsers(userResponse.data);
      setRoles(roleResponse.data);
      setZones(zoneResponse.data);
      setAssets(assetResponse.data);
    } catch {
      setError('Unable to load administrative data. Please retry.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  async function assignRole(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const target = users.find((user) => user.id === selectedUser);
    if (!target || !selectedRole) return;
    if (!window.confirm(`Assign ${selectedRole.replace(/_/g, ' ')} to ${target.email}?`)) return;
    setSubmitting(true);
    setError('');
    setNotice('');
    try {
      const { data } = await apiClient.post<User>(`/api/admin/users/${target.id}/roles`, { role: selectedRole });
      setUsers((current) => current.map((user) => user.id === data.id ? data : user));
      setNotice(`${selectedRole.replace(/_/g, ' ')} assigned to ${data.email}.`);
    } catch {
      setError('Role assignment was rejected. Confirm the user and role, then retry.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className="loading-state" role="status"><span className="spinner" aria-hidden="true"/><p>Loading administration…</p></div>;

  const assignableRoles = roles.filter((role) => role.name !== 'CITIZEN');
  return <div className="stack">
    <header className="page-heading"><div><p className="eyebrow">System administration</p><h1>Administration</h1><p>Review accounts, role assignments, configured zones, and infrastructure records.</p></div><button className="secondary" type="button" onClick={() => void load()}>Refresh</button></header>
    {error && <p className="feedback error" role="alert">{error}</p>}
    {notice && <p className="feedback success" role="status">{notice}</p>}
    <section className="dashboard-grid"><Metric title="Accounts" value={users.length}/><Metric title="Available roles" value={roles.length} tone="water"/><Metric title="Zones" value={zones.length}/><Metric title="Infrastructure assets" value={assets.length}/></section>

    <div className="section-grid">
      <section className="panel span-8"><div className="panel-header"><div><h2>Accounts</h2><p>Account identity and backend-assigned roles.</p></div></div>{users.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Account</th><th>Email</th><th>Roles</th><th>State</th></tr></thead><tbody>{users.map((user) => <tr key={user.id}><td>{user.username}</td><td>{user.email}</td><td><div className="chip-row">{user.roles.map((role) => <span className="role-badge" key={role}>{role.replace(/_/g, ' ')}</span>)}</div></td><td><span className={`status-badge ${user.active ? 'status-resolved' : 'status-failed'}`}>{user.active ? 'Active' : 'Inactive'}</span></td></tr>)}</tbody></table></div> : <EmptyState message="No accounts are available."/>}</section>

      <section className="panel span-4"><div className="panel-header"><div><h2>Provision a role</h2><p>Privileged assignments are enforced by the backend.</p></div></div><form className="stack" onSubmit={assignRole}><label>Account<select required value={selectedUser} onChange={(event) => setSelectedUser(event.target.value)}><option value="">Select an account</option>{users.map((user) => <option key={user.id} value={user.id}>{user.email} · {user.roles.join(', ')}</option>)}</select></label><label>Role<select value={selectedRole} onChange={(event) => setSelectedRole(event.target.value)}>{assignableRoles.map((role) => <option key={role.id} value={role.name}>{role.name.replace(/_/g, ' ')}</option>)}</select></label><p className="field-help">Public registration cannot assign privileged roles. Changes are recorded through the admin API.</p><button type="submit" disabled={submitting || !selectedUser}>{submitting ? 'Applying…' : 'Assign role'}</button></form></section>

      <section className="panel span-6"><div className="panel-header"><div><h2>Role catalog</h2><p>Roles configured by the backend.</p></div></div>{roles.length ? <div className="role-catalog">{roles.map((role) => <article className="role-entry" key={role.id}><span className="role-badge">{role.name.replace(/_/g, ' ')}</span><span>{role.description ?? 'No description provided.'}</span></article>)}</div> : <EmptyState message="No roles are configured."/>}</section>

      <section className="panel span-6"><div className="panel-header"><div><h2>Water zones</h2><p>Configured service geography.</p></div></div>{zones.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Code</th><th>Name</th><th>State</th></tr></thead><tbody>{zones.map((zone) => <tr key={zone.id}><td className="mono">{zone.code}</td><td>{zone.name}</td><td>{zone.active ? 'Active' : 'Inactive'}</td></tr>)}</tbody></table></div> : <EmptyState message="No water zones are configured."/>}</section>

      <section className="panel span-12"><div className="panel-header"><div><h2>Infrastructure records</h2><p>Configured/synthetic records; this is not a live SCADA feed.</p></div></div>{assets.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Asset</th><th>Type</th><th>Identifier</th><th>Zone</th><th>State</th></tr></thead><tbody>{assets.map((asset) => <tr key={asset.id}><td>{asset.name}</td><td>{asset.assetType.replace(/_/g, ' ')}</td><td className="mono">{asset.identifier}</td><td>{asset.zoneId?.slice(0, 8) ?? '—'}</td><td>{asset.active ? 'Active' : 'Inactive'}</td></tr>)}</tbody></table></div> : <EmptyState message="No infrastructure records are configured."/>}</section>
    </div>
  </div>;
}

function Metric({ title, value, tone = '' }: { title: string; value: number; tone?: string }) { return <div className={`metric ${tone}`}><span>{title}</span><strong>{value}</strong></div>; }
function EmptyState({ message }: { message: string }) { return <div className="empty-state"><p>{message}</p></div>; }
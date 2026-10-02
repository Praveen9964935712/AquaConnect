import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { dashboardPath } from '../auth/dashboardPath';

export default function LoginPage() {
  const { login, sessionExpired } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      const { data } = await apiClient.post('/api/auth/login', { email, password });
      login(data);
      const roles = data.roles ?? [];
      navigate(dashboardPath(roles));
    } catch {
      setError('Invalid email or password.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-layout">
      <section className="auth-story">
        <p className="eyebrow">Water service, connected</p>
        <h1>Every report moves the response forward.</h1>
        <p className="story-copy">A shared service desk for reporting water problems, tracking field work, and keeping communities informed.</p>
        <div className="auth-points">
          <div className="auth-point"><span className="auth-point-mark">1</span><span>Report a service issue and keep its reference close.</span></div>
          <div className="auth-point"><span className="auth-point-mark">2</span><span>Follow verification, repair, and resolution updates.</span></div>
          <div className="auth-point"><span className="auth-point-mark">3</span><span>Operations teams coordinate the response in one workflow.</span></div>
        </div>
      </section>
      <section className="auth-panel" aria-labelledby="login-title">
        <p className="eyebrow">AquaConnect account</p>
        <h2 id="login-title">Sign in</h2>
        <p>Use your registered email and password.</p>
        {sessionExpired && <p className="feedback info" role="status">Your session expired. Sign in again to continue.</p>}
        <form onSubmit={handleSubmit} className="stack">
          <label>Email address<input type="email" autoComplete="email" required value={email} onChange={(event) => setEmail(event.target.value)} placeholder="name@example.com" /></label>
          <label>Password<input type="password" autoComplete="current-password" required value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Enter your password" /></label>
          {error && <p className="feedback error" role="alert">{error}</p>}
          <button type="submit" disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'}</button>
        </form>
        <div className="auth-links"><span>New to AquaConnect?</span><Link to="/register">Create a citizen account</Link></div>
      </section>
    </div>
  );
}

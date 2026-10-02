import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/client';

export default function RegisterPage() {
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
      await apiClient.post('/api/auth/register', { email, password });
      navigate('/login');
    } catch {
      setError('Registration failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-layout">
      <section className="auth-story">
        <p className="eyebrow">Citizen access</p>
        <h1>Keep your service requests in view.</h1>
        <p className="story-copy">Create an account to report water service issues and follow their status through the response process.</p>
        <div className="auth-points">
          <div className="auth-point"><span className="auth-point-mark">✓</span><span>Your incidents stay associated with your account.</span></div>
          <div className="auth-point"><span className="auth-point-mark">✓</span><span>Updates reflect the operational workflow.</span></div>
        </div>
      </section>
      <section className="auth-panel" aria-labelledby="register-title">
        <p className="eyebrow">Get started</p>
        <h2 id="register-title">Create a citizen account</h2>
        <p>Registration creates a citizen account. Privileged roles are assigned only by authorized administrators.</p>
        <form onSubmit={handleSubmit} className="stack">
          <label>Email address<input type="email" autoComplete="email" required maxLength={255} value={email} onChange={(event) => setEmail(event.target.value)} placeholder="name@example.com" /></label>
          <label>Password<input type="password" autoComplete="new-password" required minLength={8} maxLength={72} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="At least 8 characters" /></label>
          <p className="field-help">Use at least 8 characters with upper-case, lower-case, and a number.</p>
          {error && <p className="feedback error" role="alert">{error}</p>}
          <button type="submit" disabled={submitting}>{submitting ? 'Creating account…' : 'Create account'}</button>
        </form>
        <div className="auth-links"><span>Already registered?</span><Link to="/login">Sign in</Link></div>
      </section>
    </div>
  );
}

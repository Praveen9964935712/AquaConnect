import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import LoginPage from './LoginPage';

const { postMock, loginMock } = vi.hoisted(() => ({ postMock: vi.fn(), loginMock: vi.fn() }));

vi.mock('../api/client', () => ({ default: { post: postMock } }));
vi.mock('../auth/AuthContext', () => ({ useAuth: () => ({ login: loginMock }) }));

describe('LoginPage', () => {
  beforeEach(() => {
    postMock.mockReset();
    loginMock.mockReset();
  });

  it('submits credentials and routes by returned role', async () => {
    postMock.mockResolvedValueOnce({ data: { token: 'jwt', userId: 'u1', email: 'citizen@example.com', roles: ['OPERATOR'] } });
    render(<MemoryRouter initialEntries={['/login']}><Routes><Route path="/login" element={<LoginPage />} /><Route path="/operator" element={<h1>Operations home</h1>} /></Routes></MemoryRouter>);

    fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'citizen@example.com' } });
    fireEvent.change(screen.getByLabelText(/^Password$/i), { target: { value: 'CorrectPassword1' } });
    fireEvent.click(screen.getByRole('button', { name: /^Sign in$/i }));

    expect(await screen.findByRole('heading', { name: /Operations home/i })).toBeTruthy();
    expect(loginMock).toHaveBeenCalledWith(expect.objectContaining({ token: 'jwt', roles: ['OPERATOR'] }));
  });

  it('shows a human-readable credential error and reenables submission', async () => {
    postMock.mockRejectedValueOnce(new Error('401')); 
    render(<MemoryRouter><LoginPage /></MemoryRouter>);
    fireEvent.change(screen.getByLabelText(/Email address/i), { target: { value: 'bad@example.com' } });
    fireEvent.change(screen.getByLabelText(/^Password$/i), { target: { value: 'wrong' } });
    fireEvent.click(screen.getByRole('button', { name: /^Sign in$/i }));

    await waitFor(() => expect(screen.getByRole('alert').textContent).toMatch(/Invalid email or password/i));
    expect(screen.getByRole('button', { name: /^Sign in$/i }).hasAttribute('disabled')).toBe(false);
  });
});

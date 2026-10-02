import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import IvrSimulatorPage from './IvrSimulatorPage';

const { postMock, getMock } = vi.hoisted(() => ({
  postMock: vi.fn(),
  getMock: vi.fn(),
}));

vi.mock('../api/client', () => ({
  default: {
    post: postMock,
    get: getMock
  }
}));

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    user: { roles: ['OPERATOR'] },
    isAuthenticated: true
  })
}));

describe('IvrSimulatorPage', () => {
  beforeEach(() => {
    postMock.mockReset();
    getMock.mockReset();
  });

  it('loads the simulator and starts a session', async () => {
    postMock.mockResolvedValueOnce({ data: { id: 'session-1', state: 'START', sessionStatus: 'ACTIVE' } });
    render(<IvrSimulatorPage />);

    fireEvent.click(screen.getByRole('button', { name: /start session/i }));

    await waitFor(() => expect(screen.getByRole('heading', { name: /IVR simulator/i })).toBeTruthy());
    expect(postMock).toHaveBeenCalledWith('/api/ivr/sessions', { callerIdentifier: 'dev-simulator' });
  });

  it('handles menu choice 1 for reporting a problem', async () => {
    postMock
      .mockResolvedValueOnce({ data: { id: 'session-1', state: 'START', sessionStatus: 'ACTIVE', callerIdentifier: 'dev-simulator' } })
      .mockResolvedValueOnce({ data: { id: 'session-1', state: 'MAIN_MENU', sessionStatus: 'ACTIVE', language: 'ENGLISH', callerIdentifier: 'dev-simulator' } })
      .mockResolvedValueOnce({ data: { id: 'session-1', state: 'REPORT_PROBLEM', sessionStatus: 'ACTIVE', language: 'ENGLISH', callerIdentifier: 'dev-simulator' } });

    render(<IvrSimulatorPage />);
    fireEvent.click(screen.getByRole('button', { name: /start session/i }));
    await waitFor(() => expect(postMock).toHaveBeenCalledWith('/api/ivr/sessions', { callerIdentifier: 'dev-simulator' }));

    fireEvent.click(screen.getByRole('button', { name: /ENGLISH/i }));
    await waitFor(() => expect(screen.getAllByText(/Main Menu/i).length).toBeGreaterThan(0));

    fireEvent.click(screen.getByRole('button', { name: /report a water problem/i }));
    await waitFor(() => expect(screen.getByText(/Report Problem/i)).toBeTruthy());
  });
});

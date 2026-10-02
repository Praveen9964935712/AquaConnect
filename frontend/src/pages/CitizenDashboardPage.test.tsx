import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CitizenDashboardPage from './CitizenDashboardPage';

const { getMock, postMock } = vi.hoisted(() => ({ getMock: vi.fn(), postMock: vi.fn() }));

vi.mock('../api/client', () => ({
  default: { get: getMock, post: postMock }
}));

describe('CitizenDashboardPage', () => {
  beforeEach(() => {
    getMock.mockReset();
    postMock.mockReset();
  });

  it('submits a web incident and displays the backend reference', async () => {
    getMock.mockResolvedValueOnce({ data: [] });
    postMock.mockResolvedValueOnce({ data: {
      id: 'incident-12345678', category: 'PIPE_LEAK', description: 'Leak near the park', source: 'WEB',
      status: 'SUBMITTED', priority: 'MEDIUM', reportedAt: '2026-10-02T10:00:00Z'
    } });

    render(<CitizenDashboardPage />);
    await screen.findByText(/No reports yet/i);
    fireEvent.change(screen.getByLabelText(/Description/i), { target: { value: 'Leak near the park' } });
    fireEvent.click(screen.getByRole('button', { name: /Submit report/i }));

    await waitFor(() => expect(postMock).toHaveBeenCalledWith('/api/incidents', expect.objectContaining({
      category: 'PIPE_LEAK', description: 'Leak near the park', source: 'WEB', locationSource: 'UNKNOWN'
    })));
    expect(await screen.findByText(/Report submitted\. Reference: incident-12345678/i)).toBeTruthy();
    expect(await screen.findByText(/Leak near the park/i)).toBeTruthy();
  });

  it('shows a useful error when owned incidents cannot be loaded', async () => {
    getMock.mockRejectedValueOnce(new Error('network unavailable'));
    render(<CitizenDashboardPage />);
    expect((await screen.findByRole('alert')).textContent).toMatch(/Unable to load your reports/i);
  });
});
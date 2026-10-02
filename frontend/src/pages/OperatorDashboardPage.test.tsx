import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import OperatorDashboardPage from './OperatorDashboardPage';

const { getMock } = vi.hoisted(() => ({ getMock: vi.fn() }));
vi.mock('../api/client', () => ({ default: { get: getMock } }));

describe('OperatorDashboardPage', () => {
  beforeEach(() => getMock.mockReset());

  it('loads actual analytics, work orders, and notification records', async () => {
    getMock
      .mockResolvedValueOnce({ data: { totalIncidents: 2, openIncidents: 1, incidentsByStatus: { SUBMITTED: 1, RESOLVED: 1 }, incidentsByPriority: { HIGH: 1 }, incidentsBySource: { WEB: 2 }, resolutionSummary: { approvedResolutions: 1, rejectedResolutions: 0, citizenConfirmedClosures: 0, citizenStillPresentReopenedCases: 0 } } })
      .mockResolvedValueOnce({ data: [{ id: 'work-1', incidentId: 'incident-1', status: 'ASSIGNED', priority: 'HIGH' }] })
      .mockResolvedValueOnce({ data: [] });

    render(<MemoryRouter><OperatorDashboardPage /></MemoryRouter>);

    expect(await screen.findByRole('heading', { name: /Operations overview/i })).toBeTruthy();
    expect(screen.getByText('INCIDENT')).toBeTruthy();
    expect(screen.getByText(/You are all caught up/i)).toBeTruthy();
    expect(getMock.mock.calls.map(([path]) => path)).toEqual(['/api/analytics/overview', '/api/work-orders', '/api/notifications/unread']);
  });

  it('shows a retryable error when operational APIs fail', async () => {
    getMock
      .mockRejectedValueOnce(new Error('network unavailable'))
      .mockRejectedValueOnce(new Error('network unavailable'))
      .mockRejectedValueOnce(new Error('network unavailable'));
    render(<MemoryRouter><OperatorDashboardPage /></MemoryRouter>);

    await waitFor(() => expect(screen.getByRole('alert').textContent).toMatch(/Unable to load operations data/i));
    expect(screen.getByRole('button', { name: /Retry/i })).toBeTruthy();
  });
});

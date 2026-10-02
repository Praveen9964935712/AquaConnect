import { afterEach, describe, expect, it, vi } from 'vitest';
import apiClient, { SESSION_EXPIRED_EVENT } from './client';

describe('API authentication response handling', () => {
  afterEach(() => {
    localStorage.removeItem('aquaconnect.jwt');
  });

  it('signals an expired session on 401 when a JWT is stored', async () => {
    localStorage.setItem('aquaconnect.jwt', 'test-token');
    const dispatchSpy = vi.spyOn(window, 'dispatchEvent');
    const adapter = vi.fn().mockRejectedValue({ response: { status: 401 } });

    await expect(apiClient.get('/protected', { adapter })).rejects.toBeTruthy();

    expect(dispatchSpy).toHaveBeenCalledWith(expect.objectContaining({ type: SESSION_EXPIRED_EVENT }));
    dispatchSpy.mockRestore();
  });

  it('does not expire the session on 403', async () => {
    localStorage.setItem('aquaconnect.jwt', 'test-token');
    const dispatchSpy = vi.spyOn(window, 'dispatchEvent');
    const adapter = vi.fn().mockRejectedValue({ response: { status: 403 } });

    await expect(apiClient.get('/forbidden', { adapter })).rejects.toBeTruthy();

    expect(dispatchSpy).not.toHaveBeenCalledWith(expect.objectContaining({ type: SESSION_EXPIRED_EVENT }));
    dispatchSpy.mockRestore();
  });
});

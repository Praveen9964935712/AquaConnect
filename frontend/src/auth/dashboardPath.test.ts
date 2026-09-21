import { describe, expect, it } from 'vitest';
import { dashboardPath } from './dashboardPath';

describe('dashboardPath', () => {
  it('routes each supported role to its dashboard', () => {
    expect(dashboardPath(['CITIZEN'])).toBe('/citizen');
    expect(dashboardPath(['OPERATOR'])).toBe('/operator');
    expect(dashboardPath(['OPERATIONS_MANAGER'])).toBe('/manager');
    expect(dashboardPath(['FIELD_ENGINEER'])).toBe('/engineer');
    expect(dashboardPath(['ADMIN'])).toBe('/admin');
  });

  it('does not invent a dashboard for unknown roles', () => {
    expect(dashboardPath([])).toBe('/login');
  });
});
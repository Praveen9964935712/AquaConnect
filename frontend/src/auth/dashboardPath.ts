import type { Role } from '../types/auth';

export function dashboardPath(roles: Role[]): string {
  if (roles.includes('CITIZEN')) return '/citizen';
  if (roles.includes('OPERATOR')) return '/operator';
  if (roles.includes('OPERATIONS_MANAGER')) return '/manager';
  if (roles.includes('FIELD_ENGINEER')) return '/engineer';
  if (roles.includes('ADMIN')) return '/admin';
  return '/login';
}

export type Role = 'CITIZEN' | 'OPERATOR' | 'OPERATIONS_MANAGER' | 'FIELD_ENGINEER' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  roles: Role[];
}

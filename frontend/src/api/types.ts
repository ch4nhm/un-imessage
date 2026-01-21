export interface User {
  id: number;
  username: string;
  nickname?: string;
  status: number;
  createdAt: string;
  updatedAt?: string;
}

export interface LoginResult {
  token: string;
  user: User;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

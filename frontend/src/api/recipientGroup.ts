import request from '../utils/request';
import type { PageResult } from './types';

export interface RecipientGroup {
  id: number;
  name: string; // 后端返回字段为 name
  code?: string;
  description: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export const getRecipientGroupPage = (params: any) => {
  return request.get('/recipient-group/page', { params }) as Promise<PageResult<RecipientGroup>>;
};

export const getRecipientGroupList = () => {
  return request.get('/recipient-group/list');
};

export const getGroupRecipientIds = (id: number) => {
  return request.get(`/recipient-group/${id}/recipients`) as Promise<number[]>;
};

export const createRecipientGroup = (data: any) => {
  return request.post('/recipient-group', data);
};

export const updateRecipientGroup = (id: number, data: any) => {
  return request.put(`/recipient-group/${id}`, data);
};

export const deleteRecipientGroup = (id: number) => {
  return request.delete(`/recipient-group/${id}`);
};

export const updateRecipientGroupStatus = (id: number, status: number) => {
  return request.put(`/recipient-group/${id}/status`, null, { params: { status } });
};

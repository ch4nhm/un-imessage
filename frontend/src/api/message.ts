import request from '../utils/request';
import type { PageResult } from './types';

export interface Batch {
    id: number;
    batchNo: string;
    appId: number;
    appName: string;
    templateId: number;
    channelId: number;
    msgType: number;
    title: string;
    totalCount: number;
    successCount: number;
    failCount: number;
    status: number;
    createdAt: string;
}

export interface MessageDetail {
    id: number;
    batchId: number;
    recipient: string;
    status: number;
    errorMsg?: string;
    retryCount: number;
    sendTime?: string;
    createTime: string;
}

export const getBatchPage = (params: any) => {
  return request.get('/log/batch/page', { params }) as Promise<PageResult<Batch>>;
};

export const getBatchById = (id: number) => {
  return request.get(`/log/batch/${id}`);
};

export const getDetailPage = (params: any) => {
  return request.get('/log/detail/page', { params }) as Promise<PageResult<MessageDetail>>;
};

export const getDetailByBatchId = (batchId: number, params?: { current?: number; size?: number }) => {
  return request.get(`/log/detail/batch/${batchId}`, { params }) as Promise<PageResult<MessageDetail>>;
};

export const retryMessage = (id: number) => {
  return request.post(`/log/detail/${id}/retry`);
};

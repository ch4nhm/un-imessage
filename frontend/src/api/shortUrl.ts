import request from '../utils/request';
import type { PageResult } from './types';

export interface ShortUrl {
  shortCode: string;
  shortUrl: string;
  originalUrl: string;
  clickCount: number;
  status: number;
  expireAt?: string;
  createdAt: string;
}

export interface ShortUrlStats {
  shortCode: string;
  originalUrl: string;
  totalClicks: number;
  todayClicks: number;
  createdAt: string;
  recentAccess: {
    ip: string;
    userAgent: string;
    accessTime: string;
  }[];
}

export interface CreateShortUrlRequest {
  url: string;
  customCode?: string;
  ttl?: number;
}

export const getShortUrlPage = (params: {
  current?: number;
  size?: number;
  shortCode?: string;
  originalUrl?: string;
  status?: number;
}) => {
  return request.get('/short-url/page', { params }) as Promise<PageResult<ShortUrl>>;
};

export const createShortUrl = (data: CreateShortUrlRequest) => {
  return request.post('/short-url', data) as Promise<ShortUrl>;
};

export const getShortUrlByCode = (shortCode: string) => {
  return request.get(`/short-url/${shortCode}`) as Promise<ShortUrl>;
};

export const getShortUrlStats = (shortCode: string) => {
  return request.get(`/short-url/${shortCode}/stats`) as Promise<ShortUrlStats>;
};

export const enableShortUrl = (shortCode: string) => {
  return request.put(`/short-url/${shortCode}/enable`);
};

export const disableShortUrl = (shortCode: string) => {
  return request.put(`/short-url/${shortCode}/disable`);
};

export const deleteShortUrl = (shortCode: string) => {
  return request.delete(`/short-url/${shortCode}`);
};

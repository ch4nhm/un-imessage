import request from '../utils/request';
import type {PageResult} from './types';

export interface Template {
    id: number;
    name: string;
    code: string;
    appId?: number;
    channelId: number;
    msgType: number;
    title: string;
    content: string;
    variables: string;
    deduplicationConfig: string;
    recipientGroupIds: string; // Comma separated IDs from backend
    recipientIds: string; // Comma separated IDs from backend
    rateLimit?: number;
    status: number;
    createTime: string;
}

export const getTemplatePage = (params: any) => {
    return request.get('/template/page', {params}) as Promise<PageResult<Template>>;
};

export const createTemplate = (data: any) => {
    return request.post('/template', data);
};

export const updateTemplate = (id: number, data: any) => {
    return request.put(`/template/${id}`, data);
};

export const deleteTemplate = (id: number) => {
    return request.delete(`/template/${id}`);
};

export const updateTemplateStatus = (id: number, status: number) => {
    return request.put(`/template/${id}/status`, null, {params: {status}});
};

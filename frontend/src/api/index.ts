import axios from 'axios';
import type { Activity, SeatMapResponse, RegistrationRequest, Registration, Venue } from '../types';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

export const activityApi = {
  list: () => api.get<Activity[]>('/activities').then(r => r.data),
  get: (id: number) => api.get<Activity>(`/activities/${id}`).then(r => r.data),
  create: (data: { title: string; description: string; venueId: number; startTime: string }) =>
    api.post<Activity>('/activities', data).then(r => r.data),
  updateStatus: (id: number, status: string) =>
    api.put<Activity>(`/activities/${id}/status`, { status }).then(r => r.data),
};

export const registrationApi = {
  register: (activityId: number, data: RegistrationRequest) =>
    api.post<Registration>(`/activities/${activityId}/register`, data).then(r => r.data),
  list: (activityId: number) =>
    api.get<Registration[]>(`/activities/${activityId}/registrations`).then(r => r.data),
  cancel: (id: number) => api.delete(`/registrations/${id}`),
};

export const seatApi = {
  getSeatMap: (activityId: number) =>
    api.get<SeatMapResponse>(`/activities/${activityId}/seat-map`).then(r => r.data),
  allocate: (activityId: number) =>
    api.post<{ message: string; allocatedCount: number }>(`/activities/${activityId}/allocate`).then(r => r.data),
  /** 调整座位，后端返回最新座位图 */
  adjust: (registrationId: number, newSeatId: number) =>
    api.put<SeatMapResponse>('/seats/adjust', { registrationId, newSeatId }).then(r => r.data),
  /** 交换座位，后端返回最新座位图 */
  swap: (regId1: number, regId2: number) =>
    api.put<SeatMapResponse>('/seats/swap', { regId1, regId2 }).then(r => r.data),
  exportExcel: (activityId: number) =>
    api.get(`/activities/${activityId}/export/excel`, { responseType: 'blob' }).then(r => r.data),
};

export const venueApi = {
  list: () => api.get<Venue[]>('/venues').then(r => r.data),
};

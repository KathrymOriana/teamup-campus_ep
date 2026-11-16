import { http } from '../core/api.js';

export const ProjectService = {
    list: () => http.get('/projects'),
    summary: () => http.get('/projects/summary'),
    create: (data) => http.post('/projects', data),
    update: (id, data) => http.put(`/projects/${id}`, data),
    remove: (id) => http.delete(`/projects/${id}`),
    courses: () => http.get('/courses'),

    members: (id) => http.get(`/projects/${id}/members`),
    addMember: (id, data) => http.post(`/projects/${id}/members`, data),
    setRole: (id, userId, rol) => http.patch(`/projects/${id}/members/${userId}`, { rol }),
    removeMember: (id, userId) => http.delete(`/projects/${id}/members/${userId}`),
};

import { http } from '../core/api.js';

export const TaskService = {
    list: (projectId) => http.get(projectId == null ? '/tasks' : `/tasks?projectId=${projectId}`),
    create: (data) => http.post('/tasks', data),
    update: (id, data) => http.put(`/tasks/${id}`, data),
    setState: (id, estado) => http.patch(`/tasks/${id}/state`, { estado }),
    remove: (id) => http.delete(`/tasks/${id}`),
};


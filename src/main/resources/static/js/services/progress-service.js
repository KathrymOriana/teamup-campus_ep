import { http } from '../core/api.js';

export const ProgressService = {
    report: (projectId) => http.get(projectId == null ? '/progress' : `/progress?projectId=${projectId}`),
    notifications: () => http.get('/notifications'),
};

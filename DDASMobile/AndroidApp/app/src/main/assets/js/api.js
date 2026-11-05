const API_BASE = 'http://localhost:8080';

async function apiRequest(endpoint, options = {}) {
    const token = localStorage.getItem('accessToken');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });
    if (response.status === 401) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        renderLogin();
        return null;
    }
    return response.json();
}

async function register(username, email, password) {
    return apiRequest('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, email, password })
    });
}

async function login(username, password) {
    return apiRequest('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });
}

async function logout() {
    await apiRequest('/api/auth/logout', { method: 'POST' });
}

async function uploadDocument(file, docType) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('docType', docType);
    const response = await fetch(`${API_BASE}/uploadDocument`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` },
        body: formData
    });
    return response.json();
}

async function reviewDocument(docId, action) {
    return apiRequest('/reviewDocument', {
        method: 'POST',
        body: JSON.stringify({ docId, action })
    });
}
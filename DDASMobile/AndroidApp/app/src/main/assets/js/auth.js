function getToken() {
    return localStorage.getItem('accessToken');
}

function isLoggedIn() {
    return !!getToken();
}

async function handleLogin(username, password) {
    try {
        const data = await login(username, password);
        if (data.accessToken) {
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            renderHome();
        }
    } catch (err) {
        alert('Login failed: ' + err.message);
    }
}

async function handleRegister(username, email, password) {
    try {
        await register(username, email, password);
        alert('Registration successful! Please login.');
        renderLogin();
    } catch (err) {
        alert('Registration failed: ' + err.message);
    }
}

function handleLogout() {
    logout();
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    renderLogin();
}
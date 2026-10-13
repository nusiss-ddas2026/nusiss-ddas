function showToast(message, type = 'info') {
    const existing = document.querySelector('.toast');
    if (existing) existing.remove();
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

function renderLogin() {
    document.getElementById('app').innerHTML = `
        <div class="auth-container fade-in">
            <div style="flex:1; display:flex; flex-direction:column; justify-content:center;">
                <div style="text-align:center; margin-bottom:30px;">
                    <div style="font-size:48px; margin-bottom:10px;">📄</div>
                    <h1 style="font-size:28px; font-weight:700; color:white;">DDAS</h1>
                    <p style="font-size:14px; opacity:0.85; margin-top:4px;">Document Digital Approval System</p>
                </div>
                <div class="auth-card">
                    <h2>Welcome Back</h2>
                    <p class="subtitle">Sign in to continue</p>
                    <div class="input-group">
                        <label>Username</label>
                        <input type="text" id="username" placeholder="Enter username" autocomplete="username">
                    </div>
                    <div class="input-group">
                        <label>Password</label>
                        <input type="password" id="password" placeholder="Enter password" autocomplete="current-password">
                    </div>
                    <button class="btn btn-primary" onclick="doLogin()" style="margin-top:8px;">Sign In</button>
                    <p style="text-align:center; margin-top:16px; font-size:14px; color:#8e8e93;">
                        Don't have an account? 
                        <a href="#" onclick="doRegister()" style="color:#007AFF; text-decoration:none; font-weight:600;">Register</a>
                    </p>
                </div>
            </div>
        </div>
    `;
    
    document.getElementById('password').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') doLogin();
    });
    
    window.doLogin = async () => {
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        if (!username || !password) return showToast('Please fill in all fields', 'error');
        
        const btn = document.querySelector('.btn-primary');
        btn.disabled = true;
        btn.textContent = 'Signing in...';
        
        try {
            await handleLogin(username, password);
        } catch (err) {
            btn.disabled = false;
            btn.textContent = 'Sign In';
            showToast('Login failed. Check credentials.', 'error');
        }
    };
    
    window.doRegister = () => {
        const username = document.getElementById('username').value.trim();
        document.getElementById('app').innerHTML = `
            <div class="auth-container fade-in">
                <div style="flex:1; display:flex; flex-direction:column; justify-content:center;">
                    <div class="auth-card">
                        <h2>Create Account</h2>
                        <p class="subtitle">Register to get started</p>
                        <div class="input-group">
                            <label>Username</label>
                            <input type="text" id="reg-username" placeholder="Choose username" value="${username}" autocomplete="username">
                        </div>
                        <div class="input-group">
                            <label>Email</label>
                            <input type="email" id="reg-email" placeholder="Enter email" autocomplete="email">
                        </div>
                        <div class="input-group">
                            <label>Password</label>
                            <input type="password" id="reg-password" placeholder="Choose password" autocomplete="new-password">
                        </div>
                        <button class="btn btn-primary" onclick="doRegister()" style="margin-top:8px;">Create Account</button>
                        <p style="text-align:center; margin-top:16px; font-size:14px; color:#8e8e93;">
                            Already have an account? 
                            <a href="#" onclick="renderLogin()" style="color:#007AFF; text-decoration:none; font-weight:600;">Sign In</a>
                        </p>
                    </div>
                </div>
            </div>
        `;
        
        document.getElementById('reg-password').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') doRegister();
        });
        
        window.doRegister = async () => {
            const username = document.getElementById('reg-username').value.trim();
            const email = document.getElementById('reg-email').value.trim();
            const password = document.getElementById('reg-password').value;
            if (!username || !email || !password) return showToast('Please fill in all fields', 'error');
            
            const btn = document.querySelector('.btn-primary');
            btn.disabled = true;
            btn.textContent = 'Creating...';
            
            try {
                await handleRegister(username, email, password);
            } catch (err) {
                btn.disabled = false;
                btn.textContent = 'Create Account';
                showToast('Registration failed', 'error');
            }
        };
    };
}

async function renderHome() {
    if (!isLoggedIn()) { renderLogin(); return; }
    
    document.getElementById('app').innerHTML = `
        <div class="fade-in">
            <div class="app-header">
                <div>
                    <h1>DDAS Mobile</h1>
                    <div class="subtitle">Document Management</div>
                </div>
                <button class="header-btn" onclick="handleLogout()">Logout</button>
            </div>
            
            <div class="content">
                <div class="section-title">Quick Actions</div>
                <div class="card-group">
                    <div class="card-item" onclick="renderUpload()">
                        <div class="card-icon blue">📤</div>
                        <div class="card-content">
                            <h3>Upload Document</h3>
                            <p>Submit a new document for approval</p>
                        </div>
                        <div class="card-chevron">›</div>
                    </div>
                    <div class="card-item" onclick="renderMyDocuments()">
                        <div class="card-icon green">📋</div>
                        <div class="card-content">
                            <h3>My Documents</h3>
                            <p>View your uploaded documents</p>
                        </div>
                        <div class="card-chevron">›</div>
                    </div>
                </div>
                
                <div class="section-title">Management</div>
                <div class="card-group">
                    <div class="card-item" onclick="renderReview()">
                        <div class="card-icon orange">✅</div>
                        <div class="card-content">
                            <h3>Review Documents</h3>
                            <p>Approve or reject submissions</p>
                        </div>
                        <div class="card-chevron">›</div>
                    </div>
                    <div class="card-item" onclick="showToast('Coming soon', 'info')">
                        <div class="card-icon purple">👥</div>
                        <div class="card-content">
                            <h3>User Management</h3>
                            <p>Manage users and roles</p>
                        </div>
                        <div class="card-chevron">›</div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

async function renderUpload() {
    document.getElementById('app').innerHTML = `
        <div class="fade-in">
            <div class="app-header">
                <button class="header-btn" onclick="renderHome()" style="padding:8px 12px;">‹ Back</button>
                <div style="text-align:center;">
                    <h1>Upload Document</h1>
                </div>
                <div style="width:60px;"></div>
            </div>
            
            <div class="content">
                <div class="input-group">
                    <label>Document Type</label>
                    <select id="doc-type" style="width:100%; padding:14px 16px; border:1.5px solid #e5e5ea; border-radius:12px; font-size:16px; background:#f2f2f7; font-family:inherit;">
                        <option value="1">Leave Request</option>
                        <option value="2">Procurement</option>
                        <option value="3">Medical</option>
                    </select>
                </div>
                
                <div class="input-group">
                    <label>Select File</label>
                    <div class="file-upload-area" onclick="document.getElementById('file-input').click()">
                        <div class="icon">📁</div>
                        <p id="file-name">Tap to select file</p>
                        <small>PDF, JPG, PNG (max 10MB)</small>
                    </div>
                    <input type="file" id="file-input" accept=".pdf,.jpg,.jpeg,.png" style="display:none" onchange="updateFileName(this)">
                </div>
                
                <button class="btn btn-primary" onclick="doUpload()" id="upload-btn">Upload</button>
                <button class="btn btn-secondary" onclick="renderHome()">Cancel</button>
            </div>
        </div>
    `;
    
    window.updateFileName = (input) => {
        document.getElementById('file-name').textContent = input.files[0] ? input.files[0].name : 'Tap to select file';
    };
    
    window.doUpload = async () => {
        const fileInput = document.getElementById('file-input');
        const docType = document.getElementById('doc-type').value;
        
        if (!fileInput.files[0]) return showToast('Please select a file', 'error');
        
        const btn = document.getElementById('upload-btn');
        btn.disabled = true;
        btn.textContent = 'Uploading...';
        
        try {
            await uploadDocument(fileInput.files[0], parseInt(docType));
            showToast('Document uploaded successfully!', 'success');
            setTimeout(() => renderHome(), 800);
        } catch (err) {
            showToast('Upload failed. Try again.', 'error');
            btn.disabled = false;
            btn.textContent = 'Upload';
        }
    };
}

async function renderMyDocuments() {
    document.getElementById('app').innerHTML = `
        <div class="fade-in">
            <div class="app-header">
                <button class="header-btn" onclick="renderHome()" style="padding:8px 12px;">‹ Back</button>
                <div style="text-align:center;">
                    <h1>My Documents</h1>
                </div>
                <div style="width:60px;"></div>
            </div>
            
            <div class="content">
                <div id="documents-list">
                    <div class="empty-state">
                        <div class="icon">📭</div>
                        <h3>No documents yet</h3>
                        <p>Upload your first document</p>
                    </div>
                </div>
            </div>
        </div>
    `;
}

async function renderReview() {
    document.getElementById('app').innerHTML = `
        <div class="fade-in">
            <div class="app-header">
                <button class="header-btn" onclick="renderHome()" style="padding:8px 12px;">‹ Back</button>
                <div style="text-align:center;">
                    <h1>Review Documents</h1>
                </div>
                <div style="width:60px;"></div>
            </div>
            
            <div class="content">
                <div id="review-list">
                    <div class="empty-state">
                        <div class="icon">✅</div>
                        <h3>All caught up</h3>
                        <p>No documents pending review</p>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Start the app
if (isLoggedIn()) {
    renderHome();
} else {
    renderLogin();
}
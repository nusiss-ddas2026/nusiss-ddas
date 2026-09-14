document.addEventListener('DOMContentLoaded', function() {
    const path = window.location.pathname;
    const token = getCookie('jwtToken');
    
    if (path === '/home') {
        if (!token) {
            window.location.href = '/login';
        }
        
        const role = localStorage.getItem('role');
        
        if (role === 'APPROVER') {
            document.getElementById('requestBtn').style.display = 'inline-block';
        } else {
            document.getElementById('requestBtn').style.display = 'none';
        }
        
        if (role === 'ADMIN') {
            document.getElementById('userBtn').style.display = 'inline-block';
        } else {
            document.getElementById('userBtn').style.display = 'none';
        }
        
        const logoutForm = document.querySelector('form[action="/logout"]');
        if (logoutForm) {
            logoutForm.addEventListener('submit', function() {
                localStorage.clear();
                window.location.href = '/login';
            });
        }
        
        const reviewForm = document.getElementById('requestForm');
        if (reviewForm) {
            reviewForm.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                const selects = reviewForm.querySelectorAll('select[id^="action_"]');
                const formData = new URLSearchParams();
                
                selects.forEach(function(select) {
                    var docId = select.id.replace('action_', '');
                    formData.append('documentId', docId);
                    formData.append('action', select.value);
                });
                
                formData.append('mode', 'requestManagement');
                
                try {
                    const response = await fetch('/reviewDocument', {
                        method: 'POST',
                        headers: { 'Authorization': 'Bearer ' + token },
                        body: formData
                    });
                    
                    if (response.status === 401) {
                        localStorage.clear();
                        window.location.href = '/login';
                        return;
                    }
                    
                    window.location.href = '/home?mode=requestManagement';
                } catch (error) {
                    alert('Error saving changes');
                }
            });
        }
        
        const userForm = document.getElementById('userForm');
        if (userForm) {
            userForm.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                const roleSelects = userForm.querySelectorAll('select[id^="role_"]');
                const deptSelects = userForm.querySelectorAll('select[id^="dept_"]');
                const formData = new URLSearchParams();
                
                for (var i = 0; i < roleSelects.length; i++) {
                    formData.append('userId', roleSelects[i].id.replace('role_', ''));
                    formData.append('role', roleSelects[i].value);
                    formData.append('dept', deptSelects[i].value);
                }
                
                formData.append('mode', 'userManagement');
                
                try {
                    const response = await fetch('/bulkUpdateUsers', {
                        method: 'POST',
                        headers: { 'Authorization': 'Bearer ' + token },
                        body: formData
                    });
                    
                    if (response.status === 401) {
                        localStorage.clear();
                        window.location.href = '/login';
                        return;
                    }
                    
                    window.location.href = '/home?mode=myDocuments';
                } catch (error) {
                    alert('Error saving user changes');
                }
            });
        }
        
        const verifyForms = document.querySelectorAll('form[action="/verifyDocument"]');
        verifyForms.forEach(function(form) {
            form.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                const docId = form.querySelector('input[name="documentId"]').value;
                const mode = form.querySelector('input[name="mode"]').value;
                
                const formData = new URLSearchParams();
                formData.append('documentId', docId);
                formData.append('mode', mode);
                
                try {
                    const response = await fetch('/verifyDocument', {
                        method: 'POST',
                        headers: { 'Authorization': 'Bearer ' + token },
                        body: formData
                    });
                    
                    if (response.status === 401) {
                        localStorage.clear();
                        window.location.href = '/login';
                        return;
                    }
                    
                    window.location.href = '/home?mode=' + mode;
                } catch (error) {
                    alert('Error verifying document');
                }
            });
        });
    }
    
    if (path === '/uploadDocument') {
        if (!token) {
            window.location.href = '/login';
        }
        
        const form = document.getElementById('uploadForm');
        if (form) {
            form.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                const docName = document.getElementById('docName').value;
                const docType = document.getElementById('docType').value;
                const fileInput = document.getElementById('file');
                const file = fileInput.files[0];
                
                const formData = new FormData();
                formData.append('docName', docName);
                formData.append('docType', docType);
                formData.append('file', file);
                
                try {
                    const response = await fetch('/uploadDocument', {
                        method: 'POST',
                        headers: { 'Authorization': 'Bearer ' + token },
                        body: formData
                    });
                    
                    if (response.status === 401) {
                        localStorage.clear();
                        window.location.href = '/login';
                        return;
                    }
                    
                    window.location.href = '/home?mode=myDocuments';
                } catch (error) {
                    const errorDiv = document.querySelector('.error-message');
                    if (errorDiv) errorDiv.textContent = 'Upload failed';
                }
            });
        }
    }
});
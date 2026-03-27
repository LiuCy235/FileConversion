// PDF 转 Word 表单提交
if (document.getElementById('pdfForm')) {
    document.getElementById('pdfForm').addEventListener('submit', function(e) {
        e.preventDefault();
        
        const fileInput = document.getElementById('pdfFile');
        const statusDiv = document.getElementById('status');
        const submitButton = this.querySelector('button[type="submit"]');
        
        if (!fileInput.files || fileInput.files.length === 0) {
            showStatus('请选择要上传的 PDF 文件', 'error');
            return;
        }
        
        const file = fileInput.files[0];
        if (!file.name.toLowerCase().endsWith('.pdf')) {
            showStatus('请上传 PDF 格式的文件', 'error');
            return;
        }
        
        // 禁用按钮，防止重复点击
        submitButton.disabled = true;
        submitButton.textContent = '转换中...';
        
        showStatus('<div class="loading"></div>正在转换...', 'success');
        
        const formData = new FormData();
        formData.append('file', file);
        
        fetch('/api/file/pdf-to-word', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
            return response.blob();
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            // 生成带时间戳的文件名
            const originalName = file.name.replace('.pdf', '');
            const timestamp = new Date().getTime();
            a.download = `${originalName}_${timestamp}.docx`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            showStatus('转换成功，文件已开始下载', 'success');
        })
        .catch(error => {
            try {
                const errorObj = JSON.parse(error.message);
                // 检查是否是防重复提交错误
                if (errorObj.message.includes('请勿重复提交')) {
                    showModal('提示', errorObj.message);
                } else {
                    showStatus('转换失败：' + errorObj.message, 'error');
                }
            } catch {
                showStatus('转换失败：' + error.message, 'error');
            }
        })
        .finally(() => {
            // 恢复按钮状态
            submitButton.disabled = false;
            submitButton.textContent = '转换为 Word';
        });
    });
}

// Word 转 PDF 表单提交
if (document.getElementById('wordForm')) {
    document.getElementById('wordForm').addEventListener('submit', function(e) {
        e.preventDefault();
        
        const fileInput = document.getElementById('wordFile');
        const statusDiv = document.getElementById('status');
        const submitButton = this.querySelector('button[type="submit"]');
        
        if (!fileInput.files || fileInput.files.length === 0) {
            showStatus('请选择要上传的 Word 文件', 'error');
            return;
        }
        
        const file = fileInput.files[0];
        const fileName = file.name.toLowerCase();
        if (!fileName.endsWith('.docx') && !fileName.endsWith('.doc')) {
            showStatus('请上传 Word 格式的文件 (.docx 或 .doc)', 'error');
            return;
        }
        
        // 禁用按钮，防止重复点击
        submitButton.disabled = true;
        submitButton.textContent = '转换中...';
        
        showStatus('<div class="loading"></div>正在转换...', 'success');
        
        const formData = new FormData();
        formData.append('file', file);
        
        fetch('/api/file/word-to-pdf', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
            return response.blob();
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            // 生成带时间戳的文件名
            const originalName = file.name.replace('.docx', '').replace('.doc', '');
            const timestamp = new Date().getTime();
            a.download = `${originalName}_${timestamp}.pdf`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            showStatus('转换成功，文件已开始下载', 'success');
        })
        .catch(error => {
            try {
                const errorObj = JSON.parse(error.message);
                // 检查是否是防重复提交错误
                if (errorObj.message.includes('请勿重复提交')) {
                    showModal('提示', errorObj.message);
                } else {
                    showStatus('转换失败：' + errorObj.message, 'error');
                }
            } catch {
                showStatus('转换失败：' + error.message, 'error');
            }
        })
        .finally(() => {
            // 恢复按钮状态
            submitButton.disabled = false;
            submitButton.textContent = '转换为 PDF';
        });
    });
}

function showStatus(message, type) {
    const statusDiv = document.getElementById('status');
    statusDiv.innerHTML = message;
    statusDiv.className = 'status ' + type;
    statusDiv.classList.remove('hidden');
    
    // 3秒后自动隐藏状态信息
    setTimeout(() => {
        statusDiv.classList.add('hidden');
    }, 3000);
}

// 显示弹窗
function showModal(title, message) {
    const modal = document.getElementById('modal');
    const modalTitle = document.getElementById('modal-title');
    const modalBody = document.getElementById('modal-body');
    
    modalTitle.textContent = title;
    modalBody.textContent = message;
    modal.style.display = 'block';
}

// 关闭弹窗
function closeModal() {
    document.getElementById('modal').style.display = 'none';
}

// 点击弹窗外部关闭
window.onclick = function(event) {
    const modal = document.getElementById('modal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}
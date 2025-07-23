// 공통 유틸리티 함수들
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} fixed top-4 right-4 p-4 rounded shadow-lg z-50`;
    alertDiv.textContent = message;
    document.body.appendChild(alertDiv);

    setTimeout(() => {
    alertDiv.remove();
}, 3000);
}

    // 폼 유효성 검사 헬퍼
function validateForm(formElement) {
    const inputs = formElement.querySelectorAll('input[required], textarea[required], select[required]');
    let isValid = true;

    inputs.forEach(input => {
    if (!input.value.trim()) {
    input.classList.add('border-red-500');
    isValid = false;
    } else {
        input.classList.remove('border-red-500');
    }
    });
    return isValid;
}
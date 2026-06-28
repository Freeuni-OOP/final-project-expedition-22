document.getElementById('login-form').addEventListener('submit', async function (event) {
    event.preventDefault();

    document.getElementById('username-error').textContent = '';
    document.getElementById('password-error').textContent = '';

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            localStorage.setItem('user', JSON.stringify(data));
            window.location.href = '/';
            return;
        }

        if (data.fieldErrors) {
            if (data.fieldErrors.username) {
                document.getElementById('username-error').textContent = data.fieldErrors.username;
            } else if (data.fieldErrors.password) {
                document.getElementById('password-error').textContent = data.fieldErrors.password;
            }
        } else if (data.message) {
            document.getElementById('username-error').textContent = data.message;
        }

    } catch (err) {
        document.getElementById('username-error').textContent = 'დაფიქსირდა შეცდომა, სცადეთ თავიდან';
    }
});
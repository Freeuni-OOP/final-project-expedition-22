document.getElementById('login-form').addEventListener('submit', async function (event) {
    event.preventDefault();

    document.getElementById('username-error').textContent = '';
    document.getElementById('password-error').textContent = '';

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            credentials: 'same-origin',
            body: new URLSearchParams({
                'username': username,
                'password': password
            })
        });

        if (response.ok) {
            window.location.href = '/';
            return;
        } else {
            document.getElementById('username-error').textContent = 'არასწორი მომხმარებლის სახელი ან პაროლი';
        }

    } catch (err) {
        document.getElementById('username-error').textContent = 'დაფიქსირდა შეცდომა, სცადეთ თავიდან';
    }
});
document.getElementById('registerForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const phone = document.getElementById('phoneNumber').value;
    const email = document.getElementById('email').value;
    const messageBox = document.getElementById('messageBox');

    messageBox.textContent = "";

    const allInputErrors = document.querySelectorAll('[id$="-error"]');
    allInputErrors.forEach(div => {
        div.textContent = "";
        div.style.display = 'none';
    });

    const requestData = {
        username: username,
        password: password,
        phoneNumber: phone,
        email: email
    };

    try {
        const response = await fetch('/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        if (response.ok) {
            messageBox.textContent = " რეგისტრაცია წარმატებით დასრულდა!";
            messageBox.style.color = "green";

            setTimeout(() => {
                window.location.href = "/login";
            }, 2000);

        } else {
            const errorsMap = await response.json();
            for (let fieldName in errorsMap) {
                const errorDiv = document.getElementById(`${fieldName}-error`);
                if (errorDiv) {
                    errorDiv.textContent = errorsMap[fieldName];
                    errorDiv.style.color = "red";
                }
            }

            const firstVisibleError = Array.from(document.querySelectorAll('.input-error'))
                .find(div => div.textContent.trim() !== "");

            if (firstVisibleError) {
                firstVisibleError.style.display = 'block';
            } else {
                const firstKey = Object.keys(errorsMap)[0];
                messageBox.textContent = errorsMap[firstKey] || "შეცდომა რეგისტრაციისას!";
                messageBox.style.color = "red";
            }
        }

    } catch (error) {
        messageBox.textContent = "სერვერთან კავშირი ვერ დამყარდა.";
        messageBox.style.color = "red";
    }
});
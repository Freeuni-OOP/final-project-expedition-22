const form = document.getElementById("addBookForm");
const imageInput = document.getElementById("image");
const imagePreview = document.getElementById("imagePreview");
const message = document.getElementById("message");

imageInput.addEventListener("change", () => {
    const file = imageInput.files[0];

    if (!file) {
        imagePreview.style.display = "none";
        return;
    }

    imagePreview.src = URL.createObjectURL(file);
    imagePreview.style.display = "block";
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const formData = new FormData();

    formData.append("title", document.getElementById("title").value);
    formData.append("author", document.getElementById("author").value);
    formData.append("genre", document.getElementById("genre").value);
    formData.append("releaseYear", document.getElementById("releaseYear").value);
    formData.append("price", document.getElementById("price").value);
    formData.append("description", document.getElementById("description").value);
    formData.append("image", imageInput.files[0]);

    try {
        const response = await fetch("/books", {
            method: "POST",
            body: formData
        });

        const data = await response.json();

        if (response.ok) {
            message.style.color = "green";
            message.textContent = "წიგნი წარმატებით დაემატა!";
            return;
        }

        // Validation errors
        if (data.fieldErrors) {
            if (data.fieldErrors.title) {
                document.getElementById("title-error").textContent = data.fieldErrors.title;
            }

            if (data.fieldErrors.author) {
                document.getElementById("author-error").textContent = data.fieldErrors.author;
            }

            if (data.fieldErrors.genre) {
                document.getElementById("genre-error").textContent = data.fieldErrors.genre;
            }

            if (data.fieldErrors.releaseYear) {
                document.getElementById("year-error").textContent = data.fieldErrors.releaseYear;
            }

            if (data.fieldErrors.price) {
                document.getElementById("price-error").textContent = data.fieldErrors.price;
            }

            if (data.fieldErrors.description) {
                document.getElementById("description-error").textContent = data.fieldErrors.description;
            }

            if (data.fieldErrors.image) {
                document.getElementById("image-error").textContent = data.fieldErrors.image;
            }
        }
        else if (data.message) {
            message.style.color = "red";
            message.textContent = data.message;
        }

    } catch (e) {
        message.style.color = "red";
        message.textContent = "დაფიქსირდა შეცდომა. გთხოვთ, სცადოთ თავიდან.";
    }
});
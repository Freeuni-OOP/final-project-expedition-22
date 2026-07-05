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

        if (!response.ok) {
            throw new Error("Failed to create book");
        }

        message.textContent = "Book created successfully!";
        message.style.color = "green";
        form.reset();
        imagePreview.style.display = "none";
    } catch (error) {
        message.textContent = error.message;
        message.style.color = "red";
    }
});
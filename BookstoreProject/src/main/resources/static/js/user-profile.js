loadUserInfo();
loadBooks(`/users/me/books`, "myBooksGrid", "თქვენ ჯერ წიგნი არ დაგიმატებიათ.",true,false);
loadBooks(`/users/me/favorites`, "favoriteBooksGrid", "რჩეული წიგნები ჯერ არ გაქვთ.",false,true);

async function loadBooks(
    url,
    gridId,
    emptyText,
    showDeleteButton = false,
    showFavoriteButton = false
) {
    const grid = document.getElementById(gridId);

    try {
        const response = await fetch(url);
        const books = await response.json();

        if (!response.ok || books.length === 0) {
            grid.innerHTML = `<p class="empty-message">${emptyText}</p>`;
            return;
        }

        grid.innerHTML = books
            .map(book =>
                createBookCard(
                    book,
                    showDeleteButton,
                    showFavoriteButton,
                    gridId
                )
            )
            .join("");

    } catch (error) {
        grid.innerHTML =
            `<p class="empty-message">დაფიქსირდა შეცდომა.</p>`;
    }
}

function createBookCard(book, showDeleteButton, showFavoriteButton, gridId) {
    const imageUrl = book.imageUrl || "/images/default-book.png";

    const deleteButton = showDeleteButton
        ? `
            <button
                type="button"
                class="delete-book-button"
                onclick="deleteBook(event, ${book.id})">
                წიგნის წაშლა
            </button>
          `
        : "";

    const favoriteButton = showFavoriteButton
        ? `
            <button
                type="button"
                class="favorite-button"
                onclick="removeFavorite(event, ${book.id})"
                aria-label="რჩეულებიდან ამოშლა">
                ⭐
            </button>
          `
        : "";

    return `
        <article class="book-card" id="${gridId}-book-${book.id}">
            ${favoriteButton}

            <a href="/books/details/${book.id}">
                <img class="book-card__cover"
                     src="${imageUrl}"
                     alt="${book.title}">

                <h3 class="book-card__title">${book.title}</h3>
                <p class="book-card__author">${book.author || ""}</p>
                <p class="book-card__description">${book.description || ""}</p>

                <div class="book-card__price">
                    ${book.price} ₾
                </div>
            </a>

            ${deleteButton}
        </article>
    `;
}

async function loadUserInfo() {
    try {
        const response = await fetch(`/users/me`);

        if (!response.ok) {
            throw new Error("Failed to load user");
        }

        const user = await response.json();

        document.getElementById("profileUsername").textContent =
            user.username || "მომხმარებელი";

        document.getElementById("profileEmail").textContent =
            user.email ? `ელფოსტა: ${user.email}` : "";

        document.getElementById("profilePhone").textContent =
            user.phoneNumber ? `ტელეფონი: ${user.phoneNumber}` : "";

        document.getElementById("profileInitial").textContent =
            user.username
                ? user.username.charAt(0).toUpperCase()
                : "U";

    } catch (error) {
        document.getElementById("profileUsername").textContent =
            "პროფილის მონაცემები ვერ ჩაიტვირთა";
    }
}

async function deleteBook(event, bookId) {
    event.preventDefault();
    event.stopPropagation();

    const confirmed = confirm(
        "ნამდვილად გსურთ ამ წიგნის წაშლა?"
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(`/books/${bookId}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error("Delete failed");
        }

        document.getElementById(`myBooksGrid-book-${bookId}`)?.remove();

    } catch (error) {
        alert("წიგნის წაშლა ვერ მოხერხდა.");
    }
}

async function removeFavorite(event, bookId) {
    event.preventDefault();
    event.stopPropagation();

    const response = await fetch(`/books/${bookId}/favorite`, {
        method: "DELETE"
    });

    if (response.ok) {
        document.getElementById(`favoriteBooksGrid-book-${bookId}`)?.remove();
    } else {
        alert("რჩეულებიდან წაშლა ვერ მოხერხდა.");
    }
}
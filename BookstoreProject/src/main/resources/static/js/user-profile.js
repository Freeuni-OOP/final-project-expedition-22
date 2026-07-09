const userId = 1;

loadBooks(`/users/${userId}/books`, "myBooksGrid", "თქვენ ჯერ წიგნი არ დაგიმატებიათ.");
loadBooks(`/users/${userId}/favorites`, "favoriteBooksGrid", "რჩეული წიგნები ჯერ არ გაქვთ.");

async function loadBooks(url, gridId, emptyText) {
    const grid = document.getElementById(gridId);

    try {
        const response = await fetch(url);
        const books = await response.json();

        if (!response.ok || books.length === 0) {
            grid.innerHTML = `<p class="empty-message">${emptyText}</p>`;
            return;
        }

        grid.innerHTML = books.map(createBookCard).join("");
    } catch (error) {
        grid.innerHTML = `<p class="empty-message">დაფიქსირდა შეცდომა.</p>`;
    }
}

function createBookCard(book) {
    const imageUrl = book.imageUrl || "/images/default-book.png";

    return `
        <article class="book-card">
            <a href="/books/${book.id}">
                <img class="book-card__cover" src="${imageUrl}" alt="${book.title}">
                <h3 class="book-card__title">${book.title}</h3>
                <p class="book-card__author">${book.author || ""}</p>
                <p class="book-card__description">${book.description || ""}</p>
                <div class="book-card__price">
                    <span>${book.price}</span> ₾
                </div>
            </a>
        </article>
    `;
}
document.addEventListener('DOMContentLoaded', () => {
    const searchForm = document.getElementById('searchForm');
    const searchTitle = document.getElementById('searchTitle');
    const filterGenre = document.getElementById('filterGenre');
    const filterYear = document.getElementById('filterYear');
    const resetBtn = document.getElementById('resetSearch');

    const bookGrid = document.getElementById('bookGrid');
    const emptyState = document.getElementById('emptyState');

    searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        searchBooks();
    });

    resetBtn.addEventListener('click', () => {
        searchForm.reset();
        searchBooks();
    });


    async function searchBooks() {
        const title = searchTitle.value.trim();
        const genre = filterGenre.value;
        const year = filterYear.value.trim();

        const params = new URLSearchParams();
        if (title) params.append('title', title);
        if (genre) params.append('genre', genre);
        if (year) params.append('year', year);

        const apiUrl = `/books/search?${params.toString()}`;

        try {
            showSkeletons();

            const response = await fetch(apiUrl);
            if (!response.ok) {
                throw new Error('Network response issues while pulling query data');
            }

            const books = await response.json();
            displayBooks(books);

        } catch (error) {
            console.error('Error fetching filtered search results:', error);
            displayBooks([]);
        }
    }

    function displayBooks(books) {
        bookGrid.innerHTML = '';

        if (!books || books.length === 0) {
            bookGrid.style.display = 'none';
            emptyState.style.display = 'flex';
            emptyState.querySelector('h2').textContent = 'წტს! წიგნები ვერ მოიძებნა';
            emptyState.querySelector('p').textContent = 'სცადეთ სხვა სათაური ან გამოიყენეთ განსხვავებული ფილტრები.';
            return;
        }

        emptyState.style.display = 'none';
        bookGrid.style.display = 'grid';

        books.forEach(book => {
            const imageUrl = (book.imageUrl && book.imageUrl.trim() !== '') ? book.imageUrl : '/images/default-book.png';

            const article = document.createElement('article');
            article.className = 'book-card';

            article.innerHTML = `
                <a href="/books/details/${book.id}">
                    <img class="book-card__cover" src="${imageUrl}" alt="Cover of ${book.title}">
                    <h3 class="book-card__title">${escapeHtml(book.title)}</h3>
                    <p class="book-card__author">${escapeHtml(book.author || 'Unknown')}</p>
                    <p class="book-card__description">${escapeHtml(book.description || '')}</p>
                    <div class="book-card__price">
                        <span>${parseFloat(book.price || 0).toFixed(2)}</span> ₾
                    </div>
                </a>
            `;
            bookGrid.appendChild(article);
        });
    }

    function showSkeletons() {
        bookGrid.innerHTML = Array(4).fill(`
            <div class="book-card--skeleton">
                <div class="cover-skeleton"></div>
                <div class="line-skeleton line-skeleton--wide"></div>
                <div class="line-skeleton line-skeleton--narrow"></div>
            </div>
        `).join('');
        bookGrid.style.display = 'grid';
        emptyState.style.display = 'none';
    }


    function escapeHtml(str) {
        return str.replace(/&/g, "&amp;")
                  .replace(/</g, "&lt;")
                  .replace(/>/g, "&gt;")
                  .replace(/"/g, "&quot;")
                  .replace(/'/g, "&#039;");
    }
});
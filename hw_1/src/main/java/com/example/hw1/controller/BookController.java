package com.example.hw1.controller;

import com.example.hw1.entity.Book;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookController {
    private List<Book> books = new ArrayList<>();

    // We need the '1l' to create default value, same for double which is '1f'
    private Long nextId = 1l;

    public BookController() {
        // Add 15 books with varied data for testing
        books.add(new Book(nextId++, "Spring Boot in Action", "Craig Walls", 39.99));
        books.add(new Book(nextId++, "Effective Java", "Joshua Bloch", 45.00));
        books.add(new Book(nextId++, "Clean Code", "Robert Martin", 42.50));
        books.add(new Book(nextId++, "Java Concurrency in Practice", "Brian Goetz", 49.99));
        books.add(new Book(nextId++, "Design Patterns", "Gang of Four", 54.99));
        books.add(new Book(nextId++, "Head First Java", "Kathy Sierra", 35.00));
        books.add(new Book(nextId++, "Spring in Action", "Craig Walls", 44.99));
        books.add(new Book(nextId++, "Clean Architecture", "Robert Martin", 39.99));
        books.add(new Book(nextId++, "Refactoring", "Martin Fowler", 47.50));
        books.add(new Book(nextId++, "The Pragmatic Programmer", "Andrew Hunt", 41.99));
        books.add(new Book(nextId++, "You Don't Know JS", "Kyle Simpson", 29.99));
        books.add(new Book(nextId++, "JavaScript: The Good Parts", "Douglas Crockford", 32.50));
        books.add(new Book(nextId++, "Eloquent JavaScript", "Marijn Haverbeke", 27.99));
        books.add(new Book(nextId++, "Python Crash Course", "Eric Matthes", 38.00));
        books.add(new Book(nextId++, "Automate the Boring Stuff", "Al Sweigart", 33.50));
    }




    // GET APIs

    // Note: Commented out the old GET because we made one with
    // Get all the books - /api/books
    @GetMapping("/books")
    public List<Book> getBooks() {
        return books;
    }



    // Get Book by id
    @GetMapping("/books/{id}")
    // @PathVariable says we are taking that arg from our path
    public Book getBook(@PathVariable Long id) {
        // Make a stream for array list books
        // Then filter them for a book id
        // We always return the first book, if not found at all, return null
        return books.stream().filter(book -> book.getId().equals(id))
                .findFirst().orElse(null);
        // Right now, we are returning null, but we should instead raise a 404 error saying
        //          'book not found'
    }


    // Get books searched by Title
    @GetMapping("/books/search")
    public List<Book> searchByTitle(
            // RequestParam is used to get parameters from the URL
            @RequestParam(required = false, defaultValue = "") String title
    ) {
        if (title.isEmpty()) {
            return books;
        }

        return books.stream()
                // Lambda function to search for title in Books
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }



    // Get books within a price range
    @GetMapping("/books/price-range")
    public List<Book> getBooksByPrice(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return books.stream()
                // Note: Don't add class type in .filter()
                // Ex. Don't do: .filter( Book book -> ... ); will give errors
                .filter(book -> {
                    boolean min = minPrice == null || book.getPrice() >= minPrice;
                    boolean max = maxPrice == null || book.getPrice() <= maxPrice;

                    return min && max;
                }).collect(Collectors.toList());
    }



    // Get sorted books
    @GetMapping("/books/sorted")
    public List<Book> getSortedBooks(
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "ascending") String order
    ) {
        Comparator<Book> comparator;

        switch(sortBy.toLowerCase()) {
            case "author":
                comparator = Comparator.comparing(Book::getAuthor);
                break;
            case "title":
                comparator = Comparator.comparing((Book::getTitle));
                break;
            default:
                comparator = Comparator.comparing((Book::getTitle));
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return books.stream().sorted(comparator)
                .collect(Collectors.toList());
    }




    // POST APIs

    // Make a new book, and POST it
    // Making a new book implies HTTP POST
    @PostMapping("/books")
    public List<Book> createBook(@RequestBody Book book) {
        books.add(book);
        return books;
    }




    // NOTE: All functions before this were made in class with the help of the professor
    //       All functions after this have been made by me

    // NOTE for me: API endpoint = a digital location where an API receives API calls or API requests


    // PUT endpoint - update book
    @PutMapping("/books/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book newBook) {
        // We are taking args from url, so @PathVariable
        // @RequestBody = take args from the body of the request
        //      NOTE: You can only have one @RequestBody per function

        // Lambda expression to find the required book to update
        Book existingBook = books.stream()
                .filter(book -> Objects.equals(book.getId(), id))
                .findFirst()
                .orElse(null);

        // Break down of finding book by ID
            // .stream()
                // Converts the list of objects into a stream
            // .filter(book-> Objects.equals(book.getId(), searchId))
                // Only keep those books which match our searchId
            // .findFirst().orElse(null)
                // Find the first value that matches and return that, otherwise return a null

        if (existingBook == null) {
            // Book not found
            return null;
        }

        // Update book with our given arguments
        existingBook.setId(id);
        existingBook.setTitle(newBook.getTitle());
        existingBook.setAuthor(newBook.getAuthor());
        existingBook.setPrice(newBook.getPrice());

        // New list of book makes this thread safe
            // i.e. directly changing our 'books' list makes it thread unsafe, so do it this way
        List<Book> newBooks = books;

        // Make a new final Book (the replaceAll func requires a final variable)
        final Book updatedBook = existingBook;

        // Now, replace the updated book in our books list
        newBooks.replaceAll(book -> {
            if (Objects.equals(book.getId(), id)) {
                return updatedBook;
            } else {
              return book;
            }
        });

        // Update our list of books
        books = newBooks;

        return updatedBook;
    }


    // PATCH endpoint - partial update book
    @PatchMapping("/books/{id}")
    public Book partiallyUpdateBook(@PathVariable Long id, @RequestBody Book newBook) {

        // Find book by id
        Book existingBook = books.stream()
                .filter(book-> Objects.equals(book.getId(), id))
                .findFirst().orElse(null);

        if (existingBook == null) {
            // Book not found
            return null;
        }

        // Update variables only if we have passed some
        if (newBook.getTitle() != null) {
            existingBook.setTitle(newBook.getTitle());
        }
        if (newBook.getAuthor() != null) {
            existingBook.setAuthor(newBook.getAuthor());
        }
        if (newBook.getPrice() != null) {
            existingBook.setPrice((newBook.getPrice()));
        }

        // Make a new final Book
        final Book updatedBook = existingBook;

        // Now, replace the updated book in our books list
        books.replaceAll(book -> {
            if (Objects.equals(book.getId(), id)) {
                return updatedBook;
            } else {
                return book;
            }
        });

        return updatedBook;
    }


    // DELETE endpoint - remove book
    @DeleteMapping("/books/{id}")
    public List<Book> deleteBook(@PathVariable Long id) {
        // Update our list of books
        return books = books.stream()
                .filter(book -> !Objects.equals(book.getId(), id))
                .toList();
        // Lambda Expression Explantation
            // books.stream()
                // Convert books list to a stream
            // .filter(book -> !Objects.equals(book.getId(), id))
                // Filter out books when the ids match
            // .toList()
                // Convert stream back to a list
    }


    // TO DO: GET endpoint with pagination
        // BIG NOTE: Do not include the query parameters in our URL
        // No '/books?page={page}&per_page={per_page}', Spring Boot handles it automatically
    @GetMapping("/books/paged")
    public List<Book> getBooksPaginated(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer per_page
    ) {
        // Accept 2 Query parameters
            // Query params are params after the '?', and we access them using @RequestParam
        // page = what the current page number is (note: this can be zero)
        // per_page = how many items per page

        // Check if either values are zero or negative
        if (page < 1) {
            page = 1;
        }
        if (per_page < 1) {
            per_page = 50;
        }

        // page = 1 should be zero for our calculation, and so forth
        final int skipCount = (page - 1) * per_page;

        return books.stream()
                .skip(skipCount)    // Skip elements on previous pages
                .limit(per_page)    // Only allow max num of elements in one page
                .toList();          // Convert back to a list
    }


    // TO DO: advanced GET with filtering, sorting, and pagination
    @GetMapping("/books/advanced")
    public List<Book> getBooksAdvanced(
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "ascending") String order,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer per_page
            ) {

        List<Book> filteredBooks = books;

        // First apply filtering, by title, and by price
        if (!title.isEmpty()) {
            // Filter by title
            filteredBooks = books.stream()
                    // Lambda function to search for title in Books
                    .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .toList();
        }

        // Filter by Price
        filteredBooks = filteredBooks.stream()
                .filter(book -> {
                    boolean min = minPrice == null || book.getPrice() >= minPrice;
                    boolean max = maxPrice == null || book.getPrice() <= maxPrice;

                    return min && max;
                }).collect(Collectors.toList());

        List<Book> sortedBooks = filteredBooks;

        // Then, apply sorting
        if ((sortBy != null) && (order != null)) {
            Comparator<Book> comparator;

            switch(sortBy.toLowerCase()) {
                case "author":
                    comparator = Comparator.comparing(Book::getAuthor);
                    break;
                case "title":
                    comparator = Comparator.comparing((Book::getTitle));
                    break;
                default:
                    comparator = Comparator.comparing((Book::getTitle));
                    break;
            }

            if ("desc".equalsIgnoreCase(order)) {
                comparator = comparator.reversed();
            }

            sortedBooks = filteredBooks.stream().sorted(comparator)
                    .toList();
        }

        // Check if either values are zero or negative
        if (page < 1) {
            page = 1;
        }
        if (per_page < 1) {
            per_page = 50;
        }

        // page = 1 should be zero for our calculation, and so forth
        final int skipCount = (page - 1) * per_page;

        // Now, apply pagination
        return sortedBooks.stream()
                .skip(skipCount)    // Skip elements on previous pages
                .limit(per_page)    // Only allow max num of elements in one page
                .toList();          // Convert back to a list
    }

}

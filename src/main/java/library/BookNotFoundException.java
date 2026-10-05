package library;

/**
 * Thrown when an operation refers to a book ISBN that does not exist
 * in the library's collection.
 */
public class BookNotFoundException extends Exception {

    public BookNotFoundException(String isbn) {
        super("No book found with ISBN: " + isbn);
    }
}

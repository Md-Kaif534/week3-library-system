package library;

/**
 * Thrown when a borrow operation is attempted on a book that is already
 * checked out by someone else.
 */
public class BookUnavailableException extends Exception {

    public BookUnavailableException(String title) {
        super("\"" + title + "\" is already borrowed and is not available.");
    }
}

package library;

/**
 * Thrown when an operation refers to a member ID that is not registered
 * in the library.
 */
public class MemberNotFoundException extends Exception {

    public MemberNotFoundException(String memberId) {
        super("No member found with ID: " + memberId);
    }
}

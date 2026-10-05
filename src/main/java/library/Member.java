package library;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a registered library member.
 *
 * <p>Holds the list of ISBNs this member currently has borrowed. Keeping
 * this on the Member (rather than only on each Book) makes "show all
 * books borrowed by member X" an O(1) lookup instead of a scan over every
 * book in the library.</p>
 */
public class Member {

    private final String id;
    private String name;
    private String email;
    private final List<String> borrowedIsbns;
    private final LocalDate registeredDate;

    public Member(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.borrowedIsbns = new ArrayList<>();
        this.registeredDate = LocalDate.now();
    }

    private Member(String id, String name, String email, LocalDate registeredDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.borrowedIsbns = new ArrayList<>();
        this.registeredDate = registeredDate;
    }

    // ---------------------------------------------------------------- getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public LocalDate getRegisteredDate() { return registeredDate; }

    /** Unmodifiable view — callers must go through borrowBook()/returnBook() to change it. */
    public List<String> getBorrowedIsbns() { return Collections.unmodifiableList(borrowedIsbns); }

    // ---------------------------------------------------------------- setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    public void borrowBook(String isbn) { borrowedIsbns.add(isbn); }
    public void returnBook(String isbn) { borrowedIsbns.remove(isbn); }

    public int borrowedCount() { return borrowedIsbns.size(); }

    @Override
    public String toString() {
        return String.format("ID: %-8s | Name: %-20s | Email: %-25s | Books borrowed: %d",
                id, name, email, borrowedIsbns.size());
    }

    /** Serializes this member as one pipe-delimited line for members.txt. */
    public String toFileLine() {
        String isbnList = borrowedIsbns.isEmpty() ? "-" : String.join(",", borrowedIsbns);
        return String.join("|", id, name, email, isbnList, registeredDate.toString());
    }

    /**
     * Parses one pipe-delimited line from members.txt back into a Member.
     * Returns null if the line is malformed, so one bad row doesn't abort
     * the whole file load.
     */
    public static Member fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 5) return null;

        try {
            LocalDate registered = LocalDate.parse(parts[4]);
            Member member = new Member(parts[0], parts[1], parts[2], registered);
            if (!"-".equals(parts[3])) {
                for (String isbn : parts[3].split(",")) {
                    member.borrowedIsbns.add(isbn);
                }
            }
            return member;
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

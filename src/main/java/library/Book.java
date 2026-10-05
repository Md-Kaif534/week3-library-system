package library;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Represents a single book in the library's collection.
 *
 * <p>Encapsulation: every field is private; callers interact only through
 * getters/setters, so Book can validate or change its internal
 * representation later without breaking Library or Main.</p>
 */
public class Book {

    private final String isbn;
    private String title;
    private String author;
    private int year;
    private boolean available;
    private String borrowedBy;   // member ID, or null if not borrowed
    private LocalDate dueDate;   // null if not borrowed

    public Book(String isbn, String title, String author, int year) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.available = true;
        this.borrowedBy = null;
        this.dueDate = null;
    }

    // ---------------------------------------------------------------- getters
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public boolean isAvailable() { return available; }
    public String getBorrowedBy() { return borrowedBy; }
    public LocalDate getDueDate() { return dueDate; }

    // ---------------------------------------------------------------- setters
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setYear(int year) { this.year = year; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setBorrowedBy(String borrowedBy) { this.borrowedBy = borrowedBy; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    /** True once today is strictly after the due date on a borrowed book. */
    public boolean isOverdue() {
        if (dueDate == null || available) return false;
        return LocalDate.now().isAfter(dueDate);
    }

    /** Days overdue (0 if not overdue / not borrowed). Used for fine calculation. */
    public long daysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    @Override
    public String toString() {
        String status = available
                ? "Available"
                : "Borrowed by: " + borrowedBy + " | Due: " + dueDate + (isOverdue() ? " (OVERDUE)" : "");
        return String.format("ISBN: %-14s | Title: %-30s | Author: %-20s | Year: %d | %s",
                isbn, title, author, year, status);
    }

    /** Serializes this book as one pipe-delimited line for books.txt. */
    public String toFileLine() {
        return String.join("|",
                isbn,
                title,
                author,
                String.valueOf(year),
                String.valueOf(available),
                borrowedBy == null ? "-" : borrowedBy,
                dueDate == null ? "-" : dueDate.toString());
    }

    /**
     * Parses one pipe-delimited line from books.txt back into a Book.
     * Returns null (and lets the caller log it) if the line is malformed,
     * rather than throwing and aborting the whole file load.
     */
    public static Book fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) return null;

        try {
            Book book = new Book(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]));
            book.setAvailable(Boolean.parseBoolean(parts[4]));
            book.setBorrowedBy("-".equals(parts[5]) ? null : parts[5]);
            book.setDueDate("-".equals(parts[6]) ? null : LocalDate.parse(parts[6]));
            return book;
        } catch (NumberFormatException | DateTimeParseException e) {
            return null;
        }
    }
}

package library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The facade over the whole system: owns the in-memory collections of
 * books and members, and is the only class whose methods Main calls.
 *
 * <p>Every mutating method persists immediately via FileHandler, so the
 * on-disk files are never more than one operation stale — if the program
 * crashes, at most the last action is lost.</p>
 */
public class Library {

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double FINE_PER_DAY = 5.0; // currency units per day overdue

    private List<Book> books;
    private List<Member> members;
    private final FileHandler fileHandler;

    public Library() {
        this.fileHandler = new FileHandler();
        this.books = new ArrayList<>();
        this.members = new ArrayList<>();
        loadData();
    }

    /** Constructor used by tests to point at a temporary data directory. */
    public Library(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        this.books = new ArrayList<>();
        this.members = new ArrayList<>();
        loadData();
    }

    private void loadData() {
        books = fileHandler.loadBooks();
        members = fileHandler.loadMembers();
        System.out.println("Loaded " + books.size() + " books and " + members.size() + " members.");
    }

    // ---------------------------------------------------------------- books

    public void addBook(Book book) {
        books.add(book);
        fileHandler.saveBooks(books);
    }

    public void removeBook(String isbn) throws BookNotFoundException {
        Book book = findBookByIsbn(isbn);
        if (book == null) throw new BookNotFoundException(isbn);
        books.remove(book);
        fileHandler.saveBooks(books);
    }

    public Book findBookByIsbn(String isbn) {
        return books.stream()
                .filter(b -> b.getIsbn().equalsIgnoreCase(isbn))
                .findFirst()
                .orElse(null);
    }

    public List<Book> searchBooks(String keyword) {
        String q = keyword.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q)
                        || b.getAuthor().toLowerCase().contains(q)
                        || b.getIsbn().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Book> getAllBooks() { return new ArrayList<>(books); }

    public List<Book> getAvailableBooks() {
        return books.stream().filter(Book::isAvailable).collect(Collectors.toList());
    }

    public List<Book> getOverdueBooks() {
        return books.stream().filter(Book::isOverdue).collect(Collectors.toList());
    }

    public boolean isIsbnTaken(String isbn) {
        return findBookByIsbn(isbn) != null;
    }

    // ---------------------------------------------------------------- members

    public void registerMember(Member member) {
        members.add(member);
        fileHandler.saveMembers(members);
    }

    public Member findMemberById(String id) {
        return members.stream()
                .filter(m -> m.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public List<Member> searchMembers(String keyword) {
        String q = keyword.toLowerCase();
        return members.stream()
                .filter(m -> m.getName().toLowerCase().contains(q) || m.getId().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Member> getAllMembers() { return new ArrayList<>(members); }

    public boolean isMemberIdTaken(String id) {
        return findMemberById(id) != null;
    }

    // ---------------------------------------------------------------- borrowing

    public void borrowBook(String isbn, String memberId)
            throws BookNotFoundException, MemberNotFoundException, BookUnavailableException {

        Book book = findBookByIsbn(isbn);
        if (book == null) throw new BookNotFoundException(isbn);

        Member member = findMemberById(memberId);
        if (member == null) throw new MemberNotFoundException(memberId);

        if (!book.isAvailable()) throw new BookUnavailableException(book.getTitle());

        book.setAvailable(false);
        book.setBorrowedBy(memberId);
        book.setDueDate(LocalDate.now().plusDays(LOAN_PERIOD_DAYS));
        member.borrowBook(isbn);

        fileHandler.saveBooks(books);
        fileHandler.saveMembers(members);
    }

    /** Returns a book and reports any overdue fine owed, in one call. */
    public double returnBook(String isbn, String memberId)
            throws BookNotFoundException, MemberNotFoundException {

        Book book = findBookByIsbn(isbn);
        if (book == null) throw new BookNotFoundException(isbn);

        Member member = findMemberById(memberId);
        if (member == null) throw new MemberNotFoundException(memberId);

        double fine = calculateFine(book);

        book.setAvailable(true);
        book.setBorrowedBy(null);
        book.setDueDate(null);
        member.returnBook(isbn);

        fileHandler.saveBooks(books);
        fileHandler.saveMembers(members);

        return fine;
    }

    /** Simple linear fine model: a flat rate per day overdue. */
    public double calculateFine(Book book) {
        return book.daysOverdue() * FINE_PER_DAY;
    }

    // ---------------------------------------------------------------- display / stats

    public void displayAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books in the library yet.");
            return;
        }
        System.out.println("\n=== ALL BOOKS ===");
        System.out.println("Total books: " + books.size());
        System.out.println("-".repeat(90));
        for (int i = 0; i < books.size(); i++) {
            System.out.println((i + 1) + ". " + books.get(i));
        }
    }

    public void displayAllMembers() {
        if (members.isEmpty()) {
            System.out.println("No members registered yet.");
            return;
        }
        System.out.println("\n=== ALL MEMBERS ===");
        System.out.println("-".repeat(90));
        for (int i = 0; i < members.size(); i++) {
            System.out.println((i + 1) + ". " + members.get(i));
        }
    }

    public void displayStatistics() {
        long availableBooks = books.stream().filter(Book::isAvailable).count();
        long borrowedBooks = books.size() - availableBooks;
        long overdueBooks = books.stream().filter(Book::isOverdue).count();
        double totalOutstandingFines = books.stream().mapToDouble(this::calculateFine).sum();

        System.out.println("\n=== LIBRARY STATISTICS ===");
        System.out.println("Total Books:          " + books.size());
        System.out.println("Available Books:      " + availableBooks);
        System.out.println("Borrowed Books:       " + borrowedBooks);
        System.out.println("Registered Members:   " + members.size());
        System.out.println("Overdue Books:        " + overdueBooks);
        System.out.printf("Outstanding Fines:    %.2f%n", totalOutstandingFines);
    }

    public boolean exportBooksToCsv(String path) {
        return fileHandler.exportBooksToCsv(books, path);
    }
}

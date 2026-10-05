package library;

import java.util.List;
import java.util.Scanner;

/**
 * Console entry point: owns the menu loop and all Scanner-based input.
 * Business logic stays in Library; this class is only concerned with
 * talking to the user and validating what they type.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        System.out.println("=== LIBRARY MANAGEMENT SYSTEM ===");
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readIntInRange("Enter your choice: ", 1, 10);

            switch (choice) {
                case 1 -> handleAddBook();
                case 2 -> library.displayAllBooks();
                case 3 -> handleSearchBooks();
                case 4 -> handleRegisterMember();
                case 5 -> handleBorrowBook();
                case 6 -> handleReturnBook();
                case 7 -> library.displayStatistics();
                case 8 -> library.displayAllMembers();
                case 9 -> handleExportCsv();
                case 10 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1. Add New Book");
        System.out.println("2. View All Books");
        System.out.println("3. Search Books");
        System.out.println("4. Register Member");
        System.out.println("5. Borrow Book");
        System.out.println("6. Return Book");
        System.out.println("7. View Library Statistics");
        System.out.println("8. View All Members");
        System.out.println("9. Export Books to CSV");
        System.out.println("10. Exit");
    }

    // ---------------------------------------------------------------- menu actions

    private static void handleAddBook() {
        System.out.println("\n--- Add New Book ---");
        String isbn = readNonEmptyLine("ISBN: ");
        if (library.isIsbnTaken(isbn)) {
            System.out.println("A book with that ISBN already exists. Use a unique ISBN.");
            return;
        }
        String title = readNonEmptyLine("Title: ");
        String author = readNonEmptyLine("Author: ");
        int year = readIntInRange("Year published: ", 1450, java.time.Year.now().getValue());

        library.addBook(new Book(isbn, title, author, year));
        System.out.println("Book added successfully: " + title);
    }

    private static void handleSearchBooks() {
        System.out.println("\n--- Search Books ---");
        String keyword = readNonEmptyLine("Search by title, author, or ISBN: ");
        List<Book> results = library.searchBooks(keyword);

        if (results.isEmpty()) {
            System.out.println("No books matched \"" + keyword + "\".");
            return;
        }
        System.out.println("Found " + results.size() + " result(s):");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }
    }

    private static void handleRegisterMember() {
        System.out.println("\n--- Register New Member ---");
        String id = readNonEmptyLine("Member ID: ");
        if (library.isMemberIdTaken(id)) {
            System.out.println("A member with that ID already exists. Use a unique ID.");
            return;
        }
        String name = readNonEmptyLine("Name: ");
        String email = readValidEmail("Email: ");

        library.registerMember(new Member(id, name, email));
        System.out.println("Member registered successfully: " + name);
    }

    private static void handleBorrowBook() {
        System.out.println("\n--- Borrow Book ---");
        String isbn = readNonEmptyLine("Book ISBN: ");
        String memberId = readNonEmptyLine("Member ID: ");

        try {
            library.borrowBook(isbn, memberId);
            Book book = library.findBookByIsbn(isbn);
            System.out.println("Book borrowed successfully!");
            System.out.println("Due date: " + book.getDueDate());
        } catch (BookNotFoundException | MemberNotFoundException | BookUnavailableException e) {
            System.out.println("Could not complete the loan: " + e.getMessage());
        }
    }

    private static void handleReturnBook() {
        System.out.println("\n--- Return Book ---");
        String isbn = readNonEmptyLine("Book ISBN: ");
        String memberId = readNonEmptyLine("Member ID: ");

        try {
            double fine = library.returnBook(isbn, memberId);
            System.out.println("Book returned successfully!");
            if (fine > 0) {
                System.out.printf("This book was overdue. Fine owed: %.2f%n", fine);
            } else {
                System.out.println("Returned on time — no fine.");
            }
        } catch (BookNotFoundException | MemberNotFoundException e) {
            System.out.println("Could not complete the return: " + e.getMessage());
        }
    }

    private static void handleExportCsv() {
        System.out.println("\n--- Export Books to CSV ---");
        String path = readNonEmptyLine("Output file path (e.g. data/books-export.csv): ");
        if (library.exportBooksToCsv(path)) {
            System.out.println("Exported successfully to " + path);
        }
    }

    // ---------------------------------------------------------------- input helpers

    /** Keeps asking until the user provides a non-blank line. */
    private static String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("This field cannot be empty. Please try again.");
        }
    }

    /** Keeps asking until the user provides an integer within [min, max]. */
    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("That's not a valid number. Please try again.");
            }
        }
    }

    /** Very basic email shape check — good enough for a console demo app. */
    private static String readValidEmail(String prompt) {
        while (true) {
            String input = readNonEmptyLine(prompt);
            if (input.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
                return input;
            }
            System.out.println("That doesn't look like a valid email (expected something like name@example.com).");
        }
    }
}

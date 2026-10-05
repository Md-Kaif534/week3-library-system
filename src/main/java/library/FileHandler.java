package library;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns all reading and writing of books.txt / members.txt.
 *
 * <p>This is the only class that touches the filesystem — Library deals
 * purely with in-memory Lists and calls here to persist or reload them.
 * That separation means swapping the storage format later (e.g. to a real
 * database) only means rewriting this one class.</p>
 */
public class FileHandler {

    private final Path booksFile;
    private final Path membersFile;

    public FileHandler() {
        this("data/books.txt", "data/members.txt");
    }

    public FileHandler(String booksPath, String membersPath) {
        this.booksFile = Paths.get(booksPath);
        this.membersFile = Paths.get(membersPath);
        ensureDataFilesExist();
    }

    /** Creates the data/ directory and empty files on first run, if missing. */
    private void ensureDataFilesExist() {
        try {
            Path parent = booksFile.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            if (!Files.exists(booksFile)) Files.createFile(booksFile);
            if (!Files.exists(membersFile)) Files.createFile(membersFile);
        } catch (IOException e) {
            System.out.println("Warning: could not prepare data files (" + e.getMessage() + ")");
        }
    }

    // ---------------------------------------------------------------- books

    public List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(booksFile.toFile()))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                Book book = Book.fromFileLine(line);
                if (book == null) {
                    System.out.println("Skipped malformed book record at books.txt line " + lineNumber);
                } else {
                    books.add(book);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read books.txt (" + e.getMessage() + "). Starting with an empty catalog.");
        }
        return books;
    }

    public boolean saveBooks(List<Book> books) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(booksFile.toFile()))) {
            for (Book book : books) {
                writer.println(book.toFileLine());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Failed to save books.txt (" + e.getMessage() + "). Your changes are kept in memory only.");
            return false;
        }
    }

    // ---------------------------------------------------------------- members

    public List<Member> loadMembers() {
        List<Member> members = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(membersFile.toFile()))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                Member member = Member.fromFileLine(line);
                if (member == null) {
                    System.out.println("Skipped malformed member record at members.txt line " + lineNumber);
                } else {
                    members.add(member);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read members.txt (" + e.getMessage() + "). Starting with no members.");
        }
        return members;
    }

    public boolean saveMembers(List<Member> members) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(membersFile.toFile()))) {
            for (Member member : members) {
                writer.println(member.toFileLine());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Failed to save members.txt (" + e.getMessage() + "). Your changes are kept in memory only.");
            return false;
        }
    }

    // ---------------------------------------------------------------- CSV export

    /** Exports the current book catalog to a CSV file for the "advanced features" requirement. */
    public boolean exportBooksToCsv(List<Book> books, String outputPath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("ISBN,Title,Author,Year,Status,BorrowedBy,DueDate");
            for (Book b : books) {
                writer.printf("%s,%s,%s,%d,%s,%s,%s%n",
                        csvEscape(b.getIsbn()), csvEscape(b.getTitle()), csvEscape(b.getAuthor()),
                        b.getYear(), b.isAvailable() ? "Available" : "Borrowed",
                        b.getBorrowedBy() == null ? "" : b.getBorrowedBy(),
                        b.getDueDate() == null ? "" : b.getDueDate().toString());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Failed to export CSV (" + e.getMessage() + ").");
            return false;
        }
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

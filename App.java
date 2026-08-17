import java.util.*;

/* ======================================================
   LIBRARY MANAGEMENT SYSTEM - DSA MINOR PROJECT (JAVA)
   ------------------------------------------------------
   DSA Concepts Used:
   1. HashMap        -> O(1) lookup for books & members
   2. Queue (LinkedList) -> Waitlist for unavailable books
   3. Stack           -> Undo last issue/return action
   4. Binary Search Tree (BST) -> Search books by title
   5. LinkedList      -> Member's borrowing history
   6. Sorting (Collections.sort) -> Most popular books
   ====================================================== */

// ---------- Book class ----------
class Book {
    int id;
    String title;
    String author;
    boolean isAvailable;
    int timesIssued;

    Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
        this.timesIssued = 0;
    }

    @Override
    public String toString() {
        return String.format("[%d] \"%s\" by %s | %s | Issued %d times",
                id, title, author, isAvailable ? "Available" : "Issued", timesIssued);
    }
}

// ---------- Member class ----------
class Member {
    int id;
    String name;
    LinkedList<String> borrowHistory = new LinkedList<>(); // linked list of history logs
    List<Integer> currentBooks = new ArrayList<>();
    int fineDue = 0;

    Member(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | Books held: %s | Fine due: Rs.%d",
                id, name, currentBooks, fineDue);
    }
}

// ---------- BST Node for title-based search ----------
class BSTNode {
    Book book;
    BSTNode left, right;

    BSTNode(Book book) {
        this.book = book;
    }
}

class BookBST {
    BSTNode root;

    void insert(Book book) {
        root = insertRec(root, book);
    }

    private BSTNode insertRec(BSTNode node, Book book) {
        if (node == null)
            return new BSTNode(book);
        int cmp = book.title.compareToIgnoreCase(node.book.title);
        if (cmp < 0)
            node.left = insertRec(node.left, book);
        else if (cmp > 0)
            node.right = insertRec(node.right, book);
        return node;
    }

    Book search(String title) {
        return searchRec(root, title);
    }

    private Book searchRec(BSTNode node, String title) {
        if (node == null)
            return null;
        int cmp = title.compareToIgnoreCase(node.book.title);
        if (cmp == 0)
            return node.book;
        return cmp < 0 ? searchRec(node.left, title) : searchRec(node.right, title);
    }

    // In-order traversal gives titles alphabetically
    void inorder(BSTNode node, List<Book> result) {
        if (node == null)
            return;
        inorder(node.left, result);
        result.add(node.book);
        inorder(node.right, result);
    }

    List<Book> getAllSorted() {
        List<Book> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }
}

// ---------- Action record for Undo (Stack) ----------
class Action {
    String type; // "ISSUE" or "RETURN"
    int bookId, memberId;

    Action(String type, int bookId, int memberId) {
        this.type = type;
        this.bookId = bookId;
        this.memberId = memberId;
    }
}

// ---------- Library class: the core engine ----------
class Library {
    HashMap<Integer, Book> books = new HashMap<>(); // bookId -> Book
    HashMap<Integer, Member> members = new HashMap<>(); // memberId -> Member
    HashMap<Integer, Queue<Integer>> waitlists = new HashMap<>(); // bookId -> queue of memberIds
    BookBST titleIndex = new BookBST();
    Deque<Action> undoStack = new ArrayDeque<>(); // stack for undo

    static final int FINE_PER_DAY = 5;
    static final int LOAN_DAYS = 14;

    void addBook(int id, String title, String author) {
        Book b = new Book(id, title, author);
        books.put(id, b);
        titleIndex.insert(b);
        System.out.println("Book added: " + b);
    }

    void addMember(int id, String name) {
        members.put(id, new Member(id, name));
        System.out.println("Member added: " + name);
    }

    void issueBook(int bookId, int memberId) {
        Book book = books.get(bookId);
        Member member = members.get(memberId);
        if (book == null || member == null) {
            System.out.println("Invalid book or member ID.");
            return;
        }
        if (!book.isAvailable) {
            // add to waitlist queue
            waitlists.computeIfAbsent(bookId, k -> new LinkedList<>()).add(memberId);
            System.out.println(book.title + " is currently unavailable. "
                    + member.name + " added to waitlist (position "
                    + waitlists.get(bookId).size() + ").");
            return;
        }
        book.isAvailable = false;
        book.timesIssued++;
        member.currentBooks.add(bookId);
        member.borrowHistory.add("Issued: " + book.title + " on day 0");
        undoStack.push(new Action("ISSUE", bookId, memberId));
        System.out.println(book.title + " issued to " + member.name);
    }

    void returnBook(int bookId, int memberId, int daysHeld) {
        Book book = books.get(bookId);
        Member member = members.get(memberId);
        if (book == null || member == null || !member.currentBooks.contains(bookId)) {
            System.out.println("Record not found: this member hasn't borrowed this book.");
            return;
        }
        member.currentBooks.remove((Integer) bookId);
        member.borrowHistory.add("Returned: " + book.title);

        if (daysHeld > LOAN_DAYS) {
            int fine = (daysHeld - LOAN_DAYS) * FINE_PER_DAY;
            member.fineDue += fine;
            System.out.println("Overdue by " + (daysHeld - LOAN_DAYS) + " days. Fine charged: Rs." + fine);
        }

        // check waitlist queue
        Queue<Integer> q = waitlists.get(bookId);
        if (q != null && !q.isEmpty()) {
            int nextMemberId = q.poll();
            Member nextMember = members.get(nextMemberId);
            book.isAvailable = false;
            book.timesIssued++;
            nextMember.currentBooks.add(bookId);
            nextMember.borrowHistory.add("Issued (from waitlist): " + book.title);
            System.out.println(
                    book.title + " returned, and immediately issued to " + nextMember.name + " from waitlist.");
        } else {
            book.isAvailable = true;
            System.out.println(book.title + " returned. Now available.");
        }
        undoStack.push(new Action("RETURN", bookId, memberId));
    }

    void undoLastAction() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        Action last = undoStack.pop();
        Book book = books.get(last.bookId);
        Member member = members.get(last.memberId);
        if (last.type.equals("ISSUE")) {
            book.isAvailable = true;
            book.timesIssued--;
            member.currentBooks.remove((Integer) last.bookId);
            System.out.println("Undo: Issue of \"" + book.title + "\" to " + member.name + " reversed.");
        } else {
            book.isAvailable = false;
            member.currentBooks.add(last.bookId);
            System.out.println("Undo: Return of \"" + book.title + "\" by " + member.name + " reversed.");
        }
    }

    void searchByTitle(String title) {
        Book found = titleIndex.search(title);
        System.out.println(found != null ? "Found: " + found : "No book found with that title.");
    }

    void listBooksSortedAlphabetically() {
        System.out.println("\n--- All Books (A-Z) ---");
        for (Book b : titleIndex.getAllSorted())
            System.out.println(b);
    }

    void listMostPopularBooks() {
        List<Book> all = new ArrayList<>(books.values());
        all.sort((a, b) -> b.timesIssued - a.timesIssued); // descending
        System.out.println("\n--- Most Popular Books ---");
        for (Book b : all)
            System.out.println(b);
    }

    void showMemberHistory(int memberId) {
        Member m = members.get(memberId);
        if (m == null) {
            System.out.println("Member not found.");
            return;
        }
        System.out.println("\n--- History for " + m.name + " ---");
        for (String log : m.borrowHistory)
            System.out.println(log);
    }
}

// ---------- Main: menu-driven console interface ----------
public class App {
    public static void main(String[] args) {
        Library library = new Library();
        Scanner sc = new Scanner(System.in);

        // Seed some sample data
        library.addBook(1, "The Pragmatic Programmer", "Andrew Hunt");
        library.addBook(2, "Clean Code", "Robert Martin");
        library.addBook(3, "Introduction to Algorithms", "CLRS");
        library.addMember(101, "Asha");
        library.addMember(102, "Ravi");

        int choice;
        do {
            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Book");
            System.out.println("2. Add Member");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Search Book by Title (BST)");
            System.out.println("6. List All Books (A-Z)");
            System.out.println("7. Most Popular Books");
            System.out.println("8. Member Borrow History");
            System.out.println("9. Undo Last Action");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(sc.nextLine().trim());

            switch (choice) {
                case 1:
                    System.out.print("Book ID, Title, Author (comma separated): ");
                    String[] bp = sc.nextLine().split(",");
                    library.addBook(Integer.parseInt(bp[0].trim()), bp[1].trim(), bp[2].trim());
                    break;
                case 2:
                    System.out.print("Member ID, Name (comma separated): ");
                    String[] mp = sc.nextLine().split(",");
                    library.addMember(Integer.parseInt(mp[0].trim()), mp[1].trim());
                    break;
                case 3:
                    System.out.print("Book ID, Member ID: ");
                    String[] ip = sc.nextLine().split(",");
                    library.issueBook(Integer.parseInt(ip[0].trim()), Integer.parseInt(ip[1].trim()));
                    break;
                case 4:
                    System.out.print("Book ID, Member ID, Days Held: ");
                    String[] rp = sc.nextLine().split(",");
                    library.returnBook(Integer.parseInt(rp[0].trim()), Integer.parseInt(rp[1].trim()),
                            Integer.parseInt(rp[2].trim()));
                    break;
                case 5:
                    System.out.print("Enter title to search: ");
                    library.searchByTitle(sc.nextLine().trim());
                    break;
                case 6:
                    library.listBooksSortedAlphabetically();
                    break;
                case 7:
                    library.listMostPopularBooks();
                    break;
                case 8:
                    System.out.print("Enter Member ID: ");
                    library.showMemberHistory(Integer.parseInt(sc.nextLine().trim()));
                    break;
                case 9:
                    library.undoLastAction();
                    break;
                case 0:
                    System.out.println("Exiting. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 0);

        sc.close();
    }
}

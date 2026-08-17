# Library Management System — DSA Minor Project

A console-based Library Management System built in Java to demonstrate practical application of core data structures.

## Overview

This project simulates a small library's day-to-day operations — adding books/members, issuing and returning books, managing waitlists, tracking borrowing history, and undoing the last action — all through a menu-driven console interface.

## Data Structures Used

| Data Structure | Where It's Used | Why |
|---|---|---|
| **HashMap** | `books`, `members`, `waitlists` | O(1) average lookup for books and members by ID |
| **Queue (LinkedList)** | `waitlists` | FIFO waitlist — first member to request an unavailable book gets it first when returned |
| **Stack (ArrayDeque as Deque)** | `undoStack` | LIFO undo of the most recent issue/return action |
| **Binary Search Tree (custom BST)** | `titleIndex` | Efficient search and alphabetical listing of books by title |
| **LinkedList** | `borrowHistory` | Sequential log of each member's borrow/return activity |
| **Sorting (Comparator + Collections.sort)** | `listMostPopularBooks()` | Ranks books by number of times issued |

## Features

1. Add a new book / member
2. Issue a book (auto-added to waitlist queue if unavailable)
3. Return a book (auto-issues to next person in waitlist; calculates overdue fines)
4. Search for a book by title using the BST
5. List all books alphabetically (BST in-order traversal)
6. List most popular books (sorted by times issued)
7. View a member's full borrowing history
8. Undo the last issue/return action (stack-based)

## Project Structure

```
LibraryManagementSystem/
├── src/
│   └── App.java     # All classes: Book, Member, BSTNode, BookBST, Action, Library, App
└── README.md
```

## How to Run

**Requirements:** JDK 8 or higher

```bash
cd src
javac App.java
java App
```

The program seeds a few sample books and members on startup, then presents a menu to interact with the system.

## Sample Classes

- `Book` — id, title, author, availability, times issued
- `Member` — id, name, borrow history (LinkedList), currently held books, fine due
- `BookBST` / `BSTNode` — binary search tree indexed by book title
- `Action` — records issue/return events for the undo stack
- `Library` — core engine tying all structures together
- `App` — main class with the console menu loop

## Author

Akash — B.Tech Information Technology, Hindustan Institute of Technology and Science

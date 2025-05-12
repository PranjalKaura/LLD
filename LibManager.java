import java.util.*;

public class LibManager
{
    public static void main(String[] args) {
        LibController lb = new LibController();
        lb.createLibrary();
    }
}

class LibController
{
    public LibController()
    {

    }

    public void createLibrary()
    {
        User user1 = UserFactory.createUser("Pranjal", Tier.BASIC);
        User user2 = UserFactory.createUser("Messi", Tier.GOLD);

        Book book1 = new Book("Harry Potter", 2015);
        Book book2 = new Book("Harry Potter", 2017);
        Book book3 = new Book("Wizar of Oz", 2000);

        Library lib = new Library(new DefaultBookIssuanceStrategy());
        lib.addUser(user1); lib.addUser(user2);
        lib.addBook(book1); lib.addBook(book2); lib.addBook(book3);

        lib.issueBook(user1.name, book1.title); lib.issueBook(user1.name, book2.title);
        lib.issueBook(user2.name, book3.title);

        lib.issueBook(user2.name, book3.title);
        lib.issueBook("Michael", book3.title);

        lib.printLibraryState();
    }
}

interface IUserBookIssuanceStrategy
{
    public boolean checkBookIssuance(User user);
}

class DefaultBookIssuanceStrategy implements IUserBookIssuanceStrategy
{
    private final Map<Tier, Integer> issuanceLimits;

    public DefaultBookIssuanceStrategy() {
        issuanceLimits = new HashMap<>();
        // Default limits (could be loaded from a config file in production)
        issuanceLimits.put(Tier.BASIC, 1);
        issuanceLimits.put(Tier.PLUS, 5);
        issuanceLimits.put(Tier.GOLD, 10);
    }
    @Override
    public boolean checkBookIssuance(User user) {
        return user.getNumBooksIssued()<issuanceLimits.get(user.tier);  
    }

}

class Library
{
    List<User> users;
    List<Book> books;
    IUserBookIssuanceStrategy bookIssuanceStrategy;

    public Library(IUserBookIssuanceStrategy bookIssuanceStrategy)
    {
        users = new ArrayList<>();
        books = new ArrayList<>();
        this.bookIssuanceStrategy = bookIssuanceStrategy;
    }

    public void addUser(User user)
    {
        if(users.contains(user))
        {
            System.out.println("User already added to library");
        }
        else
        {
            users.add(user);
        }
    }

    public void addBook(Book book)
    {
        books.add(book);
    }

    public synchronized void issueBook(String name, String title)
    {
        //check user reg status
        User user = findUser(name);
        if(user==null) 
        {
            System.out.println("Sorry, the user is not registered!");
            return;
        }

        //check if book can be issued based on tier
        if(!bookIssuanceStrategy.checkBookIssuance(user))
        {
            System.out.println("Sorry, the user cannot subscribe to more books based on Tier!");
            return;
        }
        
        // check book stock
        Book book = findBook(title);
        if(book==null) 
        {
            System.out.println("Sorry, the book is out of Stock!");
            return;
        }
        user.issueBook(book);
        books.remove(book);
        System.out.println("Book succesfully issued!");
    }

    private User findUser(String name)
    {
        for(User user: users)
        {
            if(user.name.equals(name)) return user;
        }
        return null;
    }

    private Book findBook(String title)
    {
        for(Book book: books)
        {
            if(book.title.equals(title))return book;
        }
        return null;
    }

    public void printLibraryState()
    {
        for(User user:users)
        {
            System.out.print(user + " Has Books: ");
            for(Book book:user.issuedBooks)
            {
                System.out.print(book + " ");
            }
            System.out.println();
        }
    }

    public void addReview(User user, Book book, String reviewDesc)
    {
        Review review = new Review(user, reviewDesc);
        book.addReview(review);
    }
}

class Book
{
    String title;
    int releaseYear;
    List<Review> reviews;

    public Book(String title, int releaseYear)
    {
        this.title = title;
        this.releaseYear = releaseYear;
        this.reviews = new ArrayList<>();
    }

    public void addReview(Review review)
    {
        reviews.add(review);
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(getClass()!=obj.getClass()) return false;
        Book book = (Book)obj;
        return book.title.equals(this.title) && book.releaseYear==this.releaseYear;
    }

    @Override
    public String toString() {
        return title + " " + Integer.toString(releaseYear);
    }

}

enum Tier
{
    BASIC,
    PLUS,
    GOLD
}

class UserFactory
{
    public static User createUser(String name, Tier tier)
    {
        switch (tier) {
            case BASIC:
                return new BasicUser(name);
            case GOLD:
                return new GoldUser(name);
            default:
                return new BasicUser(name);
        }
    }
}

abstract class User
{
    String name;
    List<Book> issuedBooks;
    Tier tier;

    public User(String name, Tier tier)
    {
        this.name = name;
        this.tier = tier;
        this.issuedBooks = new ArrayList<>();
    }

    public void issueBook(Book book)
    {
        issuedBooks.add(book);
    }

    public void returnBook(Book book)
    {
        if(issuedBooks.contains(book)) issuedBooks.remove(book);
    }

    public int getNumBooksIssued()
    {
        return issuedBooks.size();
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(getClass()!=obj.getClass()) return false;
        User user = (User)obj;
        return user.name.equals(this.name);
    }

    @Override
    public String toString() {
        return name;
    }

}

class BasicUser extends User
{
    public BasicUser(String name)
    {
        super(name, Tier.BASIC);
    }
}

class GoldUser extends User
{
    public GoldUser(String name)
    {
        super(name, Tier.GOLD);
    }
}

class Review
{
    User user;
    String desc;

    public Review(User user, String desc)
    {
        this.user = user;
        this.desc = desc;
    }

}
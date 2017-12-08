package booksdbclient.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This interface declares methods for querying a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB
 * database.
 * 
 * @author anderslm@kth.se
 */
public interface BooksDbInterface {
    
    /**
     * Connect to the database.
     * @param database, userName, passWord
     * @return true on successful connection.
     */
    public boolean connect(String database, String userName, String passWord) throws IOException, SQLException;
    /**
     * Disconnects from the database.
     */
    public void disconnect() throws IOException, SQLException;
    
    /**
     * Search for all books with a string in its title.
     * @param title
     * @return List of books that matches query.
     */
    public List<Book> searchBooksByTitle(String title) throws IOException, SQLException;
    /**
     * Search for all books with a specific isnb.
     * @param searchISBN
     * @return List of books that matches query.
     */
    public List<Book> searchBooksByISBN(String searchISBN) throws IOException, SQLException;
    /**
     * Search for all books with a specific author.
     * @param searchAuthor
     * @return List of books that matches query.
     */
    public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, SQLException;
    /**
     * Search for all books with the rating specified.
     * @param searchFor
     * @return List of books that matches query.
     */
    public List<Book> searchBooksByRating(String searchFor) throws IOException, SQLException;
    /**
     * Search for all books with a specified genre.
     * @param searchGenre
     * @return List of books that matches query.
     */
    public List<Book> searchBooksByGenre(String searchGenre) throws IOException, SQLException;
    /**
     * Creates a new author and inserts it into the database
     * @param authorName
     */
    public void insertNewAuthor(String authorName)throws IOException, SQLException;
    /**
     * Creates a new book and inserts it into the database
     * @param isbn,genre,title authorName
     */
    public void insertNewBook(String isbn, String genre,String title, String authorName)throws IOException, SQLException;
    /**
     * Adds a review with a rating to a book
     * @param userID, isbn, rating, text
     */
    public void addReview(String userID, String isbn, String rating, String text) throws IOException, SQLException;
    
}

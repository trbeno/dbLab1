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
     * @param database
     * @return true on successful connection.
     */
    public boolean connect(String database, String userName, String passWord) throws IOException, SQLException;
    
    public void disconnect() throws IOException, SQLException;
    
    public List<Book> searchBooksByTitle(String title) throws IOException, SQLException;
    public List<Book> searchBooksByISBN(String searchISBN) throws IOException, SQLException;
    public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, SQLException;
    public List<Book> searchBooksByRating(String searchFor) throws IOException, SQLException;
    public List<Book> searchBooksByGenre(String searchGenre) throws IOException, SQLException;
    public void insertNewAuthor(String authorName)throws IOException, SQLException;
    public void insertNewBook(String isbn, String genre,String title, String authorName)throws IOException, SQLException;
    public void addRating(String userID, String isbn, String rating, String text) throws IOException, SQLException;
    
    
    // TODO: Add abstract methods for all inserts, deletes and queries 
    // mentioned in the instructions for the assignement.
}

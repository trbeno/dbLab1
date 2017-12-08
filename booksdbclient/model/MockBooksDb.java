/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package booksdbclient.model;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * A mock implementation of the BooksDBInterface interface to demonstrate how to
 * use it together with the user interface.
 *
 * Your implementation should access a real database.
 *
 * @author anderslm@kth.se
 */
public class MockBooksDb implements BooksDbInterface {
    private Connection con = null;
    
    @Override
    public boolean connect(String database, String userName, String passWord) throws IOException, SQLException {
        // mock implementation
    	 System.out.println(userName + ", *********"); 
         String server
                 = "jdbc:mysql://localhost:3306/" + database
                 + "?UseClientEnc=UTF8";
  
         try {
             Class.forName("com.mysql.jdbc.Driver");
             con = DriverManager.getConnection(server, userName, passWord);
             System.out.println("Connected!");
         } catch (Exception e) {
             System.out.println("Database error, " + e.toString());
         } 
        return true;
    }

    @Override
    public void disconnect() throws IOException, SQLException {
    	try {
            if (con != null) {
                con.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
        }
    }

    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws IOException, SQLException  {

        searchTitle = searchTitle.toLowerCase();
        List<Book> result = new ArrayList<>();
        try {
            String selectSQL ="SELECT * FROM T_Book WHERE title like ?";
            PreparedStatement preStmt = con.prepareStatement(selectSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchTitle+'%');
            ResultSet rs = preStmt.executeQuery();
            while(rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String genre = rs.getString("genre");

                String askForAvgRatingSQL = "SELECT AVG(rating) FROM t_review WHERE isbn = ?";
                PreparedStatement askForAvgRating = con.prepareStatement(askForAvgRatingSQL);
                askForAvgRating.clearParameters();
                askForAvgRating.setString(1, isbn);
                ResultSet askForAvgRatingRs = askForAvgRating.executeQuery();

                while (askForAvgRatingRs.next()) {

                    float avgRating = askForAvgRatingRs.getFloat(1);

                    ArrayList<Author> authors = getAuthors(isbn);
                    ArrayList<Review> reviews = getBookReviews(isbn);
                    Book book = new Book(isbn, title, genre,avgRating,reviews);
                    book.setAuthors(authors);
                    result.add(book);
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
    
    @Override
    public List<Book> searchBooksByISBN(String searchISBN) throws IOException, SQLException{
        searchISBN = searchISBN.toLowerCase();
        List<Book> result = new ArrayList<>();
        try {
            String selectSQL ="SELECT * FROM t_book WHERE isbn like ?";
            PreparedStatement preStmt = con.prepareStatement(selectSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchISBN+'%');
            ResultSet rs = preStmt.executeQuery();
            while(rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String genre = rs.getString("genre");

                float avgRating = getAvgRating(isbn);

                ArrayList<Author> authors = getAuthors(isbn);
                ArrayList<Review> reviews = getBookReviews(isbn);
                
                Book book = new Book(isbn, title, genre, avgRating,reviews);
                book.setAuthors(authors);
                result.add(book);
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
    
    @Override
    public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, SQLException{
        List<Book> result = new ArrayList<>();
        searchAuthor = searchAuthor.toLowerCase();

        try {
            String selectAuthorSQL ="SELECT * FROM t_author WHERE name like ?";
            PreparedStatement preStmt = con.prepareStatement(selectAuthorSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchAuthor+'%');
            ResultSet rs = preStmt.executeQuery();
            while(rs.next()) {
                String name = rs.getString("name");
                String authorID = rs.getString("authorID");
                ArrayList <Author> authors = new ArrayList <Author>();

                Author author = new Author(name);
                authors.add(author);

                String selectBooksSQL ="SELECT * FROM t_book INNER JOIN t_made ON t_book.isbn = t_made.isbn WHERE T_made.authorID = ?";
                PreparedStatement booksPreStmt = con.prepareStatement(selectBooksSQL);
                booksPreStmt.clearParameters();
                booksPreStmt.setString(1,authorID);
                ResultSet bookRs = booksPreStmt.executeQuery();

                while(bookRs.next()) {

                    String isbn = bookRs.getString("isbn");
                    String title = bookRs.getString("title");
                    String genre = bookRs.getString("genre");

                    float avgRating = getAvgRating(isbn);
                    ArrayList<Review> reviews = getBookReviews(isbn);
                    Book book = new Book(isbn, title, genre, avgRating,reviews);
                    book.setAuthors(authors);

                    System.out.println(book.getTitle());

                    result.add(book);
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
    
    public List<Book> searchBooksByRating (String rating)throws IOException, SQLException{

        rating = rating.toLowerCase();
        List<Book> result = new ArrayList<>();
        try {
            String selectBookByAVGRatingSQL ="SELECT isbn FROM t_review WHERE ? <= (SELECT AVG(rating) FROM t_review) GROUP BY isbn";
            PreparedStatement preStmt = con.prepareStatement(selectBookByAVGRatingSQL);
            preStmt.clearParameters();
            preStmt.setString(1,rating);
            ResultSet rs = preStmt.executeQuery();

            while(rs.next()) {

                String isbn = rs.getString("isbn");
                String avgRatingSQL = "SELECT AVG(rating) FROM t_review WHERE isbn = ?";
                PreparedStatement getAvgRatingStmt = con.prepareStatement(avgRatingSQL);
                getAvgRatingStmt.clearParameters();
                getAvgRatingStmt.setString(1, isbn);
                ResultSet avgRatingRs = getAvgRatingStmt.executeQuery();

                while (avgRatingRs.next()) {

                    float foundRating = avgRatingRs.getFloat(1);

                    String selectSQL = "SELECT * FROM t_book WHERE isbn like ?";
                    PreparedStatement getBooksStmt = con.prepareStatement(selectSQL);
                    getBooksStmt.clearParameters();
                    getBooksStmt.setString(1, isbn);
                    ResultSet bookRs = getBooksStmt.executeQuery();

                    while (bookRs.next()) {

                        String title = bookRs.getString("title");
                        String genre = bookRs.getString("genre");

                        ArrayList<Author> authors = getAuthors(isbn);
                        ArrayList<Review> reviews = getBookReviews(isbn);
                        Book book = new Book(isbn, title, genre, foundRating,reviews);
                        book.setAuthors(authors);
                        result.add(book);
                    }
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
    
    @Override
    public List<Book> searchBooksByGenre(String searchGenre) throws IOException, SQLException  {

        searchGenre = searchGenre.toLowerCase();
        List<Book> result = new ArrayList<>();
        try {
            String selectSQL ="SELECT * FROM T_Book WHERE genre like ?";
            PreparedStatement preStmt = con.prepareStatement(selectSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchGenre+'%');
            ResultSet rs = preStmt.executeQuery();
            while(rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String genre = rs.getString("genre");

                String askForAvgRatingSQL = "SELECT AVG(rating) FROM t_review WHERE isbn = ?";
                PreparedStatement askForAvgRating = con.prepareStatement(askForAvgRatingSQL);
                askForAvgRating.clearParameters();
                askForAvgRating.setString(1, isbn);
                ResultSet askForAvgRatingRs = askForAvgRating.executeQuery();

                while (askForAvgRatingRs.next()) {

                    float avgRating = askForAvgRatingRs.getFloat(1);

                    ArrayList<Author> authors = getAuthors(isbn);
                    ArrayList<Review> reviews = getBookReviews(isbn);
                    Book book = new Book(isbn, title, genre,avgRating,reviews);
                    book.setAuthors(authors);
                    result.add(book);
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
    
    private ArrayList<Author> getAuthors(String isbn)throws IOException, SQLException{
        ArrayList<Author> authors = new ArrayList<Author>();
        try {
            String selectAuthorSQL =
                    "SELECT name FROM t_author INNER JOIN t_made ON t_author.authorID = t_made.authorID WHERE isbn = ?";
            PreparedStatement authorPreStmt = con.prepareStatement(selectAuthorSQL);
            authorPreStmt.clearParameters();
            authorPreStmt.setString(1, isbn);
            ResultSet authorRs = authorPreStmt.executeQuery();

            while (authorRs.next()) {
                String name = authorRs.getString("name");
                Author author = new Author(name);
                authors.add(author);
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return authors;
    }
    
    private ArrayList<Review> getBookReviews(String isbn) throws IOException, SQLException{
    	ArrayList<Review> reviews = new ArrayList<Review>();
        String selectReviewSQL ="SELECT rating,review FROM t_review WHERE isbn = ?";
        PreparedStatement reviewPreStmt = con.prepareStatement(selectReviewSQL);
        reviewPreStmt.clearParameters();
        reviewPreStmt.setString(1, isbn);
        ResultSet reviewRs = reviewPreStmt.executeQuery();
        while (reviewRs.next()) {
            String text = reviewRs.getString("review");
            float rating = reviewRs.getFloat("rating");
            Review review = new Review(text,rating);
            reviews.add(review);
        }
        return reviews;
    }
    
    private float getAvgRating(String isbn)throws IOException, SQLException{
        float avgRating = 0;
        try {
            String askForAvgRatingSQL = "SELECT AVG(rating) FROM t_review WHERE isbn = ?";
            PreparedStatement askForAvgRating = con.prepareStatement(askForAvgRatingSQL);
            askForAvgRating.clearParameters();
            askForAvgRating.setString(1, isbn);
            ResultSet askForAvgRatingRs = askForAvgRating.executeQuery();
            while (askForAvgRatingRs.next()) {

                avgRating = askForAvgRatingRs.getFloat(1);
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return avgRating;
    }
    
    @Override 
    public void insertNewAuthor(String authorName) throws IOException, SQLException{
    	AuthorInserter authorInsert = new AuthorInserter(authorName,con);
    	authorInsert.run();
    }
    
    @Override
    public void insertNewBook(String isbn, String genre,String title, String authorName)throws IOException, SQLException{
    	BookInserter bookInsert = new BookInserter(isbn,genre,title,authorName,con);
    	bookInsert.run();
    }
    
    @Override
    public void addRating(String userID, String isbn, String rating, String text) throws IOException, SQLException {
     ReviewAdder ratingAdder = new ReviewAdder(userID, isbn, rating, text, con);
     ratingAdder.run();
    }
}

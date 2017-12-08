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
 * An implementation of the BooksDBInterface interface
 *
 */
public class MySQL implements BooksDbInterface {
    private Connection con = null;
    /**
     * Establishes a connection with the database
     * @param The name of the daabase, username and the passord: database (String), userName (String), passWord(String)
     */
    @Override
    public boolean connect(String database, String userName, String passWord) throws IOException, SQLException {
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
    /**
     * Disconnects the current connection
     */
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
    /**
     * gets all the books which title matches the param
     * @param The title of the book searchTitle (String)
     * @return returns a List of all the selected books
     */
    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws IOException, SQLException  {


        PreparedStatement preStmt = null;
        ResultSet rs = null;

        PreparedStatement askForAvgRating = null;
        ResultSet askForAvgRatingRs = null;


        searchTitle = searchTitle.toLowerCase();
        List<Book> result = new ArrayList<>();
        try {
            String selectSQL ="SELECT * FROM T_Book WHERE title like ?";
            preStmt = con.prepareStatement(selectSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchTitle+'%');
            rs = preStmt.executeQuery();
            while(rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String genre = rs.getString("genre");

                String askForAvgRatingSQL = "SELECT AVG(rating) FROM t_review WHERE isbn = ?";
                askForAvgRating = con.prepareStatement(askForAvgRatingSQL);
                askForAvgRating.clearParameters();
                askForAvgRating.setString(1, isbn);
                askForAvgRatingRs = askForAvgRating.executeQuery();

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
        finally {
            preStmt.close();
            rs.close();
            askForAvgRating.close();
            askForAvgRatingRs.close();
        }
        return result;
    }    
    /**
     * gets all the books that which isbn matches the param
     * @param The isbn number to search for searchISBN (String)
     * @return returns a List of all the selected books
     */
    @Override
    public List<Book> searchBooksByISBN(String searchISBN) throws IOException, SQLException{
        searchISBN = searchISBN.toLowerCase();
        List<Book> result = new ArrayList<>();

        PreparedStatement preStmt = null;
        ResultSet rs = null;

        try {
            String selectSQL ="SELECT * FROM t_book WHERE isbn like ?";
            preStmt = con.prepareStatement(selectSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchISBN+'%');
            rs = preStmt.executeQuery();
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
        finally {
            preStmt.close();
            rs.close();
        }
        return result;
    }
    /**
     * gets all the books that has an Author that matches the param
     * @param The name of the Author searchAuthor (String)
     * @return returns a List of all the selected books
     */
    @Override
    public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, SQLException{
        List<Book> result = new ArrayList<>();
        searchAuthor = searchAuthor.toLowerCase();

        PreparedStatement preStmt = null;
        ResultSet rs = null;
        PreparedStatement booksPreStmt = null;
        ResultSet bookRs = null;


        try {
            String selectAuthorSQL ="SELECT * FROM t_author WHERE name like ?";
            preStmt = con.prepareStatement(selectAuthorSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchAuthor+'%');
            rs = preStmt.executeQuery();
            while(rs.next()) {
                String name = rs.getString("name");
                String authorID = rs.getString("authorID");
                ArrayList <Author> authors = new ArrayList <Author>();

                Author author = new Author(name);
                authors.add(author);

                String selectBooksSQL ="SELECT * FROM t_book INNER JOIN t_made ON t_book.isbn = t_made.isbn WHERE T_made.authorID = ?";
                booksPreStmt = con.prepareStatement(selectBooksSQL);
                booksPreStmt.clearParameters();
                booksPreStmt.setString(1,authorID);
                bookRs = booksPreStmt.executeQuery();

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
        finally {
            preStmt.close();
            rs.close();
            booksPreStmt.close();
            bookRs.close();
        }
        return result;
    }    
    
    /**
     * gets all the books that has an average rating above or equal to the param
     * @param The desired rating rating (String)
     * @return returns a List of all the selected books
     */
    public List<Book> searchBooksByRating (String rating)throws IOException, SQLException{
        rating = rating.toLowerCase();
        List<Book> result = new ArrayList<>();

        PreparedStatement preStmt = null;
        ResultSet rs = null;
        PreparedStatement getAvgRatingStmt = null;
        ResultSet avgRatingRs = null;
        PreparedStatement getBooksStmt = null;
        ResultSet bookRs = null;



        try {
            String selectBookByAVGRatingSQL ="SELECT isbn FROM t_review WHERE ? <= (SELECT AVG(rating) FROM t_review) GROUP BY isbn";
            preStmt = con.prepareStatement(selectBookByAVGRatingSQL);
            preStmt.clearParameters();
            preStmt.setString(1,rating);
            rs = preStmt.executeQuery();

            while(rs.next()) {

                String isbn = rs.getString("isbn");
                String avgRatingSQL = "SELECT AVG(rating) FROM t_review WHERE isbn = ?";
                getAvgRatingStmt = con.prepareStatement(avgRatingSQL);
                getAvgRatingStmt.clearParameters();
                getAvgRatingStmt.setString(1, isbn);
                avgRatingRs = getAvgRatingStmt.executeQuery();

                while (avgRatingRs.next()) {

                    float foundRating = avgRatingRs.getFloat(1);

                    String selectSQL = "SELECT * FROM t_book WHERE isbn like ?";
                    getBooksStmt = con.prepareStatement(selectSQL);
                    getBooksStmt.clearParameters();
                    getBooksStmt.setString(1, isbn);
                    bookRs = getBooksStmt.executeQuery();

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
        finally {
            preStmt.close();
            rs.close();
            getAvgRatingStmt.close();
            avgRatingRs.close();
            getBooksStmt.close();
            bookRs.close();
        }
        return result;
    }
    
    /**
     * gets all the books that is of a specific genre
     * @param The desired genre genre (String)
     * @return returns a List of all the selected books
     */
    @Override
    public List<Book> searchBooksByGenre(String searchGenre) throws IOException, SQLException  {
        searchGenre = searchGenre.toLowerCase();
        List<Book> result = new ArrayList<>();

        PreparedStatement preStmt = null;
        ResultSet rs = null;
        PreparedStatement askForAvgRating = null;
        ResultSet askForAvgRatingRs = null;


        try {
            String selectSQL ="SELECT * FROM T_Book WHERE genre like ?";
            preStmt = con.prepareStatement(selectSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchGenre+'%');
            rs = preStmt.executeQuery();
            while(rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String genre = rs.getString("genre");

                String askForAvgRatingSQL = "SELECT AVG(rating) FROM t_review WHERE isbn = ?";
                askForAvgRating = con.prepareStatement(askForAvgRatingSQL);
                askForAvgRating.clearParameters();
                askForAvgRating.setString(1, isbn);
                askForAvgRatingRs = askForAvgRating.executeQuery();

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
        finally {
            preStmt.close();
            rs.close();
            askForAvgRating.close();
            askForAvgRatingRs.close();
        }
        return result;
    }
    
    /**
     * helpfunction to get all authors of a book
     * @param The isbn of the selected book isbn (String)
     * @return returns a List of all the authors
     */
    private ArrayList<Author> getAuthors(String isbn)throws IOException, SQLException{
        ArrayList<Author> authors = new ArrayList<Author>();


        PreparedStatement authorPreStmt = null;
        ResultSet authorRs = null;


        try {
            String selectAuthorSQL =
                    "SELECT name FROM t_author INNER JOIN t_made ON t_author.authorID = t_made.authorID WHERE isbn = ?";
            authorPreStmt = con.prepareStatement(selectAuthorSQL);
            authorPreStmt.clearParameters();
            authorPreStmt.setString(1, isbn);
            authorRs = authorPreStmt.executeQuery();

            while (authorRs.next()) {
                String name = authorRs.getString("name");
                Author author = new Author(name);
                authors.add(author);
            }
        }
        finally {
            authorPreStmt.close();
            authorRs.close();
        }
        return authors;
    }
    
    /**
     * helpfunction to get all the reviews for a book
     * @param The isbn of the selected book isbn (String)
     * @return returns a List of all the reviews
     */
    private ArrayList<Review> getBookReviews(String isbn) throws IOException, SQLException{
        ArrayList<Review> reviews = new ArrayList<Review>();

        PreparedStatement reviewPreStmt = null;
        ResultSet reviewRs = null;

        try{
        String selectReviewSQL ="SELECT rating,review FROM t_review WHERE isbn = ?";
        reviewPreStmt = con.prepareStatement(selectReviewSQL);
        reviewPreStmt.clearParameters();
        reviewPreStmt.setString(1, isbn);reviewRs = reviewPreStmt.executeQuery();
        while (reviewRs.next()) {
            String text = reviewRs.getString("review");
            float rating = reviewRs.getFloat("rating");
            Review review = new Review(text, rating);
            reviews.add(review);
            }
        }
        finally {
            reviewPreStmt.close();
            reviewRs.close();
        }
        return reviews;
    }

    /**
     * helpfunction to get the average for a book
     * @param The isbn of the selected book isbn (String)
     * @return returns the average rating
     */
    private float getAvgRating(String isbn)throws IOException, SQLException{
        float avgRating = 0;

        PreparedStatement askForAvgRating = null;
        ResultSet askForAvgRatingRs = null;

        try {
            String askForAvgRatingSQL = "SELECT AVG(rating) FROM t_review WHERE isbn = ?";
            askForAvgRating = con.prepareStatement(askForAvgRatingSQL);
            askForAvgRating.clearParameters();
            askForAvgRating.setString(1, isbn);
            askForAvgRatingRs = askForAvgRating.executeQuery();
            while (askForAvgRatingRs.next()) {

                avgRating = askForAvgRatingRs.getFloat(1);
            }
        }
        finally {
            askForAvgRating.close();
            askForAvgRatingRs.close();
        }
        return avgRating;
    }
    
    /**
     * creates a new author and inserts it into the database
     * @param The name of the author (String)
     * @return returns the average rating
     */
    @Override 
    public void insertNewAuthor(String authorName) throws IOException, SQLException{
    	String selectAuthorSQL ="INSERT INTO T_Author(name) values(?)";
    	PreparedStatement preStmt = con.prepareStatement(selectAuthorSQL);
    	try {
        preStmt.clearParameters();
        preStmt.setString(1,authorName);
        preStmt.executeUpdate();
        }
        finally {
        	preStmt.close();
        }
    }
    /**
     * creates a new book and inserts it into the database
     * @param The name of the author (String)
     * @return returns the average rating
     */
    @Override
    public void insertNewBook(String isbn, String genre,String title, String authorName)throws IOException, SQLException{
		genre = genre.toLowerCase();
    	title = title.toLowerCase();
    	PreparedStatement bookPreStmt=null;
    	PreparedStatement authorPreStmt =null;
    	PreparedStatement madePreStmt=null;
    	ResultSet authorRs =null;
    	try {
    		con.setAutoCommit(false);
	        String insertBook = "INSERT INTO T_Book VALUES(?,?,?)";	
	        bookPreStmt = con.prepareStatement(insertBook);
	        bookPreStmt.clearParameters();
	        bookPreStmt.setString(1, isbn);
	        bookPreStmt.setString(2, genre);
	        bookPreStmt.setString(3, title);
	        bookPreStmt.executeUpdate();
	        
	        String authors[] = authorName.split(",");
	        String selectAuthorSQL ="SELECT * FROM T_Author WHERE name = ?";
	        authorPreStmt = con.prepareStatement(selectAuthorSQL);
	        for(int i=0; i<authors.length;i++) {
	        	System.out.println(authors[i]);
	        	 authorPreStmt.clearParameters();
	             authorPreStmt.setString(1, authors[i]);
	             authorRs = authorPreStmt.executeQuery();
	             if(!authorRs.next()) {
	            	System.out.println("adding new author");
	             	
	            	AuthorInserter authorInsert = new AuthorInserter(authors[i],con);
	            	authorInsert.run();
	             	authorPreStmt.clearParameters();
	                authorPreStmt.setString(1, authors[i]);
	                authorRs = authorPreStmt.executeQuery(); //getting authorId for next insert since we made a new one
	                authorRs.next();
	             }
	             
	             System.out.println("authorID: "+authorRs.getInt("authorId"));
	             String madeString = "INSERT INTO T_Made VALUES(?,?)";
	             madePreStmt = con.prepareStatement(madeString);
	             madePreStmt.clearParameters();
	             madePreStmt.setInt(1, authorRs.getInt("authorId"));
	             madePreStmt.setString(2, isbn);
	             madePreStmt.executeUpdate();
	             con.commit();
	        }
    	}
    	 finally {
    		 con.setAutoCommit(true);
    		 bookPreStmt.close();
    		 authorPreStmt.close();
    		 madePreStmt.close();
    	 }
    }
    
    @Override
    public void addReview(String userID, String isbn, String rating, String text) throws IOException, SQLException {
    	String inputReviewSQL ="INSERT INTO t_review VALUES (?, ? ,?,?) ";
        PreparedStatement pre2Stmt = con.prepareStatement(inputReviewSQL);
    	try {
	            pre2Stmt.clearParameters();
	            pre2Stmt.setString(1,userID);
	            pre2Stmt.setString(2,isbn);
	            pre2Stmt.setString(3, rating);
	            pre2Stmt.setString(4,text);
	            pre2Stmt.executeUpdate();
	        }
    	 catch (SQLException e) {
	         con.rollback();
	         throw e;
    	 }
    	 finally{
    		 pre2Stmt.close();
    	 }
    
    }
}

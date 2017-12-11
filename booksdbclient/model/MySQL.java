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
    private Customer customer;

    /**
     * Establishes a connection with the database
     * @param database name of the daabase, userName and the passWord: database (String), userName (String), passWord(String)
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
     * @param searchTitle title of the book searchTitle (String)
     * @return returns a List of all the selected books
     */
    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws IOException, SQLException  {

        PreparedStatement preStmt = null;
        ResultSet rs ;

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
                int customerID = rs.getInt("customerID");

                float avgRating = getAvgRating(isbn);

                ArrayList<Author> authors = getAuthors(isbn);
                ArrayList<Review> reviews = getBookReviews(isbn);
                Book book = new Book(isbn, title, genre,avgRating,reviews,customerID);
                book.setAuthors(authors);
                result.add(book);
            }
        }
        finally {
            preStmt.close();

        }
        return result;
    }    
    /**
     * gets all the books that which isbn matches the param
     * @param searchISBN isbn number to search for searchISBN (String)
     * @return returns a List of all the selected books
     */
    @Override
    public List<Book> searchBooksByISBN(String searchISBN) throws IOException, SQLException{
        searchISBN = searchISBN.toLowerCase();
        List<Book> result = new ArrayList<>();

        PreparedStatement preStmt = null;
        ResultSet rs;

        try {
            String selectSQL ="SELECT * FROM T_Book WHERE isbn like ?";
            preStmt = con.prepareStatement(selectSQL);
            preStmt.clearParameters();
            preStmt.setString(1,'%'+searchISBN+'%');
            rs = preStmt.executeQuery();
            while(rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String genre = rs.getString("genre");
                int customerID = rs.getInt("customerID");

                float avgRating = getAvgRating(isbn);

                ArrayList<Author> authors = getAuthors(isbn);
                ArrayList<Review> reviews = getBookReviews(isbn);
                Book book = new Book(isbn, title, genre,avgRating,reviews,customerID);
                book.setAuthors(authors);
                result.add(book);
            }
        }
        finally {
            preStmt.close();

        }
        return result;
    }
    /**
     * gets all the books that has an Author that matches the param
     * @param searchAuthor name of the Author searchAuthor (String)
     * @return returns a List of all the selected books
     */
    @Override
    public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, SQLException{
        List<Book> result = new ArrayList<>();
        searchAuthor = searchAuthor.toLowerCase();

        PreparedStatement preStmt = null;
        ResultSet rs ;
        PreparedStatement booksPreStmt = null;
        ResultSet bookRs;


        try {
            String selectAuthorSQL ="SELECT * FROM T_Author WHERE name like ?";
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

                String selectBooksSQL ="SELECT * FROM T_Book INNER JOIN T_Made ON T_Book.isbn = T_Made.isbn WHERE T_Made.authorID = ?";
                booksPreStmt = con.prepareStatement(selectBooksSQL);
                booksPreStmt.clearParameters();
                booksPreStmt.setString(1,authorID);
                bookRs = booksPreStmt.executeQuery();

                while(bookRs.next()) {

                    String isbn = bookRs.getString("isbn");
                    String title = bookRs.getString("title");
                    String genre = bookRs.getString("genre");
                    int customerID = rs.getInt("customerID");

                    float avgRating = getAvgRating(isbn);

                    ArrayList<Review> reviews = getBookReviews(isbn);
                    Book book = new Book(isbn, title, genre,avgRating,reviews,customerID);
                    book.setAuthors(authors);

                    System.out.println(book.getTitle());

                    result.add(book);
                }
            }
        }
        finally {
            preStmt.close();
            if(booksPreStmt!=null)
            	booksPreStmt.close();
        }
        return result;
    }    
    
    /**
     * gets all the books that has an average rating above or equal to the param
     * @param rating desired rating rating (String)
     * @return returns a List of all the selected books
     */
    public List<Book> searchBooksByRating (String rating)throws IOException, SQLException{
        rating = rating.toLowerCase();
        List<Book> result = new ArrayList<>();

        PreparedStatement preStmt = null;
        ResultSet rs;

        PreparedStatement getBooksStmt = null;
        ResultSet bookRs;

        try {
            String selectBookByAVGRatingSQL ="SELECT isbn FROM t_review WHERE ? <= (SELECT AVG(rating) FROM t_review) GROUP BY isbn";
            preStmt = con.prepareStatement(selectBookByAVGRatingSQL);
            preStmt.clearParameters();
            preStmt.setString(1,rating);
            rs = preStmt.executeQuery();

            while(rs.next()) {

                String isbn = rs.getString("isbn");
                float foundRating = getAvgRating(isbn);

                String selectSQL = "SELECT * FROM T_Book WHERE isbn like ?";
                getBooksStmt = con.prepareStatement(selectSQL);
                getBooksStmt.clearParameters();
                getBooksStmt.setString(1, isbn);
                bookRs = getBooksStmt.executeQuery();

                while (bookRs.next()) {

                    String title = bookRs.getString("title");
                    String genre = bookRs.getString("genre");
                    int customerID = rs.getInt("customerID");

                    ArrayList<Author> authors = getAuthors(isbn);
                    ArrayList<Review> reviews = getBookReviews(isbn);
                    Book book = new Book(isbn, title, genre, foundRating,reviews,customerID);
                    book.setAuthors(authors);
                    result.add(book);
                }

            }
        }
        finally {
            preStmt.close();
            if(getBooksStmt!=null)
            	getBooksStmt.close();
        }
        return result;
    }
    
    /**
     * gets all the books that is of a specific genre
     * @param searchGenre desired genre genre (String)
     * @return returns a List of all the selected books
     */
    @Override
    public List<Book> searchBooksByGenre(String searchGenre) throws IOException, SQLException  {
        searchGenre = searchGenre.toLowerCase();
        List<Book> result = new ArrayList<>();

        PreparedStatement preStmt = null;
        ResultSet rs;

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
                int customerID = rs.getInt("customerID");

                float avgRating = getAvgRating(isbn);

                ArrayList<Author> authors = getAuthors(isbn);
                ArrayList<Review> reviews = getBookReviews(isbn);
                Book book = new Book(isbn, title, genre,avgRating,reviews,customerID);
                book.setAuthors(authors);
                result.add(book);
            }
        }
        finally {
            preStmt.close();

        }
        return result;
    }
    
    /**
     * helpfunction to get all authors of a book
     * @param isbn of the selected book isbn (String)
     * @return returns a List of all the authors
     */
    private ArrayList<Author> getAuthors(String isbn)throws IOException, SQLException{
        ArrayList<Author> authors = new ArrayList<Author>();


        PreparedStatement authorPreStmt = null;
        ResultSet authorRs;


        try {
            String selectAuthorSQL =
                    "SELECT name FROM T_Author INNER JOIN T_Made ON T_Author.authorID = T_Made.authorID WHERE isbn = ?";
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
        }
        return authors;
    }
    
    /**
     * helpfunction to get all the reviews for a book
     * @param isbn of the selected book isbn (String)
     * @return returns a List of all the reviews
     */
    private ArrayList<Review> getBookReviews(String isbn) throws IOException, SQLException{
        ArrayList<Review> reviews = new ArrayList<Review>();

        PreparedStatement reviewPreStmt = null;
        ResultSet reviewRs;

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
        }
        return reviews;
    }

    /**
     * helpfunction to get the average for a book
     * @param isbn of the selected book isbn (String)
     * @return returns the average rating
     */
    private float getAvgRating(String isbn)throws IOException, SQLException{
        float avgRating = 0;

        PreparedStatement askForAvgRating = null;
        ResultSet askForAvgRatingRs;

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
        }
        return avgRating;
    }
    
    /**
     * creates a new author and inserts it into the database
     * @param authorName of the author (String)
     * @return returns the average rating
     */
    @Override 
    public void insertNewAuthor(String authorName) throws IOException, SQLException{
    	String selectAuthorSQL ="INSERT INTO T_Author(name,customerID) values(?,?)";
    	PreparedStatement preStmt = con.prepareStatement(selectAuthorSQL);
    	try {
        preStmt.clearParameters();
        preStmt.setString(1,authorName);
        preStmt.setString(2,authorName);
        preStmt.executeUpdate();
        }
        finally {
        	preStmt.close();
        }
    }
    /**
     * creates a new book and inserts it into the database
     * @param isbn ,genre, title, authorName strings with information about the new book
     * @return returns the average rating
     */
    @Override
    public void insertNewBook(String isbn, String genre,String title, String authorName)throws IOException, SQLException{
		genre = genre.toLowerCase();
    	title = title.toLowerCase();
    	PreparedStatement bookPreStmt=null;
    	PreparedStatement authorPreStmt =null;
    	PreparedStatement madePreStmt=null;
    	ResultSet authorRs;
    	try {
    		con.setAutoCommit(false);
	        String insertBook = "INSERT INTO T_Book VALUES(?,?,?,?)";	
	        bookPreStmt = con.prepareStatement(insertBook);
	        bookPreStmt.clearParameters();
	        bookPreStmt.setString(1, isbn);
	        bookPreStmt.setString(2, genre);
	        bookPreStmt.setString(3, title);
	        bookPreStmt.setInt(4, customer.getCustomerId());
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
	             	
	            	insertNewAuthor(authors[i]);
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
    		 if(authorPreStmt!=null)
    			 authorPreStmt.close();
    		 if(madePreStmt!=null)
    			 madePreStmt.close();
    	 }
    }
    
    @Override
    public void addReview(String isbn, String rating, String text) throws IOException, SQLException {
    	String inputReviewSQL ="INSERT INTO t_review VALUES (?, ? ,?,?) ";
        PreparedStatement pre2Stmt = con.prepareStatement(inputReviewSQL);
    	try {
	            pre2Stmt.clearParameters();
	            pre2Stmt.setString(1,Integer.toString(customer.getCustomerId()));
	            pre2Stmt.setString(2,isbn);
	            pre2Stmt.setString(3, rating);
	            pre2Stmt.setString(4,text);
	            pre2Stmt.executeUpdate();
	        }
    	 finally{
    		 pre2Stmt.close();
    	 }
    }

    @Override
    public void addCustomer(String name, String address, String userName, String password) throws IOException, SQLException {
        String inputReviewSQL ="INSERT INTO T_Customer(name,address, userName, password) VALUES (?, ? ,?,?) ";
        PreparedStatement preStmt = con.prepareStatement(inputReviewSQL);
        try {
            preStmt.clearParameters();
            preStmt.setString(1,name);
            preStmt.setString(2,address);
            preStmt.setString(3, userName);
            preStmt.setString(4,password);
            preStmt.executeUpdate();
        }
        finally{
            preStmt.close();
        }
    }

    @Override
    public void removeBookByIsbn(String isbn) throws IOException, SQLException {

        String removeRequest = "DELETE FROM T_Book WHERE isbn = ?";
        PreparedStatement removeStmt = con.prepareStatement(removeRequest);
        try {
            removeStmt.clearParameters();
            removeStmt.setString(1,isbn);
            removeStmt.executeUpdate();
            String removeFromMadeRequest = "DELETE FROM T_Made WHERE isbn = ?";
            removeStmt = con.prepareStatement(removeFromMadeRequest);
            removeStmt.clearParameters();
            removeStmt.setString(1,isbn);
            removeStmt.executeUpdate();

        }
        finally{
            removeStmt.close();
        }
    }

    @Override
    public boolean loginAttempt(String username, String password) throws IOException, SQLException {

        boolean failOrAccept = false;
        String loginQuestion =
                "SELECT customerId, name, address, userName, password FROM T_Customer WHERE username = ? AND password = ?";
        PreparedStatement preStmt = con.prepareStatement(loginQuestion);
        try{
            preStmt.clearParameters();
            preStmt.setString(1,username);
            preStmt.setString(2,password);
           ResultSet rs =  preStmt.executeQuery();
           while (rs.next()){
               int customerId = rs.getInt("customerID");
               String name = rs.getString("name");
               String address = rs.getString("address");
               String userName = rs.getString("userName");
               String passWord = rs.getString("password");

               customer = new Customer(customerId,name,address,userName,passWord);
               failOrAccept = true;
           }
        }
        finally{
            preStmt.close();
        }
        if(customer != null) System.out.println(customer.toString());
        return failOrAccept;
    }
}

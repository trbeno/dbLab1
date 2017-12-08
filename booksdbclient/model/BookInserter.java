package booksdbclient.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

public class BookInserter implements Runnable {
	private String isbn, genre,title,authorName;
	private Connection con;
	public BookInserter(String isbn, String genre,String title, String authorName, Connection con) {
		this.isbn = isbn;
		this.genre=genre;
		this.title=title;
		this.authorName=authorName;
		this.con=con;
	}
	@Override
	public void run (){
		genre = genre.toLowerCase();
    	title = title.toLowerCase();
    	try {
    		con.setAutoCommit(false);
	        String insertBook = "INSERT INTO T_Book VALUES(?,?,?)";	
	        PreparedStatement bookPreStmt = con.prepareStatement(insertBook);
	        bookPreStmt.clearParameters();
	        bookPreStmt.setString(1, isbn);
	        bookPreStmt.setString(2, genre);
	        bookPreStmt.setString(3, title);
	        bookPreStmt.executeUpdate();
	        
	        String authors[] = authorName.split(",");
	        String selectAuthorSQL ="SELECT * FROM T_Author WHERE name = ?";
	        ResultSet authorRs =null;
	        PreparedStatement authorPreStmt = con.prepareStatement(selectAuthorSQL);
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
	             PreparedStatement madePreStmt = con.prepareStatement(madeString);
	             madePreStmt.clearParameters();
	             madePreStmt.setInt(1, authorRs.getInt("authorId"));
	             madePreStmt.setString(2, isbn);
	             madePreStmt.executeUpdate();
	             con.commit();
	        }
    	}
    	 catch (SQLException e) {
             System.out.println(e.getMessage());
             try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
         }
    	try {
			con.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

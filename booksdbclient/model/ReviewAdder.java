package booksdbclient.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReviewAdder implements Runnable{

	private String userID,isbn,rating,text;
	private Connection con;
	public ReviewAdder (String userID, String isbn, String rating, String text,Connection con) {
		this.userID = userID;
		this.isbn = isbn;
		this.rating = rating;
		this.text=text;
		this.con = con;
	}
	
	@Override
	public void run() {
		 try {
			 	con.setAutoCommit(false);
	       
	            String inputReviewSQL ="INSERT INTO T_Review VALUES (?, ? ,?,?) ";
	            PreparedStatement pre2Stmt = con.prepareStatement(inputReviewSQL);
	            pre2Stmt.clearParameters();
	            pre2Stmt.setString(1,userID);
	            pre2Stmt.setString(2,isbn);
	            pre2Stmt.setString(3, rating);
	            pre2Stmt.setString(4,text);
	            pre2Stmt.executeUpdate();
	
	
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

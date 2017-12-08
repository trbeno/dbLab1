package booksdbclient.model;
import java.sql.*;
public class AuthorInserter implements Runnable {
	private String authorName;
	private Connection con;
	
	public AuthorInserter(String authorName, Connection con) {
		this.authorName = authorName;
		this.con = con;
	}
	
	@Override
	public void run() {
		String selectAuthorSQL ="INSERT INTO T_Author(name) values(?)";
        try {
		PreparedStatement preStmt = con.prepareStatement(selectAuthorSQL);
        preStmt.clearParameters();
        preStmt.setString(1,authorName);
        int success = preStmt.executeUpdate();
        if(success == 1)
        	System.out.println("insert Succeded");
        }
        catch(Exception e) {
        	System.out.println(e.getMessage());
        }
	}

}

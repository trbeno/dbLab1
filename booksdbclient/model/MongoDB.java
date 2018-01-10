package booksdbclient.model;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.print.Doc;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import java.util.Arrays;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
public class MongoDB implements BooksDbInterface {
	private MongoClient mongo = null;
	private MongoDatabase db = null;
	private Customer customer = null;
	@Override
	public boolean connect(String database, String userName, String passWord) throws IOException, SQLException, MongoException{
		 // Creating a Mongo client 
		mongo = new MongoClient("127.0.0.1",27017);
		db = mongo.getDatabase("library");  
		System.out.println("Connected to the database successfully");  
		return true;
	}
	private Block<Document> printBlock = new Block<Document>() {
        @Override
        public void apply(final Document document) {
            System.out.println(document.toJson());
        }
    };
	@Override
	public void disconnect() throws IOException, SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Book> searchBooksByTitle(String title) throws IOException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Book> searchBooksByISBN(String searchISBN) throws IOException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Book> searchBooksByRating(String searchFor) throws IOException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Book> searchBooksByGenre(String searchGenre) throws IOException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertNewAuthor(String authorName) throws IOException, SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertNewBook(String isbn, String genre, String title, String authorName)
			throws IOException, SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addReview(String isbn, String rating, String text) throws IOException, SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBookByIsbn(String isbn) throws IOException, SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addCustomer(String name, String address, String userName, String password)
			throws IOException, SQLException, MongoException {

		MongoCollection<Document> collection = db.getCollection("customer");
		Document doc = new Document("name",name).append("password", password).append("userName", userName).append("address", address);
		collection.insertOne(doc);
		
	}

	@Override
	public boolean loginAttempt(String userName, String password) throws IOException, SQLException {
		boolean failOrAccept = false;
         try{
        	 MongoCollection<Document> collection = db.getCollection("customer");
        	 Document doc = collection.find(and(eq("userName",userName),eq("password",password))).first();
             String customerOID = doc.get("_id").toString();
             System.out.println(customerOID);
             String name = doc.getString("name");
             String adress = doc.getString("adress");
             customer = new Customer(customerOID,name,adress,userName,password);
        	 failOrAccept = true;
           }finally {
        }
         System.out.println(customer);
         return failOrAccept;
        
	}

}

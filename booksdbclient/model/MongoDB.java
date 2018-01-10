package booksdbclient.model;
import java.io.IOException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



import com.mongodb.MongoClient;

import com.mongodb.MongoException;


import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;

import com.mongodb.client.model.Filters;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;


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
        public void apply(final Document document) {
			List<Book> result = new ArrayList<>();


        }
    };
	@Override
	public void disconnect() throws IOException, SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Book> searchBooksByTitle(String searchTitle) throws IOException, SQLException {
		List<Book> result = new ArrayList<>();
		MongoCollection<Document> coll = db.getCollection("book");

		BasicDBObject query = new BasicDBObject();
		query.put("title", java.util.regex.Pattern.compile(searchTitle));
        FindIterable<Document> docs = coll.find(query);

        for(Document doc : docs) {
            Book book = makeBook(doc);
            result.add(book);
        }
        return result;
	}

	@Override
	public List<Book> searchBooksByISBN(String searchISBN) throws IOException, SQLException {
        List<Book> result = new ArrayList<>();
        MongoCollection<Document> coll = db.getCollection("book");

        BasicDBObject query = new BasicDBObject();
        query.put("isbn", searchISBN);
        FindIterable<Document> docs = coll.find(query);

        for(Document doc : docs) {
            Book book = makeBook(doc);
            result.add(book);
        }
        return result;
    }

	@Override
	public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, SQLException {
        List<Book> result = new ArrayList<>();
        MongoCollection<Document> coll = db.getCollection("book");

        BasicDBObject query = new BasicDBObject();
        query.put("authorName", java.util.regex.Pattern.compile(searchAuthor));
        FindIterable<Document> docs = coll.find(query);

        for(Document doc : docs) {
            Book book = makeBook(doc);
            result.add(book);
        }
        return result;
    }

	@Override
	public List<Book> searchBooksByRating(String searchFor) throws IOException, SQLException {
		List<Book> result = new ArrayList<>();
		MongoCollection<Document> coll = db.getCollection("review");
        AggregateIterable<Document> docs = coll.aggregate(
        	      Arrays.asList(
        	              Aggregates.group("$isbn", Accumulators.avg("rating", "$rating"))
        	      )
        	);
        for(Document doc: docs) {
        	result.addAll(searchBooksByISBN(doc.getString("_id"))) ;
        }
		return result;
	}

	@Override
	public List<Book> searchBooksByGenre(String searchGenre) throws IOException, SQLException {
        List<Book> result = new ArrayList<>();
        MongoCollection<Document> coll = db.getCollection("book");

        BasicDBObject query = new BasicDBObject();
        query.put("genre", java.util.regex.Pattern.compile(searchGenre));
        FindIterable<Document> docs = coll.find(query);

        for(Document doc : docs) {
            Book book = makeBook(doc);
            result.add(book);
        }
        return result;
    }

	private Book makeBook(Document doc) throws  IOException, SQLException{
        String title = doc.getString("title");
        String authorList = doc.getString("authorName");
        String isbn = doc.getString("isbn");
        String genre = doc.getString("genre");

        String [] authorListSplit = authorList.split(",");
        ArrayList<Author> authors = new ArrayList<>();
        for(int i = 0; i < authorListSplit.length ; i++){
            Author author = new Author(authorListSplit[i]);
            authors.add(author);
        }

        Book book = new Book(isbn, title, genre,getAvgRating(isbn),getBookReviews(isbn),1);
        book.setAuthors(authors);
        return book;
    }

	@Override
	public void insertNewAuthor(String authorName, String isbn) throws IOException, SQLException {
	    String updateAuthorName = null;

	    MongoCollection<Document> coll = db.getCollection("book");
	    BasicDBObject query =  new BasicDBObject();
	    query.put("isbn",isbn);
        FindIterable<Document> docs = coll.find(query);
        for(Document doc : docs) {
            updateAuthorName = doc.getString("authorName");
        }
        updateAuthorName = updateAuthorName +", " +authorName;

	    coll.updateOne(eq("isbn",isbn),set("authorName",updateAuthorName));

	}

	@Override
	public void insertNewBook(String isbn, String genre, String title, String authorName) throws IOException, SQLException {
	    if(customer == null) throw new NullPointerException() ;
            MongoCollection<Document> coll = db.getCollection("book");
            coll.insertOne(new Document("isbn", isbn).append("genre", genre)
                    .append("title", title).append("authorName", authorName).append("customerId",customer.getCustomerId()));
	}

	@Override
	public void addReview(String isbn, String rating, String text) throws IOException, SQLException, MongoException{
		MongoCollection<Document> collection = db.getCollection("review");
		java.util.Date utilDate = new Date();
        java.sql.Date date = new java.sql.Date(utilDate.getTime());
		Document doc = new Document("isbn",isbn).append("customerOID", customer.getCustomerOId()).append("rating", Integer.parseInt(rating)).append("date", date).append("text", text);
		collection.insertOne(doc);
	}
	private ArrayList<Review> getBookReviews(String isbn) throws IOException, SQLException{
        ArrayList<Review> reviews = new ArrayList<Review>();
        
        try{
    		MongoCollection<Document> coll = db.getCollection("review");
    		FindIterable<Document> docs = coll.find(eq("isbn",isbn));
    		for(Document doc: docs) {
    			String reviewerId = doc.getString("customerOID");
    			int rating = doc.getInteger("rating");
    			Date date = doc.getDate("date");
    			String review = doc.getString("text");
    			reviews.add(new Review(review,rating,date,reviewerId));
    		}
        }finally {
        }
        return reviews;
    }
	private float getAvgRating(String isbn)throws IOException, SQLException{
        float avgRating = 0;

        try {
            MongoCollection<Document> coll = db.getCollection("review");
            AggregateIterable<Document> docs = coll.aggregate(
            	      Arrays.asList(
            	              Aggregates.match(Filters.eq("isbn", isbn)),
            	              Aggregates.group(null, Accumulators.avg("rating", "$rating"))
            	      )
            	);
            avgRating = docs.first().getDouble("rating").floatValue();
        }finally {
        }
        return avgRating;
    }

	@Override
	public void removeBookByIsbn(String isbn) throws IOException, SQLException {
        MongoCollection<Document> coll = db.getCollection("book");
        BasicDBObject query =  new BasicDBObject();
        query.put("isbn",isbn);
        coll.findOneAndDelete(query);

	}

	@Override
	public void addCustomer(String name, String address, String userName, String password)
			throws IOException, SQLException, MongoException {

		MongoCollection<Document> coll = db.getCollection("customer");
		Document doc = new Document("name",name).append("password", password).append("userName", userName).append("adress", address);
		coll.insertOne(doc);
		
	}

	@Override
	public boolean loginAttempt(String userName, String password) throws IOException, SQLException {
		boolean failOrAccept = false;
         try{
        	 MongoCollection<Document> coll = db.getCollection("customer");
        	 Document doc = coll.find(and(eq("userName",userName),eq("password",password))).first();
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

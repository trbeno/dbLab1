package booksdbclient.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.Arrays;
import java.util.Date;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;



public class MongoDB implements BooksDbInterface {
	private MongoClient mongo = null;
	private MongoDatabase db = null;
	private Customer customer = null;

    /**
     * Connection to the DB via a libraryClient user
     * @param database, userName, passWord
     * @throws IOException
     * @throws SQLException
     * @throws MongoException
     */
	@Override
	public boolean connect(String database) throws IOException, MongoException{
		 // Creating a Mongo client
        MongoClientURI uri = new MongoClientURI("mongodb://libraryClient:pot@localhost:27017/?authSource=library");
        mongo= new MongoClient(uri);
		db = mongo.getDatabase("library");

		System.out.println("Connected to the database successfully");  
		return true;
	}

    /**
     * Disconnects the current connection to a DB
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public void disconnect() throws IOException, MongoException {
		if(mongo!= null && db!= null) {
			mongo.close();

		}
	}

    /**
     * search in the DB after books by the given title
     * @param searchTitle
     * @return
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public List<Book> searchBooksByTitle(String searchTitle) throws IOException, MongoException {
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

    /**
     * search in the DB after books by the given ISBN
     * @param searchISBN
     * @return
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public List<Book> searchBooksByISBN(String searchISBN) throws IOException, MongoException {
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

    /**
     * search in the DB after books by the given Author
     * @param searchAuthor
     * @return
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, MongoException {
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

    /**
     * search in the DB after books by the given rating (averaged)
     * @param searchFor
     * @return
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public List<Book> searchBooksByRating(String searchFor) throws IOException, MongoException {
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

    /**
     * search in the DB after books by the given genre
     * @param searchGenre
     * @return
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public List<Book> searchBooksByGenre(String searchGenre) throws IOException, MongoException {
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

    /**
     * A help method the create books for the search methods
     * @param doc a document whit the information
     * @return  returns a book object
     * @throws IOException
     * @throws SQLException
     */
	private Book makeBook(Document doc) throws  IOException, MongoException{
        String title = doc.getString("title");
        String authorList = doc.getString("authorName");
        String isbn = doc.getString("isbn");
        String genre = doc.getString("genre");
        String OID = doc.getString("customerId");

        String [] authorListSplit = authorList.split(",");
        ArrayList<Author> authors = new ArrayList<>();
        for(int i = 0; i < authorListSplit.length ; i++){
            Author author = new Author(authorListSplit[i]);
            authors.add(author);
        }

        Book book = new Book(isbn, title, genre,getAvgRating(isbn),getBookReviews(isbn),OID);
        book.setAuthors(authors);
        return book;
    }

    /**
     * Inserts a new Author to a existing book
     * @param authorName
     * @param isbn
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public void insertNewAuthor(String authorName, String isbn) throws IOException, MongoException {
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

    /**
     * Inserts a hole new book to the DB
     * @param isbn,genre,title authorName the book inforamtion
     * @param genre
     * @param title
     * @param authorName
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public void insertNewBook(String isbn, String genre, String title, String authorName) throws IOException, MongoException {
	    if(customer == null) throw new NullPointerException() ;
            MongoCollection<Document> coll = db.getCollection("book");
            coll.insertOne(new Document("isbn", isbn).append("genre", genre)
                    .append("title", title).append("authorName", authorName).append("customerId",customer.getCustomerOId()));
	}

    /**
     * Add a review to a book by its ISBN
     * @param isbn
     * @param rating
     * @param text
     * @throws IOException
     * @throws SQLException
     * @throws MongoException
     */
	@Override
	public void addReview(String isbn, String rating, String text) throws IOException, MongoException{
		MongoCollection<Document> collection = db.getCollection("review");
		java.util.Date utilDate = new Date();
        java.sql.Date date = new java.sql.Date(utilDate.getTime());
		Document doc = new Document("isbn",isbn).append("customerOID", customer.getCustomerOId()).append("rating", Integer.parseInt(rating)).append("date", date).append("text", text);
		collection.insertOne(doc);
	}
    /**
     * A help method to get the review for the existing books
     * @param isbn
     * @return
     * @throws IOException
     * @throws SQLException
     */
	private ArrayList<Review> getBookReviews(String isbn) throws IOException, MongoException{
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

    /**
     * a help method to get the averaged rating for books
     * @param isbn
     * @return
     * @throws IOException
     * @throws SQLException
     */
	private float getAvgRating(String isbn)throws IOException, MongoException{
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
        	return avgRating;
        }
    }

    /**
     * Removes a book from the DB by its ISBN
     * @param  isbn: the isbn number to be used
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public void removeBookByIsbn(String isbn) throws IOException, MongoException {
        MongoCollection<Document> coll = db.getCollection("book");
        BasicDBObject query =  new BasicDBObject();
        query.put("isbn",isbn);
        coll.findOneAndDelete(query);

	}

    /**
     * Adds a new customer (who can rate add and delete books)
     * @param name
     * @param address
     * @param userName
     * @param password
     * @throws IOException
     * @throws SQLException
     * @throws MongoException
     */
	@Override
	public void addCustomer(String name, String address, String userName, String password) throws IOException, MongoException {

		MongoCollection<Document> coll = db.getCollection("customer");
		Document doc = new Document("name",name).append("password", password).append("userName", userName).append("adress", address);
		coll.insertOne(doc);

	}

    /**
     * The log in method to handel the Customer log ins
     * @param userName
     * @param password
     * @return
     * @throws IOException
     * @throws SQLException
     */
	@Override
	public boolean loginAttempt(String userName, String password) throws IOException, MongoException {
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

    /**
     * Disconects the current log in customer
     */
	@Override
	public void logOut(){
	    customer = null;
    }

}

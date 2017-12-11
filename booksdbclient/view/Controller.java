package booksdbclient.view;

import booksdbclient.model.SearchMode;
import booksdbclient.model.Book;
import booksdbclient.model.BooksDbInterface;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static javafx.scene.control.Alert.AlertType.*;

/**
 * The controller is responsible for handling user requests and update the view
 * (and in some cases the model).
 *
 * @author anderslm@kth.se
 */
public class Controller {

    private final BooksPane booksView; // view
    private final BooksDbInterface booksDb; // model

    public Controller(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }
    protected void onSearchSelected(String searchFor, SearchMode mode) {
       Searcher searchJob = new Searcher(searchFor,mode);
       searchJob.run();
    }
    private class Searcher implements Runnable {

		private String searchFor;
		private SearchMode mode;
		public Searcher(String searchFor, SearchMode mode) {
			this.searchFor = searchFor;
			this.mode = mode;
		}
		@Override
		public void run() {
			 try {
		            if (searchFor != null && searchFor.length() > 0) {
		                List<Book> result = null;
		                switch (mode) {
		                    case Title:
		                        result = booksDb.searchBooksByTitle(searchFor);
		                        break;
		                    case ISBN:
		                        result = booksDb.searchBooksByISBN(searchFor);
		                        break;
		                    case Author:
		                    	result = booksDb.searchBooksByAuthor(searchFor);
		                        break;
		                    case Rating:
		                        result = booksDb.searchBooksByRating(searchFor);
		                        break;
		                    case Genre:
		                    	result = booksDb.searchBooksByGenre(searchFor);
		                    	break;
		                    default:
		                }
		                if (result == null || result.isEmpty()) {
		                    booksView.showAlertAndWait(
		                            "No results found.", INFORMATION);
		                } else {
		                    booksView.displayBooks(result);
		                }
		            } else {
		                booksView.showAlertAndWait(
		                        "Enter a search string!", WARNING);
		            }
		        } catch (SQLException|IOException e) {
		            booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
		        }
		        catch(NullPointerException e) {
		        	booksView.showAlertAndWait("Database error. No connection to database",ERROR);
			 	}
		}
    }

    public void onConnectSelected() throws IOException, SQLException {
    	booksDb.connect("Library", "dbUser", "terror");
    }
    public void onDisconnectSelected() throws IOException, SQLException {
    	booksDb.disconnect();
    }
    public void newAuthorWindow() {
    	booksView.newAuthorWindow(this);
    }
	private class AuthorInserter implements Runnable {

			private String authorName;
			public AuthorInserter(String authorName) {
				this.authorName=authorName;
			}
			@Override
			public void run() {
				try {
					booksDb.insertNewAuthor(authorName);
				} catch (IOException | SQLException e) {
					booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
				}
			}
	 }
    public void onNewAuthorSubmit(String authorName) throws IOException, SQLException{
    	if(!"".equals(authorName)) {
    		AuthorInserter inserterJob = new AuthorInserter(authorName);
    		inserterJob.run();
    	}

    }
    public void onRateSelected() {
        booksView.ratingWindow(this);
    }
    private class ReviewInserter implements Runnable {

		private String isbn, userID, rating, review;
		public ReviewInserter(String isbn, String userID, String rating, String review) {
			this.isbn = isbn;
			this.userID=userID;
			this.rating=rating;
			this.review=review;
		}
		@Override
		public void run() {
			try {
				booksDb.addReview(userID, isbn, rating, review);
			} catch (IOException | SQLException e) {
				booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
			}
		}
    }
    public void oneNewRatingSubmit(String isbn, String userID, String rating, String review) throws IOException, SQLException{
        ReviewInserter reviewJob = new ReviewInserter(isbn,userID,rating,review);
        reviewJob.run();
    }
    public void onNewBookSelected() {
    	booksView.newBookWindow(this);
    }
    private class BookInserter implements Runnable {

		private String isbn, genre, title, authors;
		public BookInserter(String isbn,String genre, String title,String authors) {
			this.isbn = isbn;
			this.genre = genre;
			this.title=title;
			this.authors=authors;
		}
		@Override
		public void run() {
			try {
				booksDb.addReview(isbn, genre, title, authors);
			} catch (IOException | SQLException e) {
				booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
			}
		}
    }
    public void onNewBookSubmit(String isbn,String genre, String title,String authors) throws IOException, SQLException{
    	BookInserter inserterJob = new BookInserter(isbn, genre, title, authors);
    	inserterJob.run();
    }
    public void onRowSelect(Book book){
    	booksView.displayReviews(book);
    }

    public void onSignupSelect(){
    	booksView.newCustomerWindow(this);
	}
	public void onNewCustomerSubmit(String name, String address, String username, String password) throws IOException, SQLException{
    	booksDb.addCustomer(name,address,username,password);
	}
	public void onLogInSelect(){
    	booksView.logInWindow(this);
	}
	public void onLogInSubmit(String username, String password) throws IOException, SQLException{
		booksDb.loginAttempt(username,password);
	}
}

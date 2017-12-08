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
		                    case Genre:
		                    	result = booksDb.searchBooksByGenre(searchFor);
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
		        } catch (Exception e) {
		            booksView.showAlertAndWait("Database error.",ERROR);
		        }
		}	
    }
    
    public void onConnectSelected() throws IOException, SQLException {
    	booksDb.connect("Library", "root", "terror123");
    }
    public void onDisconnectSelected() throws IOException, SQLException {
    	booksDb.disconnect();
    }
    public void newAuthorWindow() {
    	booksView.newAuthorWindow(this);
    }
    public void onNewAuthorSubmit(String authorName) throws IOException, SQLException{
    	if(!"".equals(authorName))
    		booksDb.insertNewAuthor(authorName);
    }
    public void onRateSelected() {
        booksView.ratingWindow(this);
    }
    public void oneNewRatingSubmit(String isbn, String userID, String rating, String reviwe) throws IOException, SQLException{
        booksDb.addRating(isbn, userID, rating, reviwe);
    }
    public void onNewBookSelected() {
    	booksView.newBookWindow(this);
    }
    public void onNewBookSubmit(String isbn,String genre, String title,String authors) throws IOException, SQLException{
    	booksDb.insertNewBook(isbn, genre, title, authors);
    }
    public void onRowSelect(Book book){
    	booksView.displayReviews(book);
    }
    
}

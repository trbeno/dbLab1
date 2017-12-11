package booksdbclient.view;

import booksdbclient.model.SearchMode;
import javafx.application.Platform;
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
    
    private class DisplayResult implements Runnable {
    	private List<Book> result=null;
    	public DisplayResult(List<Book> result) {
    		this.result=result;
    	}
		@Override
		public void run() {
			booksView.displayBooks(result);
			
		}
    	
    }
    protected void onSearchSelected(String searchFor, SearchMode mode) {
       Thread thread = new Thread(new Searcher(searchFor,mode));
       thread.start();
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
		                	Platform.runLater(new Runnable() {
		                	    @Override
		                	    public void run() {
		                	    	booksView.showAlertAndWait("No results found.", INFORMATION);
		                	    }
		                	});
		                } else {
		                    Platform.runLater(new DisplayResult(result));
		                }
		            } else {
		            	Platform.runLater(new Runnable() {
	                	    @Override
	                	    public void run() {
	                	    	booksView.showAlertAndWait(
	    		                        "Enter a search string!", WARNING);
	                	    }
	                	});
		            }
		        } catch (SQLException|IOException e) {
		        	Platform.runLater(new Runnable() {
                	    @Override
                	    public void run() {
                	    	booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
                	    }
                	});
		        }
		        catch(NullPointerException e) {
		        	Platform.runLater(new Runnable() {
                	    @Override
                	    public void run() {
                	    	booksView.showAlertAndWait("Database error. No connection to database",ERROR);
                	    }
                	});
			 	}
		}
    }

    private class Connecter implements Runnable {
		public Connecter() {
		}
		@Override
		public void run() {
			try {
				booksDb.connect("Library", "dbUser", "terror");
			} catch (IOException | SQLException e) {
				Platform.runLater(new Runnable() {
            	    @Override
            	    public void run() {
            	    	booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
            	    }
            	});
			}
		}
    }
    public void onConnectSelected() throws IOException, SQLException {
    	Thread thread = new Thread(new Connecter());
    	thread.start();
    }
    
    private class DisConnecter implements Runnable {
		public DisConnecter() {
		}
		@Override
		public void run() {
			try {
				booksDb.disconnect();
			} catch (IOException | SQLException e) {
				Platform.runLater(new Runnable() {
            	    @Override
            	    public void run() {
            	    	booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
            	    }
            	});
			}
		}
    }
    public void onDisconnectSelected() throws IOException, SQLException {
    	Thread thread = new Thread(new DisConnecter());
    	thread.start();
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
					Platform.runLater(new Runnable() {
	            	    @Override
	            	    public void run() {
	            	    	booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
	            	    }
	            	});
				}
			}
	 }
    public void onNewAuthorSubmit(String authorName) throws IOException, SQLException{
    	if(!"".equals(authorName)) {
    		Thread thread = new Thread(new AuthorInserter(authorName));
    		thread.start();
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
				Platform.runLater(new Runnable() {
            	    @Override
            	    public void run() {
            	    	booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
            	    }
            	});
			}
		}
    }
    public void oneNewRatingSubmit(String isbn, String userID, String rating, String review) throws IOException, SQLException{
        Thread thread = new Thread(new ReviewInserter(isbn,userID,rating,review));
		thread.start();
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
				Platform.runLater(new Runnable() {
            	    @Override
            	    public void run() {
            	    	booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
            	    }
            	});
			}
		}
    }
    public void onNewBookSubmit(String isbn,String genre, String title,String authors) throws IOException, SQLException{
    	Thread thread = new Thread(new BookInserter(isbn, genre, title, authors));
		thread.start();
    	
    }
    public void onRowSelect(Book book){
    	booksView.displayReviews(book);
    }

    public void onSignupSelect(){
    	booksView.newCustomerWindow(this);
	}
    private class UserInserter implements Runnable {

		private String name, address,userName, password;
		public UserInserter(String name, String address, String username, String password) {
			this.name=name;
			this.address=address;
			this.userName = username;
			this.password = password;
		}
		@Override
		public void run() {
			try {
				booksDb.addCustomer(name, address, userName, password);
			} catch (IOException | SQLException e) {
				Platform.runLater(new Runnable() {
            	    @Override
            	    public void run() {
            	    	booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
            	    }
            	});
			}
		}
    }
	public void onNewCustomerSubmit(String name, String address, String username, String password) throws IOException, SQLException{
    	Thread thread = new Thread(new UserInserter(name,address,username,password));
    	thread.start();
	}
	
	private class LogInManeger implements Runnable {

		private String userName, password;
		public LogInManeger(String username, String password) {
			this.userName = username;
			this.password = password;
		}
		@Override
		public void run() {
			try {
				booksDb.loginAttempt(userName,password);
			} catch (IOException | SQLException e) {
				Platform.runLater(new Runnable() {
            	    @Override
            	    public void run() {
            	    	booksView.showAlertAndWait("Database error."+e.getMessage(),ERROR);
            	    }
            	});
			}
		}
    }
	public void onLogInSelect(){
    	booksView.logInWindow(this);
	}
	public void onLogInSubmit(String username, String password) throws IOException, SQLException{
		Thread thread = new Thread(new LogInManeger(username,password));
		thread.start();
	}
}

package booksdbclient.view;

import booksdbclient.model.SearchMode;
import booksdbclient.model.Book;
import booksdbclient.model.MySQL;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * The main pane for the view, extending VBox and including the menus. An
 * internal BorderPane holds the TableView for books and a search utility.
 *
 * @author anderslm@kth.se
 */
public class BooksPane extends VBox {

    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable; // the data backing the table view

    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;
    private MenuBar menuBar;

    public BooksPane(MySQL booksDb) {
        final Controller controller = new Controller(booksDb, this);
        this.init(controller);
    }

    /**
     * Display a new set of books, e.g. from a database select, in the
     * booksTable table view.
     *
     * @param books the books to display
     */
    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);
    }
    
    /**
     * Notify user on input error or exceptions.
     * 
     * @param msg the message
     * @param type types: INFORMATION, WARNING et c.
     */
    protected void showAlertAndWait(String msg, Alert.AlertType type) {
        // types: INFORMATION, WARNING et c.
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    private void init(Controller controller) {

        booksInTable = FXCollections.observableArrayList();

        // init views and event handlers
        initBooksTable(controller);
        initSearchView(controller);
        initMenus(controller);

        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(booksTable);
        mainPane.setBottom(bottomPane);
        mainPane.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(menuBar, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
    }

    private void initBooksTable(Controller controller) {
        booksTable = new TableView<>();
        booksTable.setEditable(false); // don't allow user updates (yet)
        booksTable.setRowFactory( tableRows -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Book rowData = row.getItem();
                    controller.onRowSelect(rowData);
                }
            });
            return row ;
        });
        // define columns
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        TableColumn<Book, String> ratingCol = new TableColumn<>("AvgRating");
        booksTable.getColumns().addAll(titleCol, isbnCol, authorCol,genreCol,ratingCol);
        // give title column some extra space
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.5));

        // define how to fill data for each cell,
        // get values from Book properties
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("authors"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        // associate the table view with the data
        booksTable.setItems(booksInTable);
    }

    private void initSearchView(Controller controller) {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");
        
        // event handling (dispatch to controller)
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String searchFor = searchField.getText();
                SearchMode mode = searchModeBox.getValue();
                controller.onSearchSelected(searchFor, mode);
            }
        });
    }

    private void initMenus(Controller controller) {

        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                try {
                    controller.onDisconnectSelected();
                    System.exit(0);
                } catch (Exception e) {}
            }
        });
        MenuItem connectItem = new MenuItem("Connect to Db");
        connectItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                try {
                    controller.onConnectSelected();
                } catch (IOException | SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        MenuItem disconnectItem = new MenuItem("Disconnect");
        disconnectItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                try {
                    controller.onDisconnectSelected();
                } catch (Exception e) {}
            }
        });
        fileMenu.getItems().addAll(exitItem, connectItem, disconnectItem);

        Menu searchMenu = new Menu("Search");
        MenuItem titleItem = new MenuItem("Title");
        MenuItem isbnItem = new MenuItem("ISBN");
        MenuItem authorItem = new MenuItem("Author");
        searchMenu.getItems().addAll(titleItem, isbnItem, authorItem);

        Menu manageMenu = new Menu("Manage");
        MenuItem addItem = new MenuItem("Add Book");
        addItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.onNewBookSelected();
            }
        });
        MenuItem removeItem = new MenuItem("Remove");
        MenuItem updateItem = new MenuItem("Update");
        MenuItem rateItem = new MenuItem("Rate");

        MenuItem addAuthorItem = new MenuItem("Add Author");
        addAuthorItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.newAuthorWindow();
            }
        });

        rateItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                try {
                    controller.onRateSelected();
                } catch (Exception e) {}
            }
        });

        manageMenu.getItems().addAll(addItem, removeItem, updateItem,rateItem,addAuthorItem);

        Menu accoutMenu = new Menu("Accout");
        MenuItem signinItem = new MenuItem("Sing Up");
        MenuItem loginItem = new MenuItem("Log In");
        MenuItem logout = new MenuItem("Log Out");

        accoutMenu.getItems().addAll(signinItem,loginItem,logout);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, searchMenu, manageMenu,accoutMenu);
    }
    
    public void newAuthorWindow(Controller controller) {  
    	 final Stage dialog = new Stage();
    	 dialog.initModality(Modality.APPLICATION_MODAL);
		 VBox dialogVbox = new VBox(20);
		 
		 final TextField authorField = new TextField();
		 final Label authorLabel = new Label("Enter Author name:");
	     authorField.setPromptText("Author name:");
		 
	     Button submitBtn = new Button();
		 submitBtn.setText("Submit");
		 submitBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
			   try {
				   String authorName = authorField.getText();
				   System.out.println(authorName);
				   controller.onNewAuthorSubmit(authorName);
				   dialog.close();
			} catch (IOException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
         });

		 dialogVbox.getChildren().addAll(authorLabel,authorField,submitBtn);
		 Scene dialogScene = new Scene(dialogVbox, 300, 200);
		 dialog.setScene(dialogScene);
		 dialog.show();
    }
    
    public void ratingWindow(Controller controller){

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox dialogVbox = new VBox(20);

        final TextField isbnField = new TextField();
        final Label isbnLabel = new Label("Enter ISBN:");

        final TextField userIDField = new TextField();
        final Label userIDLabel = new Label("Enter userID:");

        final TextField ratingField = new TextField();
        final Label ratingLabel = new Label("Enter Rating:");

        final TextField reviwField = new TextField();
        final Label reviwLabel = new Label("Enter Reviw:");
        isbnField.setPromptText("Rate book:");

        Button submitBtn = new Button();
        submitBtn.setText("Submit");

        submitBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    String isbn = isbnField.getText();
                    String userID = userIDField.getText();
                    String rating = ratingField.getText();
                    String reviw = reviwField.getText();
                    if(Float.parseFloat(rating)>5|| Float.parseFloat(rating)<0) {
                    	Alert alert = new Alert(AlertType.INFORMATION);
                    	alert.setTitle("Out of bounds error");
                    	alert.setHeaderText("The rating you have entered is out of bound");
                    	alert.setContentText("Enter a value between 0-5");

                    	alert.showAndWait();
                    	return;
                    }
                    	
                    controller.oneNewRatingSubmit(isbn,userID,rating,reviw);
                    dialog.close();
                    
                } catch (IOException | SQLException e) {
                	Alert alert = new Alert(AlertType.INFORMATION);
                	alert.setTitle("Error");
                	alert.setHeaderText("Could not Insert Author");
                	alert.setContentText("Try with a differnet name");

                	alert.showAndWait();
                	e.printStackTrace();
                }
            }
        });

        dialogVbox.getChildren().addAll
                (isbnLabel,isbnField,userIDLabel,userIDField,ratingLabel,ratingField,reviwLabel,reviwField,submitBtn);
        Scene dialogScene = new Scene(dialogVbox, 300, 400);
        dialog.setScene(dialogScene);
        dialog.show();
    }
    public void newBookWindow(Controller controller){

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox dialogVbox = new VBox(20);

        final TextField titleField = new TextField();
        final Label titleLabel = new Label("Enter Title:");

        final TextField isbnField = new TextField();
        final Label isbnLabel = new Label("Enter ISBN:");

        final TextField genreField = new TextField();
        final Label genreLabel = new Label("Enter Genre:");

        final TextField authorsField = new TextField();
        final Label authorsLabel = new Label("Enter Author Names:");

        isbnField.setPromptText("New Book:");

        Button submitBtn = new Button();
        submitBtn.setText("Submit");

        submitBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    String isbn = isbnField.getText();
                    String title = titleField.getText();
                    String genre = genreField.getText();
                    String authors = authorsField.getText();

                    controller.onNewBookSubmit(isbn,genre,title,authors);
                } catch (IOException | SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        dialogVbox.getChildren().addAll(titleLabel,titleField,isbnLabel,isbnField,genreLabel,genreField,authorsLabel,authorsField,submitBtn);
        Scene dialogScene = new Scene(dialogVbox, 300, 400);
        dialog.setScene(dialogScene);
        dialog.show();
    }
    
    public void displayReviews(Book book) {
    	final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Reviews:");
        TextArea textArea = new TextArea();
        textArea.setText(book.toString());
        Scene dialogScene = new Scene(textArea, 400, 600);
        dialog.setScene(dialogScene);
        dialog.show();
    }
   
}

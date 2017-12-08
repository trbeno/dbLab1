package booksdbclient.model;
import java.util.ArrayList;

/**
 * Representation of a book.
 *
 * @author anderslm@kth.se
 */
public class Book {

    private int bookId;
    private String isbn; // should check format
    private String title;
    private String storyLine = "";
    private ArrayList<Author> authors;
    private String genre;
    private ArrayList<Review> reviwList;
    private float  rating;


    public Book(int bookId, String isbn, String title,String genre) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.genre = genre;
    }
    public Book(String isbn, String title, String genre,float rating,ArrayList<Review> reviews){
        this.isbn = isbn;
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.reviwList = reviews;
    }

    public void addReviw(Review review){
        this.reviwList.add(review);
    }

    public int getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getStoryLine() { return storyLine; }

    public void setStoryLine(String storyLine) {
        this.storyLine = storyLine;
    }
    public void setAuthors (ArrayList<Author> authors) {
        this.authors = authors;
    }

    public float getRating() {
        return rating;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAuthors() {
        String info="";
        for(Author a: authors)
            info+=a.getAuthorName()+",";
        return info;
    }
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(System.getProperty("line.separator"));
        builder.append("Title: ").append(this.title).append(System.getProperty("line.separator"));
        builder.append("ISBN: ").append(this.isbn).append(System.getProperty("line.separator"));
        builder.append("Authors: ");
        for (int i = 0; i < authors.size(); i++) {
            builder.append(authors.get(i)).append(" ");
        }
        builder.append(System.getProperty("line.separator"));
        builder.append("Average ating: ").append(this.rating);
        for (int i = 0; i < reviwList.size() ; i++){
            builder.append(reviwList.get(i)).append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }
}
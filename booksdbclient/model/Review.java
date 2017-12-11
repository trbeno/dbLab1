package booksdbclient.model;

import java.util.Date;

public class Review {

    private String review;
    private float rating;
    private Date date;
    
    public Review(String review,float rating,Date date) {
        this.review = review;
        this.rating = rating;
        this.date = date;
    }

    public float getRating() {
    	return rating;
    }
    public String getReview() {
        return review;
    }
    @Override
    public String toString() {
    	StringBuilder builder = new StringBuilder().append(System.getProperty("line.separator"));
    	builder.append("Rating: ").append(this.rating).append(System.getProperty("line.separator"));
    	builder.append("Review: ").append(this.review).append(System.getProperty("line.separator"));
    	builder.append("Adding date: ").append(this.date);
    	return builder.toString();
    }
    public void setReview(String review) {
        this.review = review;
    }
}
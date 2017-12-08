package booksdbclient.model;

public class Author {

    private String authorName;

    public Author(String Author) {
        this.authorName = Author;
    }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(authorName).append(",");
        return  builder.toString();
    }
}
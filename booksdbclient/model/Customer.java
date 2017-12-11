package booksdbclient.model;

public class Customer {

    private int customerId;
    private String name;
    private String address;
    private String username;
    private String password;

    public Customer(int customerId, String name, String address, String username, String password) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.username = username;
        this.password = password;
    }
}

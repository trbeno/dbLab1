package booksdbclient.model;

public class Customer {

    private int customerId;
    private String customerOId;
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
    
    public Customer(String customerOId, String name, String address, String username, String password) {
        this.customerOId = customerOId;
        this.name = name;
        this.address = address;
        this.username = username;
        this.password = password;
    }

    public String getCustomerOId() {
		return customerOId;
	}

	public void setCustomerOId(String customerOId) {
		this.customerOId = customerOId;
	}

	public int getCustomerId() {
        return customerId;
    }
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", OId='" + customerOId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

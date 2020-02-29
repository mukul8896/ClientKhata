package BeanClasses;

public class ClientAndBalance {
    private String clientName;
    private int balance;
    private int id;

    public int getBalance() {
        return balance;
    }

    public String getClientName() {
        return clientName;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

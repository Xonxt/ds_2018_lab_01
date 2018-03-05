package ua.opu.banking;

public class Account {
  private String id;
  private int balance;

  public Account(String id, int balance) {
    this.id = id;
    this.balance = balance;
  }

  private Account() {  }

  public String getId() {
    return id;
  }

  public int getBalance() {
    return balance;
  }

  public void post(int value) {
    balance += value;
  }
}

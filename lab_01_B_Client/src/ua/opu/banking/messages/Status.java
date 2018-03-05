package ua.opu.banking.messages;
import java.io.Serializable;

public class Status implements Serializable{
  private StatusMessage status;
  private int balance;

  public Status(StatusMessage status, int balance) {
    this.status = status;
    this.balance = balance;
  }

  public StatusMessage getStatus() {
    return status;
  }

  public int getBalance() {
    return balance;
  }
}

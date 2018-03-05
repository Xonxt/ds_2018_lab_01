package ua.opu.banking;

import java.util.ArrayList;
import java.util.Random;

public class Client implements Runnable {

  private Bank bank;
  private ArrayList<String> transactions;
  private String accountId;
  private String clientName;

  public Client(Bank bank, String accountId, ArrayList<String> transactions) {
    this.clientName = generateName(4);
    this.bank = bank;
    this.transactions = transactions;
    this.accountId = accountId;
  }

  private Client() { }

  private String generateName(int length) {
    String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefggijklmnopqrstuvwxyz0123456789";

    StringBuilder stringBuilder = new StringBuilder();
    Random rand = new Random();

    for (int i = 0; i < length; i++) {
      stringBuilder.append( symbols.charAt( rand.nextInt(symbols.length()) ) );
    }

    return stringBuilder.toString();
  }

  public void executeTransactions() {
    try {
      Account account = bank.getAccount(this.accountId);

      if (account == null) {
        throw new Exception("Client '" + this.clientName + "': Account not found");
      }
      else {
        System.out.println("Client '" + this.clientName + "' connected. Balance: " + account.getBalance());
      }

      for(String transaction : transactions) {
        try {
          int value = Integer.parseInt(transaction);

          synchronized (account) {
            if (account.getBalance() + value >= 0) {
              account.post(value);
              System.out.println("\tClient '" + this.clientName + "': Transaction: " + transaction + ", Balance: " + account.getBalance());
            } else
              throw new Exception("Client '" + this.clientName + "': Not enough money for transaction '" + transaction + "'!");
          }
        }
        catch (NumberFormatException ex) {
          System.out.println("Client '" + this.clientName + "': Invalid transaction '" + transaction + "'");
        }
      }

    }
    catch (Exception ex) {
      System.out.println("Error! " + ex.getMessage());
    }
  }

  public void executeTransactions(ArrayList<String> transactions) {
    this.transactions = transactions;
    this.executeTransactions();
  }

  @Override
  public void run() {
    executeTransactions();
  }
}

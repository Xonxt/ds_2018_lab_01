package ua.opu;

import ua.opu.banking.Account;
import ua.opu.banking.Bank;
import ua.opu.banking.messages.Message;
import ua.opu.banking.messages.MessageType;
import ua.opu.banking.messages.Status;
import ua.opu.banking.messages.StatusMessage;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;

public class ClientThread extends Thread {
  private Connection connection;
  private String clientName;
  private Bank bank;
  private Account account;

  public ClientThread(Connection connection, Bank bank) {
    this.connection = connection;
    this.clientName = generateName(6);
    this.bank = bank;
    this.account = null;
  }

  private void accessAccount(String id) throws IOException {
    account = bank.getAccount(id);

    if (account == null) {
      //throw new Exception("Client '" + this.clientName + "': Account not found");
      log("Account '" + id + "' not found");
      connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, 0));
    }
    else {
      log("Connected to account '" + account.getId() + "'");
      connection.setAccount(account);

      // return status
      connection.getOutputStream().writeObject(new Status(StatusMessage.SUCCESS, account.getBalance()));
    }
  }

  private void createAccount(String id, String balance) throws IOException {
    try {
      this.account = new Account(id, Integer.parseInt(balance));

      if (bank.addAccount(account)) {
        log("Account '" + account.getId() + "' with balance " + balance + " created");
        connection.setAccount(account);
        connection.getOutputStream().writeObject(new Status(StatusMessage.SUCCESS, account.getBalance()));
      }
      else {
        log("Can't create account '" + account.getId() + "'");
        connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, 0));
      }
    }
    catch (NumberFormatException|IndexOutOfBoundsException ex) {
      log("Wrong message format");
      connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, 0));
    }
  }

  private void deleteAccount(String id) throws IOException {
    if (bank.removeAccount(id)) {
      log("Account '" + account.getId() + "' removed");
      account = null;
      connection.setAccount(null);
      connection.getOutputStream().writeObject(new Status(StatusMessage.SUCCESS, 0));
    }
    else {
      log("Can't remove account '" + account.getId() + "'");
      connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, 0));
    }
  }

  private void postAccount(String transactionValue) throws IOException {
    if (connection.getAccount() == null) {
      log("Need to connect to account first");
      // send bad status
      connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, 0));
      return;
    }

    try {
      int value = Integer.parseInt(transactionValue);

      synchronized (account) {
        if (account.getBalance() + value >= 0) {
          account.post(value);
          log("Transaction: " + transactionValue + ", Balance: " + account.getBalance());
          connection.getOutputStream().writeObject(new Status(StatusMessage.SUCCESS, account.getBalance()));
        } else {
          log("Not enough money for transaction '" + transactionValue + "'!");
          connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, account.getBalance()));
        }
      }
    }
    catch (NumberFormatException|ArrayIndexOutOfBoundsException ex) {
      log("Invalid transaction '" + transactionValue + "'");
      connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, account.getBalance()));
    }
  }

  private void processMessages() {
    try {
      // when client connects, send him a message containing his new login
      connection.getOutputStream().writeObject( new Message("LOGIN " + clientName));
      log("Connected");

      // now start the infinite loop of communications
      while (true) {
        // wait for message:
        Message message = (Message) connection.getInputStream().readObject();

        // parse message
        Map.Entry<MessageType, ArrayList<String>> pair = parseMessage(message.getMessageString());
        MessageType messageType = pair.getKey();
        ArrayList<String> arguments = pair.getValue();

        account = connection.getAccount();

        switch (messageType) {
          case ACCESS:
            // if client wants to access an account
            accessAccount(arguments.get(0));

            break;
          case CREATE:
            // if client wants to create a new account
            createAccount(arguments.get(0), arguments.get(1));

            break;
          case DELETE:
            // if client tries to delete the account
            deleteAccount(arguments.get(0));

            break;
          case DEPOSIT:
            // depositing money to the account
            postAccount(arguments.get(0));

            break;
          case WITHDRAW:
            // withdrawing money from the account
            postAccount("-" + arguments.get(0));

            break;
          default:
            log("Invalid transaction");
            connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, 0));
        }
      }

    }
    catch (SocketException e) {
      System.out.println("Connection lost");
    }
    catch (IOException e) {
      System.out.println("I/O error");
    }
    catch (ClassNotFoundException e) {
      System.out.println("Unknown object sent");
    }
    catch (Exception ex) {
      System.out.println("Error: " + ex.getMessage());
    }
  }

  /*
      This will parse the message. There can be several message types:
      ACCESS Acc_01     : will establish connection with account "Acc_01"
      CREATE Acc_02 100 : will create a new account "Acc_02" with the balance of $100
      DELETE Acc_01     : will delete the account "Acc_01"
      DEPOSIT 50        : will add (deposit) $50 to the current account
      WITHDRAW 50       : will withdraw $50 from the current account
   */
  private Map.Entry<MessageType, ArrayList<String>> parseMessage(String messageString) {
    ArrayList<String> arguments = null;
    MessageType messageType = MessageType.NONE;

    try {
      if (messageString.isEmpty()) {
//        throw new Exception("Message string is empty");
        return new AbstractMap.SimpleEntry<>(MessageType.NONE, null);
      }

      String[] messageParts = messageString.trim().split("\\s+");

      if (messageParts.length < 2) {
//        throw new UnsupportedOperationException("Wrong message format");
        return new AbstractMap.SimpleEntry<>(MessageType.NONE, null);
      }

      // first argument: message type (ACCESS, CREATE, DELETE, DEPOSIT, WITHDRAW)
      try {
        messageType = MessageType.valueOf( messageParts[0].toUpperCase() );
      }
      catch (IllegalArgumentException ex) {
        //throw new Exception("Wrong message type");
        return new AbstractMap.SimpleEntry<>(MessageType.NONE, null);
      }

      // deal with the message arguments
      switch (messageType) {
        case ACCESS:    // arguments: Account Name
        case DELETE:    // arguments: Account Name
        case DEPOSIT:   // arguments: Value
        case WITHDRAW:  // arguments: Value
          arguments = new ArrayList<>(Arrays.asList(messageParts[1]));
          break;
        case CREATE:    // arguments: Account Name, Balance
          if (messageParts.length < 3) {
//            throw new IllegalArgumentException("Insufficient number of arguments");
            return new AbstractMap.SimpleEntry<>(MessageType.NONE, null);
          }
          StringBuilder stringBuilder = new StringBuilder();
          for (int i = 1; i < messageParts.length - 1; i++) {
            stringBuilder.append(messageParts[i]);
            stringBuilder.append("_");
          }
          stringBuilder.deleteCharAt(stringBuilder.length()-1);
          // in case the user inputs something like "CREATE AA BB CC DD EE FF 100"
          // it will concat all these substrings into one long account name
          // like "AA_BB_CC_DD_EE_FF" with a balance of 100
          arguments = new ArrayList<>(
                  Arrays.asList(stringBuilder.toString(), messageParts[messageParts.length-1]));
          break;
        default:
          arguments = new ArrayList<>();
      }
    }
    catch (Exception ex) {
      System.out.println("Error: " + ex.getMessage());
      try {
        connection.getOutputStream().writeObject(new Status(StatusMessage.ERROR, 0));
      }
      catch (IOException ex2)
      {
        System.out.println("Error sending bad status");
      }
    }

    return new AbstractMap.SimpleEntry<>(messageType, arguments);
  }

  @Override
  public void run() {
    processMessages();
  }

  private String generateName(int length) {
    String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefggijklmnopqrstuvwxyz0123456789";

    StringBuilder stringBuilder = new StringBuilder();
    Random rand = new Random();

    for (int i = 0; i < length; i++) {
      stringBuilder.append( symbols.charAt( rand.nextInt(symbols.length()) ) );
    }

    return stringBuilder.toString();
  }

  private void log(String message) {
    System.out.println("Client '" + this.clientName + "': " + message);
  }
}

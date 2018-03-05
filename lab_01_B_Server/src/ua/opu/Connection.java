package ua.opu;

import ua.opu.banking.Account;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection {
  private Socket socket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;

  private Account account;

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public Connection(Socket socket) {
    this.socket = socket;

    this.socket = socket;

    try {
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
      this.inputStream = new ObjectInputStream(socket.getInputStream());
    }
    catch (IOException e) {
      System.out.println("Unable to establish connection");
    }
  }

  private Connection() { }

  public Socket getSocket() {
    return socket;
  }

  public ObjectOutputStream getOutputStream() {
    return outputStream;
  }

  public ObjectInputStream getInputStream() {
    return inputStream;
  }
}

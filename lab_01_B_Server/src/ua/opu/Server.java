package ua.opu;

import ua.opu.banking.Bank;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Thread {
  private Bank bank;
  private static CopyOnWriteArrayList<Connection> clientList;

  public Server(Bank bank) {
    this.bank = bank;
    clientList = new CopyOnWriteArrayList<>();
  }

  @Override
  public void run() {
    new Thread( () -> bankingThread() ).start();
  }

  private void bankingThread() {
    System.out.println("Waiting for clients...");
    try {
      ServerSocket socketListener = new ServerSocket(3128);

      while (true) {

        Socket client = null;
        Connection connection = null;

        while (client == null) {
          client = socketListener.accept();
          connection = new Connection(client);
          clientList.add(connection);
        }
        new ClientThread(connection, bank).start();
      }
    }
    catch (SocketException ex) {
      System.out.println("ServerSocket error");
    }
    catch (IOException ex) {
      System.err.println("I/O exception");
    }
  }
}

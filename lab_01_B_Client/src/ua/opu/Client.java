package ua.opu;

import java.io.IOException;
import java.net.Socket;

public class Client {

  private Socket socket;

  public Client(String ip, int port) {
    try {
      this.socket = new Socket(ip, port);

      ClientThread clientThread = new ClientThread(socket);
      System.out.println("Client '" + clientThread.getClientName() + "' connected. Waiting for commands...");
      System.out.println("INSTRUCTIONS:");
      System.out.println("ACCESS AccountName");
      System.out.println("CREATE AccountName Balance");
      System.out.println("DELETE AccountName");
      System.out.println("DEPOSIT Value");
      System.out.println("WITHDRAW Value");

      clientThread.start();
      clientThread.join();

    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

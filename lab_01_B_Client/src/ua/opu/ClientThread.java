package ua.opu;

import ua.opu.banking.messages.Message;
import ua.opu.banking.messages.Status;
import ua.opu.banking.messages.StatusMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientThread extends Thread {

  private Socket socket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;

  private String clientName;
  private static int clientNumber = 1;

  public ClientThread(Socket socket) {
    this.socket = socket;

    try {
      outputStream = new ObjectOutputStream(this.socket.getOutputStream());
      inputStream = new ObjectInputStream(this.socket.getInputStream());

      // get login name
      Message message = (Message) inputStream.readObject();
      this.clientName = parseClientName(message.getMessageString());

    } catch (IOException e) {
      System.out.println("I/O error");;
    } catch (ClassNotFoundException e) {
      System.out.println("Wrong message format");
      this.clientName = "User_" + (clientNumber++);
    } catch (Exception e) {
      System.out.println("Unable to get client name, setting to default");
      this.clientName = "User_" + (clientNumber++);
    }
  }

  private String parseClientName(String message) throws Exception {
    if (message.isEmpty())
      throw new Exception("Client name message incorrect");

    String[] messageParts = message.trim().split("\\s+");

    if (messageParts.length != 2) {
      throw new Exception("Client name message incorrect");
    }

    if (messageParts[0].compareToIgnoreCase("LOGIN") == 0) {
      return messageParts[1];
    }
    else {
      throw new Exception("Client name message incorrect");
    }
  }

  public String getClientName() {
    return clientName;
  }

  private void bankingThread() {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      String message = "";

      while (true) {
        System.out.print("> ");
        message = br.readLine();

        if (message.compareToIgnoreCase("EXIT") == 0) {
          this.socket.close();
          break;
        }

        // send command
        outputStream.writeObject(new Message(message));

        // wait for status
        Status status = (Status) inputStream.readObject();

        if (status.getStatus() == StatusMessage.ERROR) {
          System.out.println("Banking operation error!");
        }
        else {
          System.out.println("Operation successful, Current balance: " + status.getBalance());
        }
      }

    } catch (SocketException ex) {
      System.out.println("Connection lost");
    }
    catch (IOException e) {
      System.out.println("Connection error");
    } catch (ClassNotFoundException e) {
      System.out.println("Can't receive status message");
    }
  }

  @Override
  public void run() {
    bankingThread();
  }
}

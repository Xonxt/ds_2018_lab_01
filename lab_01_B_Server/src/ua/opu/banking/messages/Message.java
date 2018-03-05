package ua.opu.banking.messages;

import java.io.Serializable;

public class Message implements Serializable{
  private String messageString;

  public String getMessageString() {
    return messageString;
  }

  public Message(String messageString) {
    this.messageString = messageString;
  }

  private Message() { }
}

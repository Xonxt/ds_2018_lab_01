package ua.opu.banking;

import java.util.HashMap;
import java.util.Map;

public class Bank {
  private Map<String, Account> accounts;

  public Bank() {
    accounts = new HashMap<>();
  }

  public boolean addAccount(Account account) {
    return (accounts.put(account.getId(), account) == null);
  }

  public boolean removeAccount(String id) {
       return (accounts.remove(id) != null);
  }

  public Account getAccount(String id) {
    return accounts.get(id);
  }
}

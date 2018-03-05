package ua.opu.banking;

import java.util.HashMap;
import java.util.Map;

public class Bank {
  private Map<String, Account> accounts;

  public Bank() {
    accounts = new HashMap<>();
  }

  public void addAccount(Account account) {
    accounts.put(account.getId(), account);
  }

  public Account getAccount(String id) {
    return accounts.get(id);
  }
}

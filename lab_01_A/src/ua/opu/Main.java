package ua.opu;

import ua.opu.banking.Account;
import ua.opu.banking.Bank;
import ua.opu.banking.Client;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Bank bank = new Bank();

        bank.addAccount(new Account("Acc_01", 100));
        bank.addAccount(new Account("Acc_02", 120));
        bank.addAccount(new Account("Acc_03", 50));

        Client client1 = new Client(bank, "Acc_01", new ArrayList<>(Arrays.asList("-50", "+20", "-90", "-50")));
        Client client2 = new Client(bank, "Acc_01", new ArrayList<>(Arrays.asList("+10", "-30", "-45", "+20")));

        new Thread(client1).start();
        new Thread(client2).start();

    }
}

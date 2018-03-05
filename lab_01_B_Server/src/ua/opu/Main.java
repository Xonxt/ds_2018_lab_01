package ua.opu;

import ua.opu.banking.Account;
import ua.opu.banking.Bank;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Bank bank = new Bank();

        bank.addAccount( new Account("Acc_01", 100) );
        bank.addAccount( new Account("Acc_02", 50) );

        Server server = new Server(bank);
        server.start();

        try {
            server.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

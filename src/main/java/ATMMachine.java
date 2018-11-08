import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class ATMMachine extends Thread {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://ecinstance.czvxe7grlrif.eu-central-1.rds.amazonaws.com/ec_database";


    static final String USER = Credentials.userName;
    static final String PASS = Credentials.password;

    static Connection conn = null;
    static Statement stmt = null;
    static boolean isLoggedIn = false;

    boolean concurrencyTest = false;


     public ATMMachine(boolean concurrencyTest) throws SQLException, ExecutionException, InterruptedException {

        this.concurrencyTest = concurrencyTest;
        System.out.println("Load MySQL JDBC driver");

        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        try {
            System.out.println("Connecting to database...");

            this.conn = DriverManager.getConnection(DB_URL, USER, PASS);
            this.conn.setAutoCommit(false);

            if(!this.concurrencyTest) {
                this.atm();
            }
//            System.out.println("Creating table...");
//            String sql = 	"CREATE TABLE IF NOT EXISTS account (account_id INT, pinCode INT, balance DECIMAL, UNIQUE (account_id));";
//            stmt = conn.createStatement();
//            stmt.executeQuery(sql);
//            stmt.executeUpdate(sql);
//            conn.commit();

        } catch (SQLException e){

            System.out.println("Unable to connect to database.");
            this.conn.rollback();
            return;
        }



//        sql = "INSERT INTO account VALUES(1, 1111, 50), (2, 2222, 50), (3,3333,50)";
//        stmt.executeUpdate(sql);

//        enable transaction demarcation

//        stmt = conn.createStatement();
//
//        try {
//            //submit transaction (prepare)
//            System.out.println("Submit demarcated transactions to database");
////            stmt.executeUpdate(update_1);
////            stmt.executeUpdate(update_2);
//
//            //Commit both transactions.
//            System.out.println("Commit demarcated transactions.");
////            conn.commit();
//
//        } catch (SQLException e) {
//            System.out.println("Rollback demarcated transactions.");
////            conn.rollback();
//        }
//
//        System.out.println("Closing connection...");
//            this.stmt.close();
//        this.conn.close();
    }


    public void atm() throws SQLException {

        Scanner scan = new Scanner(System.in);
        int account = 0 ,pin = 0;
        try {
            System.out.println("Enter your account number : ");
             account = scan.nextInt();

            System.out.println("Enter your pin : ");
             pin = scan.nextInt();
        }
        catch (Exception e){
            System.out.println("Incorrect input.");
            atm();
        }

        if(verify_account(account,pin)){

            this.balance_inquiry(account, pin);
            isLoggedIn = true;

            while (isLoggedIn) {
                try {
                    System.out.println("Please select the method of transaction " +
                            "\n'w' to withdraw  " +
                            "\n'd' to deposit  " +
//                        "\n't' to test concurrency" +
                            "\n'e' to exit");

                    char choice = scan.next().charAt(0);
                    //        System.out.println(choice);

                    switch (choice) {
                        case ('w'):
                            withdraw(account, pin, 50);
                            balance_inquiry(account, pin);
                            break;
                        case ('d'):
                            deposit(account, pin, 20);
                            balance_inquiry(account, pin);
                            break;
//                    case ('t'):
//                        Tester test = new Tester('w', 20);
//                        break;
                        case ('e'):
                            ATMMachine.clearScreen();
                            isLoggedIn = false;
                            atm();
                            break;
                    }
                }catch (Exception e){
                    atm();
                }
            }
        } else {
                System.out.println("Incorrect account number or pin code.");
                atm();
        }
    }


    public boolean verify_account(int account, int pin) throws SQLException {

        try {
//            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String balance_inquiry = "Select balance from ec_database.account where account_id = '"+ account +"' and pinCode = '"+ pin +"' FOR UPDATE;" ;

            this.stmt = this.conn.createStatement();
            ResultSet results = this.stmt.executeQuery(balance_inquiry);
            this.conn.commit();
//            this.stmt.close();

            if(results.next()){
                return true;
            }
            else return false;


        } catch (SQLException e) {

            System.out.println(e);
            e.printStackTrace();
            this.conn.rollback();

        }

        return false;
    }

    public void deposit(int account, int pin, int amount) throws SQLException {

        System.out.println(Thread.currentThread().getName()+" Deposit");
        this.stmt = this.conn.createStatement();

        Scanner scan = new Scanner(System.in);
        int balance  = balance_inquiry(account,pin);

        if(amount >= 0 && concurrencyTest==false){
            System.out.println("Enter the amount you want to deposit : ");
            amount = scan.nextInt();
        }

        if(amount > 0) {

            int new_amount = balance + amount;

            try {

                String update_balance = "UPDATE account SET balance = " + new_amount + " WHERE account_id =" + account;

                String log_transaction = "Insert into transaction_log (trancation_details,transaction_time) VALUES (\" Previous Balance = " + balance +
                        ", Amount deposited = "+amount+" \",NOW());";

                this.stmt.executeUpdate(update_balance);

                this.stmt = conn.createStatement();
                this.stmt.execute(log_transaction);


                this.conn.commit();
                this.stmt.close();

            } catch (SQLException e) {
                System.out.println(e);

                e.printStackTrace();
                this.conn.rollback();

            } catch (Exception e){
                System.out.println("Incorrect input.");
                atm();
            }

        } else {
            System.out.println("Invalid entry.");
        }

    }

    public void withdraw(int account, int pin, int amount) throws SQLException, ExecutionException, InterruptedException {

        System.out.println(Thread.currentThread().getName()+" Withdraw");

        conn.setAutoCommit(false);

        if(amount >= 0 && concurrencyTest==false) {
            System.out.println("Enter the amount you want to withdraw : ");
            Scanner scan = new Scanner(System.in);
            amount = scan.nextInt();
        }

        int balance = balance_inquiry(account,pin);


        if(balance > 0 && balance >= amount) {

           int new_amount = balance - amount;

            try {
//                conn = DriverManager.getConnection(DB_URL, USER, PASS);

                String update_balance = "UPDATE account SET balance = " + new_amount + " WHERE account_id = " + account+";";

                String log_transaction = "Insert into transaction_log (trancation_details,transaction_time) VALUES (\" Previous Balance = " + balance +
                        ", Amount withdrawn = "+amount+" \",NOW());";

                this.stmt.executeUpdate(update_balance);

                this.stmt = conn.createStatement();
                this.stmt.execute(log_transaction);

                this.conn.commit();
//                stmt.close();
            } catch (SQLException e) {

                System.out.println(e);
                e.printStackTrace();
                this.conn.rollback();

            }catch (Exception e1){
                System.out.println("Incorrect input.");
                atm();
            }

        } else {
            System.out.println("You have insufficient balance for this transaction.");
        }

    }


    public int balance_inquiry(int account, int pin) throws SQLException {

        int balance = 0;

        try {
//            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            this.conn.setAutoCommit(false);

            String balance_inquiry = "Select balance from ec_database.account where account_id = '"+ account +"' and pinCode = '"+ pin +"' FOR UPDATE;" ;

            this.stmt = this.conn.createStatement();
            ResultSet results = stmt.executeQuery(balance_inquiry);

            if(results.next()){
//                if(!concurrencyTest) {
                    System.out.println("Current Balance :" + Thread.currentThread().getName() + " Your current balance is : " + results.getString(1));
//                }
                balance = Integer.parseInt(results.getString(1));
            }

            this.conn.commit();

        } catch (SQLException e) {

//            System.out.println(e);
            e.printStackTrace();
            this.conn.rollback();

        }


        return balance;
    }

    // this code it to test the implementation of the atm system.

    static boolean isWithdrawal = false;

    @Override
    public void run() {
//        System.out.println(Thread.currentThread().getName()+" Start. Command");
//        try {
//            conn.setAutoCommit(false);
////            balance_inquiry(1,1111);
//
//        } catch (SQLException e) {
//            System.out.print("Something went wrong.");
//            e.printStackTrace();
//        }
        if(isWithdrawal) {
            try {
                isWithdrawal = false;
                System.out.println(Thread.currentThread().getName()+" Begins to Withdrawal Amount: "+10);

                withdraw(1,1111,10);
//                balance_inquiry(1,1111);
                System.out.println(Thread.currentThread().getName()+" Final balance : "+balance_inquiry(1,1111));

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (NullPointerException e){

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
                try {
                    isWithdrawal = true;
                    System.out.println(Thread.currentThread().getName()+" Begins to Deposit Amount: "+12);
                    deposit(1,1111,12);
                    System.out.println(Thread.currentThread().getName()+" Final balance : "+balance_inquiry(1,1111));

                } catch (SQLException e ) {
                    e.printStackTrace();
                }

        }

//        System.out.println(Thread.currentThread().getName()+" End.");
    }



    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


}
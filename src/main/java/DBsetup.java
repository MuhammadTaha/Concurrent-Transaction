import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class DBsetup extends Thread {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://ecinstance.czvxe7grlrif.eu-central-1.rds.amazonaws.com/ec_database";


    static final String USER = Credentials.userName;
    static final String PASS = Credentials.password;

    static Connection conn = null;
    static Statement stmt = null;
    static boolean isLoggedIn = false;

    static boolean concurrencyTest = true;


    public static void main(String[] args) throws SQLException, ExecutionException, InterruptedException {

        DBsetup dbsetup = new DBsetup();

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
            DBsetup.conn = DriverManager.getConnection(DB_URL, USER, PASS);
            DBsetup.conn.setAutoCommit(false);

//            System.out.println("Creating table...");
//            String sql = 	"CREATE TABLE IF NOT EXISTS account (account_id INT, pinCode INT, balance DECIMAL, UNIQUE (account_id));";
//            stmt = conn.createStatement();
//            stmt.executeQuery(sql);
//            stmt.executeUpdate(sql);


        } catch (SQLException e){

            System.out.println("Unable to connect to database.");
            return;
        }

//        dbsetup.atm();
        Tester test = new Tester('w', 20);

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
//        stmt.close();
    }


    public void atm() throws SQLException, ExecutionException, InterruptedException {

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
                        DBsetup.clearScreen();
                        isLoggedIn = false;
                        atm();
                        break;
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

            String balance_inquiry = "Select balance from ec_database.account where account_id = '"+ account +"' and pinCode = '"+ pin +"' for Update ;" ;

            this.stmt = this.conn.createStatement();
            ResultSet results = stmt.executeQuery(balance_inquiry);
            conn.commit();
            if(results.next()){
//                System.out.println("Balance Inquiry:"+ Thread.currentThread().getName() +" Your current balance is : "+results.getString(1));
                return true;
            }
            else return false;


        } catch (SQLException e) {

//            System.out.println(e);
            e.printStackTrace();
            conn.rollback();

        }

        return false;
    }

    public void deposit(int account, int pin, int amount) throws SQLException {

        System.out.println(Thread.currentThread().getName()+" Deposit");

        Scanner scan = new Scanner(System.in);
        int balance  = balance_inquiry(account,pin);

        if(amount >= 0 && concurrencyTest==false){
            System.out.println("Enter the amount you want to deposit : ");
            amount = scan.nextInt();
        }

        if(amount > 0) {

            amount = balance + amount;

            try {

                String update_balance = "UPDATE account SET balance = " + amount + " WHERE account_id=" + account;
                this.stmt.executeUpdate(update_balance);

                conn.commit();
            } catch (SQLException e) {
                System.out.println(e);

                e.printStackTrace();
                conn.rollback();

            }catch (NullPointerException e){

            }

        } else {
            System.out.println("Invalid entry.");
        }

    }

    public void withdraw(int account, int pin, int amount) throws SQLException {

        System.out.println(Thread.currentThread().getName()+" Withdraw");


        if(amount >= 0 && concurrencyTest==false) {
            System.out.println("Enter the amount you want to withdraw : ");
            Scanner scan = new Scanner(System.in);
            amount = scan.nextInt();
        }

        int balance = balance_inquiry(account,pin);


        if(balance > 0 && balance >= amount) {

            amount = balance - amount;

            try {
//                conn = DriverManager.getConnection(DB_URL, USER, PASS);

                String update_balance = "UPDATE account SET balance = " + amount + " WHERE account_id=" + account;
                this.stmt.executeUpdate(update_balance);

                conn.commit();
            } catch (SQLException e) {

                System.out.println(e);
                e.printStackTrace();
                conn.rollback();

            }catch (NullPointerException e){

            }

        } else {
            System.out.println("You have insufficient balance for this transaction.");
        }

    }


    public int balance_inquiry(int account, int pin) throws SQLException {

        int balance = 0;

        try {
//            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String balance_inquiry = "Select balance from ec_database.account where account_id = '"+ account +"' and pinCode = '"+ pin +"' for UPDATE ;" ;

            this.stmt = this.conn.createStatement();
            ResultSet results = stmt.executeQuery(balance_inquiry);

            if(results.next()){
//                if(!concurrencyTest) {
                    System.out.println("Current Balance :" + Thread.currentThread().getName() + " Your current balance is : " + results.getString(1) + "\n");
//                }
//                System.out.println("Your current balance is : "+results.getString(1));
                balance = Integer.parseInt(results.getString(1));
            }
        conn.commit();
        } catch (SQLException e) {

//            System.out.println(e);
            e.printStackTrace();
            conn.rollback();

        }
//        catch (NullPointerException e){
//
//        }

        return balance;
    }

    // this code it to test the implementation of the atm system.

    static boolean isWithdrawal = false;

    @Override
    public void run() {
//        System.out.println(Thread.currentThread().getName()+" Start. Command");
        try {
            conn.setAutoCommit(false);
//            balance_inquiry(1,1111);

        } catch (SQLException e) {
            System.out.print("Something went wrong.");
            e.printStackTrace();
        }
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

            }
            } else {
                try {
                    isWithdrawal = true;
                    System.out.println(Thread.currentThread().getName()+" Begins to Deposit Amount: "+12);
                    deposit(1,1111,12);
                    System.out.println(Thread.currentThread().getName()+" Final balance : "+balance_inquiry(1,1111));

                } catch (SQLException e ) {
                    e.printStackTrace();
                } catch(NullPointerException e){

                }
            }

//        System.out.println(Thread.currentThread().getName()+" End.");
    }



    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


}
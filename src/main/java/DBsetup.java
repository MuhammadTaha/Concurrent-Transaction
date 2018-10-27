import java.sql.*;
import java.util.Scanner;


public class DBsetup {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://ecinstance.czvxe7grlrif.eu-central-1.rds.amazonaws.com/ec_database";


    static final String USER = Credentials.userName;
    static final String PASS = Credentials.password;

    static Connection conn = null;
    static Statement stmt = null;

    public static void main(String[] args) throws SQLException {

        DBsetup dbsetup = new DBsetup();



        System.out.println("Load MySQL JDBC driver");

        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        System.out.println("Connecting to database...");
        DBsetup.conn = DriverManager.getConnection(DB_URL, USER, PASS);

//        System.out.println("Creating statement...");
//        stmt = conn.createStatement();
//
        String sql;
//        System.out.println("Creating table...");
//        sql = 	"CREATE TABLE IF NOT EXISTS account (account_id INT, pinCode INT, balance DECIMAL, UNIQUE (account_id));";
//        stmt.executeUpdate(sql);

        DBsetup.conn.setAutoCommit(false);

        dbsetup.atm();


//        sql = "INSERT INTO account VALUES(1, 1111, 50), (2, 2222, 50), (3,3333,50)";
//        stmt.executeUpdate(sql);



        //enable transaction demarcation

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


    public void atm(){

        Connection conn = null;
        Statement stmt = null;

        Scanner scan = new Scanner(System.in);

        System.out.println("Enter your account number : ");
        int account = scan.nextInt();

        System.out.println("Enter your pin : ");
        int pin = scan.nextInt();

        this.balance_inquiry(account,pin);

        System.out.println("Please select the method of transaction \nPress 'w' for Withdrawal  \nPress'd' for Deposit  \nPress'e' to exit \n ");

        char choice = scan.next().charAt(0);
//        System.out.println(choice);
        switch (choice) {
            case ('w'):
                withdraw(account,pin);
                balance_inquiry(account,pin);
                break;
            case ('d'):
                deposit(account,pin);
                balance_inquiry(account,pin);
                break;
            case ('e'):
                break;
        }
    }

    public void deposit(int account, int pin){

        Scanner scan = new Scanner(System.in);

        System.out.println("Enter the amount you want to deposit : ");
        int amount = scan.nextInt();
        int balance  = balance_inquiry(account,pin);

        if(amount > 0) {

            amount = balance + amount;

            try {
                this.conn = DriverManager.getConnection(DB_URL, USER, PASS);

                String update_balance = "UPDATE account SET balance = " + amount + " WHERE account_id=" + account;
                this.stmt.executeUpdate(update_balance);
//            if(results.getString(1) != null){
//                System.out.println(results.getString(1));
//            }

            } catch (SQLException e) {
                System.out.println(e);

                e.printStackTrace();
            }

        } else {
            System.out.println("Invalid entry.");
        }

    }

    public void withdraw(int account, int pin){
//
//        Connection conn = null;
//        Statement stmt = null;

        Scanner scan = new Scanner(System.in);

        System.out.println("Enter the amount you want to withdraw : ");
        int amount = scan.nextInt();
        int balance = balance_inquiry(account,pin);

        if(balance > 0 && balance >= amount) {

            amount = balance - amount;

            try {
                this.conn = DriverManager.getConnection(DB_URL, USER, PASS);

                String update_balance = "UPDATE account SET balance = " + amount + " WHERE account_id=" + account;
                this.stmt.executeUpdate(update_balance);
//            if(results.getString(1) != null){
//                System.out.println(results.getString(1));
//            }

            } catch (SQLException e) {
                System.out.println(e);

                e.printStackTrace();
            }

        } else {
            System.out.println("You have insufficient balance.");
        }

    }


    public int balance_inquiry(int account, int pin){

        int balance = 0;

        try {
            this.conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String balance_inquiry = "Select balance from ec_database.account where account_id = '"+ account +"' and pinCode = '"+ pin +"';" ;

            this.stmt = this.conn.createStatement();
            ResultSet results = stmt.executeQuery(balance_inquiry);

            if(results.next()){
                System.out.println("Your current balance is : "+results.getString(1));
                balance = Integer.parseInt(results.getString(1));
//                System.out.println(results.getString(1));
            }

        } catch (SQLException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return balance;
    }

}
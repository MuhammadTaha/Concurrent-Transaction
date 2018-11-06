import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class Initiator {

    public static void main(String[] args) throws InterruptedException, ExecutionException, SQLException {

        //just change this to true if you want to do concurrency test
        boolean concurrencyTest = true;

        if(!concurrencyTest) {

            ATMMachine dbSetup = new ATMMachine(concurrencyTest);
//            dbSetup.concurrencyTest = false;
        }
        else {
            Tester tester = new Tester();
        }
    }
}




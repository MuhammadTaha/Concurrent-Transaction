import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class Initator {

    public static void main(String[] args) throws InterruptedException, ExecutionException, SQLException {

        boolean concurrencyTest = true;

        if(!concurrencyTest) {

            ATMMachine dbSetup = new ATMMachine();
            dbSetup.concurrencyTest = false;
        }
        else {
            Tester tester = new Tester();
        }
    }
}




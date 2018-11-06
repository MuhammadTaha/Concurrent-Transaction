import java.sql.SQLException;
import java.util.concurrent.*;

public class Tester{

    public Tester() throws ExecutionException, InterruptedException, SQLException {

        System.out.println("Testing concurrency");

        for(int i = 0 ; i<6 ;i++) {
            System.out.println("\n"+Thread.currentThread().getName()+" Starts.");

            ATMMachine ds = new ATMMachine();
            ds.concurrencyTest = true;
            ds.start();
//            Thread.sleep(100);

        }
    }
}

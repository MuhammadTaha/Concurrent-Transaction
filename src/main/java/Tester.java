import sun.tools.jconsole.Worker;

import java.util.concurrent.*;

public class Tester{

    public Tester(char type, int amount) throws ExecutionException, InterruptedException {


        System.out.println("\n Testing concurrency ");
//        switch (type){
//            case 'd':
//                break;
//            case 'w':
//                break;
//        }

//        Runnable runnable = ()->{
//            System.out.println("Hello World");
//        };
//        Executor executor = Executors.newFixedThreadPool(10);


        for(int i = 0 ; i<6 ;i++) {
            System.out.println("Thread : "+ (i+1) +" Starts.");

            DBsetup ds = new DBsetup();
            Thread.sleep(100);
            ds.start();



//            executor.execute(() -> {
//            });
//            Runnable workerThread = new DBsetup();
//            Thread.sleep(100);
//            executor.execute(workerThread);
        }

//        for(int i =0 ; i<=10 ;i++) {
//            executor.execute(() -> {
//                System.out.println(Thread.currentThread().getName()+" Start. Command");
//            });
//        }
//        ExecutorService executorService = Executors. newFixedThreadPool(10);
//        Future<String> future = executorService.submit(() -> "Hello World");
//// some operations
//        String result = future.get();
    }






}

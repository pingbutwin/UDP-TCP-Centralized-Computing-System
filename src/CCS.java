import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CCS {
    static boolean flag = true;
    static volatile StatisticVariable clients =
            new StatisticVariable("Clients amount");
    static volatile StatisticVariable calculatedOper =
            new StatisticVariable("Calculated operations");
    static volatile StatisticVariable allOper =
            new StatisticVariable("All operations");
    static volatile StatisticVariable incorrectOper =
            new StatisticVariable("Incorrect operations");
    static volatile StatisticVariable summ =
            new StatisticVariable("Sum");
    public static void main(String[] args) {
        if(args.length != 1 || args[0].length() != 5 ||
                args[0].chars().anyMatch(e -> e < '0' || e > '9')
        )
            throw new RuntimeException("Wrong parameters");


        ExecutorService execPool = Executors.newCachedThreadPool();
        final int port = Integer.parseInt(args[0]);

        execPool.submit(() -> {
            ClientDiscovery.discover(port, execPool);
        });

        execPool.submit(() -> {
            try {
                ClientCommunication.communicate(port, execPool);
            } catch(IOException e) {
                e.printStackTrace();
            }
        });

        execPool.submit(() -> {
            List<Integer> temp;
            List<StatisticVariable> temp2 =
                    Arrays.asList(clients, calculatedOper, allOper, incorrectOper, summ);
            while(flag) {
                temp = temp2.stream().map(e -> e.quantity).collect(Collectors.toList());
                try {
                    Thread.sleep(10000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("\nStatistics from start: ");
                temp2.forEach(System.out::println);
                System.out.println("Last 10 seconds statistics:");
                for(int i = 0; i < temp.size(); i++) {
                    System.out.println("\t" + temp2.get(i).name + ": " +
                                (temp2.get(i).quantity - temp.get(i))
                    );
                }
                System.out.println();
            }
        });
    }
}
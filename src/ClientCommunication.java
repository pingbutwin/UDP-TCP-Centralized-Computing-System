import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ClientCommunication implements Runnable{
    static int port;
    Socket socket;
    public ClientCommunication(Socket socket) {
        this.socket = socket;
    }

    public static void communicate(int p, ExecutorService execPool) throws IOException {
        ClientCommunication.port = p;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while(CCS.flag) {
                Socket clientSocket = serverSocket.accept();
                CCS.clients.quantity++;

                execPool.submit(new ClientCommunication(clientSocket));
            }
        }
    }

    public void run() {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(in));
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(out));

            String line;
            while((line = br.readLine()) != null) {
                String[] arguments = line.split(" ");

                List<String> operList = Arrays.asList("ADD", "SUB", "MUL", "DIV");
                try {
                    if(arguments.length != 3 || !operList.contains(arguments[0])) {
                        CCS.incorrectOper.quantity++;
                        System.out.println(arguments[1] + " " + arguments[0]
                                + " " + arguments[2] + " = ERROR");
                        bw.write("ERROR");
                        bw.newLine();
                        bw.flush();
                    } else {
                        int res = -1;
                        int a = Integer.parseInt(arguments[1]);
                        int b = Integer.parseInt(arguments[2]);
                        switch(arguments[0]) {
                            case "ADD":
                                res = a + b;
                                break;
                            case "SUB":
                                res = a - b;
                                break;
                            case "MUL":
                                res = a * b;
                                break;
                            case "DIV":
                                res = a / b;
                                break;
                        }
                        CCS.summ.quantity += res;
                        CCS.calculatedOper.quantity++;

                        bw.write(res);
                        bw.newLine();
                        bw.flush();

                        System.out.println(arguments[1] + " " + arguments[0]
                                        + " " + arguments[2] + " = " + res);
                    }
                } catch (ArithmeticException | NumberFormatException ex) {
                    System.out.println("either 0 division or text in numbers");
                    CCS.incorrectOper.quantity++;
                    System.out.println(arguments[1] + " " + arguments[0]
                            + " " + arguments[2] + " = ERROR");
                    bw.write("ERROR");
                    bw.newLine();
                    bw.flush();
                }

                CCS.allOper.quantity++;
            }
            CCS.clients.quantity--;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

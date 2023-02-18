package client_side;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket client;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private boolean done;
    ObjectInputStream objectInputStream;

    public Client() {
        try {
            client = new Socket("localhost", 8888);
            printWriter = new PrintWriter(client.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
//            objectInputStream =new ObjectInputStream(client.getInputStream());

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;

//            System.out.println(objectInputStream.readObject());
            while ((inMessage = bufferedReader.readLine()) !=null) {
                System.out.println(inMessage);
//                System.out.println(objectInputStream.readObject());

            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            bufferedReader.close();
            printWriter.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException ignored) {
        }
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit")) {
                        inReader.close();
                        shutdown();
                    } else {
                        printWriter.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}

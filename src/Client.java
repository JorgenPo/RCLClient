import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public static void main(String[] args){
        if ( args.length < 2 ) {
            printUsage();
            return;
        }

        System.out.println("> Welcome in RCLprotocol client!");
        System.out.println("> Connecting to " + args[0] + ":" + args[1] + "...");

        try (
                Socket socket = new Socket(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));

                PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedReader sysin = new BufferedReader(
                        new InputStreamReader(System.in))
        ) {

            System.out.println("> Connected!");

            String userInput;
            String serverOutput;

            System.out.print("> Enter server login and password: ");
            while ( (userInput = sysin.readLine()) != null ) {

                if ( socket.isClosed() || !socket.isConnected() ) {
                    System.out.println("> Connection lost");
                }

                if ( userInput.equals("exit") ) {
                    userInput = "disconnect";
                }

                out.println(userInput);

                if ( userInput.equals("disconnect") ) {
                    socket.close();
                    break;
                }

                while ( !(serverOutput = in.readLine()).equals("EOT") ) {

                    if (serverOutput.equals("null")) {
                        System.out.println("> Server not responding...");
                        out.println("status");

                        if (serverOutput.equals("null")) {
                            System.out.println("> Server is down.");
                            break;
                        }
                    }

                    System.out.println(args[0] + ": " + serverOutput);
                }

                System.out.print("rcl@client $ ");
            }

        } catch (IOException e) {
            System.err.println("! An error occurred: " + e);
            //e.printStackTrace();
        }

        System.out.println("> Disconnecting...");
    }

    private static void printUsage() {
        System.out.println("RCLClient usage: rclclient.jar host port");
    }
}

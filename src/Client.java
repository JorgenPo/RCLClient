import rcl.core.RemoteInterface;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {

    public static void main(String[] args){
        if ( args.length < 2 ) {
            printUsage();
            return;
        }

        System.out.println("> Welcome in RCLprotocol client!");
        System.out.println("> Connecting to " + args[0] + ":" + args[1] + "...");

        try (
                BufferedReader sysin = new BufferedReader(
                        new InputStreamReader(System.in))
        ) {
            Registry reg = LocateRegistry.getRegistry(args[0]);
            RemoteInterface server = (RemoteInterface) reg.lookup("RCL");

            System.out.println("> Connected!");

            String userInput;
            String serverOutput;

            boolean isAuthorized = false;

            System.out.println("Authorization needed!");

            String username = "", password;
            // AUTHORIZATION
            while ( !isAuthorized ) {
                try {
                    System.out.print("Login: ");
                    username = sysin.readLine();
                    System.out.print("Password: ");
                    password = sysin.readLine();

                    serverOutput = server.authorize(username, password);

                    if ( !serverOutput.contains("Welcome") ) {
                        System.out.println(args[0] + ": " + serverOutput);
                        continue;
                    }

                    isAuthorized = true;

                    System.out.println("> Authorized successful");
                    System.out.println(args[0] + ": " + serverOutput);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String[] requestParts;
            // DIALOG
            System.out.print("rcl@client $ ");
            while ( (userInput = sysin.readLine()) != null ) {

                if ( false ) {
                    System.out.println("> Connection lost");
                }

                if ( userInput.equals("exit") ) {
                    break;
                } else if ( userInput.equals("disconnect") ) {
                    break;
                }

                requestParts = userInput.split(" ");
                if ( requestParts.length == 1 ) {
                    serverOutput = server.exec(username, requestParts[0], null);
                } else {
                    String methodName = requestParts[0];
                    String[] params = new String[requestParts.length - 1];

                    for (int i = 1; i < requestParts.length; ++i) {
                        params[i-1] = requestParts[i];
                    }

                    ArrayList<String> arrParams = new ArrayList<>(Arrays.asList(params));
                    serverOutput = server.exec(username, methodName, arrParams);
                }

                System.out.println(args[0] + ": " + serverOutput);
                System.out.print("rcl@client $ ");
            }

        } catch (IOException e) {
            System.err.println("! An error occurred: " + e);
            //e.printStackTrace();
        } catch (Exception e) {
            System.err.println("! An error occurred: " + e);
            e.printStackTrace();
        }

        System.out.println("> Disconnecting...");
    }

    private static void printUsage() {
        System.out.println("RCLClient usage: rclclient.jar host port");
    }
}


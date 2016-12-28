import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class Client {

    private static PrintWriter serverOut;
    private static BufferedReader serverIn;

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

            Client.serverOut = out;
            Client.serverIn = in;

            System.out.println("> Connected!");

            String userInput;
            String serverOutput;

            System.out.print("> Enter server login and password: ");

            boolean isAuthorized = false;

            // AUTHORIZATION
            while ( !isAuthorized ) {
                try {
                    userInput = sysin.readLine();
                    serverOutput = authorize(userInput);

                    if ( serverOutput.contains("rcl error") ) {
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
            while ( (userInput = sysin.readLine()) != null ) {

                if ( socket.isClosed() || !socket.isConnected() ) {
                    System.out.println("> Connection lost");
                }

                if ( userInput.equals("exit") ) {
                    userInput = "rcl.disconnect";
                } else if ( userInput.equals("disconnect") ) {
                    socket.close();
                    break;
                }

                requestParts = userInput.split(" ");
                if ( requestParts.length == 1 ) {
                    serverOutput = invokeFunction(userInput, null);
                } else {
                    String methodName = requestParts[0];
                    String[] params = new String[requestParts.length - 1];

                    for (int i = 1; i < requestParts.length; ++i) {
                        params[i-1] = requestParts[i];
                    }
                    serverOutput = invokeFunction(methodName, params);
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

    /**
     * Send xml-rpc authorize
     * message
     *
     * @retur String about success auth when okay
     *  and exception if result was failed
     */
    private static String authorize(String data) throws Exception {
        String[] parts = data.split(" ");

        String username, password;
        try {
            username = parts[0].trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            username = " ";
        }

        try {
            password = parts[1].trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            password = " ";
        }

        Object[] params = { username, password };

        String result;
        try {
            result = invokeFunction("rcl.authorize", params);
        } catch (Exception e) {
            throw e;
        }

        return result;
    }

    private static String invokeFunction(String name, Object[] params)
        throws Exception {
        String result = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element body = document.createElement("methodCall");
        document.appendChild(body);

        Element methodName = document.createElement("methodName");
        methodName.setTextContent(name);
        body.appendChild(methodName);

        if ( params != null ) {
            Element paramsElement = document.createElement("params");
            for (int i = 0; i < params.length; ++i) {
                Element param = document.createElement("param");
                Element value = document.createElement("value");
                Element type = document.createElement("string"); // TODO: other param types

                paramsElement.appendChild(param);
                param.appendChild(value);
                value.appendChild(type);
                type.setTextContent(params[i].toString());
            }
            body.appendChild(paramsElement);
        }

        String serialized = XMLUtil.documentToText(document);

        serverOut.println(serialized);
        serverOut.println("EOT");

        String serverResponse = null;

        StringBuilder response = new StringBuilder();
        while ( !(serverResponse = serverIn.readLine()).equals("EOT") ) {
            response.append(serverResponse);
            response.append('\n');
        }

        result = XMLUtil.getResultOf(response.toString());

        return result;
    }
}


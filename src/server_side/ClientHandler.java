package server_side;

import client_side.GroupChatClientHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import shared_classes.User;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;


public class ClientHandler implements Runnable {
    Socket socket;
    PrintWriter printWriter = null;
    BufferedReader bufferedReader = null;
    public static String newLine = System.getProperty("line.separator");
    boolean loginStatus;
    String f = "res/users.xml";
//    ObjectInputStream inputStream = new

    public ClientHandler(Socket clientSocket, PrintWriter printWriter, BufferedReader bufferedReader) {
        this.socket= clientSocket;
        this.printWriter = printWriter;
        this.bufferedReader = bufferedReader;

    }

    public void run() {
        String name = null;

        while (true){

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                org.w3c.dom.Document document;
                String f = "res/users.xml";
                File file= new File(f);

                document = documentBuilder.parse(file);

                NodeList users = document.getElementsByTagName("User");

                userValidation(name, users);

            } catch (IOException | ParserConfigurationException | SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void joinServer(String name, NodeList usersList) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        String message, recipient;

        try {
            broadcast(name + " joined the chat");
            messagePrompt(name);

            boolean exit = false;

            while (!exit && (message = bufferedReader.readLine()) != null) {
                messagePrompt(name);

                if (message.startsWith("/")) {
                    String[] words = message.split("/");

                    String command = words[1];
                    System.out.println(command);
                    switch (command) {
                        case "edit":
                            showEditMenu();
                            printWriter.println("Input your choice: ");
                            byte choice = Byte.parseByte(bufferedReader.readLine());
                            switch (choice) {
                                //TODO
                                case 1 -> changeUName(name, usersList);
                                case 2 -> System.out.println();
                                case 3 -> System.out.println();
                                case 4 -> System.out.println();
                            }
                            break;
                        case "pm":
                            printWriter.println("Send to: ");
                            recipient = bufferedReader.readLine();
                            messagePrompt(name);
                            message = bufferedReader.readLine();
                            for (Map.Entry<ClientHandler, User> hash : Server.loggedInUserHashMap.entrySet()) {
                                if (hash.getValue().name().equals(recipient)) {
                                    for (ClientHandler loginHandler : Server.loginHandlerArraylist) {
                                        if (loginHandler.socket.equals(hash.getKey().socket)) {
                                            loginHandler.sendMessage(name + ": " + message);
                                            break;
                                        }
                                    }
                                    break;
                                }  else
                                    sendMessage("User not existing");

                            }

                            break;
                        case "create":
                            GroupChatClientHandler gcClientHandler = new GroupChatClientHandler(socket, printWriter, bufferedReader);
                            gcClientHandler.start();
                            break;
                        case "help":
                            showCommands();
                            break;
                        case "quit":
                            broadcast(name + " has left the chat.");
                            exit = true;
                            break;

                        case "ban":
                            //TODO
                            System.out.println();
                            break;
                        default:
                            printWriter.println("command not recognized. input '/help' for a list of commands");
                    }
                } else {
                    broadcast(name + ": " + message);
                }
            }


        } catch (SocketException socketException) {
            throw new SocketException("Socket closed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {

            socket.close();
        }
    }
    private void messagePrompt(String name) {
        printWriter.println("\n\n" + name + ": ");
    }

    private void showCommands() {
        printWriter.println("\n\n");
        printWriter.println("COMMANDS");
        printWriter.println("/help");
        printWriter.println("/edit");
        printWriter.println("/privatemessage");
        printWriter.println("\n\n");
    }

    private void showEditMenu() {
        printWriter.println("\n\n");
        printWriter.println("[1] Change username");
        printWriter.println("[2] Change name");
        printWriter.println("[3] Change age");
        printWriter.println("[4] Change password");
        printWriter.println("\n\n");
    }

    public void editName(String name, String newName) {

    }
    public void broadcast(String message ){
        for (ClientHandler loginHandler : Server.loginHandlerArraylist) {
            if (loginHandler != null && loginStatus) {
                loginHandler.sendMessage(message);

            }
        }
    }

    public void sendMessage ( String message){
        printWriter.println(message);
    }

    public void changeUName(String name, NodeList usersList) {
        File xmlFile = new File("res/users.xml");
        Document document;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(xmlFile);
            NodeList users;

            users = document.getElementsByTagName("User");

            printWriter.println("Enter new username: ");
            String nameToChangeTo = bufferedReader.readLine();
            for (int i = 0; i < usersList.getLength(); i++) {
                Element u = (Element) users.item(i);
                String nameNode = u.getElementsByTagName("name").item(0).getTextContent();
                printWriter.println(nameNode);
                if (nameNode.equals(name)) {
                    u.getElementsByTagName("name").item(0).setTextContent(nameToChangeTo);
                    Server.updateXML(users, document);
                    printWriter.println("User " + nameNode + " has changed name to " + nameToChangeTo);
                    break;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void userValidation(String name, NodeList users) {

        while (!loginStatus) {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(Server.f);

                String username, password;
                users = document.getElementsByTagName("User");
                Element u = null;

                printWriter.println("\nUsername: ");
                username = bufferedReader.readLine();

                for (int i = 0; i < users.getLength(); i++) {
                    u = (Element) users.item(i);
                    String uName = u.getElementsByTagName("Username").item(0).getTextContent();

                    if (uName.equals(username)) {
                        for (int j = 0; j < users.getLength(); j++) {
                            printWriter.println("\nPassword: ");
                            password = bufferedReader.readLine();
                            String pass = u.getElementsByTagName("Password").item(0).getTextContent();
                            String nameNode = u.getElementsByTagName("name").item(0).getTextContent();

                            if (pass.equals(password)) {
                                name = nameNode;
                                loginStatus = true;

                                if (u.getElementsByTagName("BanStatus").item(0).getTextContent().equalsIgnoreCase("Banned")) {
                                    printWriter.println("Sorry. Your account is currently banned from the system.");
                                    break;
                                }


                                    Server.loginHandlerArraylist.add(new ClientHandler(socket, printWriter, bufferedReader));
                                User user = new User(u.getAttribute("User"), u.getElementsByTagName("name").item(0).getTextContent(), u.getElementsByTagName("Age").item(0).getTextContent(),
                                        u.getElementsByTagName("Username").item(0).getTextContent(),
                                        u.getElementsByTagName("Password").item(0).getTextContent(), u.getElementsByTagName("status").item(0).getTextContent(), u.getElementsByTagName("BanStatus").item(0).getTextContent());

                                // IP, USER HASHMAP
                                Server.loggedInUserHashMap.put(new ClientHandler(socket, printWriter, bufferedReader), user);

                                u.getElementsByTagName("status").item(0).setTextContent("online");
                                Server.updateXML(users, document);


                                System.out.println("Login Successful!");

                                System.out.println(u.getElementsByTagName("name").item(0).getTextContent() + " " +  u.getElementsByTagName("status").item(0).getTextContent());

                                joinServer(name, users);
                                broadcast(name + ": ");
                                break;
                            }
                            printWriter.println("Invalid password.");
                        }
                        break;
                    } else if (i == users.getLength() - 1)
                        printWriter.println("User is not existing");
                }
            } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
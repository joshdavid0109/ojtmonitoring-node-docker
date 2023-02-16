import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Server {
    static String f = "res/users.xml";
    static Socket clientSocket;
    static PrintWriter printWriter;
    static BufferedReader bufferedReader;
    static ArrayList<LoginHandler> loginHandlerArraylist = new ArrayList<>();
    static List<User> registeredUsersList = new ArrayList<>();
    static HashMap<String, User> loggedInUserHashMap = new HashMap<>();
    static Scanner scanner = new Scanner(System.in);

    public void run() throws IOException, SAXException, ParserConfigurationException {


            System.out.println("Ban or unban a user - /ban or /unban + [name]\n");
            System.out.println("Add a user - /add");
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            printWriter = new PrintWriter(System.out);

            new Thread(() -> {
                String input;
                input = scanner.nextLine();

                if (input.startsWith("/ban") || input.startsWith("/unban")) {
                    banUser(input.split(" ")[0], input.split(" ")[1]);
                } else if (input.startsWith("/add")) {
                    RegClientHandler regClientHandler = new RegClientHandler(printWriter, bufferedReader);
                    regClientHandler.start();
                }
            }).start();

            while (true) {
            try (ServerSocket serverSocket = new ServerSocket(8888)) {
                clientSocket = serverSocket.accept();

                bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                printWriter = new PrintWriter(clientSocket.getOutputStream(), true);


                new Thread(() -> {
                    getRegisteredUsers();
                    System.out.println("A client has connected.");
                    printWriter.println("Login");
                    Thread login = new LoginHandler(clientSocket, printWriter, bufferedReader);
                    login.start();

                }).start();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }


             // set status of all users to offline working yung code pero di ko sure san dapat nakalagay
            /*finally {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(f);

                NodeList users = document.getElementsByTagName("User");

                for (int i = 0; i < users.getLength(); i++) {
                    Element element = (Element) users.item(i);

                    element.getElementsByTagName("status").item(0).setTextContent("offline");

                    Server.updateXML(users, document);

                }
            }*/
        }
    }

    private void banUser(String command, String name) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(f);

            NodeList users = document.getElementsByTagName("User");
            Element element = null;

            if (command.equals("/ban")) {
                for (int i = 0; i < users.getLength(); i++) {
                    element = (Element) users.item(i);
                    if (element.getElementsByTagName("name").item(0).getTextContent().equals(name)) {
                        element.getElementsByTagName("BanStatus").item(0).setTextContent("Banned");
                        element.getElementsByTagName("status").item(0).setTextContent("online");
                        System.out.println(name + " is banned.\n");
                        break;
                    }
                }
            } else if (command.equals("/unban")) {
                for (int i = 0; i < users.getLength(); i++) {
                    element = (Element) users.item(i);
                    if (element.getElementsByTagName("name").item(0).getTextContent().equals(name)) {
                        element.getElementsByTagName("BanStatus").item(0).setTextContent(" ");
                        System.out.println(name + " is unbanned\n");
                        break;
                    }
                }
            }

           updateXML(users, document);



        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateXML(NodeList users, Document document) {
        users = document.getElementsByTagName("Users");
        Element element = (Element) users.item(0);
        XMLParse.trimWhiteSpace(element);

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(f));
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.run();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }


    }

    private void getRegisteredUsers() {
        String id, name, age, username, password;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(f);

            NodeList users = document.getElementsByTagName("User");

            for (int i = 0; i < users.getLength(); i++) {
                Element element = (Element) users.item(i);
                id = element.getAttribute("User");
                name = element.getElementsByTagName("name").item(0).getTextContent();
                age = element.getElementsByTagName("Age").item(0).getTextContent();
                username = element.getElementsByTagName("Username").item(0).getTextContent();
                password = element.getElementsByTagName("Password").item(0).getTextContent();

                registeredUsersList.add(new User(id, name, age, username, password));

            }



        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }


    }
}

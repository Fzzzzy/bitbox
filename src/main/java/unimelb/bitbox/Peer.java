package unimelb.bitbox;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

// take care of each listening event
class Connection {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    // private static AtomicInteger numOfConnections;
    private static JSONArray ConnectedPeers;
    // private String hostname;
    // private int port;
    // private String command;

    public Connection(Socket aClientSocket) {

        try {
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            // numOfConnections.incrementAndGet();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    public void socketClose() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            // 把当前链接从管理链接中移除
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String read() {

        String command = null;
        try { // an echo server
            System.out.println("server reading data");
            String data = in.readUTF(); // read a line of data from the stream
            JSONObject json = (JSONObject) new JSONParser().parse(data);
            command = (String) json.get("command");
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // finally{
        // try {
        // clientSocket.close();
        // }catch (IOException e){/*close failed*/}
        // }
        return command;
    }

    public void send(String command) {

        try {
            out.writeUTF(command);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // 触发把当前链接从管理链接中移除
            System.out.println("消息回复失败");
        } finally {
            this.socketClose();
        }

    }

}

// basic functions: listening and sending, need to be included as a new thread;
// implement thread pool to deal with each incoming connection

// 1, only 10 threads, when 11th come, handshake, how to refuse;
class Server implements Runnable {
    // ExecutorService executor = Executors.newFixedThreadPool(10);
    private int port;
    private int numOfConnections = 0;
    public static ArrayList<Connection> connectionList = new ArrayList<>();
    public int maximumConnections = 0;
    public boolean isRunning = false;

    // private ArrayList<String> listeningPeers = new ArrayList<>();
    public Server(int port, int MaximumConnections) {
        this.port = port;
        this.maximumConnections = MaximumConnections;
        this.isRunning = true;
        // ConnectionManager connectionManager = new ConnectionManager(this);
        // Thread connectionManagerThread = new Thread(connectionManager);
        // connectionManagerThread.start();
    }

    public int getConnectionNum() {
        return connectionList.size();
    }
    // public synchronized void AddConnection()
    // {
    // numOfConnections++;
    // }
    // public synchronized void ReduceConnection()
    // {
    // numOfConnections--;
    //
    // }

    public void run() {
        try {

            ServerSocket serverSocket = new ServerSocket(port);
            // isRunning = true;

            while (this.getConnectionNum() <= maximumConnections) {
                System.out.println("Server listening for a connection");
                Socket clientSocket = serverSocket.accept();
                // this.numOfConnections++;

                // using submit rather than execute, cause it can allow us operate the returning
                // future object
                Connection c = new Connection(clientSocket);
                connectionList.add(c);
                System.out.println(connectionList.size());
                // get the numOfConnection value and update the outside numof Connection

                // this.executor.submit(c);

            }
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }
}

class ConnectionManager implements Runnable {
    ArrayList<Connection> connectionList;
    int maximumConnections;
    public JSONArray ConnectedPeers;
    public Server server;

    public ConnectionManager(Server server) {
        this.connectionList = server.connectionList;
        this.maximumConnections = server.maximumConnections;
        this.server = server;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.out.println("connectionManager is started");
        // 4.16 1. bug fixed when the list is empty, no need to run analysis
        while (server.isRunning && !connectionList.isEmpty()) {
            // System.out.println(connectionList.size());
            for (Connection connection : connectionList) {
                String command = connection.read();
                commandAnalysis(command, connection);
                removeConnection(connection);
                System.out.println(connection.clientSocket.getRemoteSocketAddress());
                System.out.println(connection.clientSocket.getPort());
            }
        }

    }

    public void removeConnection(Connection connection) {
        connectionList.remove(connection);
    }

    public void commandAnalysis(String command, Connection connection) {

        switch (command) {
        case "HANDSHAKE_REQUEST": {
            if (connectionList.size() <= maximumConnections) {
                JSONObject commandJson = CommandGroup.CommandMap.get("HANDSHAKE_RESPONSE");
                JSONObject PeerJson = (JSONObject) commandJson.get("hostport");
                // unnecessary handshake
                if (ConnectedPeers.contains(PeerJson)) {
                    commandJson = CommandGroup.CommandMap.get("INVALID_PROTOCOL");
                } else {
                    ConnectedPeers.add(PeerJson);
                    // numOfConnections.incrementAndGet();
                }
                // out.writeUTF(commandJson.toString());
                connection.send(commandJson.toString());
            } else if (connectionList.size() == maximumConnections) {
                JSONObject commandJson = CommandGroup.CommandMap.get("CONNECTION_REFUSED");
                // out.writeUTF(commandJson.toString());
                connection.send(commandJson.toString());
            }
            break;
        }
        //
        // case "FILE_CREATE_REQUEST":
        // {
        // // issafepathname filenameexist -> check the file managerment system
        // // respond, failed-> status. other message-> return the as the methods
        // returns
        //
        // //success: createFileLoader, checkshortcut
        // // stop, use the local copy or start requesting bytes
        //
        // break;
        // }
        //
        //
        // case "FILE_BYTES_RESPONSE":
        // {
        // // writefile check filecomplete.. untill-> complete
        // break;
        // }
        //
        // case "FILE_DELETE_REQUEST":
        // {
        // // // issafepathname filenameexist -> check the file managerment system
        // // // respond, failed-> status. other message-> return the as the methods
        // returns
        // break;
        // }
        // case "FILE_MODIFY_REQUEST":
        // {
        // // issafepathname filenameexist -> check the file managerment system
        // // respond, failed-> status. other message-> return the as the methods
        // returns -> response
        // break;
        // }
        //
        //
        // case "DIRECTORY_CREATE_REQUEST":
        // {
        // // issafepathname filenameexist -> check the file managerment system
        // // respond, failed-> status. other message-> return the as the methods
        // returns
        // break;
        // }
        //
        // case "DIRECTORY_DELETE_REQUEST":
        // {
        // break;
        // }
        //

        default: {
            JSONObject commandJson = CommandGroup.CommandMap.get("INVALID_PROTOCOL");
            connection.send(commandJson.toString());
        }
        }
    }
}

// sending thread, needs to implement all the different commands
class Client implements Runnable {
    private String hostname;
    private int port;
    private String command;
    public String[] peers = Configuration.getConfigurationValue("peers").split(",");;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

    }

    public void send(String serverIP, int serverPort, String command) {
        Socket s = null;
        try {
            s = new Socket(hostname, port);
            System.out.println("Connection Established");
            System.out.println("Sending command");
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream o = new DataOutputStream(s.getOutputStream());
            // PrintWriter out= new PrintWriter(s.getOutputStream(), true );
            // System.out.println(command);
            // JSONObject json = (JSONObject) new JSONParser().parse(command);
            o.writeUTF(command);
            // String r = in.readUTF();
            // System.out.println(r);

        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        }
        // catch (ParseException e) {
        // e.printStackTrace();
        // }
        finally {
            if (s != null)
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
        }
    }

    public void run() {

        for (int i = 0; i < peers.length; i++) {
            String serverIP = peers[i].split(":")[0];
            int serverPort = Integer.parseInt(peers[i].split(":")[1]);

            String handshake = "{\"hostport\":{\"port\":" + port + ",\"host\":" + hostname
                    + "},\"command\":\"handshake\"}";
            send(serverIP, serverPort, handshake);
            System.out.println(handshake);
        }
    }

}

public class Peer {
    private String address;
    private int numConnection;
    private int portNo;
    private String[] peers;

    private ArrayList<String> IncomingPeers = new ArrayList<String>();
    private ArrayList<String> OutGoingPeers = new ArrayList<String>();

    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static final int maximumConnections = Integer
            .parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
    private static final int synxInterval = Integer.parseInt(Configuration.getConfigurationValue("syncInterval"));

    private ArrayList<Peer> AvailablePeers = new ArrayList<Peer>();

    // getter for all necessary attributes of the peer.

    public String getAddress() {
        return address;
    }

    public int getNumConnection() {
        return numConnection;
    }

    public int getPortNo() {
        return portNo;
    }

    public int getMaximumConnections() {
        return maximumConnections;
    }

    public int getSynxInterval() {
        return synxInterval;
    }

    // setter for NumConnection and incomingPeers

    public void setNumConnection(int numConnection) {
        this.numConnection = numConnection;
    }

    public void setIncomingPeers(ArrayList<String> incomingPeers) {
        IncomingPeers = incomingPeers;
    }

    // Construction function
    public Peer() {
        // 可以封装为方法loadconfig()

        this.address = Configuration.getConfigurationValue("advertisedName");
        // int maximumConnections =
        // Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
        this.portNo = Integer.parseInt(Configuration.getConfigurationValue("port"));
        // this.peers = Configuration.getConfigurationValue("peers").split(",");
        // System.out.println(peers[0]);
        CommandGroup commandGroup = new CommandGroup();
    }

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException {

        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();
        // System.out.println("st1");
        /*
         * to test the peer to peer communication, the following line needs to be
         * commented.
         */
        // new ServerMain();

        Peer peer1 = new Peer();
        Server listen = new Server(peer1.portNo, peer1.maximumConnections);
        Thread listenThread = new Thread(listen);
        listenThread.start();

        ConnectionManager connectionManager = new ConnectionManager(listen);
        Thread connectionManagerThread = new Thread(connectionManager);
        connectionManagerThread.start();
        Client send = new Client(peer1.address, peer1.portNo);
        Thread sendThread = new Thread(send);
        sendThread.start();
        /* listening testing */
        // peer1.listen();
        /* sending testing */
        // peer1.sendCommand(8112, "localhost", "saved for different command");

    }
}

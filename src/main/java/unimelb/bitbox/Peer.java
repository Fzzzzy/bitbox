package unimelb.bitbox;
<<<<<<< HEAD
import unimelb.bitbox.util.Configuration;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;
=======
>>>>>>> upstream/Cassie

import org.json.simple.JSONArray;

<<<<<<< HEAD
public class Peer
{
    private String address;
    private int portNo;

    public String[] getPeers() {
        return peers;
    }

    private String[] peers;



    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static final int maximumConnections= Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
    private static final int synxInterval = Integer.parseInt(Configuration.getConfigurationValue("syncInterval"));



// getter for all necessary attributes of the peer.


    public int getPortNo() {
        return portNo;
    }

    public int getMaximumConnections() {
        return maximumConnections;
    }

    public int getSynxInterval() {
        return synxInterval;
=======
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;


// take care of each listening event
class Connection {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    // private static AtomicInteger numOfConnections;
    //private static JSONArray ConnectedPeers;
    // private String hostname;
    // private int port;
    // private String command;
    public int getPort() {
 
    	return this.clientSocket.getLocalPort();
    }
    //return the local
    public String getHost() {
    	 
    	return this.clientSocket.getLocalAddress().getHostAddress();
    }
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

    public JSONObject read() {

        String command = null;
        JSONObject jsonObject = null;
        try { 
            String data = in.readUTF(); // read a line of data from the stream
            System.out.println("server reading data hahahhaah: " + data);
            // JSONObject json = (JSONObject) new JSONParser().parse(data);
            jsonObject = JSONObject.fromObject(data); 
            command = (String) jsonObject.get("command");
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        }
        
        	return jsonObject;
    }

    public void send(String command) {

        try {
            out.writeUTF(command);
            System.out.println("send: "+ command);
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
    //public static ArrayList<Connection> connectionList = new ArrayList<>();
   // public static Queue<Connection> connectionQueue = new LinkedList<Connection>();
    public static ConcurrentLinkedDeque<Connection> connectionQueue = new ConcurrentLinkedDeque<Connection>();;
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
        return connectionQueue.size();
    }

    public void run() {
        try {

            ServerSocket serverSocket = new ServerSocket(port);
            // isRunning = true;

            while (this.getConnectionNum() <= maximumConnections) {
                System.out.println("Server listening for a connection");
                Socket clientSocket = serverSocket.accept();
              
                // using submit rather than execute, cause it can allow us operate the returning
                // future object
                Connection c = new Connection(clientSocket);
                Server.connectionQueue.addLast(c);;
                System.out.println("Server Connection Add: "+ Server.connectionQueue.size());
                System.out.println(c.hashCode());
                
                // get the numOfConnection value and update the outside numof Connection
                // this.executor.submit(c);

            }
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }
}

class ConnectionManager implements Runnable {
    //ArrayList<Connection> connectionList;
    int maximumConnections;
   // public JSONArray ConnectedPeers = new JSONArray();
    public Server server;
	//private boolean isInstance = false;
	public static ConnectionManager single = null;
   // public Connection connection;

    public ConnectionManager(Server server) {
    	//isInstance = true;
       // this.connectionList = server.connectionList;
        this.maximumConnections = server.maximumConnections;
        this.server = server;
    }
    public ConnectionManager(){
    	
    }
    public static ConnectionManager getInstance() {
        if (single == null) {  
            single = new ConnectionManager();
        }  
       return single;
   }
    @Override
    public void run() {
    	Connection connection = null;
    	 
        // TODO Auto-generated method stub
        System.out.println("connectionManager is started");
        while (server.isRunning) {
        	if (Server.connectionQueue.size() > 0)
        	{
        		System.out.println("Server connection read: "+ Server.connectionQueue.size());
        	}
        	
            while(Server.connectionQueue.size() > 0){
            	    connection = connectionPoll();
            	   // command = (String) jsonObject.get("command");
            	   // String command = (String)connection.read().get("command");
            	    commandAnalysis(connection);
                 
                    System.out.println(connection.clientSocket.getRemoteSocketAddress());
                    System.out.println(connection.clientSocket.getPort());
                  }

        }

    }

    public Connection connectionPoll() {
    	     	return Server.connectionQueue.removeFirst();  
    }

    public void commandAnalysis(Connection connection) {
    	JSONObject jsonObject = connection.read();
    	System.out.println("Ahhhhhh"+jsonObject);
        switch ((String) jsonObject.get("command")) { 
        case "HANDSHAKE_REQUEST": {
            if (Server.connectionQueue.size() <= maximumConnections) {
            	
              //  JSONObject commandJson = CommandGroup.CommandMap.get("HANDSHAKE_RESPONSE");
            	 String response =  "{\"command\":\"HANDSHAKE_RESPONSE\"," + "\"hostPort\":{" +
            			 			"\"host\":\"" + connection.getHost() + "\"," +
            			 			"\"port\":" + connection.getPort() + "}}";
                
            	 JSONObject PeerJson = (JSONObject) jsonObject.get("hostPort");
               
                if ( Peer.availablePeers.containsKey(PeerJson.toString())) {
                    //commandJson = CommandGroup.CommandMap.get("INVALID_PROTOCOL");
                	response  =  "{\"command\":\"INVALID_PROTOCOL\",\"message\":\"message must contain a command field as string\"}";

                } else {
                	 Peer.availablePeers.put(PeerJson.toString(), PeerJson); 
                }
                System.out.println("Because of the handshake request, availablePeers is identified" + response);
                System.out.println("available peer in peer1: "+ Peer.availablePeers.toString());
               
                connection.send(response);
            } else if (Server.connectionQueue.size() == maximumConnections) {
                //  JSONObject commandJson = CommandGroup.CommandMap.get("CONNECTION_REFUSED");
                // out.writeUTF(commandJson.toString());
            	
            	String refuse =  "{\"command\":\"CONNECTION_REFUSED\",\"message\":\"connection limit reached\",\"peers\":{\"host\":\"sunrise.cis.unimelb.edu.au\",\"port\":8111}}";
                connection.send(refuse);
            }
            break;
        }
        case "HANDSHAKE_RESPONSE":{
        	//JSONObject commandJson = connection.read();
        	//  System.out.println(commandJson.toString());
            JSONObject PeerJson = (JSONObject)jsonObject.get("hostPort");
           // System.out.println("leilei"+ jsonObject.get("hostPort"));
          
            if(!Peer.availablePeers.containsKey(PeerJson)){
            		
            	Peer.availablePeers.put(PeerJson.toString(),PeerJson);
                	System.out.println("Because of the handshake response, availablePeers is identified" + PeerJson.toString());
            }
            else {
            	System.out.println("it is already an existed available peer");

            }
            System.out.println("available peer in peer2: "+ Peer.availablePeers.toString());
            break;
        }
        case "INVALID_PROTOCOL":{
        	JSONObject commandJson = connection.read();
        	System.out.println();
        	break;
        }
        case "CONNECTION_REFUSED":{
        	break;
        }
        default: {
           // JSONObject commandJson = CommandGroup.CommandMap.get("INVALID_PROTOCOL");
        	String invaild = "{\"command\":\"INVALID_PROTOCOL\",\"message\":\"message must contain a command field as string\"}";
            connection.send(invaild);
        }
        }
>>>>>>> upstream/Cassie
    }
}

<<<<<<< HEAD
    // setter for NumConnection and incomingPeers








    // Construction function
    public Peer() {
        address=  Configuration.getConfigurationValue("advertisedName");
        portNo = Integer.parseInt(Configuration.getConfigurationValue("port"));
        peers = Configuration.getConfigurationValue("peers").split(",");
=======
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
            s = new Socket(serverIP, serverPort);
            System.out.println("Connection Established: " + serverIP + serverPort);
            System.out.println("Sending command: " + command);
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream o = new DataOutputStream(s.getOutputStream());
            // PrintWriter out= new PrintWriter(s.getOutputStream(), true );
            // System.out.println(command);
            // JSONObject json = (JSONObject) new JSONParser().parse(command);
            
            o.writeUTF(command);
            
            Connection connection = new Connection(s);
          //  ConnectionManager connectionManager = new ConnectionManager();
            ConnectionManager.getInstance().commandAnalysis(connection);
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
>>>>>>> upstream/Cassie
    }
    
    public void run() {

        for (int i = 0; i < peers.length; i++) {
            String serverIP = peers[i].split(":")[0];
            int serverPort = Integer.parseInt(peers[i].split(":")[1]);

            String handshake = "{\"hostPort\":{\"port\":" + port + ",\"host\":\"" + hostname 
            		+ "\""
                    + "},\"command\":\"HANDSHAKE_REQUEST\"}";
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

<<<<<<< HEAD


       Peer peer1= new Peer();
       ConnectionHost host = new ConnectionHost(peer1);
       Thread HostThread = new Thread(host);
       HostThread.start();










=======
    private ArrayList<String> IncomingPeers = new ArrayList<String>();
    private ArrayList<String> OutGoingPeers = new ArrayList<String>();

    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static final int maximumConnections = Integer
            .parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
    private static final int synxInterval = Integer.parseInt(Configuration.getConfigurationValue("syncInterval"));
>>>>>>> upstream/Cassie

    public static Map<String, JSONObject> availablePeers = new HashMap<>();

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
    //    CommandGroup commandGroup = new CommandGroup();
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
       // listenThread.setDaemon(true);
        listenThread.start();
       
        ConnectionManager connectionManager = new ConnectionManager(listen);
        Thread connectionManagerThread = new Thread(connectionManager);
        //connectionManagerThread.start();
        
        Client send = new Client(peer1.address, peer1.portNo);
        Thread sendThread = new Thread(send);
       // sendThread.setDaemon(true);
        sendThread.start();
//        try {
//			Thread.sleep(20000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        connectionManagerThread.start();
        /* listening testing */
        // peer1.listen();
        /* sending testing */
        // peer1.sendCommand(8112, "localhost", "saved for different command");

    }
}

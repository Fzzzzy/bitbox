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
class Connection implements Runnable {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    private static AtomicInteger numOfConnections;
    private static JSONArray ConnectedPeers;

    public int getNumOfConnections() {
        return numOfConnections.get() ;
    }

    public Connection (Socket aClientSocket) {

        try {
            clientSocket = aClientSocket;
            in = new DataInputStream( clientSocket.getInputStream());
            out =new DataOutputStream( clientSocket.getOutputStream());
            numOfConnections.incrementAndGet();
        } catch(IOException e) {
            System.out.println("Connection:"+e.getMessage());
        }
}
    public void run(){
        try {           // an echo server
            System.out.println("server reading data");
            String data = in.readUTF();  // read a line of data from the stream
            JSONObject json = (JSONObject) new JSONParser().parse(data);
            String command = (String) json.get("command");


            // JSONObject json = (JSONObject) new JSONParser().parse(handshake);
            //            o.writeUTF(json.toString());

              switch(command)
            {
                case "HANDSHAKE_REQUEST": {
                    if (numOfConnections.get()<11)
                    {
                        JSONObject commandJson = CommandGroup.CommandMap.get("HANDSHAKE_RESPONSE");
                        JSONObject PeerJson = (JSONObject) commandJson.get("hostport");
                        // unnecessary handshake
                        if(ConnectedPeers.contains(PeerJson))
                        {
                            commandJson = CommandGroup.CommandMap.get("INVALID_PROTOCOL");
                        }
                        else {
                            ConnectedPeers.add(PeerJson);
                            numOfConnections.incrementAndGet();
                        }
                        out.writeUTF(commandJson.toString());
                    }
                    else if(numOfConnections.get()==11)
                    {
                        JSONObject commandJson = CommandGroup.CommandMap.get("CONNECTION_REFUSED");
                        out.writeUTF(commandJson.toString());
                        try {
                            clientSocket.close();
                        }catch (IOException e){ }

                    }
                    break;
                }

                case "FILE_CREATE_REQUEST":
                {
                    // issafepathname      filenameexist     -> check the file managerment system
                    // respond, failed-> status.  other message-> return the as the methods returns

                    //success:  createFileLoader, checkshortcut
                    // stop, use the local copy or start requesting bytes

                    break;
                }


                case "FILE_BYTES_RESPONSE":
                {
                    // writefile   check filecomplete..     untill-> complete
                    break;
                }

                case "FILE_DELETE_REQUEST":
                {
                    // // issafepathname      filenameexist     -> check the file managerment system
                    //                    // respond, failed-> status.  other message-> return the as the methods returns
                    break;
                }
                case "FILE_MODIFY_REQUEST":
                {
                    // issafepathname      filenameexist     -> check the file managerment system
                    // respond, failed-> status.  other message-> return the as the methods returns -> response
                    break;
                }


                case "DIRECTORY_CREATE_REQUEST":
                {
                    // issafepathname      filenameexist     -> check the file managerment system
                    // respond, failed-> status.  other message-> return the as the methods returns
                    break;
                }

                case "DIRECTORY_DELETE_REQUEST":
                {
                    break;
                }


                default:
                {
                   JSONObject commandJson = CommandGroup.CommandMap.get("INVALID_PROTOCOL");
                    out.writeUTF(commandJson.toString());
                }
            }
            // timeout expression:  and then reduce the number and delete the peer

        }catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        } catch(IOException e) {
            System.out.println("readline:"+e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        } finally{
            try {
                clientSocket.close();
            }catch (IOException e){/*close failed*/}
        }
    }
}

// basic functions: listening and sending, need to be included as a new thread;
// implement thread pool to deal with each incoming connection

// 1, only 10 threads, when 11th come, handshake, how to refuse;
class Listen implements Runnable
{
    ExecutorService executor = Executors.newFixedThreadPool(10);
    private int hostingPort;
    private int numOfConnections=0;

    private ArrayList<String> listeningPeers = new ArrayList<>();
    public Listen (Peer peer)
    {
        hostingPort= peer.getPortNo();
    }
    public int getNumOfConnections() {
        return numOfConnections;

    }
    public synchronized void AddConnection()
    {
        numOfConnections++;
    }
    public synchronized void ReduceConnection()
    {
        numOfConnections--;

    }
    public void run ()
    {
        try {
            ServerSocket serverSocket = new ServerSocket(hostingPort);
            while (numOfConnections<11) {
                 System.out.println("Server listening for a connection");
                Socket clientSocket = serverSocket.accept();
                this.numOfConnections++;
                // using submit rather than execute, cause it can allow us operate the returning future object
                Connection c = new Connection(clientSocket);
                //get the numOfConnection value and update the outside numof Connection

                this.executor.submit(c);
            }
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }
}


// sending thread, needs to implement all the different commands
class Sending implements Runnable
{
    private String hostname;
    private int port;
    private String command;

    public Sending(String h, int p, String c)
    {
        hostname=h;
        port=p;
        command = c;
    }

    public void run() {
        Socket s = null;
        String handshake = "{\"hostport\":{\"port\":8111,\"host\":\"localhost\"},\"command\":\"handshake\"}";
        try {
            s = new Socket(hostname, port);
            System.out.println("Connection Established");
            System.out.println("Sending command");
            DataOutputStream o = new DataOutputStream(s.getOutputStream());
            //     PrintWriter out= new PrintWriter(s.getOutputStream(), true );

            JSONObject json = (JSONObject) new JSONParser().parse(handshake);
            o.writeUTF(json.toString());

        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (s != null) try {
                s.close();
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            }
        }
    }
}



public class Peer
{
    private String address;
    private int numConnection;
    private int portNo;
    private String[] peers;


    private ArrayList<String>  IncomingPeers= new ArrayList<String>();
    private ArrayList<String>  OutGoingPeers = new ArrayList<String>();

    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static final int maximumConnections= Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
    private static final int synxInterval = Integer.parseInt(Configuration.getConfigurationValue("syncInterval"));



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
        address=  Configuration.getConfigurationValue("advertisedName");
        numConnection = 0;
        portNo = Integer.parseInt(Configuration.getConfigurationValue("port"));
        peers = Configuration.getConfigurationValue("peers").split(",");
        CommandGroup commandGroup = new CommandGroup();
    }

    public static void main(String[] args ) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {

        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        /* to test the peer to peer communication, the following line needs to be commented. */
        //  new ServerMain();

       Peer peer1= new Peer();
   Listen listen = new Listen(peer1);
     Thread listenThread = new Thread(listen);
     listenThread.start();

        /*  listening testing    */
       // peer1.listen();
       /* sending testing */
       //peer1.sendCommand(8112, "localhost", "saved for different command");

    }
}



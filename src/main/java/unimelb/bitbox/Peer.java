package unimelb.bitbox;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


import com.oracle.javafx.jmx.json.JSONDocument;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;


// take care of each listening event
class Connection extends Thread {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    public Connection (Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            in = new DataInputStream( clientSocket.getInputStream());
            out =new DataOutputStream( clientSocket.getOutputStream());
            this.start();
        } catch(IOException e) {
            System.out.println("Connection:"+e.getMessage());
        }
    }
    public void run(){
        try {           // an echo server
            System.out.println("server reading data");
            String data = in.readUTF();  // read a line of data from the stream
            System.out.println("server writing data");
            out.writeUTF(data);
        }catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        } catch(IOException e) {
            System.out.println("readline:"+e.getMessage());
        } finally{
            try {
                clientSocket.close();
            }catch (IOException e){/*close failed*/}
        }
    }
}



public class Peer 
{
    private String address;
    private int numConnection;
    private int portNo;
    private String[] peers;
	private static Logger log = Logger.getLogger(Peer.class.getName());
	private int maximumConnections;
	private int synxInterval;
	private ServerSocket serverSocket;


	public void send(int port, String hostname, String Message)
    {
        Socket s = null;
        try{
            s = new Socket(hostname, port);
            System.out.println("Connection Established");

            DataOutputStream out =new DataOutputStream( s.getOutputStream());
            System.out.println("Sending data");
            out.writeUTF(Message);     // UTF is a string encoding see Sn. 4.4


        }catch (UnknownHostException e) {
            System.out.println("Socket:"+e.getMessage());
        }catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        }catch (IOException e){
            System.out.println("readline:"+e.getMessage());
        }finally {
            if(s!=null) try {
                s.close();
            }catch (IOException e){
                System.out.println("close:"+e.getMessage());
            }
        }
    }

    public void sendCommand(int port, String hostname)

    {
        Socket s = null;
        String handshake="{\"hostport\":{\"port\":8111,\"host\":\"localhost\"},\"command\":\"handshake\"}";

        try{
            s = new Socket(hostname, port);
            System.out.println("Connection Established");
            System.out.println("Sending command");
            DataOutputStream o= new DataOutputStream( s.getOutputStream());
   //     PrintWriter out= new PrintWriter(s.getOutputStream(), true );

            JSONObject json = (JSONObject) new JSONParser().parse(handshake);
            o.writeUTF(json.toString());

        }catch (UnknownHostException e) {
            System.out.println("Socket:"+e.getMessage());
        }catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        }catch (IOException e){
            System.out.println("readline:"+e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if(s!=null) try {
                s.close();
            }catch (IOException e){
                System.out.println("close:"+e.getMessage());
            }
        }
    }


	// Construction function
	public void Peer () {
          address=  Configuration.getConfigurationValue("advertisedName");
          numConnection = 0;
          portNo = Integer.parseInt(Configuration.getConfigurationValue("port"));
          peers = Configuration.getConfigurationValue("peers").split(",");

           maximumConnections = Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
           synxInterval = Integer.parseInt(Configuration.getConfigurationValue("syncInterval"));

    }

    public void listen()
    {
        try {
            ServerSocket serverSocket = new ServerSocket(portNo);
            while(true)
            {
                System.out.println("Server listening for a connection");
                Socket clientSocket = serverSocket.accept();
                Connection c = new Connection(clientSocket);
            }
        }
        catch(IOException e)
        {
            System.out.println("Listen socket:"+e.getMessage());
        }
    }
    public static void main(String[] args ) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {

    	System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();
        // new ServerMain();
        Peer peer2 = new Peer();
    //  peer2.send(8112, "localhost", "rara");
peer2.sendCommand(8112,"localhost");
        
    }
}



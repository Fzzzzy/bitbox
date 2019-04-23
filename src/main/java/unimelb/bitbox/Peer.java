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

public class Peer {
    private String address;
    private int numConnection;
    private int portNo;

    public String[] getPeers() {
        return peers;
    }

    private String[] peers;

    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static final int maximumConnections = Integer
            .parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
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
    }

    // Construction function
    public Peer() {
        address = Configuration.getConfigurationValue("advertisedName");
        numConnection = 0;
        portNo = Integer.parseInt(Configuration.getConfigurationValue("port"));
        peers = Configuration.getConfigurationValue("peers").split(",");
    }

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException {

        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        /*
         * to test the peer to peer communication, the following line needs to be
         * commented.
         */

        Peer peer1 = new Peer();
        ConnectionHost host = new ConnectionHost(peer1);
        Thread HostThread = new Thread(host);
        HostThread.start();

        /* listening testing */
        // peer1.listen();

        /* sending testing */
        // peer1.sendCommand(8112, "localhost", "saved for different command");

    }
}

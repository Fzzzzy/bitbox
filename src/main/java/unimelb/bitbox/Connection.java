package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

// store and implement the basic functions for each connection
public class Connection implements Runnable {
    private BufferedReader inreader;
    private PrintWriter outwriter;
    Socket clientSocket;
    boolean flag = true;

    public Connection(Socket socket) {
        try {
            this.clientSocket = socket;
            this.outwriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
            this.inreader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    public void ConnectionClose() {
        try {
            flag = false;
            inreader.close();
            outwriter.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // only for Response or Request
    public void send(String command) throws IOException {
        outwriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
        JSONObject jObj = new JSONObject();
        CommandGroup commands = new CommandGroup();
        int port = clientSocket.getLocalPort();
        String address = clientSocket.getLocalAddress().toString().split("/", 2)[1];

        JSONObject HostingPeer = new JSONObject();
        HostingPeer.put("host", address);
        HostingPeer.put("port", port);
        jObj.put("command", command);

        if (command.equals("HANDSHAKE_REQUEST") || command.equals("HANDSHAKE_RESPONSE")) {
            jObj.put("hostPort", HostingPeer);
        }
        outwriter.println(jObj.toJSONString());
        outwriter.flush();
    }

    // send json files related to the file activity
    public void sendJson(JSONObject json) throws IOException {
        outwriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
        JSONObject jObj = json;
        outwriter.println(jObj.toJSONString());
        outwriter.flush();
    }


    @Override
    public void run() {
        String command;
        String data = null; // read a line of data from the stream
        JSONObject inComingPeer;
        while (flag) {
            try {
                data = inreader.readLine();
                if (data != null) {
                    System.out.println(data);
                    JSONObject json = new JSONObject();
                    json = (JSONObject) new JSONParser().parse(data);
                    inComingPeer = (JSONObject) json.get("hostPort");
                    command = json.get("command").toString();

                    switch (command) {
                    case "HANDSHAKE_REQUEST": {
                        System.out.println("handshake received from " + inComingPeer);
                        // unnecessary handshake
                        if (ConnectionHost.getConnectedPeers().contains(inComingPeer)) {
                            send("INVALID_PROTOCOL");
                            System.out.println("replicated request!");
                            if(ConnectionHost.ClientConnectionList.contains(ConnectionHost.getConnectionMap().get(inComingPeer)) )
                                this.ConnectionClose();
                        } else {
                            if (ConnectionHost.getConnectionNum() <= ConnectionHost.getMaximumConnections()) {
                                send("HANDSHAKE_RESPONSE");
                                System.out.println("Handshake response sent!");
                                ConnectionHost.AddServerConnectionList(this);
                                ConnectionHost.AddConnectedPeers(inComingPeer, this);
                            } else {
                                send("CONNECTION_REFUSED");
                                System.out.println("Handshake refused message sent");
                                this.ConnectionClose();
                            }
                        }
                        break;
                    }
                    case "HANDSHAKE_RESPONSE": {
                        ConnectionHost.AddConnectedPeers(inComingPeer, this);
                        ConnectionHost.AddClientConnectionList(this);
                        System.out.println("connection established.");

                        break;
                    }
                    case "INVALID_PROTOCOL": {
                        System.out.println("connection been refused by protocol problems.");
                        this.ConnectionClose();
                        break;
                    }

                    case "CONNECTION_REFUSED": {
                        System.out.println("connection been refused by incoming limit.");
                        this.ConnectionClose();
                        break;
                    }

                    case "FILE_CREATE_REQUEST": {
                        System.out.println("FILE_CREATE_REQUEST received.");
                        JSONObject response = ConnectionHost.fileOperator.fileCreateResponse(json);

                        // if the file loader is ready, ask for file bytes
                        if (response.get("message") == "file loader ready") {
                            JSONObject byteRequest = ConnectionHost.fileOperator.fileBytesRequest(response);
                            if (byteRequest.get("command") == null) {
                                System.out.println("file writing is finished.");
                                // this.ConnectionClose();
                            } else {
                                sendJson(byteRequest);
                                System.out.println("FILE_BYTES_REQUEST sended.");
                            }
                        }
                        break;
                    }

                    case "FILE_CREATE_RESPONSE": {
                        System.out.println(json.get("message").toString());
                        // if (json.get("status") == "false") {
                        // this.ConnectionClose();
                        // }
                        break;
                    }

                    case "FILE_BYTES_REQUEST": {
                        System.out.println("FILE_BYTES_REQUEST received.");
                        JSONObject response = ConnectionHost.fileOperator.fileBytesResponse(json);
                        sendJson(response);
                        System.out.println("FILE_BYTES_RESPONSE sended.");
                        break;
                    }

                    case "FILE_BYTES_RESPONSE": {
                        System.out.println("FILE_BYTES_RESPONSE received.");
                        if (json.get("status").toString() == "true") {
                            JSONObject byteRequest = ConnectionHost.fileOperator.fileBytesRequest(json);
                            if (byteRequest.get("command") == null) {
                                System.out.println("file writing is finished.");
                                // this.ConnectionClose();
                            } else {
                                sendJson(byteRequest);
                                System.out.println("FILE_BYTES_REQUEST sended.");
                            }
                        }
                        break;
                    }

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
                     case "DIRECTORY_CREATE_REQUEST":
                     {
                         System.out.println("DIRECTORY_CREATE_REQUEST received.");
                         JSONObject response = ConnectionHost.fileOperator.dirCreateResponse(json);
                         sendJson(response);
                         System.out.println("DIRECTORY_CREATE_RESPONSE sended");
                    }
                    case "DIRECTORY_CREATE_RESPONSE":
                        {
                            System.out.println(json.get("message").toString());
                            break;

                        }
                    //
                    // case "DIRECTORY_DELETE_REQUEST":
                    // {
                    // break;
                    // }
                    //

                    }
                    System.out.println("incoming connection num :" + ConnectionHost.ServerConnectionList.size());
                    System.out.println("outgoing connection num :" + ConnectionHost.ClientConnectionList.size());

                }

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                if (ConnectionHost.ServerConnectionList.contains(this))
                    ConnectionHost.ServerConnectionList.remove(this);
                else if (ConnectionHost.ClientConnectionList.contains(this))
                    ConnectionHost.ClientConnectionList.remove(this);
                ConnectionHost.RemoveMapByConnection(this);

                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}

package com.example.anthony.eddiebalancecontroler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by antho on 1/24/2017.
 */

public class DatagramHandler {

    public static final char UDP_COMMAND_PORT= 4242; //UDP Port for receiving command packets
    public static final char UDP_CONTROL_PORT = 4240; //UDP Port for receiving control packets
    public static final char  UDP_RESPOND_PORT =4243; //UDP Port for returning data to user
    private static final char MAXMESSAGESIZE =64;


    private byte[] message = new byte[MAXMESSAGESIZE];
    private DatagramSocket Socket;
    private DatagramPacket Packet;
    private int port;
    private InetAddress address;

    public DatagramHandler(String address, int port) throws UnknownHostException {
        this.address =InetAddress.getByName(address) ;
        this.port = port;
        try {
            Socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    public DatagramHandler(int udpListenPort) {
        try {
            Socket = new DatagramSocket(udpListenPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Packet= new DatagramPacket(message, message.length);
    }

    public void send(String data) {
        Packet = new DatagramPacket(data.getBytes(), data.length(), address, port);
        try {
            Socket.send(Packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        Socket.close();
    }

    public boolean hasData() throws IOException {
        Socket.receive(Packet);
        return (Packet.getLength() != 0);
    }

    public String receive() {
        String text = new String(message, 0, Packet.getLength());
        return text;
    }
    public String receiveIP() {
        return Packet.getAddress().getHostAddress();
    }

}


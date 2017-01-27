package com.example.anthony.eddiebalancecontroler.eddie;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.regex.Pattern;

import static android.content.Context.WIFI_SERVICE;
import static com.example.anthony.eddiebalancecontroler.MainActivity.print;

/**
 * Created by antho on 1/26/2017.
 */

public abstract class Eddie {

    private UDP_TxThread udpNetworkDiscoveryThread;
    private UDP_RxThread udplistenerThread;
    private UDPHandler udpNetworkDiscoveryHandler;
    boolean discovering =false;
    String IP = "";
    public boolean BIND =false;

    public double DriveSpeed = 50;
    public double TurnSpeed = 0.8;
    public double oldx=0,oldy=0;


    protected Eddie(AppCompatActivity activate ){
        String Brodcast = wifiIpAddress(activate);
        print("Raw IP: %s",Brodcast);
        String[] addressarray = Brodcast.split(Pattern.quote("."));

        Brodcast = String.format("%s.%s.%s.255",addressarray[0],addressarray[1],addressarray[2]);
        print("Brodcasting on: %s",Brodcast);
        udpNetworkDiscoveryThread = new UDP_TxThread(Brodcast, DatagramHandler.UDP_CONTROL_PORT);
        udpNetworkDiscoveryThread.start();

        udplistenerThread = new UDP_RxThread(DatagramHandler.UDP_RESPOND_PORT,this);
        udplistenerThread.start();

        udpNetworkDiscoveryHandler = udpNetworkDiscoveryThread.mHandler;
        udpNetworkDiscoveryHandler.send("DISCOVER");
        updateTextDisplay("Discovering on: "+Brodcast);
    }

    protected String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }


    public  void onDestroy() {
        udpNetworkDiscoveryThread.dh_control.close();
        udplistenerThread.dh_recive.close();
        udplistenerThread.stopRX();
        try {
            udplistenerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void UpdateNetworkControl(String ip, char udpPort) {
        try {
            this.IP = ip;
            udpNetworkDiscoveryThread.dh_control.close();
            udpNetworkDiscoveryThread.dh_control = null;
            udpNetworkDiscoveryThread.dh_control = new DatagramHandler(this.IP,udpPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void send(String buffer, Object... b) {
        udpNetworkDiscoveryHandler.send(buffer,b);
    }

    public abstract void handalPIDUPdate(String id);

    public abstract void updateTextDisplay(String id);
}

package com.example.anthony.eddiebalancecontroler.eddie;

import com.example.anthony.eddiebalancecontroler.DatagramHandler;

import java.net.UnknownHostException;

/**
 * Created by antho on 1/26/2017.
 */

public abstract class Eddie {

    private UDP_TxThread udpNetworkDiscoveryThread;
    private UDP_RxThread udplistenerThread;
    private UDPHandler udpNetworkDiscoveryHandler;
    public boolean discovering =false;
    public String IP = "";
    public boolean BIND =false;

    public double DriveSpeed = 50;
    public double TurnSpeed = 0.8;



    public Eddie(){
        udpNetworkDiscoveryThread = new UDP_TxThread("10.0.0.255", DatagramHandler.UDP_CONTROL_PORT);
        udpNetworkDiscoveryThread.start();

        udplistenerThread = new UDP_RxThread(DatagramHandler.UDP_RESPOND_PORT,this);
        udplistenerThread.start();

        udpNetworkDiscoveryHandler = udpNetworkDiscoveryThread.mHandler;
        udpNetworkDiscoveryHandler.send("DISCOVER");
    }


    public  void onDestroy() {
        udpNetworkDiscoveryThread.dh_control.close();
        udplistenerThread.dh_recive.close();
    }

    public void UpdateNetworkControl(String ip, char udpControlPort) {
        try {
            udpNetworkDiscoveryThread.dh_control.close();
            udpNetworkDiscoveryThread.dh_control = null;
            udpNetworkDiscoveryThread.dh_control = new DatagramHandler(this.IP,DatagramHandler.UDP_CONTROL_PORT);
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

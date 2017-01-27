package com.example.anthony.eddiebalancecontroler.eddie;

import android.os.Looper;
import android.os.Message;

import java.net.UnknownHostException;

import static com.example.anthony.eddiebalancecontroler.MainActivity.print;

/**
 * Created by antho on 1/26/2017.
 */

class UDP_TxThread extends Thread {
    UDPHandler mHandler;
    DatagramHandler dh_control; 	//DatagramHandler

    UDP_TxThread(String address, int port) {
        try {
            dh_control = new DatagramHandler(address,port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        mHandler = new UDPHandler() {
            @Override
            public void handleMessage(Message inputMessage){
                print("handleMessage: %s",(String) inputMessage.obj);
                dh_control.send((String) inputMessage.obj);
            }
        };
    }


    @Override
    public void run(){
        Looper.prepare();

        Looper.loop();
    }

}
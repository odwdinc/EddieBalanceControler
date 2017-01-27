package com.example.anthony.eddiebalancecontroler.eddie;

import java.io.IOException;

import static com.example.anthony.eddiebalancecontroler.MainActivity.print;

/**
 * Created by antho on 1/26/2017.
 */

public class UDP_RxThread  extends Thread{
    private final Eddie eddie;
    private int port;
    DatagramHandler dh_recive; 	//DatagramHandler
    private boolean running =true;

    UDP_RxThread(int udpRespondPort, Eddie eddie) {
        this.port = udpRespondPort;
        this.dh_recive = new DatagramHandler(port);
        this.eddie = eddie;
    }


    @Override
    public void run() {

        while (running) {
            try {
                while (!dh_recive.hasData()) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                if (!this.eddie.BIND && !this.eddie.discovering){
                    this.eddie.IP = dh_recive.receiveIP();
                }
                String ID = dh_recive.receive();

                print("Recived UDP[%d](%s): %s",port,this.eddie.IP,ID);
                if(ID.contains("EddieBalance")){
                        this.eddie.UpdateNetworkControl(this.eddie.IP,DatagramHandler.UDP_CONTROL_PORT);
                        this.eddie.send("BIND");
                        this.eddie.discovering = true;
                }
                if(ID.contains("BIND: OK")){
                    this.eddie.BIND = true;
                    this.eddie.UpdateNetworkControl(this.eddie.IP,DatagramHandler.UDP_COMMAND_PORT);
                    this.eddie.send("GETPIDS");



                }
                if(ID.contains("CURRENTPIDS")) {
                    this.eddie.handalPIDUPdate(ID);
                }
                try {
                    if(!this.eddie.BIND && !this.eddie.discovering) {
                        sleep(1000);
                        this.eddie.send("DISCOVER");
                    }else {
                        this.eddie.updateTextDisplay(ID);
                        sleep(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {

            }catch (NullPointerException e){
                this.dh_recive = new DatagramHandler(port);
            }
        }

    }
    public void stopRX(){
        running = false;
    }
}
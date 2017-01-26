package com.example.anthony.eddiebalancecontroler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.UnknownHostException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import static java.lang.StrictMath.abs;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private UDPHandler udpNetworkDiscoveryHandler;
    private boolean BIND =false;
    private UDP_TxThread udpNetworkDiscoveryThread;
    private UDP_RxThread udplistenerThread;
    private TextView myLabel;
    private TextView myLabelP;
    private TextView myLabelI;
    private TextView myLabelD;
    private SeekBar mySeekP;
    private SeekBar mySeekI;
    private SeekBar mySeekD;
    private RadioButton pitchMode;
    private RadioButton speedMode;
    private RadioButton kalmanMode;
    private Button Updatebutton;
    private double DriveSpeed = 50;
    private double TurnSpeed = 0.8;

    public static int print(String buffer, Object... b) {
        String result = String.format(buffer,b);
        Log.d(TAG, result);
        return buffer.length();
    }

    double oldx=0,oldy=0;

    int mode =0; //0=pitch ,1=speed
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        myLabel= (TextView) findViewById(R.id.textView);
        myLabelP= (TextView) findViewById(R.id.textViewP);
        myLabelI= (TextView) findViewById(R.id.textViewI);
        myLabelD= (TextView) findViewById(R.id.textViewD);

        mySeekP = (SeekBar) findViewById(R.id.seekBarP);
        mySeekI = (SeekBar) findViewById(R.id.seekBarI);
        mySeekD = (SeekBar) findViewById(R.id.seekBarD);

        mySeekP.setOnSeekBarChangeListener(seeker);
        mySeekI.setOnSeekBarChangeListener(seeker);
        mySeekD.setOnSeekBarChangeListener(seeker);

        pitchMode = (RadioButton) findViewById(R.id.pitchMode);
        pitchMode.setChecked(true);
        speedMode = (RadioButton) findViewById(R.id.speedMode);

        pitchMode.setOnClickListener(onRadioButtonClicked);
        speedMode.setOnClickListener(onRadioButtonClicked);


        Updatebutton = (Button) findViewById(R.id.Updatebutton);

        Updatebutton.setOnClickListener(onButtonClicked);



        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                double x = (strength)* Math.cos((Math.toRadians(angle)));
                double y = (strength)* Math.sin((Math.toRadians(angle)));
                if(Math.abs(x-oldx) > 2){
                    oldx = x;
                    x = ((x/100)*TurnSpeed);
                    if(abs(x) < TurnSpeed){
                        if(BIND) {
                            udpNetworkDiscoveryHandler.send("TURN%3.2f",x);
                        }
                    }
                }

                if(Math.abs(y-oldy) > 2){
                    oldy = y;
                    if(BIND) {
                        y=((y/100)*DriveSpeed);
                        if(abs(y)<DriveSpeed) {
                            udpNetworkDiscoveryHandler.send("DRIVE%3.2f", y);
                        }
                    }
                }
                if((oldx == x) || (oldy == y) ) {
                    if(BIND) {
                        //print("onMove  angle: %d (%d, %d) ,strength: %d", angle, (int)x, (int)y, strength);
                    }
                }
            }
        }, 2); // around 60/sec


        udpNetworkDiscoveryThread = new UDP_TxThread("10.0.0.255", DatagramHandler.UDP_CONTROL_PORT);
        udpNetworkDiscoveryThread.start();

        udplistenerThread = new UDP_RxThread(DatagramHandler.UDP_RESPOND_PORT);
        udplistenerThread.start();

        udpNetworkDiscoveryHandler = udpNetworkDiscoveryThread.mHandler;
        udpNetworkDiscoveryHandler.send("DISCOVER");

    }



    View.OnClickListener onButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Update
            if(BIND) {
                float P = mySeekP.getProgress() / 10;
                float I = mySeekI.getProgress() * 10.0f;
                float D = mySeekD.getProgress() * 1.0f;
                if (mode == 1) {
                    udpNetworkDiscoveryHandler.send("SPIDP%3.3f",P);
                    udpNetworkDiscoveryHandler.send("SPIDI%3.3f",I);
                    udpNetworkDiscoveryHandler.send("SPIDD%3.3f",D);
                }else{
                    udpNetworkDiscoveryHandler.send("PPIDP%3.3f",P);
                    udpNetworkDiscoveryHandler.send("PPIDI%3.3f",I);
                    udpNetworkDiscoveryHandler.send("PPIDD%3.3f",D);
                }
            }
        }
    };

    View.OnClickListener onRadioButtonClicked =new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton) view).isChecked();

            // Check which radio button was clicked
            switch(view.getId()) {
                case R.id.pitchMode:
                    if (checked)
                        // P
                        mode = 0;
                        break;
                case R.id.speedMode:
                    if (checked)
                        // speedMode
                        mode = 1;
                        break;
            }

            if(BIND){
                udpNetworkDiscoveryHandler.send("GETPIDS");
            }
        }
    };


    SeekBar.OnSeekBarChangeListener seeker = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch(seekBar.getId()) {
                case R.id.seekBarP:
                    // P
                    myLabelP.setText("P: "+Float.toString(progress/10));
                    break;
                case R.id.seekBarI:
                    // D
                    myLabelI.setText("P: "+Float.toString(progress*10));
                    break;
                case R.id.seekBarD:
                    // D
                    myLabelD.setText("P: "+Float.toString(progress*10));
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


    @Override
    protected void onDestroy() {
        udpNetworkDiscoveryThread.dh_control.close();
        udplistenerThread.dh_recive.close();

        super.onDestroy();
    }

    class   UDP_RxThread extends Thread{
        private final int port;
        DatagramHandler dh_recive; 	//DatagramHandler
        public boolean running =true;
        public boolean discovering =false;
        private String IP = "";

        public  UDP_RxThread(int port ) {
            this.port = port;
            this.dh_recive = new DatagramHandler(port);
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


                if (!BIND && !discovering){
                     this.IP = dh_recive.receiveIP();
                }
                String ID = dh_recive.receive();

                print("Recived UDP[%d](%s): %s",port,IP,ID);
                if(ID.contains("EddieBalance")){
                    try {
                        udpNetworkDiscoveryThread.dh_control.close();
                        udpNetworkDiscoveryThread.dh_control = null;
                        udpNetworkDiscoveryThread.dh_control = new DatagramHandler(IP,DatagramHandler.UDP_CONTROL_PORT);
                        udpNetworkDiscoveryHandler.send("BIND");
                        discovering = true;
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                }
                if(ID.contains("BIND: OK")){
                    BIND = true;
                    try {
                        udpNetworkDiscoveryThread.dh_control.close();
                        udpNetworkDiscoveryThread.dh_control = null;
                        udpNetworkDiscoveryThread.dh_control = new DatagramHandler(IP,DatagramHandler.UDP_COMMAND_PORT);
                        udpNetworkDiscoveryHandler.send("GETPIDS");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }


                }
                if(ID.contains("CURRENTPIDS")) {
                    handalPIDUPdate(ID);
                }
                try {
                    if(!BIND && !discovering) {
                        sleep(1000);
                        udpNetworkDiscoveryHandler.send("DISCOVER");
                    }else {
                        updateTextDisplay(ID);
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
    }

    private void handalPIDUPdate(final String id) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String[] pidlist = id.split(":");
                pidlist = pidlist[1].split(",");
                float P = 0;
                int I = 0;
                int D =0;
                if(mode == 0) {
                    P = Float.parseFloat(pidlist[0]);
                    I = (int)Float.parseFloat(pidlist[1]);
                    D =  (int)Float.parseFloat(pidlist[2]);
                }else if (mode ==1){
                    P = Float.parseFloat(pidlist[3]);
                    I =(int)Float.parseFloat(pidlist[4]);
                    D = (int)Float.parseFloat(pidlist[5]);
                }
                mySeekP.setProgress((int)(P*10),true);
                myLabelP.setText("P: "+Float.toString(P));

                mySeekI.setProgress((int)(I/10),true);
                myLabelI.setText("I: "+Float.toString(I));

                mySeekD.setProgress((int)(D),true);
                myLabelD.setText("D: "+Float.toString(D));

            }
        });
    }

    private void updateTextDisplay(final String Text) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                myLabel.setText(Text);
            }
        });

    }


    static class UDP_TxThread extends Thread {
        public UDPHandler mHandler;
        public DatagramHandler dh_control; 	//DatagramHandler

        UDP_TxThread(String address, int port) {
            try {
                dh_control = new DatagramHandler(address,port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            mHandler = new UDPHandler() {
                @Override
                public void handleMessage(Message inputMessage){
                    Log.d(TAG,"handleMessage: "+(String) inputMessage.obj);
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

    static class  UDPHandler extends Handler {
        public UDPHandler() {
            super();
        }


        public  void send(String buffer, Object... b) {
            String result = String.format(buffer,b);
            Message msg = this.obtainMessage();
            msg.obj =  result;
            this.sendMessage(msg);
        }
    }



}

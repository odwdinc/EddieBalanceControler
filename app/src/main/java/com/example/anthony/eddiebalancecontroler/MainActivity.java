package com.example.anthony.eddiebalancecontroler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.anthony.eddiebalancecontroler.eddie.Eddie;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import static java.lang.StrictMath.abs;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();



    private TextView myLabel;
    private TextView myLabelP;
    private TextView myLabelI;
    private TextView myLabelD;
    private SeekBar mySeekP;
    private SeekBar mySeekI;
    private SeekBar mySeekD;
    private RadioButton pitchMode;
    private RadioButton speedMode;
    private Button Updatebutton;
    private Eddie eddie;

    public static int print(String buffer, Object... b) {
        String result = String.format(buffer,b);
        Log.d(TAG, result);
        return buffer.length();
    }

    double oldx=0,oldy=0;

    int mode =0; //0=pitch ,1=speed

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

        this.eddie = new Eddie() {
            @Override
            public void handalPIDUPdate(final String id) {
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

            @Override
            public void updateTextDisplay(final String Text) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        myLabel.setText(Text);
                    }
                });
            }
        };



        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                double x = (strength)* Math.cos((Math.toRadians(angle)));
                double y = (strength)* Math.sin((Math.toRadians(angle)));
                if(Math.abs(x-oldx) > 2){
                    oldx = x;
                    x = ((x/100)*eddie.TurnSpeed);
                    if(abs(x) < eddie.TurnSpeed){
                        if(eddie.BIND) {
                            eddie.send("TURN%3.2f",x);
                        }
                    }
                }

                if(Math.abs(y-oldy) > 2){
                    oldy = y;
                    if(eddie.BIND) {
                        y=((y/100)*eddie.DriveSpeed);
                        if(abs(y)<eddie.DriveSpeed) {
                            eddie.send("DRIVE%3.2f", y);
                        }
                    }
                }
                if((oldx == x) || (oldy == y) ) {
                    if(eddie.BIND) {
                        //print("onMove  angle: %d (%d, %d) ,strength: %d", angle, (int)x, (int)y, strength);
                    }
                }
            }
        }, 2); // around 60/sec

    }



    View.OnClickListener onButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Update
            if(eddie.BIND) {
                float P = mySeekP.getProgress() / 10;
                float I = mySeekI.getProgress() * 10.0f;
                float D = mySeekD.getProgress() * 1.0f;
                if (mode == 1) {
                    eddie.send("SPIDP%3.3f",P);
                    eddie.send("SPIDI%3.3f",I);
                    eddie.send("SPIDD%3.3f",D);
                }else{
                    eddie.send("PPIDP%3.3f",P);
                    eddie.send("PPIDI%3.3f",I);
                    eddie.send("PPIDD%3.3f",D);
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

            if(eddie.BIND){
                eddie.send("GETPIDS");
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
        super.onDestroy();
    }

}

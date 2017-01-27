package com.example.anthony.eddiebalancecontroler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.anthony.eddiebalancecontroler.eddie.Eddie;

import java.util.Locale;

/**
 * Created by anthony on 1/26/2017.
 */

public class SettingsActvity extends AppCompatActivity {
    private TextView myLabel;
    private TextView myLabelP;
    private TextView myLabelI;
    private TextView myLabelD;
    private SeekBar mySeekP;
    private SeekBar mySeekI;
    private SeekBar mySeekD;

    private Eddie eddie;


    int mode =0; //0=pitch ,1=speed


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        myLabelP= (TextView) findViewById(R.id.textViewP);
        myLabelI= (TextView) findViewById(R.id.textViewI);
        myLabelD= (TextView) findViewById(R.id.textViewD);
        myLabel = (TextView) findViewById(R.id.textView2);

        mySeekP = (SeekBar) findViewById(R.id.seekBarP);
        mySeekI = (SeekBar) findViewById(R.id.seekBarI);
        mySeekD = (SeekBar) findViewById(R.id.seekBarD);

        mySeekP.setOnSeekBarChangeListener(seeker);
        mySeekI.setOnSeekBarChangeListener(seeker);
        mySeekD.setOnSeekBarChangeListener(seeker);

        RadioButton pitchMode = (RadioButton) findViewById(R.id.pitchMode);
        pitchMode.setChecked(true);
        RadioButton speedMode = (RadioButton) findViewById(R.id.speedMode);

        pitchMode.setOnClickListener(onRadioButtonClicked);
        speedMode.setOnClickListener(onRadioButtonClicked);


        Button updatebutton = (Button) findViewById(R.id.Updatebutton);

        updatebutton.setOnClickListener(onButtonClicked);

        this.eddie = new Eddie(this) {
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
                        mySeekP.setProgress((int)(P*1),true);
                        myLabelP.setText(String.format(Locale.US,"P: %3.3f",P));

                        mySeekI.setProgress((I/10),true);
                        myLabelI.setText(String.format(Locale.US,"I: %d",I));

                        mySeekD.setProgress(D,true);
                        myLabelD.setText(String.format(Locale.US,"D: %d",D));

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


        Button Settings = (Button) findViewById(R.id.Drive);
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActvity.this, MainActivity.class);
                SettingsActvity.this.eddie.onDestroy();
                startActivity(intent);
                finish();
            }
        });
    }

    View.OnClickListener onButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Update
            if(eddie.BIND) {
                float P = mySeekP.getProgress();
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


                    myLabelP.setText(String.format(Locale.US,"P: %3.3f",progress*1.0f));
                    break;
                case R.id.seekBarI:
                    // D
                    myLabelI.setText(String.format(Locale.US,"I: %3.3f",progress/10.0f));
                    break;
                case R.id.seekBarD:
                    // D
                    myLabelD.setText(String.format(Locale.US,"D: %3.3f",progress/1.0f));
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
}

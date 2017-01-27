package com.example.anthony.eddiebalancecontroler;
import android.content.Intent;
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
    private Eddie eddie;

    public static int print(String buffer, Object... b) {
        String result = String.format(buffer,b);
        Log.d(TAG, result);
        return buffer.length();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        myLabel= (TextView) findViewById(R.id.textView);
        Button Settings = (Button) findViewById(R.id.Settings);
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActvity.class);
                MainActivity.this.eddie.onDestroy();
                startActivity(intent);
                finish();
            }
        });



        this.eddie = new Eddie(this) {
            @Override
            public void handalPIDUPdate(final String id) {

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
                if(Math.abs(x-eddie.oldx) > 2){
                    eddie.oldx = x;
                    x = ((x/100)*eddie.TurnSpeed);
                    if(abs(x) < eddie.TurnSpeed){
                        if(eddie.BIND) {
                            eddie.send("TURN%3.2f",x);
                        }
                    }
                }

                if(Math.abs(y-eddie.oldy) > 2){
                    eddie.oldy = y;
                    if(eddie.BIND) {
                        y=((y/100)*eddie.DriveSpeed);
                        if(abs(y)<eddie.DriveSpeed) {
                            eddie.send("DRIVE%3.2f", y);
                        }
                    }
                }
                if((eddie.oldx == x) || (eddie.oldy == y) ) {
                    if(eddie.BIND) {
                        //print("onMove  angle: %d (%d, %d) ,strength: %d", angle, (int)x, (int)y, strength);
                    }
                }
            }
        }, 2); // around 60/sec

    }

    @Override
    protected void onDestroy() {
        eddie.onDestroy();
        super.onDestroy();
    }

}

package com.example.anthony.eddiebalancecontroler.eddie;

import android.os.Handler;
import android.os.Message;

/**
 * Created by antho on 1/26/2017.
 */

public class UDPHandler extends Handler {
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
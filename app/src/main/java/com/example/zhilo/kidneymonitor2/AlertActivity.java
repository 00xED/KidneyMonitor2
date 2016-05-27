package com.example.zhilo.kidneymonitor2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

public class AlertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_alert);
        Intent intent=getIntent();
        String text = "";
        if(intent.hasExtra("text")) text = intent.getStringExtra("text");

        final Vibrator vibrator;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = { 0, 200, 500 };
        vibrator.vibrate(pattern, 0);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.setStreamType(AudioManager.STREAM_ALARM);
        r.play();


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Алярм!");
        alert.setIcon(android.R.drawable.ic_dialog_info);
        alert.setMessage(text);
        alert.setIcon(R.drawable.ic_danger);
        alert.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vibrator.cancel();
                        r.stop();
                        AlertActivity.this.finish();
                    }
                });
        alert.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vibrator.cancel();
                r.stop();
                AlertActivity.this.finish();
            }
        });
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                vibrator.cancel();
                r.stop();
                AlertActivity.this.finish();
            }
        });
        alert.show();
    }
}

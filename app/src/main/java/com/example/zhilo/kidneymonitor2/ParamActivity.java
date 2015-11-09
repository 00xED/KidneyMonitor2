package com.example.zhilo.kidneymonitor2;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ParamActivity extends AppCompatActivity {

    private TextView tvDPumpFlow1, tvDPumpFlow1Min, tvDPumpFlow1Max;
    private TextView tvDPumpFlow2, tvDPumpFlow2Min, tvDPumpFlow2Max;
    private TextView tvDPumpFlow3, tvDPumpFlow3Min, tvDPumpFlow3Max;
    private TextView tvDUFVolume, tvDUFVolumeMin, tvDUFVolumeMax;

    private TextView tvDPress1, tvDPress1Min, tvDPress1Max;
    private TextView tvDPress2, tvDPress2Min, tvDPress2Max;
    private TextView tvDPress3, tvDPress3Min, tvDPress3Max;

    private TextView tvDTemp, tvDTempMin, tvDTempMax;
    private TextView tvDCond, tvDCondMin, tvDCondMax;

    private TextView tvDCur1, tvDCur2, tvDCur3, tvDCur4;

    private ProgressBar pbDPumpFlow1, pbDPumpFlow2, pbDPumpFlow3, pbDUFVolume;
    private ProgressBar pbDPress1, pbDPress2, pbDPress3, pbDTemp, pbDCond;
    private ProgressBar pbDCur1, pbDCur2, pbDCur3, pbDCur4;

    //Handler for automatic refreshing of screen values
    Handler RefreshHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_param);

        pbDPumpFlow1 = (ProgressBar) findViewById(R.id.pb_DPumpFlow1);
        pbDPumpFlow1.setMax(200);
        pbDPumpFlow1.setProgress(ProcedureSettings.getInstance().getDialPump1Flow());
        tvDPumpFlow1Min = (TextView) findViewById(R.id.tv_DPumpFlow1Min);
        tvDPumpFlow1 = (TextView) findViewById(R.id.tv_DPumpFlow1);
        tvDPumpFlow1.setText(String.valueOf(ProcedureSettings.getInstance().getDialPump1Flow()));
        tvDPumpFlow1Max = (TextView) findViewById(R.id.tv_DPumpFlow1Max);

        pbDPumpFlow2 = (ProgressBar) findViewById(R.id.pb_DPumpFlow2);
        pbDPumpFlow2.setMax(200);
        pbDPumpFlow2.setProgress(ProcedureSettings.getInstance().getDialPump2Flow());
        tvDPumpFlow1Min = (TextView) findViewById(R.id.tv_DPumpFlow1Min);
        tvDPumpFlow2 = (TextView) findViewById(R.id.tv_DPumpFlow2);
        tvDPumpFlow2.setText(String.valueOf(ProcedureSettings.getInstance().getDialPump2Flow()));
        tvDPumpFlow1Max = (TextView) findViewById(R.id.tv_DPumpFlow1Max);

        pbDPumpFlow3 = (ProgressBar) findViewById(R.id.pb_DPumpFlow3);
        pbDPumpFlow3.setMax(200);
        pbDPumpFlow3.setProgress(ProcedureSettings.getInstance().getDialPump3Flow());
        tvDPumpFlow1Min = (TextView) findViewById(R.id.tv_DPumpFlow1Min);
        tvDPumpFlow3 = (TextView) findViewById(R.id.tv_DPumpFlow3);
        tvDPumpFlow3.setText(String.valueOf(ProcedureSettings.getInstance().getDialPump3Flow()));
        tvDPumpFlow1Max = (TextView) findViewById(R.id.tv_DPumpFlow1Max);

        pbDUFVolume = (ProgressBar) findViewById(R.id.pb_DUFVolume);
        tvDUFVolumeMin = (TextView) findViewById(R.id.tv_DUFVolumeMin);
        tvDUFVolume = (TextView) findViewById(R.id.tv_DUFVolume);
        tvDUFVolumeMax = (TextView) findViewById(R.id.tv_DUFVolumeMax);

        pbDPress1 = (ProgressBar) findViewById(R.id.pb_DPress1);
        //pbDPress1.setMax(Math.round(ConnectionService.DPRESS1MAX));
        tvDPress1Min = (TextView) findViewById(R.id.tv_DPress1Min);
        tvDPress1Min.setText(String.valueOf(ProcedureSettings.getInstance().getDialPress1Min()));
        tvDPress1 = (TextView) findViewById(R.id.tv_DPress1);
        tvDPress1Max = (TextView) findViewById(R.id.tv_DPress1Max);
        tvDPress1Max.setText(String.valueOf(ProcedureSettings.getInstance().getDialPress1Max()));

        pbDPress2 = (ProgressBar) findViewById(R.id.pb_DPress2);
        //pbDPress2.setMax(Math.round(ConnectionService.DPRESS2MAX));
        tvDPress2Min = (TextView) findViewById(R.id.tv_DPress2Min);
        tvDPress2Min.setText(String.valueOf(ProcedureSettings.getInstance().getDialPress2Min()));
        tvDPress2 = (TextView) findViewById(R.id.tv_DPress2);
        tvDPress2Max = (TextView) findViewById(R.id.tv_DPress2Max);
        tvDPress2Max.setText(String.valueOf(ProcedureSettings.getInstance().getDialPress2Max()));

        pbDPress3 = (ProgressBar) findViewById(R.id.pb_DPress3);
        //pbDPress3.setMax(Math.round(ConnectionService.DPRESS3MAX));
        tvDPress3Min = (TextView) findViewById(R.id.tv_DPress3Min);
        tvDPress3Min.setText(String.valueOf(ProcedureSettings.getInstance().getDialPress3Min()));
        tvDPress3 = (TextView) findViewById(R.id.tv_DPress3);
        tvDPress3Max = (TextView) findViewById(R.id.tv_DPress3Max);
        tvDPress3Max.setText(String.valueOf(ProcedureSettings.getInstance().getDialPress3Max()));

        pbDTemp = (ProgressBar) findViewById(R.id.pb_DTemp);
        //pbDTemp.setMax(Math.round(ConnectionService.DTEMP1MAX));
        tvDTempMin = (TextView) findViewById(R.id.tv_DTempMin);
        tvDTempMin.setText(String.valueOf(Math.round(ProcedureSettings.getInstance().getDialTemp1Min())));
        tvDTemp = (TextView) findViewById(R.id.tv_DTemp);
        tvDTempMax = (TextView) findViewById(R.id.tv_DTempMax);
        tvDTempMax.setText(String.valueOf(Math.round(ProcedureSettings.getInstance().getDialTemp1Max())));

        pbDCond = (ProgressBar) findViewById(R.id.pb_DCond);
        //pbDCond.setMax(Math.round(ConnectionService.DCOND1MAX));
        tvDCondMin = (TextView) findViewById(R.id.tv_DCondMin);
        tvDCondMin.setText(String.valueOf(Math.round(ProcedureSettings.getInstance().getDialCond1Min())));
        tvDCond = (TextView) findViewById(R.id.tv_DCond);
        tvDCondMax = (TextView) findViewById(R.id.tv_DCondMax);
        tvDCondMax.setText(String.valueOf(Math.round(ProcedureSettings.getInstance().getDialCond1Max())));

        pbDCur1 = (ProgressBar) findViewById(R.id.pb_DCur1);
        pbDCur1.setMax(500);
        tvDCur1 = (TextView) findViewById(R.id.tv_DCur1);

        pbDCur2 = (ProgressBar) findViewById(R.id.pb_DCur2);
        pbDCur2.setMax(500);
        tvDCur2 = (TextView) findViewById(R.id.tv_DCur2);

        pbDCur3 = (ProgressBar) findViewById(R.id.pb_DCur3);
        pbDCur3.setMax(500);
        tvDCur3 = (TextView) findViewById(R.id.tv_DCur3);

        pbDCur4 = (ProgressBar) findViewById(R.id.pb_DCur4);
        pbDCur4.setMax(500);
        tvDCur4 = (TextView) findViewById(R.id.tv_DCur4);

        RefreshHandler.post(RefreshTask);
    }

    Runnable RefreshTask = new Runnable() {
        @Override
        public void run() {

            pbDPress1.setMax(0);
            pbDPress1.setProgress(0);
            float value = ProcedureSettings.getInstance().getDialPress1();
            float percent = value - ProcedureSettings.getInstance().getDialPress1Min();
            int progress = Math.round(percent);
            tvDPress1.setText(String.format("%.2f", value));
            if (value > ProcedureSettings.getInstance().getDialPress1Max() || value < ProcedureSettings.getInstance().getDialPress1Min()) {
                tvDPress1.setTextColor(Color.RED);
                pbDPress1.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            } else {
                tvDPress1.setTextColor(tvDPress1Min.getCurrentTextColor());
                pbDPress1.getProgressDrawable().setColorFilter(null);

            }
            pbDPress1.setMax(Math.round(ProcedureSettings.getInstance().getDialPress1Max() - ProcedureSettings.getInstance().getDialPress1Min()));
            pbDPress1.setProgress(progress);

            pbDPress2.setMax(0);
            pbDPress2.setProgress(0);
            value = ProcedureSettings.getInstance().getDialPress2();
            percent = value - ProcedureSettings.getInstance().getDialPress2Min();
            progress = Math.round(percent);
            tvDPress2.setText(String.format("%.2f", value));
            if (value > ProcedureSettings.getInstance().getDialPress2Max() || value < ProcedureSettings.getInstance().getDialPress2Min()) {
                tvDPress2.setTextColor(Color.RED);
                pbDPress2.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            } else {
                tvDPress2.setTextColor(tvDPress1Min.getCurrentTextColor());
                pbDPress2.getProgressDrawable().setColorFilter(null);

            }
            pbDPress2.setMax(Math.round(ProcedureSettings.getInstance().getDialPress2Max() - ProcedureSettings.getInstance().getDialPress2Min()));
            pbDPress2.setProgress(progress);

            pbDPress3.setMax(0);
            pbDPress3.setProgress(0);
            value = ProcedureSettings.getInstance().getDialPress3();
            percent = value - ProcedureSettings.getInstance().getDialPress3Min();
            progress = Math.round(percent);
            tvDPress3.setText(String.format("%.2f", value));
            if (value > ProcedureSettings.getInstance().getDialPress3Max() || value < ProcedureSettings.getInstance().getDialPress3Min()) {
                tvDPress3.setTextColor(Color.RED);
                pbDPress3.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            } else {
                tvDPress3.setTextColor(tvDPress1Min.getCurrentTextColor());
                pbDPress3.getProgressDrawable().setColorFilter(null);

            }
            pbDPress3.setMax(Math.round(ProcedureSettings.getInstance().getDialPress3Max() - ProcedureSettings.getInstance().getDialPress3Min()));
            pbDPress3.setProgress(progress);

            pbDTemp.setMax(0);
            pbDTemp.setProgress(0);
            value = ProcedureSettings.getInstance().getDialTemp1();
            percent = value - ProcedureSettings.getInstance().getDialTemp1Min();
            progress = Math.round(percent);
            tvDTemp.setText(String.format("%.2f", value));
            if (value > ProcedureSettings.getInstance().getDialTemp1Max() || value < ProcedureSettings.getInstance().getDialTemp1Min()) {
                tvDTemp.setTextColor(Color.RED);
                pbDTemp.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            } else {
                tvDTemp.setTextColor(tvDPress1Min.getCurrentTextColor());
                pbDTemp.getProgressDrawable().setColorFilter(null);

            }
            pbDTemp.setMax(Math.round(ProcedureSettings.getInstance().getDialTemp1Max() - ProcedureSettings.getInstance().getDialTemp1Min()));
            pbDTemp.setProgress(progress);

            pbDCond.setMax(0);
            pbDCond.setProgress(0);
            value = ProcedureSettings.getInstance().getDialCond1();
            percent = value - ProcedureSettings.getInstance().getDialCond1Min();
            progress = Math.round(percent);
            tvDCond.setText(String.format("%.2f", value));
            if (value > ProcedureSettings.getInstance().getDialCond1Max() || value < ProcedureSettings.getInstance().getDialCond1Min()) {
                tvDCond.setTextColor(Color.RED);
                pbDCond.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            } else {
                tvDCond.setTextColor(tvDPress1Min.getCurrentTextColor());
                pbDCond.getProgressDrawable().setColorFilter(null);

            }
            pbDCond.setMax(Math.round(ProcedureSettings.getInstance().getDialCond1Max() - ProcedureSettings.getInstance().getDialCond1Min()));
            pbDCond.setProgress(progress);

            pbDCur1.setMax(0);
            pbDCur1.setProgress(0);
            progress = Math.round(ProcedureSettings.getInstance().getDialCurrent1());
            tvDCur1.setText(String.valueOf(progress));
            pbDCur1.setMax(500);
            pbDCur1.setProgress(progress);

            pbDCur2.setMax(0);
            pbDCur2.setProgress(0);
            progress = Math.round(Float.valueOf(ProcedureSettings.getInstance().getDialCurrent2()));
            tvDCur2.setText(String.valueOf(progress));
            pbDCur2.setMax(500);
            pbDCur2.setProgress(progress);

            pbDCur3.setMax(0);
            pbDCur3.setProgress(0);
            progress = Math.round(Float.valueOf(ProcedureSettings.getInstance().getDialCurrent3()));
            tvDCur3.setText(String.valueOf(progress));
            pbDCur3.setMax(500);
            pbDCur3.setProgress(progress);

            pbDCur4.setMax(0);
            pbDCur4.setProgress(0);
            progress = Math.round(Float.valueOf(ProcedureSettings.getInstance().getDialCurrent4()));
            tvDCur4.setText(String.valueOf(progress));
            pbDCur4.setMax(500);
            pbDCur4.setProgress(progress);

            RefreshHandler.postDelayed(RefreshTask, Constants.REFRESH_INTERVAL_MS);//refresh after RECONNECT_INTERVAL_MS milliseconds
        }
    };
}

package com.example.zhilo.kidneymonitor2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class InstructionActivity extends AppCompatActivity {

    private ImageView ivBackground, ivInstructionImage;
    private TextView tvInstructionText;
    private int selectedProcedure;
    private int stage = 0;
    private String[] instructionsStrings;

    private static final int[] instruct_fill = {
            R.drawable.instruct_fill_1,
            R.drawable.instruct_fill_2,
            R.drawable.instruct_fill_3,
            R.drawable.instruct_fill_4,
            R.drawable.instruct_fill_5,
            R.drawable.ic_question_mark,
            R.drawable.empty
    };

    private static final int[] instruct_dialisys = {
            R.drawable.instruct_dialisys_1,
            R.drawable.instruct_dialisys_2,
            R.drawable.ic_question_mark,
            R.drawable.instruct_dialisys_4,
            R.drawable.empty
    };

    private static final int[] instruct_flush = {
            R.drawable.instruct_flush_1,
            R.drawable.instruct_flush_2,
            R.drawable.ic_question_mark,
            R.drawable.instruct_flush_4,
            R.drawable.instruct_flush_5,
            R.drawable.empty
    };

    private static final int[] instruct_disinfection = {
            R.drawable.instruct_disinfection_1,
            R.drawable.ic_question_mark,
            R.drawable.instruct_disinfection_3,
            R.drawable.empty,
    };

    private static final int[] instruct_fill_done = {
            R.drawable.instruct_fill_done_1,
            R.drawable.empty,
    };

    private static final int[] instruct_disinfection_done = {
            R.drawable.empty,
            R.drawable.instruct_disinfection_done_2,
            R.drawable.ic_question_mark,
            R.drawable.instruct_disinfection_done_4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);
        getSupportActionBar().hide();
        ivBackground = (ImageView) findViewById(R.id.iv_Header);
        ivInstructionImage = (ImageView) findViewById(R.id.iv_InstructionImage);
        tvInstructionText = (TextView) findViewById(R.id.tv_InstructionText);
        WebView wv = (WebView) findViewById(R.id.webView);

        Bundle parameters = getIntent().getExtras();
        selectedProcedure = parameters.getInt("procedure");

        switch (selectedProcedure){
            case(Constants.PROCEDURE_FILL):{
                ivBackground.setImageResource(R.drawable.bg_header_fill);
                instructionsStrings = getResources().getStringArray(R.array.instruction_filling);
                break;
            }

            case(Constants.PROCEDURE_DIALYSIS):{
                ivBackground.setImageResource(R.drawable.bg_header_dialisys);
                instructionsStrings = getResources().getStringArray(R.array.instruction_dialysis);
                break;
            }

            case(Constants.PROCEDURE_FLUSH):{
                ivBackground.setImageResource(R.drawable.bg_header_flush);
                instructionsStrings = getResources().getStringArray(R.array.instruction_flush);
                break;
            }

            case(Constants.PROCEDURE_SHUTDOWN):{
                ivBackground.setImageResource(R.drawable.bg_header_shutdown);
                instructionsStrings = getResources().getStringArray(R.array.instruction_shutdown);
                break;
            }

            case(Constants.PROCEDURE_DISINFECTION):{
                ivBackground.setImageResource(R.drawable.bg_header_disinfection);
                instructionsStrings = getResources().getStringArray(R.array.instruction_disinfection);
                break;
            }

            case(Constants.PROCEDURE_FILL_DONE):{
                ivBackground.setImageResource(R.drawable.bg_header_fill);
                instructionsStrings = getResources().getStringArray(R.array.instruction_filling_done);
                break;
            }

            case(Constants.PROCEDURE_DIALYSIS_DONE):{
                ivBackground.setImageResource(R.drawable.bg_header_dialisys);
                instructionsStrings = getResources().getStringArray(R.array.instruction_dialysis_done);
                break;
            }

            case(Constants.PROCEDURE_DISINFECTION_DONE):{
                ivBackground.setImageResource(R.drawable.bg_header_disinfection);
                instructionsStrings = getResources().getStringArray(R.array.instruction_disinfection_done);
                break;
            }

            default:
                break;
        }
        updateScreen();
    }

    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.ib_cancel: {
                if(stage==0)
                    InstructionActivity.this.finish();
                else {
                    stage--;
                    updateScreen();
                }
                break;
            }

            case R.id.ib_ok: {
                stage++;
                updateScreen();
                break;
            }

            default:
                break;
        }
    }

    private int getScale(){
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(572);
        val = val * 100d;
        return val.intValue();
    }

    public void updateScreen()
    {

        switch (selectedProcedure){
            case(Constants.PROCEDURE_FILL):{
                if(stage == instructionsStrings.length){
                    Intent intent = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, selectedProcedure);
                    sendBroadcast(intent);
                    InstructionActivity.this.finish();
                } else {
                    /*tvInstructionText.setText(instructionsStrings[stage]);
                    WebView wv = (WebView) findViewById(R.id.webView);
                    wv.loadUrl("file:///android_asset/images/fill_3.gif");
                    wv.setInitialScale(getScale());
                    //ivInstructionImage.setImageResource(instruct_fill[stage]);*/
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(instruct_fill[stage]);

                }
                break;
            }

            case(Constants.PROCEDURE_DIALYSIS):{
                if(stage == instructionsStrings.length){
                    Intent intent = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, selectedProcedure);
                    sendBroadcast(intent);
                    InstructionActivity.this.finish();
                }
                else{
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(instruct_dialisys[stage]);
                }
                break;
            }

            case(Constants.PROCEDURE_FLUSH):{
                if(stage == instructionsStrings.length){
                    /*Intent intent = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, selectedProcedure);
                    sendBroadcast(intent);*/
                    Intent intent = new Intent(this, ProceduresActivity.class);
                    startActivity(intent);
                    InstructionActivity.this.finish();
                }
                else {
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(instruct_flush[stage]);
                }
                break;
            }

            case(Constants.PROCEDURE_SHUTDOWN):{
                if(stage == instructionsStrings.length){
                    Intent intent = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, selectedProcedure);
                    sendBroadcast(intent);
                    InstructionActivity.this.finish();
                }
                else{
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(R.drawable.empty);
                }
                break;
            }

            case(Constants.PROCEDURE_DISINFECTION):{
                if(stage == instructionsStrings.length){
                    Intent intent = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, selectedProcedure);
                    sendBroadcast(intent);
                    InstructionActivity.this.finish();
                }
                else{
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(instruct_disinfection[stage]);
                }
                break;
            }

            case(Constants.PROCEDURE_FILL_DONE):{
                if(stage == instructionsStrings.length){
                    Intent intent = new Intent(this, InstructionActivity.class);
                    Bundle parameters = new Bundle();
                    parameters.putInt("procedure", Constants.PROCEDURE_DIALYSIS); //Your id
                    intent.putExtras(parameters); //Put your id to your next Intent
                    InstructionActivity.this.finish();
                    startActivity(intent);
                }
                else{
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(instruct_fill_done[stage]);
                }
                break;
            }

            case(Constants.PROCEDURE_DIALYSIS_DONE):{
                if(stage == instructionsStrings.length){
                    Intent intent = new Intent(this, InstructionActivity.class);
                    Bundle parameters = new Bundle();
                    parameters.putInt("procedure", Constants.PROCEDURE_FLUSH); //Your id
                    intent.putExtras(parameters); //Put your id to your next Intent
                    InstructionActivity.this.finish();
                    startActivity(intent);
                }
                else{
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(R.drawable.empty);
                }
                break;
            }

            case(Constants.PROCEDURE_DISINFECTION_DONE):{
                if(stage == instructionsStrings.length){
                    Intent intent = new Intent(this, ProceduresActivity.class);
                    InstructionActivity.this.finish();
                    startActivity(intent);
                }
                else{
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(instruct_disinfection_done[stage]);
                }
                break;
            }

            default:
                break;
        }
    }
}

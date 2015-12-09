package com.example.zhilo.kidneymonitor2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class InstructionActivity extends AppCompatActivity {

    private ImageView ivBackground, ivInstructionImage;
    private TextView tvInstructionText;
    private int selectedProcedure;
    private int stage = 0;
    private String[] instructionsStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);
        getSupportActionBar().hide();
        ivBackground = (ImageView) findViewById(R.id.iv_Header);
        ivInstructionImage = (ImageView) findViewById(R.id.iv_InstructionImage);
        tvInstructionText = (TextView) findViewById(R.id.tv_InstructionText);

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
                }
                else{
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(R.drawable.instruct_filling);
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
                    ivInstructionImage.setImageResource(R.drawable.instruct_dialysis);
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
                else{
                    tvInstructionText.setText(instructionsStrings[stage]);
                    ivInstructionImage.setImageResource(R.drawable.instruct_flush);
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
                    ivInstructionImage.setImageResource(R.drawable.instruct_shutdown);
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
                    ivInstructionImage.setImageResource(R.drawable.instruct_disinfection);
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
                    ivInstructionImage.setImageResource(R.drawable.instruct_filling);
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
                    ivInstructionImage.setImageResource(R.drawable.instruct_dialysis);
                }
                break;
            }

            default:
                break;
        }
    }
}

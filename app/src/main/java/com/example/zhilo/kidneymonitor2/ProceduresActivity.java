package com.example.zhilo.kidneymonitor2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

public class ProceduresActivity extends AppCompatActivity {

    private RadioButton rbFill, rbDialisys, rbFlush, rbShutdown, rbDisinfection;
    private ImageView ivFilling, ivDialisys, ivFlush, ivShutdown, ivDisinfection;

    int selectedProcedure = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedures);
        getSupportActionBar().hide();

        rbFill = (RadioButton) findViewById(R.id.rb_fill);
        rbDialisys = (RadioButton) findViewById(R.id.rb_dialisys);
        rbFlush = (RadioButton) findViewById(R.id.rb_flush);
        rbShutdown = (RadioButton) findViewById(R.id.rb_shutdown);
        rbDisinfection = (RadioButton) findViewById(R.id.rb_disinfection);

        ivFilling = (ImageView) findViewById(R.id.ib_fill);
        ivDialisys = (ImageView) findViewById(R.id.ib_dialisys);
        ivFlush = (ImageView) findViewById(R.id.ib_flush);
        ivShutdown = (ImageView) findViewById(R.id.ib_shutdown);
        ivDisinfection = (ImageView) findViewById(R.id.ib_disinfection);

        int currentProcedure = ProcedureSettings.getInstance().getProcedure();
        int previousProcedure = ProcedureSettings.getInstance().getProcedure_previous();
        switch (currentProcedure) {
            case Constants.PROCEDURE_DIALYSIS: {
                disableAllRows();
                rbFlush.setEnabled(true);
                ivFlush.setImageResource(R.drawable.ib_flush);
                break;
            }

            case Constants.PROCEDURE_FILL: {
                disableAllRows();
                rbDialisys.setEnabled(true);
                ivDialisys.setImageResource(R.drawable.ib_dialisys);
                break;
            }

            case Constants.PROCEDURE_SHUTDOWN: {
                disableAllRows();
                rbShutdown.setEnabled(true);
                ivShutdown.setImageResource(R.drawable.ib_shutdown);
                break;
            }

            case Constants.PROCEDURE_DISINFECTION: {
                disableAllRows();
                rbShutdown.setEnabled(true);
                ivShutdown.setImageResource(R.drawable.ib_shutdown);
                break;
            }

            case Constants.PROCEDURE_FLUSH: {
                disableAllRows();
                rbShutdown.setEnabled(true);
                ivShutdown.setImageResource(R.drawable.ib_shutdown);
                break;
            }

            case Constants.PROCEDURE_READY: {
                switch (previousProcedure) {
                    case Constants.PROCEDURE_DIALYSIS: {
                        disableAllRows();
                        rbFlush.setEnabled(true);
                        ivFlush.setImageResource(R.drawable.ib_flush);
                        break;
                    }

                    case Constants.PROCEDURE_FILL: {
                        disableAllRows();
                        rbDialisys.setEnabled(true);
                        ivDialisys.setImageResource(R.drawable.ib_dialisys);
                        break;
                    }

                    case Constants.PROCEDURE_SHUTDOWN: {
                        disableAllRows();
                        rbShutdown.setEnabled(true);
                        ivShutdown.setImageResource(R.drawable.ib_shutdown);
                        break;
                    }

                    case Constants.PROCEDURE_DISINFECTION: {
                        disableAllRows();
                        rbShutdown.setEnabled(true);
                        ivShutdown.setImageResource(R.drawable.ib_shutdown);
                        break;
                    }

                    case Constants.PROCEDURE_FLUSH: {
                        disableAllRows();
                        rbShutdown.setEnabled(true);
                        ivShutdown.setImageResource(R.drawable.ib_shutdown);
                        break;
                    }

                    default: {
                        enableAllRows();
                        break;
                    }
                }
                break;
            }

            default: {
                enableAllRows();
                break;
            }
        }

        SharedPreferences sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE); //Load preferences
        if (sPref.getBoolean(Constants.SETTINGS_TESTMODE, false))
            enableAllRows();
    }

    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.ib_fill:{
                resetRadioButtons();
                rbFill.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_FILL;
                break;
            }

            case R.id.ib_dialisys:{
                resetRadioButtons();
                rbDialisys.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_DIALYSIS;
                break;
            }

            case R.id.ib_flush:{
                resetRadioButtons();
                rbFlush.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_FLUSH;
                break;
            }

            case R.id.ib_shutdown:{
                resetRadioButtons();
                rbShutdown.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_SHUTDOWN;
                break;
            }

            case R.id.ib_disinfection:{
                resetRadioButtons();
                rbDisinfection.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_DISINFECTION;
                break;
            }
            case R.id.rb_fill:{
                resetRadioButtons();
                rbFill.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_FILL;
                break;
            }

            case R.id.rb_dialisys:{
                resetRadioButtons();
                rbDialisys.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_DIALYSIS;
                break;
            }

            case R.id.rb_flush:{
                resetRadioButtons();
                rbFlush.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_FLUSH;
                break;
            }

            case R.id.rb_shutdown:{
                resetRadioButtons();
                rbShutdown.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_SHUTDOWN;
                break;
            }

            case R.id.rb_disinfection:{
                resetRadioButtons();
                rbDisinfection.setChecked(true);
                selectedProcedure = Constants.PROCEDURE_DISINFECTION;
                break;
            }

            case R.id.ib_ok:{
                if(selectedProcedure != -1){
                    Intent intent = new Intent(this, InstructionActivity.class);
                    Bundle parameters = new Bundle();
                    parameters.putInt("procedure", selectedProcedure); //Your id
                    intent.putExtras(parameters); //Put your id to your next Intent
                    startActivity(intent);
                    ProceduresActivity.this.finish();
                }

                break;
            }

            default:
                break;
        }
    }

    public void resetRadioButtons(){
        rbFill.setChecked(false);
        rbDialisys.setChecked(false);
        rbFlush.setChecked(false);
        rbShutdown.setChecked(false);
        rbDisinfection.setChecked(false);
    }

    public void disableAllRows(){
        rbFill.setEnabled(false);
        ivFilling.setImageResource(R.drawable.ib_fill_disabled);
        rbDialisys.setEnabled(false);
        ivDialisys.setImageResource(R.drawable.ib_dialisys_disabled);
        rbFlush.setEnabled(false);
        ivFlush.setImageResource(R.drawable.ib_flush_disabled);
        rbShutdown.setEnabled(false);
        ivShutdown.setImageResource(R.drawable.ib_shutdown_disabled);
        rbDisinfection.setEnabled(false);
        ivDisinfection.setImageResource(R.drawable.ib_disinfection_disabled);
    }

    public void enableAllRows(){
        rbFill.setEnabled(true);
        ivFilling.setImageResource(R.drawable.ib_fill);
        rbDialisys.setEnabled(true);
        ivDialisys.setImageResource(R.drawable.ib_dialisys);
        rbFlush.setEnabled(true);
        ivFlush.setImageResource(R.drawable.ib_flush);
        rbShutdown.setEnabled(true);
        ivShutdown.setImageResource(R.drawable.ib_shutdown);
        rbDisinfection.setEnabled(true);
        ivDisinfection.setImageResource(R.drawable.ib_disinfection);
    }
}

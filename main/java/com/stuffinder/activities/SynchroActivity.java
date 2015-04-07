package com.stuffinder.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.stuffinder.R;

public class SynchroActivity extends Activity {


    private Switch mySwitch;
    private Button recup;

    public void retour10 (View view) {
        finish();
    }

    public void recup (View view) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchro);

        mySwitch = (Switch) findViewById(R.id.mySwitch);
        recup = (Button) findViewById(R.id.buttonRecup);

        //set the switch to OFF
        mySwitch.setChecked(false);
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    recup.setEnabled(false);
                }else{
                    recup.setEnabled(true);
                }

            }
        });

        //check the current state before we display the screen
        if(mySwitch.isChecked()){
            recup.setEnabled(false);
        }
        else {
            recup.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_synchro, menu);
        return true;
    }

}

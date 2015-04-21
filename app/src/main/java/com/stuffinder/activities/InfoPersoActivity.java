package com.stuffinder.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.stuffinder.data.Account;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;
import com.stuffinder.R;


public class InfoPersoActivity extends Activity {


    TextView nomTextView ;
    TextView prenomTextView ;
    TextView idTextView ;

    String currentEmail;
    EditText editTextMail ;

    EditText editTextMdp;
    EditText editTextConfirmMdp ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_info_perso);

        nomTextView = (TextView)findViewById(R.id.textViewNomVue);
        prenomTextView = (TextView)findViewById(R.id.textViewPrenomVue);
        idTextView = (TextView)findViewById(R.id.textViewIdVue);
        editTextMail= (EditText)findViewById(R.id.editTextMail);
        editTextMdp = (EditText)findViewById(R.id.editTextModp);
        editTextConfirmMdp = (EditText)findViewById(R.id.editTextConfirmPassword);


        Account account = null;
        try {
            account = NetworkServiceProvider.getNetworkService().getCurrentAccount();
            nomTextView.setText(account.getLastName());
            prenomTextView.setText(account.getFirstName());
            idTextView.setText(account.getPseudo());
            currentEmail = account.getEMailAddress();
            editTextMail.setText(currentEmail, TextView.BufferType.EDITABLE);
        } catch (NotAuthenticatedException e) {
            e.printStackTrace();
        }

    }

    public void retour5 (View view) {
        finish();
    }

    public void actionModifier(View view) {
        String email = editTextMail.getText().toString();
        String mdp = editTextMdp.getText().toString();
        String confirmMdp = editTextConfirmMdp.getText().toString();

        if(email.length() == 0)
            Toast.makeText(this, "Entrer email", Toast.LENGTH_LONG).show();
        else if(mdp.length() == 0 && confirmMdp.length() > 0)
            Toast.makeText(this, "Entrer mot de passe", Toast.LENGTH_LONG).show();
        else if(mdp.length() != 0 && confirmMdp.length() == 0)
            Toast.makeText(this, "Confirmer mot de passe", Toast.LENGTH_LONG).show();
        else if(! confirmMdp.equals(mdp))
            Toast.makeText(this, "Confirmation de mot de passe incorrecte", Toast.LENGTH_LONG).show();
        else
        {
            try {
                if(! email.equals(currentEmail)) // l'adresse email a changé.
                    NetworkServiceProvider.getNetworkService().modifyEMailAddress(editTextMail.getText().toString());

                if(mdp.length() > 0)
                    NetworkServiceProvider.getNetworkService().modifyPassword(editTextMdp.getText().toString());

                //Intent intent = new Intent (InfoPersoActivity.this,ConfigurationActivity.class);
                //startActivity(intent);
                finish();
            }
            catch (IllegalFieldException e) {
                switch (e.getFieldId()) {
                    case IllegalFieldException.EMAIL_ADDRESS:
                        Toast.makeText(this, "Email Incorrect", Toast.LENGTH_LONG).show();
                        break;
                    case IllegalFieldException.PASSWORD:
                        Toast.makeText(this, "Mot de passe incorect ; il doit contenir au moins 6 caractères", Toast.LENGTH_LONG).show();
                        break;
                }
            }
            catch (NotAuthenticatedException e) {
                Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
            }
            catch (NetworkServiceException e) {
                Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
            }
        }



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info_perso, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

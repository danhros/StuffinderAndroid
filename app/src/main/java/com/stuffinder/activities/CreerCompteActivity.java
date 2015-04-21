package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Account;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.engine.NetworkServiceProvider;

import java.lang.Override;


public class CreerCompteActivity extends Activity {

    EditText editTextNom ;
    EditText editTextPrenom;
    EditText editTextIdentifiant;
    EditText editTextEmail ;
    EditText editTextMdP ;
    EditText editTextConfirmPassword ;



    public void retourAccueil ( View view) {
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_creer_compte);

        editTextNom= (EditText)findViewById(R.id.editTextNom);
        editTextPrenom= (EditText)findViewById(R.id.editTextPrenom);
        editTextIdentifiant= (EditText)findViewById(R.id.editTextIdentifiant1);
        editTextEmail= (EditText)findViewById(R.id.editTextEmail);
        editTextMdP= (EditText)findViewById(R.id.editTextMdP);
        editTextConfirmPassword= (EditText)findViewById(R.id.editTextConfirmPassword);
    }




    public void creerCompte (View view) {


        Intent intentToReussite = new Intent(CreerCompteActivity.this, ReussiteCreationCompteActivity.class);   /* objet qui permet de passer un l'activité Home*/
        String nom = editTextNom.getText().toString();
        String prenom = editTextPrenom.getText().toString();
        String identifiant = editTextIdentifiant.getText().toString();
        String email = editTextEmail.getText().toString();
        String mdp = editTextMdP.getText().toString();
        String confirmMdp = editTextConfirmPassword.getText().toString();

        if(nom.length() == 0)
            Toast.makeText(CreerCompteActivity.this, "Entrez votre nom", Toast.LENGTH_LONG).show();
        else if(prenom.length() == 0)
            Toast.makeText(CreerCompteActivity.this, "Entrez votre prénom", Toast.LENGTH_LONG).show();
        else if(identifiant.length() == 0)
            Toast.makeText(CreerCompteActivity.this, "Entrez un pseudo", Toast.LENGTH_LONG).show();
        else if(email.length() == 0)
            Toast.makeText(CreerCompteActivity.this, "Entrez votre email", Toast.LENGTH_LONG).show();
        else if(mdp.length() == 0)
            Toast.makeText(CreerCompteActivity.this, "Entrez un mot de passe", Toast.LENGTH_LONG).show();
        else if(confirmMdp.length() == 0)
            Toast.makeText(CreerCompteActivity.this, "Confirmez le mot de passe", Toast.LENGTH_LONG).show();
        else if(! confirmMdp.equals(mdp))
            Toast.makeText(CreerCompteActivity.this, "Confirmation de mot de passe incorrecte", Toast.LENGTH_LONG).show();
        else
        {
            try {

                Account account = new Account(identifiant, prenom, nom, email);                            /* Création d'un compte*/
                NetworkServiceProvider.getNetworkService().createAccount(account, mdp);                                        /*Demande de création de compte au web service */

                finish();
                startActivity(intentToReussite);
            }

            catch (IllegalFieldException e) {
                switch (e.getFieldId()) {
                    case IllegalFieldException.PSEUDO:
                        if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                            Toast.makeText(CreerCompteActivity.this, "Pseudo déja utilise", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(CreerCompteActivity.this, "Pseudo incorrect", Toast.LENGTH_LONG).show();
                        break;
                    case IllegalFieldException.FIRSTNAME:
                        Toast.makeText(CreerCompteActivity.this, "Prénom incorrect", Toast.LENGTH_LONG).show();
                    case IllegalFieldException.LASTNAME:
                        Toast.makeText(CreerCompteActivity.this, "Nom incorrect", Toast.LENGTH_LONG).show();
                        break;
                    case IllegalFieldException.EMAIL_ADDRESS:
                        Toast.makeText(CreerCompteActivity.this, "Email Incorrect", Toast.LENGTH_LONG).show();
                        break;
                    case IllegalFieldException.PASSWORD:
                        Toast.makeText(CreerCompteActivity.this, "Mot de passe incorect ; il doit contenir au moins 6 caractères", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            catch (NetworkServiceException e) {
                Toast.makeText(CreerCompteActivity.this, "Problème avec le serveur, la création de compte n'a pas fonctionné", Toast.LENGTH_LONG).show();
            }
        }



    }



    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_creer_compte, menu);
        return true;
    }


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

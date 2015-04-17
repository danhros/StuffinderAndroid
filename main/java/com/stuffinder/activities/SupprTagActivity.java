package com.stuffinder.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SupprTagActivity extends Activity {

    private ListView mListSuppr = null;
    private ArrayAdapter<Tag> tagArrayAdapter;

    private Button mSend = null;


    private static List<Tag> tagsList = new ArrayList<>();

    public void retour4 (View view) {
        finish();
    }

    void supprimerTagsSelectionnes(){

        SparseBooleanArray tab = mListSuppr.getCheckedItemPositions() ;

        boolean errorOccured = false;
        boolean oneTagRemoved = false;

        int i=0;
        try {

            for(i=0; i< tagsList.size(); i++) {
                if (tab.get(i) == true) {
                    NetworkServiceProvider.getNetworkService().removeTag(tagsList.get(i));
                    oneTagRemoved = true;
                }
            }

            finish();
        } catch (IllegalFieldException e) {// abnormal error.
            if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                Toast.makeText(this, "Suppresion impossible : le tag \"" + tagsList.get(i).getObjectName() + "\" a déjà été supprimé.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
            errorOccured = true;
        } catch (NotAuthenticatedException e) {// abnormal error.
            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
            errorOccured = true;
        } catch (NetworkServiceException e) {
            Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
            errorOccured = true;
        }

        if(errorOccured && oneTagRemoved) { // to try to reload tags list because one tag or more has been removed.
            try {
                changeTagsList(NetworkServiceProvider.getNetworkService().getTags());

                tagArrayAdapter.clear();
                tagArrayAdapter.addAll(tagsList);
            } catch (NotAuthenticatedException | NetworkServiceException e) {
                //
            }
        }
    }

    public void actionSupprimerTagsSelectionnes(View view){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Vous êtes sur le point de supprimer les tags sélectionnés.")
                .setTitle("Supprimer les tags sélectionnés ?");

        // Add the buttons
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                supprimerTagsSelectionnes();
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });


        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_suppr_tag);

        mListSuppr = (ListView) findViewById(R.id.listSuppr);

        mSend = (Button) findViewById(R.id.send);

        tagArrayAdapter = new ArrayAdapter<Tag>(this, android.R.layout.simple_list_item_multiple_choice);
        tagArrayAdapter.addAll(tagsList);

        mListSuppr.setAdapter(tagArrayAdapter);




    }

    public static void changeTagsList(List<Tag> list)
    {
        tagsList.clear();

        tagsList.addAll(list);

        Collections.sort(tagsList, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return lhs.getObjectName().compareTo(rhs.getObjectName());
            }
        });
    }

}


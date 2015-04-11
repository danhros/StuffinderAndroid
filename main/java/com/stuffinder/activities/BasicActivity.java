package com.stuffinder.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.engine.EngineServiceProvider;
import com.stuffinder.engine.FieldVerifier;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.io.File;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nthproprio on 28/03/2015.
 */
public class BasicActivity extends Activity
{
    private static Stack<BasicActivity> activityStack = new Stack<>();

    public static BasicActivity getCurrentActivity()
    {
        return activityStack.peek();
    }



    @Override
    protected void onStart() {
        super.onStart();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Activity \"" + getClass() + "\" started.");
        activityStack.push(this);
    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//        activityStack.pop();
//    }

    @Override
    public void finish() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Activity \"" + getClass() + "\" finished.");
        activityStack.pop();
        super.finish();
    }


    public synchronized void showErrorMessage(String message)
    {
        runOnUiThread(new ErrorMessageProcessor(message));
    }

    class ErrorMessageProcessor implements Runnable{
        private String errorMessage;
        ErrorMessageProcessor(String errorMessage)
        {
            this.errorMessage = errorMessage;
        }
        @Override
        public void run() {
            Toast.makeText(BasicActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }


    public void askPasswordAfterError()
    {
        runOnUiThread(new PasswordUpdater());
    }

    class PasswordUpdater implements Runnable
    {
        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(BasicActivity.this);
            // Get the layout inflater
            LayoutInflater inflater = BasicActivity.this.getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View view = inflater.inflate(R.layout.password_dialog, null);

            try {
                ((EditText) view.findViewById(R.id.password_dialog_pseudo_field)).setText(EngineServiceProvider.getEngineService().getCurrentAccount().getPseudo());
            } catch (NotAuthenticatedException e) { // will never occur.
                e.printStackTrace();
                return;
            }

            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton(R.string.password_dialog_validate_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            AlertDialog alertDialog = (AlertDialog) dialog;

                            EditText editText = (EditText) alertDialog.findViewById(R.id.password_dialog_password_field);
                            String password = editText.getText().toString();

                            Logger.getLogger(getClass().getName()).log(Level.INFO, "Validate button pressed : password is : " + password);
                            if(FieldVerifier.verifyPassword(password))
                            {
                                try {
                                    EngineServiceProvider.getEngineService().resolveAutoSynchronizationErrorOnPassword(password);
                                    alertDialog.dismiss();
                                    onValidateButton();
                                } catch (NotAuthenticatedException e) { // will never occur.
                                    Toast.makeText(((AlertDialog) dialog).getContext(), "Une erreur est survenue.", Toast.LENGTH_LONG).show();
                                }
                            }
                            else
                                Toast.makeText(((AlertDialog) dialog).getContext(), "Mot de passe incorrect.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.password_dialog_cancel_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Logger.getLogger(getClass().getName()).log(Level.INFO, "Cancel button pressed.");
                            dialog.cancel();
                            onCancelButton();
                        }
                    });
            builder.show();
        }
    }

    public File getImageFileByResource(int resourceId)
    {
        String resourceEntryName = getResources().getResourceEntryName(resourceId);

        return new File(getFilesDir().getAbsolutePath() + File.separator + "default_images", resourceEntryName + ".png");
    }

    /**
     * Override this method to add processing if the validate button has been pressed and the password is given successfully to the engine service.
     */
    protected void onValidateButton()
    {

    }

    /**
     * Override this method to add processing if the cancel button has been pressed.
     */
    protected void onCancelButton()
    {

    }
}

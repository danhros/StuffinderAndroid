package com.stuffinder.activities;

import android.app.Activity;
import android.widget.Toast;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by propriétaire on 28/03/2015.
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


    public void showErrorMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }

}

package com.stuffinder.engine;

import android.graphics.BitmapFactory;

import java.io.File;

/**
 * This class provides methods to verify if fields like passwords or e-mail addresses are syntactically correct.
 * @author propriétaire
 *
 */
public class FieldVerifier
{

    public static boolean verifyName(String value)
    {
        return value.length() > 0;
    }

    public static boolean verifyEMailAddress(String email)
    {
        return email.indexOf('@') >=0;
    }

    public static boolean verifyPassword(String password)
    {
        return password.length() >= 6;
    }

    public static boolean verifyTagUID(String uid)
    {
        return uid.length() > 0;
    }

    public static boolean verifyTagName(String name)
    {
        return name.length() > 0;
    }

    public static boolean verifyImageFileName(String imageFileName)
    {
        File file = new File(imageFileName);

        if (file == null || !file.exists()) {
            return false;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        return options.outWidth != -1 && options.outHeight != -1;
    }




//	public static boolean verify(String value)
//	{
//		return true;
//	}

}

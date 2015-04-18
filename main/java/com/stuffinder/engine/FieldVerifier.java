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
        return uid.matches("([0-9A-F][0-9A-F]:){5}[0-9A-F][0-9A-F]");
    }

    public static boolean verifyTagName(String name)
    {
        return name.length() > 0;
    }

    public static boolean verifyImageFileName(String imageFileName)
    {
        return verifyImageFileName(new File(imageFileName));
    }
    public static boolean verifyImageFileName(File imageFile)
    {
        if (imageFile == null || !imageFile.exists())
            return false;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        return options.outWidth != -1 && options.outHeight != -1;
    }




//	public static boolean verify(String value)
//	{
//		return true;
//	}

}

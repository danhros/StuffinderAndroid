package com.stuffinder.engine;



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

    @SuppressWarnings("unused")
    public static boolean verifyImageFileName(String imageFileName)
    {/*
        try {
            if(imageFileName == null)
                throw new NullPointerException();

            new ImageIcon(ImageIO.read(new File(imageFileName))); // to test if the file can be loaded as an image.

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }*/
        return true;
    }




//	public static boolean verify(String value)
//	{
//		return true;
//	}

}

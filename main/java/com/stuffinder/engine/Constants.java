package com.stuffinder.engine;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by propriétaire on 06/04/2015.
 */
public class Constants {

    private static File imageFolder;

    static void setImageFolder(File imageFolder)
    {
        Constants.imageFolder = imageFolder;
    }

    public static File getImageFolder() {
        return imageFolder;
    }
}

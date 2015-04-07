package com.stuffinder.engine;

import com.stuffinder.data.Tag;

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

    public static File getTagImageFile(Tag tag)
    {
        return tag.getObjectImageName() == null || tag.getObjectImageName().length() == 0 ? null : new File(imageFolder.getAbsolutePath(), tag.getUid() + "_" + ".jpg"); // TODO traiter les differents types de fichiers images.
    }


}

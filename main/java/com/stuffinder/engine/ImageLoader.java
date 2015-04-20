package com.stuffinder.engine;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by propriétaire on 13/04/2015.
 */
public class ImageLoader {

    private Semaphore mutex;
    private HashMap<File, Bitmap> loadedImagesWithLowSize;

    private ImageLoader() {
        mutex = new Semaphore(1);
        loadedImagesWithLowSize = new HashMap<>();
    }

    public Bitmap getImageAtLowSize(File imageFile) throws FileNotFoundException
    {
        if(! imageFile.exists())
        {
            mutex.acquireUninterruptibly();
            loadedImagesWithLowSize.remove(imageFile);
            mutex.release();

            throw new FileNotFoundException(imageFile.getAbsolutePath());
        }

        mutex.acquireUninterruptibly();
        Bitmap result = loadedImagesWithLowSize.get(imageFile);
        mutex.release();

        if(result != null)
        {
            Logger.getLogger(ImageLoader.class.getName()).log(Level.INFO, "Image file \"" + imageFile.getAbsolutePath() + "\" found in map.");
            return result;
        }

        result = FileManager.loadImageForList(imageFile);
        if(result == null)
            return null;
        else
        {
            mutex.acquireUninterruptibly();
            loadedImagesWithLowSize.put(imageFile, result);
            mutex.release();

            Logger.getLogger(ImageLoader.class.getName()).log(Level.INFO, "Image file \"" + imageFile.getAbsolutePath() + "\" loaded.");
            return result;
        }
    }


    public boolean reloadImageAtLowSize(File imageFile) throws FileNotFoundException
    {
        Bitmap result = FileManager.loadImageForList(imageFile);
        if(result == null)
            return false;
        else
        {
            mutex.acquireUninterruptibly();
            loadedImagesWithLowSize.put(imageFile, result);
            mutex.release();
            Logger.getLogger(ImageLoader.class.getName()).log(Level.INFO, "Image file \"" + imageFile.getAbsolutePath() + "\" reloaded.");

            return true;
        }
    }

    public boolean removeImageLoadedAtLowSize(File imageFile)
    {
        if(imageFile == null)
            return false;

        mutex.acquireUninterruptibly();
        boolean result = loadedImagesWithLowSize.remove(imageFile) != null;
        mutex.release();

        return result;
    }


    private static ImageLoader instance = new ImageLoader();

    public static ImageLoader getInstance() {
        return instance;
    }
}

package com.stuffinder.engine;

import android.util.Log;

import com.stuffinder.data.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by propriétaire on 06/04/2015.
 */
public class FileManager {

    private static File requestImageFolder;
    private static File userImageFolder;
    private static File autoSyncImageFolder;

    private static File rootFolder;

    static void initFileManager(String rootFolderPath) throws IllegalArgumentException, IOException {
        rootFolder = new File(rootFolderPath);

        Logger.getLogger(FileManager.class.getName()).log(Level.INFO, "file manager initialization begins with folder \"" + rootFolderPath + "\" as root folder");

        if(! rootFolder.exists() || ! rootFolder.isDirectory())
            throw new IllegalArgumentException("path " + rootFolder + "doesn't exist or is not a directory");

        requestImageFolder = new File(rootFolder, "images/requests");
        if(! requestImageFolder.exists())
        {
            if(! requestImageFolder.mkdirs())
                throw new IOException("creation of folder images/requests has failed.");

            Logger.getLogger(FileManager.class.getName()).log(Level.INFO, "request folder created.");
        }
        else
            cleanFolder(requestImageFolder);


        userImageFolder = new File(rootFolder, "images/user");
        if(! userImageFolder.exists())
        {
            if(! userImageFolder.mkdirs())
                throw new IOException("creation of folder images/user has failed.");

            Logger.getLogger(FileManager.class.getName()).log(Level.INFO, "user folder created.");
        }
        else
            cleanFolder(userImageFolder);

        autoSyncImageFolder = new File(rootFolder, "images/autoSync");
        if(! autoSyncImageFolder.exists())
        {
            if(! autoSyncImageFolder.mkdirs())
                throw new IOException("creation of folder images/autoSync has failed.");

            Logger.getLogger(FileManager.class.getName()).log(Level.INFO, "autoSync folder created.");
        }
        else
            cleanFolder(autoSyncImageFolder);

        Logger.getLogger(FileManager.class.getName()).log(Level.INFO, "file manager initialization ends successfully.");
    }


    public static File getRequestImageFolder() {
        return requestImageFolder;
    }

    public static File getUserImageFolder() {
        return userImageFolder;
    }

    public static File getAutoSyncImageFolder() {
        return autoSyncImageFolder;
    }




    static boolean copyFileToRequestFolder(File file, String filename) throws FileNotFoundException {
        File newFile = new File(requestImageFolder, filename);

        return copyFile(file, newFile);
    }

    static boolean copyFileToUserFolder(File file, String filename) throws FileNotFoundException {
        File newFile = new File(userImageFolder, filename);

        return copyFile(file, newFile);
    }

    static boolean copyFileToAutoSyncFolder(File file, String filename) throws FileNotFoundException {
        File newFile = new File(autoSyncImageFolder, filename);

        return copyFile(file, newFile);
    }

    static boolean copyFileFromAutoSyncFolderToUserFolder(Tag tag) throws FileNotFoundException {
        File original = getTagImageFileForAutoSynchronization(tag);
        File destination = getTagImageFileForUser(tag);

        return copyFile(original, destination);
    }

    static boolean moveFileFromRequestFolderToAutoSyncFolder(int requestNumber, Tag associatedTag) throws FileNotFoundException {
        File fileMoved = getTagImageFileForAutoSynchronization(associatedTag);
        File file = getTagImageFileForRequest(requestNumber);

        return moveFile(file, fileMoved);
    }
    static boolean copyFileFromRequestFolderToUserFolder(int requestNumber, Tag associatedTag) throws FileNotFoundException {
        File original = getTagImageFileForRequest(requestNumber);
        File newFile = getTagImageFileForUser(associatedTag);

        return copyFile(original, newFile);
    }


    static boolean removeFileFromRequestFolder(String filename)
    {
        return new File(requestImageFolder, filename).delete();
    }

    static boolean removeFileFromUserFolder(String filename)
    {
        return new File(userImageFolder, filename).delete();
    }

    static boolean removeFileFromAutoSyncFolder(String filename)
    {
        return new File(autoSyncImageFolder, filename).delete();
    }


    static void cleanFolder(File folder)
    {
        if(folder.exists() && folder.isDirectory())
        {
            File files[] = folder.listFiles();

            for(File file : files)
                file.delete();
        }
    }

    static boolean copyFile(File file, File newFile) throws FileNotFoundException {

        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(file);
            out = new FileOutputStream(newFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();

            return true;
        }  catch (FileNotFoundException e) {
            Log.e("tag", e.getMessage());
            throw e;
        } catch (IOException e) { // to be sure streams are closed.
            if(in != null)
                try {
                    in.close();
                } catch (Exception e1) {
                }
            if(out != null)
                try {
                    out.close();
                } catch (Exception e1) {
                }
            return false;
        }
    }

    static boolean moveFile(File file, File newFile) throws FileNotFoundException {
        return copyFile(file, newFile) && file.delete();
    }



    public static File getTagImageFileForRequest(int requestNumber)
    {
        return new File(requestImageFolder.getAbsolutePath(), "img_" + requestNumber + ".jpg"); // TODO traiter les differents types de fichiers images.
    }

    public static File getTagImageFileForUser(Tag tag)
    {
        return new File(userImageFolder.getAbsolutePath(), getFilenameFromTag(tag)); // TODO traiter les differents types de fichiers images.
    }

    public static File getTagImageFileForAutoSynchronization(Tag tag)
    {
        return new File(autoSyncImageFolder.getAbsolutePath(), getFilenameFromTag(tag)); // TODO traiter les differents types de fichiers images.
    }

    private static String getFilenameFromTag(Tag tag)
    {
        return tag.getUid().replaceAll("\\:", "_") + ".jpg";
    }
}

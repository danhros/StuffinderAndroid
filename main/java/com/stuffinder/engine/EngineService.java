package com.stuffinder.engine;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.stuffinder.activities.BasicActivity;
import com.stuffinder.data.Account;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.exceptions.AccountNotFoundException;
import com.stuffinder.exceptions.EngineServiceException;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;
import com.stuffinder.exceptions.SynchronisationConflictException;

import static com.stuffinder.engine.Requests.*;
import static com.stuffinder.exceptions.IllegalFieldException.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by propriétaire on 14/03/2015.
 */
public class EngineService {


    /**
     * Mutex used to be sure auto-synchroniser and the rest of the engine service doesn't acces or modify account data at same time.
     */

    private Account currentAccount; // non null if authentication done, null otherwise.
    private String currentPassword; // non null if authentication done, null otherwise.

    private List<Tag> tags;
    private List<Profile> profiles;

    /**
     * The requests to do throw modifications on the server. Used when auto-synchronization is disabled.
     */
    private List<Requests.Request> requests;

    public EngineService()
    {
        tags = new ArrayList<>();
        profiles = new ArrayList<>();

        requests = new ArrayList<>();
    }

    public void initEngineService(Context context) throws EngineServiceException {
        File rootFolder = context.getApplicationContext().getFilesDir();

        try {
            FileManager.initFileManager(rootFolder.getAbsolutePath());
        } catch (IOException e) {
            throw new EngineServiceException("File manager initialization failed.");
        }

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private ConnectivityManager connectivityManager;

    private boolean isInternetConnectionDone()
    {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


    public void createAccount(Account newAccount, String newPassword) throws IllegalFieldException, NetworkServiceException {
        NetworkServiceProvider.getNetworkService().createAccount(newAccount, newPassword);
    }

    public Account authenticate(String pseudo, String password) throws AccountNotFoundException, NetworkServiceException
    {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Authentication will be performed with pseudo = " + pseudo);

        if(currentAccount != null)
            logOut();

        try {
            currentAccount = NetworkServiceProvider.getNetworkService().authenticate(pseudo, password);
            Logger.getLogger(getClass().getName()).log(Level.INFO, "account is : " + currentAccount);
            tags.addAll(NetworkServiceProvider.getNetworkService().getTags());
            int lastTagsUpdate = NetworkServiceProvider.getNetworkService().getLastTagsUpdateTime();
            int lastProfilesUpdate = NetworkServiceProvider.getNetworkService().getLastProfilesUpdateTime();

            List<Profile> profileList = NetworkServiceProvider.getNetworkService().getProfiles();

            for(Profile profile : profileList)
            {
                Profile tmp = new Profile(profile.getName());

                for(Tag tag : profile.getTags())
                    tmp.addTag(tags.get(tags.indexOf(tag)));

                profiles.add(tmp);
            }
            currentPassword = password;

            if(isAutoSynchronizationEnabled()) // to start auto-synchronization.
            {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Start auto-synchronization thread");
                autoSynchronizer.startAutoSynchronization(currentAccount, tags, profiles, currentPassword, lastTagsUpdate, lastProfilesUpdate);
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Auto-synchronization thread started");
            }

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Authentication done.");
        } catch (NotAuthenticatedException e) { // will normally never occur.
            currentAccount = null;
            tags.clear();
            profiles.clear();
            throw new NetworkServiceException("");
        } catch (NetworkServiceException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "A network service error has occured :    " + e.getMessage());
            e.printStackTrace();
            currentAccount = null;
            tags.clear();
            profiles.clear();
            throw e;
        }

        return currentAccount;
    }

    public void logOut() {

        Logger.getLogger(getClass().getName()).log(Level.INFO, "Log out will be done.");

        if(isAutoSynchronizationEnabled()) // to stop and reinitialize auto-synchronization.
        {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Stop auto-synchronization thread");
            autoSynchronizer.stopAutoSynchronization();
            autoSynchronizer = new AutoSynchronizer();
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Auto-synchronization thread stopped");
        }

        FileManager.cleanImageFolders();

        currentAccount = null;
        currentPassword = null;

        tags.clear();
        profiles.clear();

        Logger.getLogger(getClass().getName()).log(Level.INFO, "Log out operation done.");
    }

    public Account getCurrentAccount() throws NotAuthenticatedException {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();

        return currentAccount;
    }

    public void modifyEMailAddress(String newEmailAddress) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(! FieldVerifier.verifyEMailAddress(newEmailAddress))
            throw  new IllegalFieldException(IllegalFieldException.EMAIL_ADDRESS, IllegalFieldException.REASON_VALUE_INCORRECT, newEmailAddress);

        if(! newEmailAddress.equals(currentAccount.getEMailAddress()))
        {
            currentAccount.setMailAddress(newEmailAddress);
            addRequest(new ModifyEmailRequest(newEmailAddress));
        }
    }

    public void modifyPassword(String newPassword) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(! FieldVerifier.verifyPassword(newPassword))
            throw  new IllegalFieldException(IllegalFieldException.PASSWORD, IllegalFieldException.REASON_VALUE_INCORRECT, newPassword);

        addRequest(new ModifyPasswordRequest(newPassword));
    }

    public void modifyBraceletUID(String braceletUID) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(! FieldVerifier.verifyTagUID(braceletUID))
            throw new IllegalFieldException(BRACELET_UID, REASON_VALUE_INCORRECT, braceletUID);

        addRequest(new ModifyBraceletUIDRequest(braceletUID));
        currentAccount.setBraceletUID(braceletUID);
    }


    public List<Tag> getTags() throws NotAuthenticatedException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        return tags;
    }

    public Tag addTag(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return addTag(tag, tag.getObjectImageName() == null || tag.getObjectImageName().length() == 0 ? null : new File(tag.getObjectImageName()));
    }

    public Tag addTag(Tag tag, File imageFile) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
        if(! FieldVerifier.verifyName(tag.getObjectName()))
            throw new IllegalFieldException(TAG_OBJECT_NAME, REASON_VALUE_INCORRECT, tag.getObjectName());
        if(imageFile != null && ! FieldVerifier.verifyImageFileName(imageFile))
            throw new IllegalFieldException(TAG_OBJECT_IMAGE, REASON_VALUE_INCORRECT, tag.getObjectImageName());

        for(Tag tmp : tags)
        {
            if(tmp.getObjectName().equals(tag.getObjectName()))
                throw new IllegalFieldException(TAG_OBJECT_NAME, REASON_VALUE_ALREADY_USED, tag.getObjectName());
            else if(tmp.getUid().equals(tag.getUid()))
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_ALREADY_USED, tag.getUid());
        }

        AddTagRequest request = new AddTagRequest(new Tag(tag.getUid(), tag.getObjectName(), imageFile == null ? null : imageFile.getPath()));

        Tag tmp = new Tag(tag.getUid(), tag.getObjectName(), null);
        tmp.setImageVersion(-1);

        if(imageFile != null) // if an image is added.
        {
            try {
                FileManager.copyFileToRequestFolder(imageFile, request.getRequestNumber());
                FileManager.copyFileToUserFolder(imageFile, tmp);

                tmp.setObjectImageName(FileManager.getTagImageFileForUser(tmp).getAbsolutePath());
            } catch (FileNotFoundException e) {
                throw new IllegalFieldException(TAG_OBJECT_IMAGE, REASON_VALUE_NOT_FOUND, imageFile.getPath());
            }
        }
        tags.add(tmp);

        addRequest(request); // new tag to be sure it will not be modified.

        return tmp;
    }

    public Tag modifyObjectName(Tag tag, String newObjectName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());

        if(! FieldVerifier.verifyTagName(newObjectName))
            throw new IllegalFieldException(TAG_OBJECT_NAME, REASON_VALUE_INCORRECT, newObjectName);

        for(Tag tmp : tags)
            if(tmp.getObjectName().equals(newObjectName) && ! tmp.getUid().equals(tag.getUid()))
                throw new IllegalFieldException(TAG_OBJECT_NAME, REASON_VALUE_ALREADY_USED, newObjectName);


        int index = tags.indexOf(tag);

        if(index < 0)
            throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_NOT_FOUND, tag.getUid());
        else
        {
            Tag tmp = tags.get(index);
            tmp.setObjectName(newObjectName);

            addRequest(new ModifyTagObjectNameRequest(tag, newObjectName));
            return tmp;
        }
    }


    public Tag modifyObjectImage(Tag tag, String newImageFileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        newImageFileName = newImageFileName == null ? "" : newImageFileName;

        return modifyObjectImage(tag, newImageFileName.length() == 0 ? null : new File(newImageFileName));
    }

    public Tag modifyObjectImage(Tag tag, File newImageFile) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
        if(newImageFile != null && ! FieldVerifier.verifyImageFileName(newImageFile))
            throw new IllegalFieldException(TAG_OBJECT_IMAGE, REASON_VALUE_INCORRECT, newImageFile.getPath());

        int index = tags.indexOf(tag);

        if(index < 0)
            throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_NOT_FOUND, tag.getUid());
        else
        {
            Tag tmp = tags.get(index);

            ModifyTagObjectImageRequest request = new ModifyTagObjectImageRequest(tag, newImageFile == null ? null : newImageFile.getPath());

            if(newImageFile != null) // if the image is added or modified.
            {
                try {
                    FileManager.copyFileToRequestFolder(newImageFile, request.getRequestNumber());
                    FileManager.copyFileToUserFolder(newImageFile, tmp);

                    tmp.setObjectImageName(FileManager.getTagImageFileForUser(tmp).getAbsolutePath());
                } catch (FileNotFoundException e) {
                    throw new IllegalFieldException(TAG_OBJECT_IMAGE, REASON_VALUE_NOT_FOUND, newImageFile.getPath());
                }
            }
            else if(tmp.getObjectImageName() != null && tmp.getObjectImageName().length() > 0) // if the image is removed.
            {
                FileManager.removeFileFromUserFolder(tmp.getObjectImageName());
                tmp.setObjectImageName(null);
            }

            tmp.setImageVersion(tmp.getImageVersion() - 1);
            addRequest(request);

            return tmp;
        }
    }

    public void removeTag(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());

        int index = tags.indexOf(tag);

        if(index < 0)
            throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_NOT_FOUND, tag.getUid());
        else
        {
            Tag tmp = tags.get(index);
            addRequest(new RemoveTagRequest(tmp));

            if(tmp.getObjectImageName() != null && tmp.getObjectImageName().length() > 0) // removes the associated image if there is one.
                FileManager.removeFileFromUserFolder(tmp.getObjectImageName());

            tags.remove(index);
        }
    }



    public Profile createProfile(String profileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return createProfile(profileName, new ArrayList<Tag>());
    }


    public Profile createProfile(String profileName, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(!FieldVerifier.verifyName(profileName))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profileName);

        Profile profile = new Profile(profileName);

        if(profiles.contains(profile))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_ALREADY_USED, profileName);


        for(Tag tag : tagList)
        {
            if(! FieldVerifier.verifyTagUID(tag.getUid())) // If this tag UID isn't incorrect.
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());

            int tagIndex = tags.indexOf(tag);

            if(tagIndex < 0) // if no tag of the current account has this UID.
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
            else if(! profile.getTags().contains(tag)) // to have this tag at most once.
                profile.getTags().add(tags.get(tagIndex));
        }

        Profile tmp = new Profile(profileName);
        tmp.getTags().addAll(profile.getTags());

        addRequest(new CreateProfileWithTagsRequest(tmp));

        return profile;
    }

    public Profile addTagToProfile(Profile profile, Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        List<Tag> tagsToAdd = new ArrayList<>();
        tagsToAdd.add(tag);

        return addTagsToProfile(profile, tagsToAdd);
    }

    public Profile addTagsToProfile(Profile profile, List<Tag> tagsToAdd) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(!FieldVerifier.verifyName(profile.getName()))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());

        int index = profiles.indexOf(profile);

        if(index < 0)
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());

        Profile profile1 = profiles.get(index);


        Set<Tag> tmp = new HashSet<Tag>();

        for(Tag tag : tagsToAdd)
        {
            if(! FieldVerifier.verifyTagUID(tag.getUid())) // If this tag UID isn't incorrect.
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());

            int tagIndex = tags.indexOf(tag);

            if(tagIndex < 0) // if no tag of the current account has this UID.
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
            else if(! profile1.getTags().contains(tag))
                tmp.add(tags.get(tagIndex));
        }

        if(tmp.size() > 0)
        {
            profile1.getTags().addAll(tmp);
            addRequest(new AddTagsToProfileRequest(profile.getName(), new ArrayList<>(tmp)));

            return profile1;
        }
        else
            return null; // because this profile is not modified.
    }

    public Profile removeTagFromProfile(Profile profile, Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        List<Tag> tagToRemove = new ArrayList<>();
        tagToRemove.add(tag);

        return removeTagsFromProfile(profile, tagToRemove);
    }

    public Profile removeTagsFromProfile(Profile profile, List<Tag> tagsToRemove) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(!FieldVerifier.verifyName(profile.getName()))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());

        int index = profiles.indexOf(profile);

        if(index < 0)
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());

        Profile profile1 = profiles.get(index);


        Set<Tag> tmp = new HashSet<>();

        for(Tag tag : tagsToRemove)
        {
            if(! FieldVerifier.verifyTagUID(tag.getUid())) // If this tag UID isn't incorrect.
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());

            int tagIndex = tags.indexOf(tag);

            if(tagIndex < 0) // if no tag of the current account has this UID.
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
            else if(profile1.getTags().contains(tag))
                tmp.add(tags.get(tagIndex));
        }

        if(tmp.size() > 0)
        {
            profile1.getTags().removeAll(tmp);
            addRequest(new RemoveTagsFromProfileRequest(profile.getName(), new ArrayList<>(tmp)));
            return profile1;
        }
        else
            return null; // because this profile is not modified.
    }

    public Profile replaceTagListOfProfile(Profile profile, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(!FieldVerifier.verifyName(profile.getName()))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());

        int index = profiles.indexOf(profile);

        if(index < 0)
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());

        Profile profile1 = profiles.get(index);


        Set<Tag> tmp = new HashSet<>();

        for(Tag tag : tagList)
        {
            if(! FieldVerifier.verifyTagUID(tag.getUid())) // If this tag UID isn't incorrect.
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());

            int tagIndex = tags.indexOf(tag);

            if(tagIndex < 0) // if no tag of the current account has this UID.
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
            else
                tmp.add(tags.get(tagIndex));
        }

        if(profile1.getTags().size() > 0 || tmp.size() > 0)
        {
            profile1.getTags().clear();
            profile1.getTags().addAll(tmp);
            addRequest(new ReplaceTagListOfProfileRequest(profile.getName(), new ArrayList<>(tmp)));

            return profile1;
        }
        else
            return null; // to indicate the profile is not modified.
    }

    public Profile modifyProfileName(Profile profile, String newProfileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(!FieldVerifier.verifyName(newProfileName))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, newProfileName);

        int index = profiles.indexOf(new Profile(profile.getName()));

        if(index < 0)
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());

        Profile tmp = profiles.get(index);

        for(Profile otherProfile : profiles)
            if(otherProfile != tmp && otherProfile.getName().equals(newProfileName))
                throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_ALREADY_USED, newProfileName);

        addRequest(new ModifyProfileNameRequest(profile.getName(), newProfileName));
        tmp.setName(newProfileName);

        return tmp;
    }


    public void removeProfile(Profile profile) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(!FieldVerifier.verifyName(profile.getName()))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());

        int index = profiles.indexOf(profile);

        if(index < 0)
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());
        else
        {
            addRequest(new RemoveProfileRequest(profile.getName()));
            profiles.remove(index);
        }
    }

    public Profile getProfile(String profileName) throws NotAuthenticatedException, NetworkServiceException, IllegalFieldException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        if(!FieldVerifier.verifyName(profileName))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profileName);

        int index = profiles.indexOf(new Profile(profileName));

        if(index < 0)
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profileName);
        else
            return profiles.get(index);
    }

    public List<Profile> getProfiles() throws NotAuthenticatedException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();
        checkAutoSynchronizerState();

        return profiles;
    }


    /**
     * To use only for manual synchronization.
     * @param request The request to be added for the next manual synchronization.
     */
    private void addRequest(Request request)
    {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Add request : " + request);

        if(isAutoSynchronizationEnabled())
            autoSynchronizer.appendRequest(request);
        else
            requests.add(request);

    }

// for manual synchronization.
//TODO optimization to resolve possible conflicts.
    public void synchroniseWithServer() throws NotAuthenticatedException, NetworkServiceException, SynchronisationConflictException, EngineServiceException {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        Synchroniser synchroniser = new Synchroniser(new ArrayList<>(requests));

        synchroniser.start();
        try {
            synchroniser.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new EngineServiceException();
        }

        if(! synchroniser.isSychronizationSuccessFul())
        {
            for(int i=0; i < synchroniser.getRequestOnFailureIndex(); i++)
                requests.remove(0);

            if(synchroniser.hasFailedOnConflict())
                throw new SynchronisationConflictException();
            else
                throw synchroniser.getCatchedNetworkServiceException();
        }
        else
            requests.clear();
    }


    class Synchroniser extends Thread
    {
        private List<Request> requests;
        private int requestOnFailureIndex;

        private NetworkServiceException exception;
        private boolean failedOnConflict;

        private boolean sychronisationSuccessFul;

        Synchroniser(List<Request> requests)
        {
            this.requests = requests;
            failedOnConflict = false;
            sychronisationSuccessFul = false;
        }

        @Override
        public void run()
        {
            boolean continueSync = true;
            for(int i=0; continueSync && i < requests.size(); i++)
            {
                Request request = requests.get(i);

                Logger.getLogger(getClass().getName()).log(Level.INFO, "Begins request processing :   " + request);

                try {
                    switch (request.getRequestType()) {
                        case MODIFY_EMAIL:
                            ModifyEmailRequest modifyEmailRequest = (ModifyEmailRequest) request;
                            NetworkServiceProvider.getNetworkService().modifyEMailAddress(modifyEmailRequest.getNewEmailAddress());
                            break;
                        case MODIFY_PASSWORD:
                            ModifyPasswordRequest modifyPasswordRequest = (ModifyPasswordRequest) request;
                            NetworkServiceProvider.getNetworkService().modifyPassword(modifyPasswordRequest.getNewPassword());
                            break;
                        case ADD_TAG:
                            AddTagRequest addTagRequest = (AddTagRequest) request;
                            NetworkServiceProvider.getNetworkService().addTag(addTagRequest.getNewTag());
                            break;
                        case MODIFY_TAG_OBJECT_NAME:
                            ModifyTagObjectNameRequest modifyTagObjectNameRequest = (ModifyTagObjectNameRequest) request;
                            NetworkServiceProvider.getNetworkService().modifyObjectName(modifyTagObjectNameRequest.getTag(), modifyTagObjectNameRequest.getNewObjectName());
                            break;
                        case MODIFY_TAG_OBJECT_IMAGE:
                            ModifyTagObjectImageRequest modifyTagObjectImageRequest = (ModifyTagObjectImageRequest) request;
                            NetworkServiceProvider.getNetworkService().modifyObjectImage(modifyTagObjectImageRequest.getTag(), modifyTagObjectImageRequest.getNewObjectImageFilename());
                            break;
                        case REMOVE_TAG:
                            RemoveTagRequest removeTagRequest = (RemoveTagRequest) request;
                            NetworkServiceProvider.getNetworkService().removeTag(removeTagRequest.getTag());
                            break;
                    }
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Request processing succeeds:   " + request);
                } catch (IllegalFieldException e) { // means there is conflict between local version and server version.
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception of type IllegalFieldException has occurred while processing this request :   " + request);
                    continueSync = false;
                    failedOnConflict = true;
                } catch (NotAuthenticatedException e) { // this case can't arrive.
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error on password : it became incorrect.");
                    continueSync = false;
                } catch (NetworkServiceException e) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Network service error while processing this request :   " + request);
                    continueSync = false;
                    exception = e;
                }

                requestOnFailureIndex = i;
            }
        }

        boolean hasFailedOnConflict()
        {
            return failedOnConflict;
        }

        boolean isSychronizationSuccessFul()
        {
            return sychronisationSuccessFul;
        }

        NetworkServiceException getCatchedNetworkServiceException()
        {
            return exception;
        }

        int getRequestOnFailureIndex()
        {
            return requestOnFailureIndex;
        }
    }

    public boolean isSynchronised()
    {
        return requests.size() == 0;
    }

    /**
     * Overwrites local version with server version. If an error occurs, the content of this version is not modified.
     * @throws IllegalFieldException If the password is not correct any more.
     * @throws NotAuthenticatedException If the authentication is not done.
     * @throws NetworkServiceException If a network error has occured.
     * @throws EngineServiceException If an internal error has occured.
     */
    public void overwriteLocalVersion() throws IllegalFieldException, NotAuthenticatedException, NetworkServiceException, EngineServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        OverwriteLocalVersionThread overwriteLocalVersionThread = new OverwriteLocalVersionThread();

        overwriteLocalVersionThread.start();

        try {
            overwriteLocalVersionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new EngineServiceException();
        }

        if(! overwriteLocalVersionThread.isOverwriteSuccessful())
        {
            if(overwriteLocalVersionThread.isWrongAuthenticationInformation())
                throw new IllegalFieldException(PASSWORD, REASON_VALUE_INCORRECT, "");
            else
                throw  overwriteLocalVersionThread.getCatchedException();
        }
    }

    class OverwriteLocalVersionThread extends Thread
    {
        private boolean isOverwriteSuccessful;
        private boolean isWrongAuthenticationInformation;

        private NetworkServiceException catchedException;

        OverwriteLocalVersionThread()
        {
            isOverwriteSuccessful = false;
            catchedException = null;
            isWrongAuthenticationInformation = false;
        }


        @Override
        public void run() {
            NetworkServiceProvider.getNetworkService().logOut();

            try {

                Logger.getLogger(getClass().getName()).log(Level.INFO, "Beginning of local version overwrite");
                Account currentAccount = NetworkServiceProvider.getNetworkService().authenticate(EngineService.this.currentAccount.getPseudo(), currentPassword);
                List<Tag> tags = NetworkServiceProvider.getNetworkService().getTags();
                List<Profile> profiles = NetworkServiceProvider.getNetworkService().getProfiles();

                EngineService.this.currentAccount = currentAccount;
                EngineService.this.tags = tags;
                EngineService.this.profiles = profiles;
                isOverwriteSuccessful = true;
                Logger.getLogger(getClass().getName()).log(Level.INFO, "End of local version overwrite : operation is successful.");
            } catch (AccountNotFoundException e) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "The password became incorrect.");
            } catch (NetworkServiceException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Network service error while processing local version overwrite");
                catchedException = e;
            } catch (NotAuthenticatedException e) { // abnormal error, normally never occurs.
                Logger.getLogger(getClass().getName()).log(Level.INFO, "The password became incorrect.");
                isWrongAuthenticationInformation = true;
            }
        }

        boolean isOverwriteSuccessful() {
            return isOverwriteSuccessful;
        }

        boolean isWrongAuthenticationInformation() {
            return isWrongAuthenticationInformation;
        }

        NetworkServiceException getCatchedException()
        {
            return catchedException;
        }
    }


//TODO implement server version overwrite. Not possible for the moment.

    public void overwriteServerVersion() throws NotAuthenticatedException, NetworkServiceException
    {
    }

    class OverwriteServerVersionThread extends Thread
    {
        private boolean isOverwriteSuccessful;
        private boolean isWrongAuthenticationInformation;

        private NetworkServiceException catchedException;

        OverwriteServerVersionThread()
        {
            isOverwriteSuccessful = false;
            catchedException = null;
            isWrongAuthenticationInformation = false;
        }


        @Override
        public void run() {
            //TODO
        }

        boolean isOverwriteSuccessful() {
            return isOverwriteSuccessful;
        }

        boolean isWrongAuthenticationInformation() {
            return isWrongAuthenticationInformation;
        }

        NetworkServiceException getCatchedException()
        {
            return catchedException;
        }
    }




// For auto-synchronization.

    /**
     * The auto-synchronizer. Non null if the auto-synchronization is enabled, null otherwise.
     */
    private AutoSynchronizer autoSynchronizer;

    /**
     * Enable/disable the auto-synchronization.
     * @param enable set to true to enable the auto-synchronization, false to disable it.
     */
    public void setAutoSynchronization(boolean enable)
    {
        if(autoSynchronizer == null && enable)
        {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Set auto-synchronization.");
            autoSynchronizer = new AutoSynchronizer();

            if(currentAccount != null)
            {
                for(int i=0; i < requests.size(); i++)
                    autoSynchronizer.appendRequest(requests.get(i));

                Logger.getLogger(getClass().getName()).log(Level.INFO, "Start auto-synchronization.");
                autoSynchronizer.start();
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Auto-synchronization started.");
            }
        }
        else if(autoSynchronizer != null && ! enable)
        {
            if(currentAccount != null)
            {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Stop auto-synchronization.");
                autoSynchronizer.stopAutoSynchronization();
                try {
                    autoSynchronizer.join();
                    requests.addAll(autoSynchronizer.getNotDoneRequests());
                    autoSynchronizer = null;
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Auto-synchronization stopped.");
                } catch (InterruptedException e) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Auto-synchronization stop failed because of an interruption : " + e);
                    e.printStackTrace();
                }
            }
            else
                autoSynchronizer = null;
        }
    }

    /**
     *
     * @return true if auto-synchronization is enabled, false otherwise.
     */
    public boolean isAutoSynchronizationEnabled()
    {
        return autoSynchronizer != null;
    }

    /**
     * apply changes from the server's data on the local version.
     */
    private void checkForAccountUpdate()
    {
        if(isAutoSynchronizationEnabled() && (autoSynchronizer.isErrorOccurredOnData() || autoSynchronizer.isAccountDataUpdatedFromServer()))
        {
            AutoSynchronizer.AccountData accountData = autoSynchronizer.getAccountCopyUpdated(tags);

            currentAccount = accountData.getAccount();
            currentPassword = accountData.getPassword();

            tags.clear();
            tags.addAll(currentAccount.getTags());
            currentAccount.getTags().clear();

            profiles.clear();
            profiles.addAll(currentAccount.getProfiles());
            currentAccount.getProfiles().clear();
        }
    }

    /**
     * To check if an error has occurred about authentication information.
     * If there is a such error, the dialog box to ask again the password to the user is started and an exception of type NotAuthenticatedException is thrown.
     * @throws NotAuthenticatedException If there is an error about authentication information.
     */
    private void checkAutoSynchronizerState() throws NotAuthenticatedException {
        if(isAutoSynchronizationEnabled() && autoSynchronizer.hasFailedOnPassword())
        {
            BasicActivity.getCurrentActivity().askPasswordAfterError();
            throw new NotAuthenticatedException();
        }
    }

    /**
     * To resolve the authentication error detected with the method checkAutoSynchronizerState().
     * @param password the password to use to resolve the error.
     * @throws NotAuthenticatedException
     */
    public void resolveAutoSynchronizationErrorOnPassword(String password) throws NotAuthenticatedException {

        if(password != null && FieldVerifier.verifyPassword(password))
        {
            currentPassword = password;
            autoSynchronizer.startAfterErrorOnPassword(password);

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Password error resolved.");
        }
        else
            throw new NotAuthenticatedException();
    }

    class AutoSynchronizer extends Thread
    {
//        private BlockingQueue<Request> requestQueue;

        private Semaphore requestsMutex;
        private List<Request> requests;
        private Semaphore requestNumber;


        private boolean errorOccurredOnData;
        private boolean accountDataUpdatedFromServer;


        private Semaphore accountMutex;

        /**
         * this account field is a version synchronized with server.
         */
        private Account account;
        private String password;


        private int lastProfileUpdate;
        private int lastTagsUpdate;

        private boolean failedOnPassword;

        private boolean continueAutoSynchronization;

        private Thread runningThread;

        AutoSynchronizer()
        {
            requestsMutex = new Semaphore(1, true);
            requests = new LinkedList<>();

            requestNumber = new Semaphore(0);

            accountMutex = new Semaphore(1, true);

            errorOccurredOnData = false;
            accountDataUpdatedFromServer = false;
            failedOnPassword = false;

            runningThread = null;
        }

        boolean appendRequest(Request request)
        {
            try {
                requestsMutex.acquire();
                requests.add(request);
                requestsMutex.release();

                requestNumber.release();

                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        private void removeFirstRequest()
        {
            requestsMutex.acquireUninterruptibly();

            requests.remove(0);
            requestsMutex.release();
        }

        private Request getFirstRequest()
        {
            requestsMutex.acquireUninterruptibly();

            Request tmp = requests.get(0);
            requestsMutex.release();

            return tmp;
        }

        List<Request> getNotDoneRequests()
        {
            Object array[] = requests.toArray();

            List<Request> requestList = new ArrayList<>();
            for (Object anArray : array)
                requestList.add((Request) anArray);

            return requestList;
        }


        boolean hasFailedOnPassword()
        {
            return failedOnPassword;
        }

        boolean isErrorOccurredOnData() {
            return errorOccurredOnData;
        }

        public boolean isAccountDataUpdatedFromServer() {
            return accountDataUpdatedFromServer;
        }

        synchronized void startAutoSynchronization(Account account, List<Tag> tagList, List<Profile> profileList, String password, int lastTagsUpdate, int lastProfileUpdate)
        {
            if(account == null || password == null)
                throw new NullPointerException("account and password parameters can't be null");

            this.account = new Account(account.getPseudo(), account.getFirstName(), account.getLastName(), account.getEMailAddress());

            List<Tag> tags = this.account.getTags();

            for(Tag tag : tagList)
            {
                Tag tmp = new Tag(tag.getUid(), tag.getObjectName(), tag.getObjectImageName());
                tmp.setImageVersion(tag.getImageVersion());
                tags.add(tmp);

                if(tag.getObjectImageName() != null) // to specify the images to download.
                {
                    tagToUpdateImageFile.add(tmp);
                    tag.setObjectImageName(FileManager.getTagImageFileForUser(tmp).getAbsolutePath());
                }
            }

            List<Profile> profiles = this.account.getProfiles();

            for(Profile profile : profileList)
            {
                Profile tmp = new Profile(profile.getName());

                for(Tag tag : profile.getTags())
                    tmp.addTag(tags.get(tags.indexOf(tag)));

                profiles.add(tmp);
            }

            this.lastTagsUpdate = lastTagsUpdate;
            this.lastProfileUpdate = lastProfileUpdate;

            runningThread = new Thread(this);
            runningThread.start();
        }

        synchronized void startAfterErrorOnPassword(String password)
        {
            if(runningThread.isAlive() || ! failedOnPassword)
                throw new IllegalStateException("Auto synchronizer already running.");

            this.password = password;

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Auto synchronization will be restarted after failure on password.");

            runningThread = new Thread(this);
            runningThread.start();
        }

        void stopAutoSynchronization()
        {
            continueAutoSynchronization = false;
            requestNumber.release();
        }

        private Account getAccountCopy()
        {
            Account accountCopy = new Account(account.getPseudo(), account.getFirstName(), account.getLastName(), account.getEMailAddress());

            List<Tag> tags = accountCopy.getTags();

            for(Tag tag : account.getTags())
                tags.add(new Tag(tag.getUid(), tag.getObjectName(), tag.getObjectImageName()));

            List<Profile> profiles = accountCopy.getProfiles();

            for(Profile profile : account.getProfiles())
            {
                Profile tmp = new Profile(profile.getName());

                for(Tag tag : profile.getTags())
                    tmp.addTag(tags.get(tags.indexOf(tag)));

                profiles.add(tmp);
            }

            accountMutex.release();

            return accountCopy;
        }

        class AccountData
        {
            private Account account;
            private String password;

            AccountData(Account account, String password)
            {
                this.account = account;
                this.password = password;
            }

            public Account getAccount() {
                return account;
            }

            public String getPassword() {
                return password;
            }
        }

        AccountData getAccountCopyUpdated(List<Tag> currentTagList)
        {
            requestsMutex.acquireUninterruptibly();

            accountMutex.acquireUninterruptibly();
            Account copy = getAccountCopy();

            // update tag object images by replacing only image files which need to be updated. Removes also old images, i.e. image files which become useless.
            updateUserTagImagesFromAccountCopy(currentTagList, copy);

            accountMutex.release();

            AccountData accountData = new AccountData(copy, password);

            List<Request> requestList = getNotDoneRequests();
            errorOccurredOnData = false;
            accountDataUpdatedFromServer = false;
            int index;

            for(Request request : requestList)
            {
                switch (request.getRequestType()) {
                    case MODIFY_EMAIL:
                        ModifyEmailRequest modifyEmailRequest = (ModifyEmailRequest) request;

                        copy.setMailAddress(modifyEmailRequest.getNewEmailAddress());
                        break;
                    case MODIFY_PASSWORD:
//                        ModifyPasswordRequest modifyPasswordRequest = (ModifyPasswordRequest) request;
//                        password = modifyPasswordRequest.getNewPassword();
                        break;
                    case MODIFY_BRACELET_UID:
                        ModifyBraceletUIDRequest modifyBraceletUIDRequest = (ModifyBraceletUIDRequest) request;

                        copy.setBraceletUID(modifyBraceletUIDRequest.getNewBraceletUID());
                        break;
                    case ADD_TAG:
                        AddTagRequest addTagRequest = (AddTagRequest) request;
                        if(! copy.getTags().contains(addTagRequest.getNewTag()))
                        {
                            boolean applyAddition = true;
                            for(Tag tag : copy.getTags())
                                if(tag.getObjectName().equals(addTagRequest.getNewTag().getObjectName()))
                                    applyAddition = false;
                            if(applyAddition)
                                copy.getTags().add(new Tag(addTagRequest.getNewTag().getUid(), addTagRequest.getNewTag().getObjectName(), addTagRequest.getNewTag().getObjectImageName()));
                        }
                        break;
                    case MODIFY_TAG_OBJECT_NAME:
                        ModifyTagObjectNameRequest modifyTagObjectNameRequest = (ModifyTagObjectNameRequest) request;
                        index = copy.getTags().indexOf(modifyTagObjectNameRequest.getTag());
                        if(index >= 0)
                        {
                            Tag tmp = copy.getTags().get(index);
                            boolean applyModification = true;
                            for(Tag tag : copy.getTags())
                                if(tag != tmp && tag.getObjectName().equals(modifyTagObjectNameRequest.getTag().getObjectName()))
                                    applyModification = false;
                            if(applyModification)
                                copy.getTags().get(index).setObjectName(modifyTagObjectNameRequest.getNewObjectName());
                        }
                        break;
                    case MODIFY_TAG_OBJECT_IMAGE:
                        ModifyTagObjectImageRequest modifyTagObjectImageRequest = (ModifyTagObjectImageRequest) request;
                        index = copy.getTags().indexOf(modifyTagObjectImageRequest.getTag());
                        if(index >= 0)
                        {
                            Tag tmp = copy.getTags().get(index);

                            if(modifyTagObjectImageRequest.getNewObjectImageFilename() != null)
                                try {
                                    FileManager.copyFileFromRequestFolderToUserFolder(modifyTagObjectImageRequest.getRequestNumber(), tmp);
                                    //TODO maybe see if there is a version conflit.
                                } catch (FileNotFoundException e) { // will never occur.
                                    e.printStackTrace();
                                }
                            else if(tmp.getObjectImageName() != null)
                                FileManager.getTagImageFileForUser(tmp).delete();
                        }
                        break;
                    case REMOVE_TAG:
                        RemoveTagRequest removeTagRequest = (RemoveTagRequest) request;
                        index = copy.getTags().indexOf(removeTagRequest.getTag());
                        if(index >= 0)
                        {
                            Tag tmp = copy.getTags().get(index);

                            if(tmp.getObjectImageName() != null) // delete image file before remove this tag.
                                FileManager.getTagImageFileForUser(tmp).delete();

                            copy.getTags().remove(index);
                        }
                        break;
                    case CREATE_PROFILE_WITH_TAGS:
                        CreateProfileWithTagsRequest createProfileWithTagsRequest = (CreateProfileWithTagsRequest) request;
                        Profile profile = createProfileWithTagsRequest.getProfile();

                        if(! copy.getProfiles().contains(profile))
                        {
                            Profile tmp = new Profile(profile.getName());
                            boolean applyOperation = true;

                            for(int i=0; applyOperation && i < profile.getTags().size(); i++)
                            {
                                index = copy.getTags().indexOf(profile.getTags().get(i));
                                if(index < 0)
                                    applyOperation = false;
                                else
                                    tmp.addTag(copy.getTags().get(index));
                            }

                            if(applyOperation)
                                copy.getProfiles().add(tmp);
                        }
                    break;
                    case ADD_TAGS_TO_PROFILE:
                        AddTagsToProfileRequest addTagsToProfileRequest = (AddTagsToProfileRequest) request;
                        profile = new Profile(addTagsToProfileRequest.getProfileName());
                        List<Tag> tagsToAdd = addTagsToProfileRequest.getTagsToAdd();

                        index = copy.getProfiles().indexOf(profile);
                        if(index >= 0)
                        {
                            Profile profile1 = copy.getProfiles().get(index);
                            boolean applyOperation = true;
                            List<Tag> tmp = new LinkedList<>();

                            for(int i=0; applyOperation && i < tagsToAdd.size(); i++)
                            {
                                index = copy.getTags().indexOf(tagsToAdd.get(i));
                                if(index < 0)
                                    applyOperation = false;
                                else if(! profile1.getTags().contains(tagsToAdd.get(i)))
                                    tmp.add(copy.getTags().get(index));
                            }

                            if(applyOperation)
                                profile1.getTags().addAll(tmp);
                        }
                    break;
                    case REMOVE_TAGS_FROM_PROFILE:
                        RemoveTagsFromProfileRequest removeTagsFromProfileRequest = (RemoveTagsFromProfileRequest) request;
                        List<Tag> tagsToRemove = removeTagsFromProfileRequest.getTagsToRemove();

                        index = copy.getProfiles().indexOf(new Profile(removeTagsFromProfileRequest.getProfileName()));
                        if(index >= 0)
                        {
                            Profile profile1 = copy.getProfiles().get(index);
                            boolean applyOperation = true;
                            List<Tag> tmp = new LinkedList<>();

                            for(int i=0; applyOperation && i < tagsToRemove.size(); i++)
                            {
                                index = copy.getTags().indexOf(tagsToRemove.get(i));
                                if(index < 0)
                                    applyOperation = false;
                                else
                                    tmp.add(copy.getTags().get(index));
                            }

                            if(applyOperation)
                                profile1.getTags().removeAll(tmp);
                        }
                    break;
                    case REPLACE_TAG_LIST_OF_PROFILE:
                        ReplaceTagListOfProfileRequest replaceTagListOfProfileRequest = (ReplaceTagListOfProfileRequest) request;
                        List<Tag> newTagList = replaceTagListOfProfileRequest.getNewTagList();

                        index = copy.getProfiles().indexOf(new Profile(replaceTagListOfProfileRequest.getProfileName()));
                        if(index >= 0)
                        {
                            Profile profile1 = copy.getProfiles().get(index);
                            boolean applyOperation = true;
                            List<Tag> tmp = new LinkedList<>();

                            for(int i=0; applyOperation && i < newTagList.size(); i++)
                            {
                                index = copy.getTags().indexOf(newTagList.get(i));
                                if(index < 0)
                                    applyOperation = false;
                                else
                                    tmp.add(copy.getTags().get(index));
                            }

                            if(applyOperation)
                            {
                                profile1.getTags().clear();
                                profile1.getTags().addAll(tmp);
                            }
                        }
                    break;
                    case MODIFY_PROFILE_NAME:
                        ModifyProfileNameRequest modifyProfileNameRequest = (ModifyProfileNameRequest) request;
                        String newProfileName = modifyProfileNameRequest.getNewProfileName();

                        index = copy.getProfiles().indexOf(new Profile(modifyProfileNameRequest.getProfileName()));
                        if(index >= 0)
                        {
                            Profile profile1 = copy.getProfiles().get(index);
                            boolean applyOperation = true;

                            for(int i=0; applyOperation && i < copy.getProfiles().size(); i++)
                                if(copy.getProfiles().get(i) != profile1 && copy.getProfiles().get(i).getName().equals(newProfileName))
                                    applyOperation = false;

                            if(applyOperation)
                                profile1.setName(newProfileName);
                        }
                    break;
                    case REMOVE_PROFILE:
                        RemoveProfileRequest removeProfileRequest = (RemoveProfileRequest) request;

                        index = copy.getProfiles().indexOf(new Profile(removeProfileRequest.getProfileName()));
                        if(index >= 0)
                            copy.getProfiles().remove(index);
                    break;
                }
            }

            requestsMutex.release();

            return accountData;
        }

        private void updateUserTagImagesFromAccountCopy(List<Tag> currentTagList, Account copy)
        {
            SortUtility.sortTagListByUID(copy.getTags());
            SortUtility.sortTagListByUID(currentTagList);
            int i=0, j=0;

            while(i < currentTagList.size() && j < copy.getTags().size())
            {
                int res = currentTagList.get(i).getUid().compareTo(copy.getTags().get(j).getUid());

                if(res == 0) // the tag is in the two lists. the fields of this will be updated.
                {
                    if(currentTagList.get(i).getImageVersion() < copy.getTags().get(j).getImageVersion()) // update image file if necessary.
                    {
                        if(copy.getTags().get(j).getObjectImageName() != null) //there is a new image file.
                            try {
                                FileManager.copyFileFromAutoSyncFolderToUserFolder(copy.getTags().get(j));
                            } catch (FileNotFoundException e) { // will normally never occur.
                                e.printStackTrace();
                            }
                        else if(currentTagList.get(i).getObjectImageName() != null) // if the image is removed from the server version.
                            FileManager.getTagImageFileForUser(copy.getTags().get(j)).delete();
                    }

                    i++;
                    j++;
                }
                else if(res < 0) //there is a new tag.
                {
                    if(copy.getTags().get(j).getObjectImageName() != null)
                        try {
                            FileManager.copyFileFromAutoSyncFolderToUserFolder(copy.getTags().get(j));
                        } catch (FileNotFoundException e) {// will nevzr occur.
                            e.printStackTrace();
                        }

                    j++;
                }
                else // a tag has been removed.
                {
                    if(currentTagList.get(i).getObjectImageName() != null)
                        FileManager.getTagImageFileForUser(currentTagList.get(i)).delete();

                    i++;
                }
            }

            if(j < copy.getTags().size()) // there is new tags to add.
                for(; j < copy.getTags().size(); j++)
                {
                    if(copy.getTags().get(j).getObjectImageName() != null)
                        try {
                            FileManager.copyFileFromAutoSyncFolderToUserFolder(copy.getTags().get(j));
                        } catch (FileNotFoundException e) {// will nevzr occur.
                            e.printStackTrace();
                        }
                }
            else if(i < currentTagList.size()) // there is tags to remove
                for(; i < currentTagList.size(); i++)
                {
                    if(currentTagList.get(i).getObjectImageName() != null)
                        FileManager.getTagImageFileForUser(currentTagList.get(i)).delete();
                }
        }

        private boolean connectedToInternet;

        @Override
        public void run()
        {
            if(! isInternetConnectionDone())
                checkAndWaitForInternetConnection();
            else
                connectedToInternet = true;

            if(failedOnPassword)
                try {
                    NetworkServiceProvider.getNetworkService().updatePassword(password);
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Password successfully updated on the network service to resolve the password problem about authentication.");
                } catch (NotAuthenticatedException e) { // this case will normally never occur.
                    e.printStackTrace();
                    return;
                }

            Request currentRequest = null;
            continueAutoSynchronization = true;
            failedOnPassword = false;
            String errorMessage = null;

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Auto synchronization is started.");

            while(continueAutoSynchronization)
            {
                requestsMutex.acquireUninterruptibly();
                int size = requests.size();
                requestsMutex.release();
                if(requests.size() == 0 && tagToUpdateImageFile.size() > 0) // if there is image to download and no request, launches image download.
                {
                    // downloads needed images. This operation is perform with a lower priority than the requests.
                    Object tags[] = tagToUpdateImageFile.toArray();
                    for(int index = 0; index < tags.length && size == 0 && continueAutoSynchronization; index++)
                    {
                        try {
                            String downloadImageFilename = NetworkServiceProvider.getNetworkService().downloadObjectImage((Tag) tags[index]);
                            Logger.getLogger(getClass().getName()).log(Level.INFO, "image file download for the tag " + tags[index] + " succeeds.");

                            accountMutex.acquireUninterruptibly();
                            FileManager.moveFile(new File(downloadImageFilename), FileManager.getTagImageFileForAutoSynchronization((Tag) tags[index]));

                            if(! isThereRequestAboutTagImage((Tag) tags[index]))
                                FileManager.copyFileFromAutoSyncFolderToUserFolder((Tag) tags[index]);

                            accountMutex.release();
                            tagToUpdateImageFile.remove(tags[index]);
                        } catch (NotAuthenticatedException e) {
                            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An authentication error has occured (it has failed on password.)");
                            continueAutoSynchronization = false;

                            failedOnPassword = true;
                            BasicActivity.getCurrentActivity().askPasswordAfterError();
                        } catch (NetworkServiceException e) {// maybe the connexion to the server has failed.
                            Logger.getLogger(getClass().getName()).log(Level.WARNING, "A network service error has occured : " + e.getMessage());

                            if(! isInternetConnectionDone()) // if the internet connection is down, it will wait until it is up again.
                                checkAndWaitForInternetConnection();
                            else
                                BasicActivity.getCurrentActivity().showErrorMessage("A network error has occured.");
                        } catch (FileNotFoundException e) {
                            Logger.getLogger(getClass().getName()).log(Level.WARNING, "The downloaded file is not found for tag " + tags[index] + ".");
                            accountMutex.release();
                            e.printStackTrace();
                        }

                        requestsMutex.acquireUninterruptibly();
                        size = requests.size();
                        requestsMutex.release();
                    }
                }

                try {

                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Will wait for another request to process.");

                    ThreadAlarm alarm = null;
                    if(size == 0)
                    {
                        alarm = new ThreadAlarm(Thread.currentThread(), 15000);
                        alarm.start();
                    }
                    requestNumber.acquire();
                    if(alarm != null)
                    {
                        try{
                            alarm.stopAlarm();
                            alarm = null;

                            // wait for a little time to be sure the following case will not occur : the alarm has finished to wait the needed time before we stopped it and has not done interruption on the current thread.
                            Thread.sleep(1);
                        } catch (InterruptedException e){
                        }
                    }

                    if(! continueAutoSynchronization) // means the method stopAutoSynchronization() has been called.
                        return;

                    currentRequest = getFirstRequest();

                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Begins request processing :   " + currentRequest);

                    switch (currentRequest.getRequestType()) {
                        case MODIFY_EMAIL:
                            try
                            {
                                ModifyEmailRequest modifyEmailRequest = (ModifyEmailRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().modifyEMailAddress(modifyEmailRequest.getNewEmailAddress());

                                accountMutex.acquireUninterruptibly();
                                account.setMailAddress(modifyEmailRequest.getNewEmailAddress());
                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                errorMessage = "Email address modification failed : \"" + e.getFieldValue() + "\" is incorrect.";
                                throw e;
                            }
                            break;
                        case MODIFY_PASSWORD:
                            try
                            {
                                ModifyPasswordRequest modifyPasswordRequest = (ModifyPasswordRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().modifyPassword(modifyPasswordRequest.getNewPassword());

                                accountMutex.acquireUninterruptibly();
                                password = modifyPasswordRequest.getNewPassword();
                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                errorMessage = "password modification failed : specified passxord is incorrect.";
                                throw e;
                            }
                            break;
                        case MODIFY_BRACELET_UID:
                            try
                            {
                                ModifyBraceletUIDRequest modifyBraceletUIDRequest = (ModifyBraceletUIDRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().modifyBraceletUID(modifyBraceletUIDRequest.getNewBraceletUID());

                                accountMutex.acquireUninterruptibly();
                                account.setBraceletUID(modifyBraceletUIDRequest.getNewBraceletUID());
                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                if(e.getReason() == REASON_VALUE_ALREADY_USED)
                                    errorMessage = "Bracelet UID modification failed : bracelet UID \"" + e.getFieldValue() + "\" is already associated with another account.";
                                else
                                    errorMessage = "Bracelet UID modification failed : \"" + e.getFieldValue() + "\" is incorrect.";

                                throw e;
                            }
                            break;
                        case ADD_TAG:
                            AddTagRequest addTagRequest = (AddTagRequest) currentRequest;
                            try
                            {
                                if(addTagRequest.getNewTag().getObjectImageName() != null)
                                    addTagRequest.getNewTag().setObjectImageName(FileManager.getTagImageFileForRequest(addTagRequest.getRequestNumber()).getAbsolutePath());

                                Tag result = NetworkServiceProvider.getNetworkService().addTag(addTagRequest.getNewTag());

                                accountMutex.acquireUninterruptibly();

                                if(addTagRequest.getNewTag().getObjectImageName() != null)
                                    FileManager.moveFileFromRequestFolderToAutoSyncFolder(addTagRequest.getRequestNumber(), addTagRequest.getNewTag());

                                Tag tag = new Tag(addTagRequest.getNewTag().getUid(), addTagRequest.getNewTag().getObjectName(), addTagRequest.getNewTag().getObjectImageName());
                                tag.setImageVersion(result.getImageVersion());
                                account.getTags().add(tag);

                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                switch (e.getFieldId()) {
                                    case IllegalFieldException.TAG_OBJECT_NAME:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                                            errorMessage = "Tag addition failed : object name \"" + e.getFieldValue() + "\" is already used.";
                                        else
                                            errorMessage = "Tag addition failed : object name \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    case IllegalFieldException.TAG_OBJECT_IMAGE:
                                        errorMessage = "Tag addition failed : image filename is incorrect";
                                    case IllegalFieldException.TAG_UID:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                                            errorMessage = "Tag addition failed : tag UID \"" + e.getFieldValue() + "\" is already used.";
                                        else
                                            errorMessage = "Tag addition failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                }

                                if(addTagRequest.getNewTag().getObjectImageName() != null) // to be sure the request is properly removed about potential image file.
                                    if(FileManager.getTagImageFileForRequest(addTagRequest.getRequestNumber()).delete())
                                        Logger.getLogger(getClass().getName()).log(Level.INFO, "image file for request " + addTagRequest.getRequestNumber() + "deleted");
                                    else
                                        Logger.getLogger(getClass().getName()).log(Level.INFO, "image file for request " + addTagRequest.getRequestNumber() + "may not be deleted");

                                throw e;
                            } catch (FileNotFoundException e) { // will normally never occur.
                                e.printStackTrace();
                                accountMutex.release();
                            }
                            break;
                        case MODIFY_TAG_OBJECT_NAME:
                            try
                            {
                                ModifyTagObjectNameRequest modifyTagObjectNameRequest = (ModifyTagObjectNameRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().modifyObjectName(modifyTagObjectNameRequest.getTag(), modifyTagObjectNameRequest.getNewObjectName());

                                accountMutex.acquireUninterruptibly();
                                account.getTags().get(account.getTags().indexOf(modifyTagObjectNameRequest.getTag())).setObjectName(modifyTagObjectNameRequest.getNewObjectName());
                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                switch(e.getFieldId())
                                {
                                    case IllegalFieldException.TAG_UID:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Tag modification failed : tag with UID \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Tag modification failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    case IllegalFieldException.TAG_OBJECT_NAME:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                                            errorMessage = "Tag modification failed : object name \"" + e.getFieldValue() + "\" is already used.";
                                        else
                                            errorMessage = "Tag modification failed : object name \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                }
                                throw e;
                            }
                            break;
                        case MODIFY_TAG_OBJECT_IMAGE:
                            ModifyTagObjectImageRequest modifyTagObjectImageRequest = (ModifyTagObjectImageRequest) currentRequest;
                            String imageFilename = modifyTagObjectImageRequest.getNewObjectImageFilename() == null ? null : FileManager.getTagImageFileForRequest(modifyTagObjectImageRequest.getRequestNumber()).getAbsolutePath();
                            try
                            {
                                Tag result = NetworkServiceProvider.getNetworkService().modifyObjectImage(modifyTagObjectImageRequest.getTag(), imageFilename);

                                accountMutex.acquireUninterruptibly();
                                String newImageName = imageFilename == null ? null : FileManager.getTagImageFileForAutoSynchronization(modifyTagObjectImageRequest.getTag()).getAbsolutePath();
                                Tag tag = account.getTags().get(account.getTags().indexOf(modifyTagObjectImageRequest.getTag()));

                                if(imageFilename != null) // if there is a new image file
                                    FileManager.moveFileFromRequestFolderToAutoSyncFolder(modifyTagObjectImageRequest.getRequestNumber(), modifyTagObjectImageRequest.getTag());
                                else if(tag.getObjectImageName() != null) // if this tag has an image and the requeest is image remove.
                                    if(FileManager.getTagImageFileForAutoSynchronization(tag).delete())
                                        Logger.getLogger(getClass().getName()).log(Level.INFO, "image file deleted from auto sync folder.");
                                    else
                                        Logger.getLogger(getClass().getName()).log(Level.INFO, "image file may not be deleted from auto sync folder.");

                                tag.setObjectImageName(newImageName);
                                tag.setImageVersion(result.getImageVersion());

                                accountMutex.release();

                                tagToUpdateImageFile.remove(tag); // to be sure this tag image will not be updated until a new call of the method checkForUpdate() is not done because it is useless.
                            }
                            catch(IllegalFieldException e)
                            {
                                switch(e.getFieldId())
                                {
                                    case IllegalFieldException.TAG_UID:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Tag modification failed : tag with UID \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Tag modification failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    case IllegalFieldException.TAG_OBJECT_IMAGE :
                                        errorMessage = "Tag addition failed : image filename is incorrect";
                                        break;
                                }
                                if(imageFilename != null) // to be sure the request is properly removed about potential image file.
                                    if(FileManager.getTagImageFileForRequest(modifyTagObjectImageRequest.getRequestNumber()).delete())
                                        Logger.getLogger(getClass().getName()).log(Level.INFO, "image file for request " + modifyTagObjectImageRequest.getRequestNumber() + "deleted");
                                    else
                                        Logger.getLogger(getClass().getName()).log(Level.INFO, "image file for request " + modifyTagObjectImageRequest.getRequestNumber() + "may not be deleted");

                                throw e;
                            } catch (FileNotFoundException e) { // will normally never occur.
                                e.printStackTrace();
                                accountMutex.release();
                            }
                            break;
                        case REMOVE_TAG:
                            RemoveTagRequest removeTagRequest = (RemoveTagRequest) currentRequest;
                            try
                            {
                                NetworkServiceProvider.getNetworkService().removeTag(removeTagRequest.getTag());

                                accountMutex.acquireUninterruptibly();
                                int index = account.getTags().indexOf(removeTagRequest.getTag());
                                Tag tag = account.getTags().get(index);
                                if(tag.getObjectImageName() != null)
                                    if(FileManager.getTagImageFileForAutoSynchronization(tag).delete())
                                        Logger.getLogger(getClass().getName()).log(Level.INFO, "image file for request " + removeTagRequest.getRequestNumber() + "deleted");
                                    else
                                        Logger.getLogger(getClass().getName()).log(Level.INFO, "image file for request " + removeTagRequest.getRequestNumber() + "may not be deleted");

                                account.getTags().remove(index);
                                accountMutex.release();

                                tagToUpdateImageFile.remove(tag); // removes this removed tag from this set if it is "waiting" for an image update.
                            }
                            catch(IllegalFieldException e) // the only possible error is about the tag UID.
                            {
                                if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND) // means this tag has already been removed from the server.
                                {
                                    account.getTags().remove(removeTagRequest.getTag());
                                    errorMessage = "Tag modification failed : tag with UID \"" + e.getFieldValue() + "\" is not found.";
                                }
                                else
                                    errorMessage = "Tag modification failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";

                                throw e;
                            }
                            break;
                        case CREATE_PROFILE_WITH_TAGS:
                            try
                            {
                                CreateProfileWithTagsRequest createProfileWithTagsRequest = (CreateProfileWithTagsRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().createProfile(createProfileWithTagsRequest.getProfile().getName(), createProfileWithTagsRequest.getProfile().getTags());

                                accountMutex.acquireUninterruptibly();

                                Profile profile = new Profile(createProfileWithTagsRequest.getProfile().getName());
                                for(Tag tag : createProfileWithTagsRequest.getProfile().getTags())
                                    profile.addTag(account.getTags().get(account.getTags().indexOf(tag)));

                                account.getProfiles().add(profile);

                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                switch(e.getFieldId())
                                {
                                    case IllegalFieldException.TAG_UID:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Profile creation failed : tag with UID \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Profile creation failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    case IllegalFieldException.PROFILE_NAME:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                                            errorMessage = "Profile creation failed : the name \"" + e.getFieldValue() + "\" is already used for another profile.";
                                        else
                                            errorMessage = "Profile creation failed : the profile name \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                }
                                throw e;
                            }
                        break;
                        case ADD_TAGS_TO_PROFILE:
                            try
                            {
                                AddTagsToProfileRequest addTagsToProfileRequest = (AddTagsToProfileRequest) currentRequest;
                                Profile tmp = new Profile(addTagsToProfileRequest.getProfileName());
                                NetworkServiceProvider.getNetworkService().addTagsToProfile(tmp, addTagsToProfileRequest.getTagsToAdd());

                                accountMutex.acquireUninterruptibly();

                                Profile profile = account.getProfiles().get(account.getProfiles().indexOf(tmp));
                                for(Tag tag : addTagsToProfileRequest.getTagsToAdd())
                                    profile.addTag(account.getTags().get(account.getTags().indexOf(tag)));

                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                switch(e.getFieldId())
                                {
                                    case IllegalFieldException.TAG_UID:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Profile modification failed : tag with UID \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Profile modification failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    case IllegalFieldException.PROFILE_NAME:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Profile modification failed : the profile \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Profile modification failed : the profile name \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                }
                                throw e;
                            }
                        break;
                        case REMOVE_TAGS_FROM_PROFILE:
                            try
                            {
                                RemoveTagsFromProfileRequest removeTagsFromProfileRequest = (RemoveTagsFromProfileRequest) currentRequest;
                                Profile tmp = new Profile(removeTagsFromProfileRequest.getProfileName());
                                NetworkServiceProvider.getNetworkService().removeTagsFromProfile(tmp, removeTagsFromProfileRequest.getTagsToRemove());

                                accountMutex.acquireUninterruptibly();

                                Profile profile = account.getProfiles().get(account.getProfiles().indexOf(tmp));
                                for(Tag tag : removeTagsFromProfileRequest.getTagsToRemove())
                                    profile.removeTag(tag);

                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                switch(e.getFieldId())
                                {
                                    case IllegalFieldException.TAG_UID:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Profile modification failed : tag with UID \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Profile modification failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    case IllegalFieldException.PROFILE_NAME:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Profile modification failed : the profile \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Profile modification failed : the profile name \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                }
                                throw e;
                            }
                        break;
                        case REPLACE_TAG_LIST_OF_PROFILE:
                            try
                            {
                                ReplaceTagListOfProfileRequest replaceTagListOfProfileRequest = (ReplaceTagListOfProfileRequest) currentRequest;
                                Profile tmp = new Profile(replaceTagListOfProfileRequest.getProfileName());
                                NetworkServiceProvider.getNetworkService().replaceTagListOfProfile(tmp, replaceTagListOfProfileRequest.getNewTagList());

                                accountMutex.acquireUninterruptibly();

                                Profile profile = account.getProfiles().get(account.getProfiles().indexOf(tmp));
                                profile.removeAllTags();
                                for(Tag tag : replaceTagListOfProfileRequest.getNewTagList())
                                    profile.addTag(account.getTags().get(account.getTags().indexOf(tag)));

                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                switch(e.getFieldId())
                                {
                                    case IllegalFieldException.TAG_UID:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Profile modification failed : tag with UID \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Profile modification failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    case IllegalFieldException.PROFILE_NAME:
                                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                            errorMessage = "Profile modification failed : the profile \"" + e.getFieldValue() + "\" is not found.";
                                        else
                                            errorMessage = "Profile modification failed : the profile name \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                }
                                throw e;
                            }
                        break;
                        case MODIFY_PROFILE_NAME:
                            try
                            {
                                ModifyProfileNameRequest modifyProfileNameRequest = (ModifyProfileNameRequest) currentRequest;
                                Profile tmp = new Profile(modifyProfileNameRequest.getProfileName());
                                NetworkServiceProvider.getNetworkService().modifyProfileName(tmp, modifyProfileNameRequest.getNewProfileName());

                                accountMutex.acquireUninterruptibly();
                                account.getProfiles().get(account.getProfiles().indexOf(tmp)).setName(modifyProfileNameRequest.getNewProfileName());
                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                if(e.getFieldId() == PROFILE_NAME)
                                    switch(e.getReason())
                                    {
                                        case REASON_VALUE_NOT_FOUND:
                                            errorMessage = "Profile name modification failed : the profile \"" + e.getFieldValue() + "\" is not found.";
                                        break;
                                        case REASON_VALUE_ALREADY_USED:
                                            errorMessage = "Profile name modification failed : the name \"" + e.getFieldValue() + "\" is already used for another profile.";
                                        break;
                                        default:
                                            errorMessage = "Profile name modification failed : the profile name \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    }
                                throw e;
                            }
                        break;
                        case REMOVE_PROFILE:
                            try
                            {
                                RemoveProfileRequest removeProfileRequest = (RemoveProfileRequest) currentRequest;
                                Profile tmp = new Profile(removeProfileRequest.getProfileName());
                                NetworkServiceProvider.getNetworkService().removeProfile(tmp);

                                accountMutex.acquireUninterruptibly();
                                account.getProfiles().remove(tmp);
                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                if(e.getFieldId() == PROFILE_NAME)
                                    switch(e.getReason())
                                    {
                                        case REASON_VALUE_NOT_FOUND:
                                            errorMessage = "Fails to remove profile : the profile \"" + e.getFieldValue() + "\" is not found.";
                                        break;
                                        default:
                                            errorMessage = "Fails to remove profile : the profile name \"" + e.getFieldValue() + "\" is incorrect.";
                                        break;
                                    }
                                throw e;
                            }
                        break;
                    }
                    removeFirstRequest();

                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Ends request processing :   " + currentRequest);
                } catch (IllegalFieldException e) { // means there is conflict between local version and server version.
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception of type IllegalFieldException has occurred while processing this request :   " + currentRequest + "   |   error message : " + errorMessage);
                    removeFirstRequest();
                    errorOccurredOnData = true;
                    BasicActivity.getCurrentActivity().showErrorMessage(errorMessage);
                } catch (NotAuthenticatedException e) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "An authentication error has occured (it has failed on password.)");
                    continueAutoSynchronization = false;
                    requestNumber.release();

                    failedOnPassword = true;
                    BasicActivity.getCurrentActivity().askPasswordAfterError();
                } catch (NetworkServiceException e) {// maybe the connexion to the server has failed.
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "A network service error has occured : " + e.getMessage());

                    if(! isInternetConnectionDone()) // if the internet connection is down, it will wait until it is up again.
                        checkAndWaitForInternetConnection();
                    else
                        BasicActivity.getCurrentActivity().showErrorMessage("A network error has occured.");

                    requestNumber.release();
                } catch (InterruptedException e) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Interruption has occured :   " + e);
                }

                if(continueAutoSynchronization)
                {
                    checkForUpdates();
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "now up to date for data, without images.");
                }
            }

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Auto synchronization is ending.");
        }

        /**
         *
         * @param tag the tag to check
         * @return true if there is a request of type "modify object image" or "remove tag" about the specified tag, false otherwise.
         */
        private boolean isThereRequestAboutTagImage(Tag tag)
        {
            requestsMutex.acquireUninterruptibly();
            for(Request request : requests)
                if((request.getRequestType() == RequestType.MODIFY_TAG_OBJECT_IMAGE && ((ModifyTagObjectImageRequest) request).getTag().equals(tag))
                    || (request.getRequestType() == RequestType.REMOVE_TAG && ((RemoveTagRequest) request).getTag().equals(tag)))
                {
                    requestsMutex.release();
                    return true;
                }

            requestsMutex.release();
            return false;
        }

        private void checkAndWaitForInternetConnection()
        {
            if(! isInternetConnectionDone()) // if the internet connection is down, it will wait until it is up again.
            {
                connectedToInternet = false;
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "A network service error has occured because internet connection is down. trying to resole the problem.");
                BasicActivity.getCurrentActivity().showErrorMessage("Error : Internet connection is down.");

                while(continueAutoSynchronization && ! connectedToInternet)
                {
                    // to wait 5 seconds before checking again internet connection.
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    connectedToInternet = isInternetConnectionDone();
                }

                if(connectedToInternet)
                    BasicActivity.getCurrentActivity().showErrorMessage("Internet connection available again");
            }
        }

        private Set<Tag> tagToUpdateImageFile = new HashSet<>();

        /**
         * Check if the local data are up to date or not. If it's not this case, it updates the local data.
         * The use of this method supposes the account data will not be modified on the server until this method call is not finished.
         */
        private void checkForUpdates()
        {
            try {
                int profileUpdate = NetworkServiceProvider.getNetworkService().getLastProfilesUpdateTime();
                int tagsUpdate = NetworkServiceProvider.getNetworkService().getLastTagsUpdateTime();

                List<Tag> tagList = null;
                List<Profile> profileList = null;

                //TODO update for personnal information, like email address.

                if(tagsUpdate > lastTagsUpdate) // the tag list isn't up to date.
                    tagList = NetworkServiceProvider.getNetworkService().getTags();

                if(profileUpdate > lastProfileUpdate) // the profile list isn't up to date.
                    profileList = NetworkServiceProvider.getNetworkService().getProfiles();

                accountMutex.acquire();
                if(tagList != null) // there is an update to apply for tags.
                {
                    SortUtility.sortTagListByUID(tagList);
                    SortUtility.sortTagListByUID(account.getTags());

                    int size = account.getTags().size();
                    int i, j;

                    for(i=0, j=0; i<tagList.size() && j<size;)
                    {
                        int res = tagList.get(i).getUid().compareTo(account.getTags().get(j).getUid());

                        if(res == 0) // the tag is in the two lists. the fields of this will be updated.
                        {
                            if(tagList.get(i).getImageVersion() > account.getTags().get(j).getImageVersion()) // update image file if necessary.
                            {
                                if(tagList.get(i).getObjectImageName() != null) //there is a new image file.
                                    tagToUpdateImageFile.add(account.getTags().get(j));
                                else if(account.getTags().get(j).getObjectImageName() != null) // if the image is removed from the server version.
                                    FileManager.getTagImageFileForAutoSynchronization(account.getTags().get(j)).delete();

                                account.getTags().get(j).setImageVersion(tagList.get(i).getImageVersion());
                                account.getTags().get(j).setObjectImageName(tagList.get(i).getObjectImageName());
                            }

                            account.getTags().get(j).setObjectName(tagList.get(i).getObjectName()); // update object name.

                            i++;
                            j++;
                        }
                        else if(res > 0) //there is a new tag.
                        {
                            Tag tag = new Tag(tagList.get(i).getUid(), tagList.get(i).getObjectName(), tagList.get(i).getObjectImageName());
                            tag.setImageVersion(tagList.get(i).getImageVersion());
                            account.getTags().add(tag);

                            if(tag.getObjectImageName() != null)
                                tagToUpdateImageFile.add(tag);

                            i++;
                        }
                        else // a tag has been removed.
                        {
                            Tag removedTag = account.getTags().remove(j);
                            if(removedTag.getObjectImageName() != null)
                                FileManager.getTagImageFileForAutoSynchronization(removedTag).delete();

                            size--;
                        }
                    }

                    if(i < tagList.size()) // there is new tags to add.
                    {
                        for(; i < tagList.size(); i++)
                        {
                            Tag tag = new Tag(tagList.get(i).getUid(), tagList.get(i).getObjectName(), tagList.get(i).getObjectImageName());
                            tag.setImageVersion(tagList.get(i).getImageVersion());
                            account.getTags().add(tag);

                            if(tag.getObjectImageName() != null)
                                tagToUpdateImageFile.add(tag);
                        }
                    }
                    else if(j < size) // there is tags to remove
                        for(; j<size; size--)
                        {
                            Tag removedTag = account.getTags().remove(j);
                            if(removedTag.getObjectImageName() != null)
                                FileManager.getTagImageFileForAutoSynchronization(removedTag).delete();
                        }

                    accountDataUpdatedFromServer = true;
                }

                if(profileList != null)
                {
                    account.getProfiles().clear();
                    SortUtility.sortTagListByUID(account.getTags());

                    for(Profile profile : profileList)
                    {
                        Profile tmp = new Profile(profile.getName());
                        for(Tag tag : profile.getTags())
                            tmp.getTags().add(SortUtility.getTagByUID(account.getTags(), tag.getUid()));

                        account.getProfiles().add(tmp);
                    }

                    accountDataUpdatedFromServer = true;
                }

                accountMutex.release();
            }
            catch (NetworkServiceException e) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "A network service error has occured : " + e.getMessage());
                e.printStackTrace();
            }
            catch (NotAuthenticatedException e) { // this case can't arrive.
                failedOnPassword = true;
                continueAutoSynchronization = false;
            } catch (InterruptedException e) { // will never occur.
                e.printStackTrace();
            }
        }
    }



// Singleton desing pattern used to be sure there is only one engine service instance.

    private static EngineService instance = new EngineService();

    public static EngineService getInstance()
    {
        return instance;
    }
}

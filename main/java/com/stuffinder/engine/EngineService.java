package com.stuffinder.engine;

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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

    Account currentAccount; // non null if authentication done, null otherwise.
    String currentPassword; // non null if authentication done, null otherwise.

    List<Tag> tags;
    List<Profile> profiles;

    /**
     * The requests to do throw modifications on the server. Used when auto-synchronization is disabled.
     */
    List<Requests.Request> requests;

    public EngineService()
    {
        tags = new ArrayList<>();
        profiles = new ArrayList<>();

        requests = new ArrayList<>();
    }

    public void initEngineService() throws NetworkServiceException {
        // nothing to do.
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
            for(Profile profile : NetworkServiceProvider.getNetworkService().getProfiles())
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
                autoSynchronizer.startAutoSynchronization(currentAccount, tags, profiles, currentPassword);
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

        currentAccount = null;
        currentPassword = null;

        tags.clear();
        profiles.clear();

        Logger.getLogger(getClass().getName()).log(Level.INFO, "Log out operation done.");
    }

    public Account getCurrentAccount() throws NotAuthenticatedException {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        return currentAccount;
    }

    public void modifyEMailAddress(String newEmailAddress) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();

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

        if(! FieldVerifier.verifyPassword(newPassword))
            throw  new IllegalFieldException(IllegalFieldException.PASSWORD, IllegalFieldException.REASON_VALUE_INCORRECT, newPassword);

        addRequest(new ModifyPasswordRequest(newPassword));
    }

    public List<Tag> getTags() throws NotAuthenticatedException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();

        return tags;
    }

    public Tag addTag(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();

        if(! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
        if(! FieldVerifier.verifyName(tag.getObjectName()))
            throw new IllegalFieldException(TAG_OBJECT_NAME, REASON_VALUE_INCORRECT, tag.getObjectName());
        if(tag.getObjectImageName() != null && FieldVerifier.verifyImageFileName(tag.getObjectImageName()) == false)
            throw new IllegalFieldException(TAG_OBJECT_IMAGE, REASON_VALUE_INCORRECT, tag.getObjectImageName());

        for(Tag tmp : tags)
        {
            if(tmp.getObjectName().equals(tag.getObjectName()))
                throw new IllegalFieldException(TAG_OBJECT_NAME, REASON_VALUE_ALREADY_USED, tag.getObjectName());
            else if(tmp.getUid().equals(tag.getUid()))
                throw new IllegalFieldException(TAG_UID, REASON_VALUE_ALREADY_USED, tag.getUid());
        }

        Tag tmp = new Tag(tag.getUid(), tag.getObjectName(), tag.getObjectImageName());
        tags.add(tmp);

        addRequest(new AddTagRequest(new Tag(tag.getUid(), tag.getObjectName(), tag.getObjectImageName()))); // new tag to be sure it will not be modified.

        return tmp;
    }

    public Tag modifyObjectName(Tag tag, String newObjectName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();

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

    public Tag modifyObjectImage(Tag tag, String newImageFileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {

        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();

        if(! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
        if(tag.getObjectImageName() != null && FieldVerifier.verifyImageFileName(newImageFileName) == false)
            throw new IllegalFieldException(TAG_OBJECT_IMAGE, REASON_VALUE_INCORRECT, newImageFileName);


        int index = tags.indexOf(tag);

        if(index < 0)
            throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_NOT_FOUND, tag.getUid());
        else
        {
            Tag tmp = tags.get(index);
            tmp.setObjectImageName(newImageFileName);

            addRequest(new ModifyTagObjectImageRequest(tag, newImageFileName));

            return tmp;
        }
    }

    public void removeTag(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(currentAccount == null)
            throw new NotAuthenticatedException();

        checkForAccountUpdate();

        if(! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());

        int index = tags.indexOf(tag);

        if(index < 0)
            throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_NOT_FOUND, tag.getUid());
        else
        {
            addRequest(new RemoveTagRequest(tags.get(index)));
            tags.remove(index);
        }
    }



    public Profile createProfile(String profileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    public Profile addTagToProfile(Profile profile, Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    public Profile addTagsToProfile(Profile profile, List<Tag> tags) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    public Profile removeTagFromProfile(Profile profile, Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    public Profile removeAllFromProfile(Profile profile) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    public Profile replaceTagListOfProfile(Profile profile, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    public Profile replaceTagListOfProfile(Profile profile, Tag[] tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    public Profile getProfile(String profileName) throws NotAuthenticatedException {
        return null;
    }

    public List<Profile> getProfiles() throws NotAuthenticatedException, NetworkServiceException {
        return null;
    }



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

    private AutoSynchronizer autoSynchronizer;

    public void setAutoSynchronization(boolean enable) throws NotAuthenticatedException
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

    public boolean isAutoSynchronizationEnabled()
    {
        return autoSynchronizer != null;
    }

    private void checkForAccountUpdate()
    {
        if(isAutoSynchronizationEnabled() && autoSynchronizer.isErrorOccurredOnData())
        {
            AutoSynchronizer.AccountData accountData = autoSynchronizer.getAccountCopyUpdatedAfterErrorOnData();

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

    class AutoSynchronizer extends Thread
    {
//        private BlockingQueue<Request> requestQueue;

        Semaphore requestsMutex;
        private List<Request> requests;
        private Semaphore requestNumber;

        private boolean errorOccurredOnData;


        private Semaphore accountMutex;

        /**
         * this account field is a version synchronized with server.
         */
        private Account account;
        private String password;


//        private BlockingQueue<Request> failedRequestQueue;
//        private BlockingQueue<IllegalFieldException> catchedExceptionQueue;

        private boolean failedOnPassword;

        private boolean continueAutoSynchronization;

        AutoSynchronizer()
        {
            requestsMutex = new Semaphore(1);
            requests = new LinkedList<>();

            requestNumber = new Semaphore(0);

            accountMutex = new Semaphore(1);

            errorOccurredOnData = false;
            failedOnPassword = false;

//            requestQueue = new LinkedBlockingQueue<>();
//            failedRequestQueue = new LinkedBlockingQueue<>();
//            catchedExceptionQueue = new LinkedBlockingQueue<>();
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

//        BlockingQueue<Request> getFailedRequestQueue()
//        {
//            return failedRequestQueue;
//        }
//
//        BlockingQueue<IllegalFieldException> getCatchedExceptionQueue()
//        {
//            return catchedExceptionQueue;
//        }

        boolean hasFailedOnPasswork()
        {
            return failedOnPassword;
        }

        List<Request> getNotDoneRequests()
        {
            Object array[] = requests.toArray();

            List<Request> requestList = new ArrayList<>();
            for (Object anArray : array)
                requestList.add((Request) anArray);

            return requestList;
        }

        boolean isErrorOccurredOnData() {
            return errorOccurredOnData;
        }

        void startAutoSynchronization(Account account, List<Tag> tagList, List<Profile> profileList, String password)
        {
            if(account == null || password == null)
                throw new NullPointerException("account and password parameters can't be null");

            this.account = new Account(account.getPseudo(), account.getFirstName(), account.getLastName(), account.getEMailAddress());

            List<Tag> tags = this.account.getTags();

            for(Tag tag : tagList)
                tags.add(new Tag(tag.getUid(), tag.getObjectName(), tag.getObjectImageName()));

            List<Profile> profiles = this.account.getProfiles();

            for(Profile profile : profileList)
            {
                Profile tmp = new Profile(profile.getName());

                for(Tag tag : profile.getTags())
                    tmp.addTag(tags.get(tags.indexOf(tag)));

                profiles.add(tmp);
            }

            start();
        }

        void startAfterErrorOnPassword(String password)
        {
            if(isAlive() || ! failedOnPassword)
                throw new IllegalStateException("Auto synchronizer already running.");

            failedOnPassword = false;

            this.password = password;
            start();
        }

        Account getAccountCopy()
        {
            try {
                accountMutex.acquire();

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
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null; // to be modified.
            }
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

        AccountData getAccountCopyUpdatedAfterErrorOnData()
        {
            try {
                requestsMutex.acquire();
                Account copy = getAccountCopy();

                AccountData accountData = new AccountData(copy, password);

                List<Request> requestList = getNotDoneRequests();
                errorOccurredOnData = false;

                requestsMutex.release();

                for(Request request : requestList)
                {

                    switch (request.getRequestType()) {
                        case MODIFY_EMAIL:
                            ModifyEmailRequest modifyEmailRequest = (ModifyEmailRequest) request;

                            account.setMailAddress(modifyEmailRequest.getNewEmailAddress());
                            break;
                        case MODIFY_PASSWORD:
                            ModifyPasswordRequest modifyPasswordRequest = (ModifyPasswordRequest) request;
                            password = modifyPasswordRequest.getNewPassword();
                            break;
                        case ADD_TAG:
                            AddTagRequest addTagRequest = (AddTagRequest) request;
                            if(! account.getTags().contains(addTagRequest.getNewTag()))
                            {
                                boolean applyAddition = true;
                                for(Tag tag : account.getTags())
                                    if(tag.getObjectName().equals(addTagRequest.getNewTag().getObjectName()))
                                        applyAddition = false;
                                if(applyAddition)
                                    account.getTags().add(new Tag(addTagRequest.getNewTag().getUid(), addTagRequest.getNewTag().getObjectName(), addTagRequest.getNewTag().getObjectImageName()));
                            }
                            break;
                        case MODIFY_TAG_OBJECT_NAME:
                            ModifyTagObjectNameRequest modifyTagObjectNameRequest = (ModifyTagObjectNameRequest) request;
                            int index = account.getTags().indexOf(modifyTagObjectNameRequest.getTag());
                            if(index >= 0)
                            {
                                Tag tmp = account.getTags().get(index);
                                boolean applyModification = true;
                                for(Tag tag : account.getTags())
                                    if(tag != tmp && tag.getObjectName().equals(modifyTagObjectNameRequest.getTag().getObjectName()))
                                        applyModification = false;
                                if(applyModification)
                                    account.getTags().get(index).setObjectName(modifyTagObjectNameRequest.getNewObjectName());
                            }
                            break;
                        case MODIFY_TAG_OBJECT_IMAGE:
                            ModifyTagObjectImageRequest modifyTagObjectImageRequest = (ModifyTagObjectImageRequest) request;
                            account.getTags().get(account.getTags().indexOf(modifyTagObjectImageRequest.getTag())).setObjectImageName(modifyTagObjectImageRequest.getNewObjectImageFilename());
                            break;
                        case REMOVE_TAG:
                            RemoveTagRequest removeTagRequest = (RemoveTagRequest) request;
                            account.getTags().remove(removeTagRequest.getTag());
                            break;
                    }
                }

                return accountData;
            } catch (InterruptedException e) {
                return null;
            }

        }


        void stopAutoSynchronization()
        {
            continueAutoSynchronization = false;
            requestNumber.release();
        }

        private void removeFirstRequest()
        {
            try {
                requestsMutex.acquire();

                requests.remove(0);
                requestsMutex.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private Request getFirstRequest()
        {
            try {
                requestsMutex.acquire();

                Request tmp = requests.get(0);
                requestsMutex.release();

                return tmp;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        public void run() {
            Request currentRequest = null;
            continueAutoSynchronization = true;
            String errorMessage = null;

            while(continueAutoSynchronization)
            {
                try {
                    requestNumber.acquire();
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

                                accountMutex.acquire();
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

                                accountMutex.acquire();
                                password = modifyPasswordRequest.getNewPassword();
                                accountMutex.release();
                            }
                            catch(IllegalFieldException e)
                            {
                                errorMessage = "password modification failed : specified passxord is incorrect.";
                                throw e;
                            }
                            break;
                        case ADD_TAG:
                            try
                            {
                                AddTagRequest addTagRequest = (AddTagRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().addTag(addTagRequest.getNewTag());

                                accountMutex.acquire();
                                account.getTags().add(new Tag(addTagRequest.getNewTag().getUid(), addTagRequest.getNewTag().getObjectName(), addTagRequest.getNewTag().getObjectImageName()));
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
                                throw e;
                            }
                            break;
                        case MODIFY_TAG_OBJECT_NAME:
                            try
                            {
                                ModifyTagObjectNameRequest modifyTagObjectNameRequest = (ModifyTagObjectNameRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().modifyObjectName(modifyTagObjectNameRequest.getTag(), modifyTagObjectNameRequest.getNewObjectName());

                                accountMutex.acquire();
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
                            try
                            {
                                ModifyTagObjectImageRequest modifyTagObjectImageRequest = (ModifyTagObjectImageRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().modifyObjectImage(modifyTagObjectImageRequest.getTag(), modifyTagObjectImageRequest.getNewObjectImageFilename());

                                accountMutex.acquire();
                                account.getTags().get(account.getTags().indexOf(modifyTagObjectImageRequest.getTag())).setObjectImageName(modifyTagObjectImageRequest.getNewObjectImageFilename());
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
                                    case IllegalFieldException.TAG_OBJECT_IMAGE :
                                        errorMessage = "Tag addition failed : image filename is incorrect";
                                        break;
                                }
                                throw e;
                            }
                            break;
                        case REMOVE_TAG:
                            try
                            {
                                RemoveTagRequest removeTagRequest = (RemoveTagRequest) currentRequest;
                                NetworkServiceProvider.getNetworkService().removeTag(removeTagRequest.getTag());

                                accountMutex.acquire();
                                account.getTags().remove(removeTagRequest.getTag());
                                accountMutex.release();
                            }
                            catch(IllegalFieldException e) // the only possible error is about the tag UID.
                            {
                                if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                    errorMessage = "Tag modification failed : tag with UID \"" + e.getFieldValue() + "\" is not found.";
                                else
                                    errorMessage = "Tag modification failed : tag UID \"" + e.getFieldValue() + "\" is incorrect.";

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
                } catch (NotAuthenticatedException e) { // this case can't arrive.
                    failedOnPassword = true;
                    continueAutoSynchronization = false;
                    requestNumber.release();
                } catch (NetworkServiceException e) {// maybe the connexion to the server has failed.
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "A network service error has occured : " + e.getMessage());
                    //TODO implement an optimized solution.
                    requestNumber.release();
                } catch (InterruptedException e) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Interruption has occured :   " + e);
                }
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

package com.stuffinder.webservice;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import com.stuffinder.data.Account;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.FieldVerifier;
import com.stuffinder.exceptions.AccountNotFoundException;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;
import com.stuffinder.interfaces.NetworkServiceInterface;

public class NetworkService implements NetworkServiceInterface {

    private static HttpClient client;
    private Account currentAccount = null;
    private String currentPassword = null;

    private NetworkService() {

    }

    private HttpResponse executeRequest(HttpUriRequest request) throws InterruptedException, IOException {
        HttpExecuter httpExecuter = new HttpExecuter(request);

        httpExecuter.start();
        httpExecuter.join();

        if(httpExecuter.isRequestSuccessful())
            return httpExecuter.getResponse();
        else
            throw httpExecuter.getCatchedIOException();
    }

    class HttpExecuter extends Thread {
        private HttpUriRequest request;

        private HttpResponse response;
        private IOException catchedIOException;

        HttpExecuter(HttpUriRequest request){
            super();
            this.request = request;
        }

        @Override
        public void run(){
            try {
                response = client.execute(request);
            } catch (IOException e) {
                catchedIOException = e;
            }
        }

        boolean isRequestSuccessful(){
            return catchedIOException == null;
        }

        IOException getCatchedIOException(){
            return catchedIOException;
        }

        HttpResponse getResponse(){
            return response;
        }
    }

    @Override
    public void initNetworkService() throws NetworkServiceException {
        NetworkService.client = new DefaultHttpClient();
    }

    @Override
    public void createAccount(Account newAccount, String newPassword)
            throws IllegalFieldException, NetworkServiceException {    // We first check the validity of the arguments to create the parameters
        if (!FieldVerifier.verifyName(newAccount.getPseudo()))
            throw new IllegalFieldException(IllegalFieldException.PSEUDO, IllegalFieldException.REASON_VALUE_INCORRECT, newAccount.getPseudo());

        if (!FieldVerifier.verifyPassword(newPassword))
            throw new IllegalFieldException(IllegalFieldException.PASSWORD, IllegalFieldException.REASON_VALUE_INCORRECT, newPassword);

        if (!FieldVerifier.verifyName(newAccount.getFirstName()))
            throw new IllegalFieldException(IllegalFieldException.FIRSTNAME, IllegalFieldException.REASON_VALUE_INCORRECT, newAccount.getFirstName());

        if (!FieldVerifier.verifyName(newAccount.getLastName()))
            throw new IllegalFieldException(IllegalFieldException.LASTNAME, IllegalFieldException.REASON_VALUE_INCORRECT, newAccount.getLastName());

        if (!FieldVerifier.verifyEMailAddress(newAccount.getEMailAddress()))
            throw new IllegalFieldException(IllegalFieldException.EMAIL_ADDRESS, IllegalFieldException.REASON_VALUE_INCORRECT, newAccount.getEMailAddress());

        InputStream inputStream;
        String result = "";
        try {
            // make GET request to the given URL
            HttpResponse httpResponse = executeRequest(new HttpGet("http://92.222.33.38:8080/app_server/ns/register?pseudo=" + newAccount.getPseudo() + "&password=" + newPassword + "&first_name=" + newAccount.getFirstName() + "&last_name=" + newAccount.getLastName() + "&email=" + newAccount.getEMailAddress()));
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // receive response as inputStream
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();
                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        // creation JSON Object
                        JSONObject obj = new JSONObject(result);
                        int returnCode = obj.getInt("returncode");
                        if (returnCode == 0) {
                        }
                        // Else display error message
                        else if (returnCode == 1) {
                            currentPassword = null;
                            throw new IllegalFieldException(IllegalFieldException.PSEUDO, IllegalFieldException.REASON_VALUE_ALREADY_USED, newAccount.getPseudo());
                        } else {
                            currentPassword = null;
                            throw new IllegalFieldException(IllegalFieldException.PSEUDO, IllegalFieldException.REASON_VALUE_INCORRECT, newAccount.getPseudo());
                        }
                    } catch (JSONException e) {
                        // "Error Occured [Server's JSON response might be invalid]!"
                        throw new NetworkServiceException("Server response might be invalid.");
                    }
                } else {
                    throw new NetworkServiceException("Connection issue with ther server, null imput stream");
                }
            }

            // When Http response code is '404'
            else if (statusCode == 404) {
                throw new NetworkServiceException("Requested resource not found");
            }

            // When Http response code is '500'
            else if (statusCode == 500) {
                throw new NetworkServiceException("Something went wrong at server end");
            }

            // When Http response code other than 404, 500
            else {
                throw new NetworkServiceException("Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        } catch (IOException | IllegalStateException e) {
            throw new NetworkServiceException("exception of type IOExcption or IllegalStateException catched.");
        } catch (InterruptedException e) {
            throw new NetworkServiceException("error occured while executing request.");
        }

    }


    @Override
    public Account authenticate(String pseudo, String password)
            throws AccountNotFoundException, NetworkServiceException {
        // We first check the validity of the arguments to create the parameters
        if(! FieldVerifier.verifyName(pseudo))
            throw new IllegalFieldException(IllegalFieldException.PSEUDO, IllegalFieldException.REASON_VALUE_INCORRECT, pseudo);

        if (! FieldVerifier.verifyPassword(password))
            throw new IllegalFieldException(IllegalFieldException.PASSWORD, IllegalFieldException.REASON_VALUE_INCORRECT, password);

        InputStream inputStream;
        String result = "";

        try {
            // make GET request to the given URL
            HttpResponse httpResponse = executeRequest(new HttpGet("http://92.222.33.38:8080/app_server/ns/login?pseudo=" + pseudo + "&password=" + password));
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // receive response as inputStream
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();

                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        // creation JSON Object
                        JSONObject obj = new JSONObject(result);
                        int returnCode = obj.getInt("returncode");
                        if (returnCode == 0) {
                            currentAccount = new Account(obj.getString("pseudo"), obj.getString("first_name"), obj.getString("last_name"), obj.getString("email"));
                            currentPassword = password; // correction.
                        }
                        // Else display error message
                        else if (returnCode == 1) {
                            currentPassword = null;
                            throw new AccountNotFoundException();
                        }
                    } catch (JSONException e) {
                        // "Error Occured [Server's JSON response might be invalid]!"
                        throw new NetworkServiceException("Server response might be invalid.");
                    }
                } else {
                    throw new NetworkServiceException("Connection issue with ther server, null imput stream");
                }
            }


            // When Http response code is '404'
            else if (statusCode == 404) {
                throw new NetworkServiceException("Requested resource not found");
            }

            // When Http response code is '500'
            else if (statusCode == 500) {
                throw new NetworkServiceException("Something went wrong at server end");
            }

            // When Http response code other than 404, 500
            else {
                throw new NetworkServiceException("Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        } catch (IOException | IllegalStateException e) {
            throw new NetworkServiceException("exception of type IOExcption or IllegalStateException catched.");
        } catch (InterruptedException e) {
            throw new NetworkServiceException("error occured while executing request.");
        }

        return currentAccount;
    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException, InterruptedException {
        InputStreamConverter inputStreamConverter = new InputStreamConverter(inputStream);

        inputStreamConverter.start();
        inputStreamConverter.join();

        if(inputStreamConverter.isConversionSuccessful())
            return inputStreamConverter.getResult();
        else
            throw inputStreamConverter.getCatchedIOException();
    }

    static class InputStreamConverter extends Thread{
        private InputStream inputStream;
        private String result;

        IOException catchedIOException;

        InputStreamConverter(InputStream inputStream){
            this.inputStream = inputStream;
        }

        public void run(){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                result = "";
                while ((line = bufferedReader.readLine()) != null)
                    result += line;
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                catchedIOException = e;
            }
        }

        public boolean isConversionSuccessful(){
            return catchedIOException == null;
        }

        IOException getCatchedIOException(){
            return catchedIOException;
        }

        String getResult(){
            return result;
        }
    }


    @Override
    public void logOut() {
        currentAccount = null;
        currentPassword = null;
    }

    @Override
    public Account getCurrentAccount() throws NotAuthenticatedException {
        if (currentAccount != null) {
            return currentAccount;
        } else {
            throw new NotAuthenticatedException();
        }
    }

    @Override
    public void modifyEMailAddress(String emailAddress)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // We first check the validity of the arguments to create the parameters
        if (! FieldVerifier.verifyEMailAddress(emailAddress))
            throw new IllegalFieldException(IllegalFieldException.EMAIL_ADDRESS, IllegalFieldException.REASON_VALUE_INCORRECT, emailAddress);

        InputStream inputStream;
        String result = "";
        try {
            // make GET request to the given URL
            HttpResponse httpResponse = executeRequest(new HttpGet("http://92.222.33.38:8080/app_server/ns/modifyemail?pseudo=" + currentAccount.getPseudo() + "&password=" + currentPassword + "&new_email=" + emailAddress));
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // receive response as inputStream
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();
                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        // creation JSON Object
                        JSONObject obj = new JSONObject(result);
                        int returnCode = obj.getInt("returncode");
                        if (returnCode == 0) {
                            currentAccount.setMailAddress(obj.getString("email"));
                        }
                        // Else display error message
                        else if (returnCode == 1) {
                            throw new NetworkServiceException("Problem of access to the DB");
                        } else {
                            throw new NotAuthenticatedException();
                        }
                    } catch (JSONException e) {
                        // "Error Occured [Server's JSON response might be invalid]!"
                        throw new NetworkServiceException("Server response might be invalid.");
                    }
                } else {
                    throw new NetworkServiceException("Connection issue with ther server, null imput stream");
                }
            }


            // When Http response code is '404'
            else if (statusCode == 404) {
                throw new NetworkServiceException("Requested resource not found");
            }

            // When Http response code is '500'
            else if (statusCode == 500) {
                throw new NetworkServiceException("Something went wrong at server end");
            }

            // When Http response code other than 404, 500
            else {
                throw new NetworkServiceException("Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        } catch (IOException | IllegalStateException e) {
            throw new NetworkServiceException("exception of type IOExcption or IllegalStateException catched.");
        } catch (InterruptedException e) {
            throw new NetworkServiceException("error occured while executing request.");
        }

    }

    @Override
    public void modifyPassword(String newPassword)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // We first check the validity of the arguments to create the parameters
        if (! FieldVerifier.verifyPassword(newPassword))
            throw new IllegalFieldException(IllegalFieldException.PASSWORD, IllegalFieldException.REASON_VALUE_INCORRECT, newPassword);

        InputStream inputStream;
        String result = "";
        try {
            // make GET request to the given URL
            HttpResponse httpResponse = executeRequest(new HttpGet("http://92.222.33.38:8080/app_server/ns/modifypassword?pseudo=" + currentAccount.getPseudo() + "&password=" + currentPassword + "&new_password=" + newPassword));
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // receive response as inputStream
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();
                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        // creation JSON Object
                        JSONObject obj = new JSONObject(result);
                        int returnCode = obj.getInt("returncode");
                        if (returnCode == 0) {
                            currentPassword = newPassword;
                        }
                        // Else display error message
                        else if (returnCode == 1) {
                            throw new NetworkServiceException("Problem of access to the DB");
                        } else {
                            throw new NotAuthenticatedException();
                        }
                    } catch (JSONException e) {
                        // "Error Occured [Server's JSON response might be invalid]!"
                        throw new NetworkServiceException("Server response might be invalid.");
                    }
                } else {
                    throw new NetworkServiceException("Connection issue with ther server, null imput stream");
                }
            }


            // When Http response code is '404'
            else if (statusCode == 404) {
                throw new NetworkServiceException("Requested resource not found");
            }

            // When Http response code is '500'
            else if (statusCode == 500) {
                throw new NetworkServiceException("Something went wrong at server end");
            }

            // When Http response code other than 404, 500
            else {
                throw new NetworkServiceException("Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        } catch (IOException | IllegalStateException e) {
            throw new NetworkServiceException("exception of type IOExcption or IllegalStateException catched.");
        } catch (InterruptedException e) {
            throw new NetworkServiceException("error occured while executing request.");
        }
    }

    @Override
    public List<Tag> getTags() throws NotAuthenticatedException,
            NetworkServiceException {
        List<Tag> res = new ArrayList<>();
        // We first check the validity of the arguments to create the parameters
        if (! FieldVerifier.verifyName(currentAccount.getPseudo()))
            throw new IllegalFieldException(IllegalFieldException.PSEUDO, IllegalFieldException.REASON_VALUE_INCORRECT, currentAccount.getPseudo());

        InputStream inputStream;
        String result = "";
        try {
            // make GET request to the given URL
            HttpResponse httpResponse = executeRequest(new HttpGet("http://92.222.33.38:8080/app_server/ns/retrievetag?pseudo=" + currentAccount.getPseudo() + "&password=" + currentPassword));
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // receive response as inputStream
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();
                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        // creation JSON Object
                        JSONObject obj = new JSONObject(result);
                        int returnCode = obj.getInt("returncode");
                        if (returnCode == 0) {
                            org.json.JSONArray arrayOfJsonTag = obj.getJSONArray("listTags");
                            for (int i = 0; i < arrayOfJsonTag.length(); i++) {
                                JSONObject tagjson = arrayOfJsonTag.getJSONObject(i);
                                Tag tag = new Tag(tagjson.getString("tag_id"), tagjson.getString("object_name"), tagjson.getString("picture"));
                                res.add(tag);
                            }
                        }
                        // Else display error message

                        else {
                            throw new NotAuthenticatedException();
                        }
                    } catch (JSONException e) {
                        Log.e("content of the catched JSON", result);
                        e.printStackTrace();
                        // "Error Occured [Server's JSON response might be invalid]!"
                        throw new NetworkServiceException("Server response might be invalid.");
                    }
                } else {
                    throw new NetworkServiceException("Connection issue with ther server, null imput stream");
                }
            }


            // When Http response code is '404'
            else if (statusCode == 404) {
                throw new NetworkServiceException("Requested resource not found");
            }

            // When Http response code is '500'
            else if (statusCode == 500) {
                throw new NetworkServiceException("Something went wrong at server end");
            }

            // When Http response code other than 404, 500
            else {
                throw new NetworkServiceException("Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            throw new NetworkServiceException("exception of type IOExcption or IllegalStateException catched.");
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new NetworkServiceException("error occured while executing request.");
        }

        return res;
    }

    @Override
    public Tag addTag(Tag tag) throws NotAuthenticatedException,
            IllegalFieldException, NetworkServiceException {
        // We first check the validity of the arguments to create the parameters
        if (! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_INCORRECT, tag.getUid());

        if (! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_NAME, IllegalFieldException.REASON_VALUE_INCORRECT, tag.getObjectName());

        InputStream inputStream;
        String result = "";
        try {
            // make GET request to the given URL
            HttpResponse httpResponse = executeRequest(new HttpGet("http://92.222.33.38:8080/app_server/ns/addtag?pseudo=" + currentAccount.getPseudo() + "&password=" + currentPassword + "&id=" + tag.getUid() + "&object_name=" + tag.getObjectName() + "&picture=" + tag.getObjectImageName()));
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // receive response as inputStream
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();
                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        // creation JSON Object
                        JSONObject obj = new JSONObject(result);
                        int returnCode = obj.getInt("returncode");
                        if (returnCode == 0) {
                        }
                        // Else display error message

                        else {
                            throw new NetworkServiceException("Wrong pseudo/password combination or access to the DB");
                        }
                    } catch (JSONException e) {
                        // "Error Occured [Server's JSON response might be invalid]!"
                        throw new NetworkServiceException("Server response might be invalid.");
                    }
                } else {
                    throw new NetworkServiceException("Connection issue with ther server, null imput stream");
                }
            }


            // When Http response code is '404'
            else if (statusCode == 404) {
                throw new NetworkServiceException("Requested resource not found");
            }

            // When Http response code is '500'
            else if (statusCode == 500) {
                throw new NetworkServiceException("Something went wrong at server end");
            }

            // When Http response code other than 404, 500
            else {
                throw new NetworkServiceException("Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        } catch (IOException | IllegalStateException e) {
            throw new NetworkServiceException("exception of type IOExcption or IllegalStateException catched.");
        } catch (InterruptedException e) {
            throw new NetworkServiceException("error occured while executing request.");
        }

        return tag;
    }

    /**
     * Downloads an object image and returns the path of the downloaded image file.
     *
     * @param tag The tag to be modified.
     * @return the local filePath of the tag.
     * @throws com.stuffinder.exceptions.NotAuthenticatedException If the authentication is not done.
     * @throws com.stuffinder.exceptions.IllegalFieldException     If one field (i.e. one information) is illegal. <br/>
     *                                                             The possible fields with the reason(s) are : <br/>
     *                                                             <ul>
     *                                                             <li>{@link com.stuffinder.exceptions.IllegalFieldException#TAG_UID tag uid} (reasons {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_INCORRECT value incorrect} if it is syntactically incorrect and {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_NOT_FOUND value not found} if the tag is not found) </li>
     *                                                             <li>{@link com.stuffinder.exceptions.IllegalFieldException#TAG_OBJECT_IMAGE TAG_OBJECT_IMAGE} (reason {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no image associated with this tag).</li>
     *                                                             </ul>
     * @throws com.stuffinder.exceptions.NetworkServiceException   If a network service error has occurred.
     */
    @Override
    public String downloadObjectImage(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    @Override
    public Tag modifyObjectName(Tag tag, String newObjectName)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // We first check the validity of the arguments to create the parameters
        if (! FieldVerifier.verifyName(newObjectName))
            throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_NAME, IllegalFieldException.REASON_VALUE_INCORRECT, newObjectName);

        InputStream inputStream = null;
        String result = "";
        try {
            // make GET request to the given URL
            HttpResponse httpResponse = executeRequest(new HttpGet("http://92.222.33.38:8080/app_server/ns/modifyobjectname?pseudo=" + currentAccount.getPseudo() + "&password=" + currentPassword + "&id=" + tag.getUid() + "&new_object_name=" + newObjectName));
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // receive response as inputStream
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();
                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        // creation JSON Object
                        JSONObject obj = new JSONObject(result);
                        int returnCode = obj.getInt("returncode");
                        if (returnCode == 0) {
                            tag.setObjectName(obj.getString("newobjectname"));
                        }
                        // Else display error message
                        else if (returnCode == 1) {
                            throw new NetworkServiceException("Problem of access to the DB");
                        } else {
                            throw new NotAuthenticatedException();
                        }
                    } catch (JSONException e) {
                        // "Error Occured [Server's JSON response might be invalid]!"
                        throw new NetworkServiceException("Server response might be invalid.");
                    }
                } else {
                    throw new NetworkServiceException("Connection issue with ther server, null imput stream");
                }
            }


            // When Http response code is '404'
            else if (statusCode == 404) {
                throw new NetworkServiceException("Requested resource not found");
            }

            // When Http response code is '500'
            else if (statusCode == 500) {
                throw new NetworkServiceException("Something went wrong at server end");
            }

            // When Http response code other than 404, 500
            else {
                throw new NetworkServiceException("Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        } catch (IOException | IllegalStateException e) {
            throw new NetworkServiceException("exception of type IOExcption or IllegalStateException catched.");
        } catch (InterruptedException e) {
            throw new NetworkServiceException("error occured while executing request.");
        }

        return tag;
    }

    @Override
    public Tag modifyObjectImage(Tag tag, String newImageFileName)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeTag(Tag tag) throws NotAuthenticatedException,
            IllegalFieldException, NetworkServiceException {
        InputStream inputStream;
        String result = "";
        try {
            // make GET request to the given URL
            HttpResponse httpResponse = executeRequest(new HttpGet("http://92.222.33.38:8080/app_server/ns/deletetag?pseudo=" + currentAccount.getPseudo() + "&password=" + currentPassword + "&id=" + tag.getUid()));
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                // receive response as inputStream
                HttpEntity entity = httpResponse.getEntity();
                inputStream = entity.getContent();
                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    try {
                        // creation JSON Object
                        JSONObject obj = new JSONObject(result);
                        int returnCode = obj.getInt("returncode");
                        if (returnCode == 0) {
                        }
                        // Else display error message
                        else if (returnCode == 1) {
                            throw new NetworkServiceException("Problem of access to the DB");
                        } else {
                            throw new NotAuthenticatedException();
                        }
                    } catch (JSONException e) {
                        // "Error Occured [Server's JSON response might be invalid]!"
                        throw new NetworkServiceException("Server response might be invalid.");
                    }
                } else {
                    throw new NetworkServiceException("Connection issue with ther server, null imput stream");
                }
            }


            // When Http response code is '404'
            else if (statusCode == 404) {
                throw new NetworkServiceException("Requested resource not found");
            }

            // When Http response code is '500'
            else if (statusCode == 500) {
                throw new NetworkServiceException("Something went wrong at server end");
            }

            // When Http response code other than 404, 500
            else {
                throw new NetworkServiceException("Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
            }
        } catch (IOException | IllegalStateException e) {
            throw new NetworkServiceException("exception of type IOExcption or IllegalStateException catched.");
        } catch (InterruptedException e) {
            throw new NetworkServiceException("error occured while executing request.");
        }

    }

    @Override
    public Profile createProfile(String profileName)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Creates a new profile named <code>profileName</code> and adds the tags of the list <code>tagList</code> in this profile.
     * <p/>
     * <br/><br />Only the UID of the tags are used to perform this operation.
     * <p/>
     * <br />If one of these UID is incorrect or doesn't exist (i.e. no tag linked with the current account has this UID),
     * an exception of type {@link com.stuffinder.exceptions.IllegalFieldException} is thrown to indicate this problem.
     * <p/>
     * <br />If a tag is twice or more in the specified tag list, it will be added only once.
     * <p/>
     * <br /><br />At the end of call, it returns the new profile.
     *
     * @param profileName The name of the new profile to be created.
     * @param tagList     The tags to add in the new profile.
     * @return The profile created.
     * @throws com.stuffinder.exceptions.NotAuthenticatedException If the authentication is not done.
     * @throws com.stuffinder.exceptions.IllegalFieldException     If one field (i.e. one information) is illegal. <br/>
     *                                                             The possible fields with the reason(s) are : <br/>
     *                                                             <ul>
     *                                                             <li>{@link com.stuffinder.exceptions.IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if you have already a profile with this name) </li>
     *                                                             <li>{@link com.stuffinder.exceptions.IllegalFieldException#TAG_UID tag UID} (reasons {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no tag with this UID) </li>
     *                                                             </ul>
     * @throws com.stuffinder.exceptions.NetworkServiceException   If a network service error has occurred.
     */
    @Override
    public Profile createProfile(String profileName, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    /**
     * Modify the name of a profile.
     *
     * @param profile        The profile to be modified.
     * @param newProfileName the new name for the profile.
     * @return The profile modified if this operation succeeds.
     * @throws com.stuffinder.exceptions.NotAuthenticatedException If the authentication is not done.
     * @throws com.stuffinder.exceptions.IllegalFieldException     If one field (i.e. one information) is illegal. <br/>
     *                                                             The possible fields with the reason(s) are : <br/>
     *                                                             <ul>
     *                                                             <li>{@link com.stuffinder.exceptions.IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect, {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile with this name and {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if you have already another profile with this name) </li>
     *                                                             </ul>
     * @throws com.stuffinder.exceptions.NetworkServiceException   If a network service error has occurred.
     */
    @Override
    public Profile modifyProfileName(Profile profile, String newProfileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }

    @Override
    public Profile addTagToProfile(Profile profile, Tag tag)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Profile addTagsToProfile(Profile profile, List<Tag> tags)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Profile removeTagFromProfile(Profile profile, Tag tag)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Removes tags from a profile.
     * <p/>
     * <br/><br />Only the UID of the tags are used to perform this operation.
     * <p/>
     * <br />If one of these UID is incorrect or doesn't exist (i.e. no tag linked with the current account has this UID),
     * an exception of type {@link com.stuffinder.exceptions.IllegalFieldException} is thrown to indicate this problem.
     * Else, each tag of this tag list are removed in the specified profile if it contains this tag.
     * <p/>
     * <br /><br />At the end of the call, if the profile is modified, it is returned. Else, <code>null</code> is returned.
     * <p/>
     * <br /> <br /> To remove all tags from a profile, call the method {@link #replaceTagListOfProfile(com.stuffinder.data.Profile, java.util.List)} with an empty list as second parameter.
     *
     * @param profile The profile to be modified.
     * @param tagList The tags to be removed from the specified profile.
     * @return The profile modified if this operation succeeds.
     * @throws com.stuffinder.exceptions.NotAuthenticatedException If the authentication is not done.
     * @throws com.stuffinder.exceptions.IllegalFieldException     If one field (i.e. one information) is illegal. <br/>
     *                                                             The possible fields with the reason(s) are : <br/>
     *                                                             <ul>
     *                                                             <li>{@link com.stuffinder.exceptions.IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
     *                                                             <li>{@link com.stuffinder.exceptions.IllegalFieldException#TAG_UID UID} of one tag of the list (reasons {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if no tag linked with the current account has this UID) </li>
     *                                                             </ul>
     * @throws com.stuffinder.exceptions.NetworkServiceException   If a network service error has occurred.
     */
    @Override
    public Profile removeTagsFromProfile(Profile profile, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {
        return null;
    }


    public Profile removeAllFromProfile(Profile profile)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Profile replaceTagListOfProfile(Profile profile, List<Tag> tagList)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Removes a profile.
     *
     * @param profile The profile to be removed.
     * @throws com.stuffinder.exceptions.NotAuthenticatedException If the authentication is not done.
     * @throws com.stuffinder.exceptions.IllegalFieldException     If one field (i.e. one information) is illegal. <br/>
     *                                                             The possible fields with the reason(s) are : <br/>
     *                                                             <ul>
     *                                                             <li>{@link com.stuffinder.exceptions.IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link com.stuffinder.exceptions.IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
     *                                                             </ul>
     * @throws com.stuffinder.exceptions.NetworkServiceException   If a network error has occurred.
     */
    @Override
    public void removeProfile(Profile profile) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException {

    }


    public Profile replaceTagListOfProfile(Profile profile, Tag[] tagList)
            throws NotAuthenticatedException, IllegalFieldException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Profile getProfile(String profileName)
            throws NotAuthenticatedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Profile> getProfiles() throws NotAuthenticatedException,
            NetworkServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLastTagsUpdateTime() throws NetworkServiceException {
        return 0;
    }

    @Override
    public int getLastProfilesUpdateTime() throws NetworkServiceException {
        return 0;
    }


    // to use singleton design pattern.

    private static NetworkService instance = new NetworkService();

    public static NetworkService getInstance() {
        return instance;
    }
}

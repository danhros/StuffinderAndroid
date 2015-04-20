package com.stuffinder.webservice;

/**
 * Created by philippeheurtaux on 22/03/15.
 */
public class ErrorCode {

    public final static int NO_ERROR = 0;

    public final static int MISSING_PSEUDO = 1;
    public final static int MISSING_PASSWORD = 2;
    public final static int MISSING_FIRST_NAME = 3;
    public final static int MISSING_LAST_NAME = 4;
    public final static int MISSING_EMAIL = 5;
    public final static int MISSING_TAG_ID = 6;
    public final static int MISSING_TAG_NAME = 7;
    public final static int MISSING_NEW_PASSWORD = 8;
    public final static int MISSING_NEW_OBJECT_NAME = 9;
    public final static int MISSING_PROFILE_NAME = 15;
    public final static int MISSING_TAG_PICTURE_NAME = 16;

    public final static int INVALID_PSEUDO_PASSWORD_COMBINATION = 10;
    public final static int USER_ALREADY_REGISTERED = 11;
    public final static int ILLEGAL_USE_OF_SPECIAL_CHARACTER = 12;
    public final static int DATABASE_ACCESS_ISSUE = 13;
    public final static int INFORMATION_INCOMPLETE = 14;

    public final static int UNKNOWN_ERROR = 666;


}

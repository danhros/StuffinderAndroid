package com.stuffinder.exceptions;

import com.stuffinder.interfaces.NetworkServiceInterface;

/**
 * Thrown to indicate that an illegal field has been passed to a method. The field can be incorrect (syntactically) or already used.<br/>
 * To know the concerned field, get the field id by using the method <code> {@link #getFieldId()} </code> and compare with the different constants of this class.<br/>
 * To get the reason why this field is illegal, get the reason code (an integer)  by using the method <code> {@link #getReason()}</code> and compare with the different constants (prefixed by <code>REASON_</code> ) of this class.<br/>
 * To get the field value which is illegal, use the method <code>{@link #getFieldValue()}</code>.<br/>
 * If the field is incorrect, there can be an additional message. To get it, use the method {@link #getMessage()}<br/><br/>
 * For example, if an exception of this type is thrown during a call to the method {@link NetworkServiceInterface#addTag(com.stuffinder.data.Tag) addTag(Tag tag)} of the interface {@link NetworkServiceInterface}, the code inside the catch clause can be : <br/>
 * <code><pre>
 * catch (IllegalFieldException e) {
 * 		switch(e.fieldId)
 * 		{
 * 			case IllegalFieldException.TAG_UID :
 * 				if(e.getReason() == IllegalFieldException.REASON_VALUE_INCORRECT)
 * 					system.err.println("The tag id is incorrect : " + e.getMessage());
 * 				else
 * 					system.err.println("The tag id is already used for another tag");
 * 			break;
 * 			case IllegalFieldException.TAG_OBJECT_NAME :
 * 				if(e.getReason() == IllegalFieldException.REASON_VALUE_INCORRECT)
 * 					system.err.println("The object name is incorrect : " + e.getMessage());
 * 				else
 * 					system.err.println("The object name is already used for another tag.");
 * 			break;
 * 			case IllegalFieldException.TAG_OBJECT_IMAGE :
 * 				system.err.println("The image filename is incorrect : " + e.getMessage());
 * 			break;
 * 			default:
 * 				system.err.println("Unknown error has occured");
 * 			break;
 * 		}
 * }
 * </pre></code>
 *
 * @author Nicolas Thierce
 *
 */
public class IllegalFieldException extends IllegalArgumentException
{
    private static final long serialVersionUID = 6349027407947995384L;

    private final int fieldId;
    private final int reason;
    private final String fieldValue;


    public static final int PSEUDO = 0;
    public static final int FIRSTNAME = 1;
    public static final int LASTNAME = 2;
    public static final int EMAIL_ADDRESS = 3;
    public static final int BIRTH_DATE = 4;
    public static final int PASSWORD = 5;

    public static final int TAG_UID = 10;
    public static final int TAG_OBJECT_NAME = 11;
    public static final int TAG_OBJECT_IMAGE = 12;

    public static final int PROFILE_NAME = 20;


    public static final int REASON_VALUE_INCORRECT = 0;
    public static final int REASON_VALUE_ALREADY_USED = 1;
    public static final int REASON_VALUE_NOT_FOUND = 2;

//	/**
//	 * Used only for the tag uid; For example, if the tag which has the Id "0ef35ca2b89a" is added into the account of the user 1, and the user 2 wants to add this tag to one of his profile, there will be an exception thrown with this reason.
//	 */
//	public static final int REASON_ACCESS_FORBIDDEN = 3;

    /**
     * Use the constructor {@link IllegalFieldException#IllegalFieldException(int, int, String, String) IllegalFieldException(int fieldId, int reason, String fieldValue, String message)}
     * @param fieldId the constant of this class matching with the concerned field.
     * @param message the message describing while the field value is illegal.
     * @see IllegalFieldException#IllegalFieldException(int, int, String) IllegalFieldException(int fieldId, int reason, String message)
     */
    @Deprecated
    public IllegalFieldException(int fieldId, String message)
    {
        this(fieldId, REASON_VALUE_INCORRECT, message);
    }

    /**
     * Use the constructor {@link IllegalFieldException#IllegalFieldException(int, int, String) IllegalFieldException(int fieldId, int reason, String fieldValue)}
     * @param fieldId the constant of this class matching with the concerned field.
     * @param reason The constant of this class (prefixed by <code>REASON_</code>)
     * @see IllegalFieldException#IllegalFieldException(int, int, String) IllegalFieldException(int fieldId, int reason, String message)
     */
    @Deprecated
    public IllegalFieldException(int fieldId, int reason)
    {
        this(fieldId, reason, "");
    }

    /**
     * Is equivalent to <code>new IllegalFieldException(fieldId, reason, fieldValue, "");</code>
     * @param fieldId the constant of this class matching with the concerned field.
     * @param reason The constant of this class (prefixed by <code>REASON_</code>)
     * @param fieldValue The illegal value of the concerned field.
     * @see IllegalFieldException#IllegalFieldException(int, int, String, String) IllegalFieldException(int fieldId, int reason, String fieldValue, String message)
     */
    public IllegalFieldException(int fieldId, int reason, String fieldValue)
    {
        this(fieldId, reason, fieldValue, "");
    }

    /**
     * To create an exception to indicate that a field is illegal.
     * @param fieldId The constant of this class matching with the concerned field.
     * @param reason The constant of this class (prefixed by <code>REASON_</code>).
     * @param fieldValue The illegal value of the concerned field.
     * @param message An additional message if the reason is incorrect field (can be an empty string).
     */
    public IllegalFieldException(int fieldId, int reason, String fieldValue, String message)
    {
        super(message != null ? message : "");
        this.reason = reason;
        this.fieldId = fieldId;
        this.fieldValue = fieldValue != null ? fieldValue : "";
    }


    /**
     * To get the id of the illegal field. One of the constants defined in this class
     * @return The field id.
     */
    public int getFieldId()
    {
        return fieldId;
    }

    /**
     * To get the reason why the field is illegal. One of the constants (prefixed by REASON_) defined in this class.
     * @return The constant value associated with the reason.
     */
    public int getReason()
    {
        return reason;
    }

    /**
     * To get the field value which is illegal.
     * @return The field value.
     */
    public String getFieldValue()
    {
        return fieldValue;
    }

}

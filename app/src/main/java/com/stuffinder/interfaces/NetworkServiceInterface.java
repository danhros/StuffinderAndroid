package com.stuffinder.interfaces;

import com.stuffinder.data.Account;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.AccountNotFoundException;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.List;

/**
 * This interface makes the link between the network service (in this case, this is the client web service) and the engine of the application.
 * It defines all the needed methods to use this service.
 * @author Nicolas Thierce
 * 
 * @see NetworkServiceProvider
 */
public interface NetworkServiceInterface
{
	
	/**
	 * Initializes this service to rend it available for network communications.
	 * @throws NetworkServiceException if an error has occurred during the initialization.
	 */
	public void initNetworkService() throws NetworkServiceException;
	
	
	/**
	 * Creates an account with the specified informations. Throws an exception if an error has occurred.
	 * @param newAccount Object which contains all informations about the account to be created.
	 * @param newPassword Password for the account to be created.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PSEUDO Pseudo} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the value is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if the pseudo is already used for another account) </li>
	 * 		<li>{@link IllegalFieldException#FIRSTNAME First name} (reason {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the value is syntactically incorrect) </li>
	 * 		<li>{@link IllegalFieldException#LASTNAME Last name} (reason {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the value is syntactically incorrect) </li>
	 * 		<li>{@link IllegalFieldException#EMAIL_ADDRESS Email address} (reason {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the value is incorrect) </li>
	 * 		<li>{@link IllegalFieldException#PASSWORD Password} (reason {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the value is syntactically incorrect) </li>
	 * </ul>
	 * 
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public void createAccount(Account newAccount, String newPassword) throws IllegalFieldException, NetworkServiceException;

	
	/**
	 * Performs authentication with specified information.
	 * @param pseudo The pseudo
	 * @param password The password
	 * @return The matching account if this operation succeeds.
	 * @throws AccountNotFoundException If the couple (pseudo, password) doesn't exist.
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Account authenticate(String pseudo, String password) throws AccountNotFoundException, NetworkServiceException;
	
	/**
	 * To logout.
	 */
	public void logOut();
	
	/**
	 * To get the current account.
	 * @return The current account.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 */
	public Account getCurrentAccount() throws NotAuthenticatedException;


	
	/**
	 * Modifies the email address of the current account.
	 * @param emailAddress The new email address
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If the specified email address is incorrect (field id {@link IllegalFieldException#EMAIL_ADDRESS EMAIL_ADDRESS} and reason {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT}).
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public void modifyEMailAddress(String emailAddress) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;

	/**
	 * Modifies the password of the current account.
	 * @param newPassword The new password
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If the specified password is syntactically incorrect (field id {@link IllegalFieldException#PASSWORD PASSWORD} and reason {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT}).
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public void modifyPassword(String newPassword) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;
	
	
	/**
	 * To get the tags list of the current account.
	 * @return the tags list if the authentication is already done successfully.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public List<Tag> getTags() throws NotAuthenticatedException, NetworkServiceException;
	
	/**
	 * Adds a new tag to the current account.
	 * @param tag The new tag to be added.
	 * @return the tag added if this operation succeeds.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag uid} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the value is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if the tag uid is already used) </li>
	 * 		<li>{@link IllegalFieldException#TAG_OBJECT_NAME object name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the value is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if the object name is already used for another tag) </li>
	 * 		<li>{@link IllegalFieldException#TAG_OBJECT_IMAGE object image} (reason {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the filename is incorrect, i.e. if the file is not found or is not an image) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Tag addTag(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;
	
	/**
	 * Modifies the object name for a tag.
	 * @param tag The tag to be modified.
	 * @param newObjectName The new object name.
	 * @return The tag modified if this operation succeeds.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag UID} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if the tag is not found (i.e. no tag linked with the current account has this UID)) </li>
	 * 		<li>{@link IllegalFieldException#TAG_OBJECT_NAME object name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the value is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if this object name is already used for another tag) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Tag modifyObjectName(Tag tag, String newObjectName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;
	
	/**
	 * Modifies the object image for a tag.
	 * @param tag The tag to be modified.
	 * @param newImageFileName The filename of the new object image.
	 * @return The tag modified if this operation succeeds.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag uid} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if the tag is not found (i.e. no tag linked with the current account has this UID)) </li>
	 * 		<li>{@link IllegalFieldException#TAG_OBJECT_IMAGE object image} (reason {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if the filename is incorrect, i.e. if the file is not found or is not an image) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Tag modifyObjectImage(Tag tag, String newImageFileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;

	/**
	 * Removes a tag from the current account. This tag is also removed from all profiles which contains it.
	 * @param tag The tag to be removed.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag uid} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if the tag is not found (i.e. no tag linked with the current account has this UID)) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public void removeTag(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;
	
	
	
	

	/**
	 * Creates an empty profile named <code>profileName</code>.
	 * @param profileName The name of the new profile to be created.
	 * @return The profile created.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if you have already a profile with this name) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Profile createProfile(String profileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;

	/**
	 * Creates a new profile named <code>profileName</code> and adds the tags of the list <code>tagList</code> in this profile.
	 * 
	 * <br/><br />Only the UID of the tags are used to perform this operation.
	 * 
	 * <br />If one of these UID is incorrect or doesn't exist (i.e. no tag linked with the current account has this UID), 
	 * an exception of type {@link IllegalFieldException} is thrown to indicate this problem.
	 * 
	 * <br />If a tag is twice or more in the specified tag list, it will be added only once.
	 * 
	 * <br /><br />At the end of call, it returns the new profile.
	 * 
	 * @param profileName The name of the new profile to be created.
	 * @param tagList The tags to add in the new profile.
	 * @return The profile created.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if you have already a profile with this name) </li>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag UID} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no tag with this UID) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Profile createProfile(String profileName, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;
	

	/**
	 * Adds a tag in a profile.
	 * 
	 * <br/><br />Only the UID of the tag is used to perform this operation.
	 * 
	 * <br /> If this UID is incorrect or doesn't exist (i.e. no tag linked with the current account has this UID),
	 * an exception of type {@link IllegalFieldException} is thrown to indicate this problem.
	 * 
	 * <br /><br />If the specified profile already contains the specified tag, this profile is not modified and the return value is <code>null</code>.
	 * Else, this tag is added in this profile and the return value is this profile modified. 
	 * 
	 * @param profile The profile to be modified.
	 * @param tag The tag to be added in the specified profile.
	 * @return The profile modified if this profile did not already contain the specified tag, <code>null</code> otherwise.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag UID} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect, {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no tag with this UID) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Profile addTagToProfile(Profile profile, Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;
	

	/**
	 * Adds the tags of a list in a profile.
	 * 
	 * <br/><br />Only the UID of the tags are used to perform this operation.
	 * 
	 * <br />If one of these UID is incorrect or doesn't exist (i.e. no tag linked with the current account has this UID), 
	 * an exception of type {@link IllegalFieldException} is thrown to indicate this problem.
	 * Else, each tag of this tag list are added in the specified profile if it doesn't already contain this tag.
	 * 
	 * <br /><br />At the end of the call, if the profile is modified, it is returned. Else, <code>null</code> is returned.
	 * 
	 * @param profile The profile to be modified.
	 * @param tagList the tags to be added in the specified profile.
	 * @return The profile modified if it did not already contain one tag of the specified tag list or more, <code>null</code> otherwise.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag UID} of one tag of the list. (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no tag with this UID) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Profile addTagsToProfile(Profile profile, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;

	

	/**
	 * Removes a tag from a profile.
	 * 
	 * <br/><br />Only the UID of the tag is used to perform this operation.
	 * 
	 * <br /> If this UID is incorrect or doesn't exist (i.e. no tag linked with the current account has this UID),
	 * an exception of type {@link IllegalFieldException} is thrown to indicate this problem.
	 * 
	 * <br /><br />If the specified profile doesn't contain the specified tag, this profile is not modified and the return value is <code>null</code>.
	 * Else, this tag is removed from this profile and the return value is this profile modified. 
	 * 
	 * @param profile The profile to be modified.
	 * @param tag The tag to be removed from the specified profile.
	 * @return The profile modified if this profile did contain the specified tag, <code>null</code> otherwise.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag UID} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect, {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if no tag linked with the current account has this UID) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public Profile removeTagFromProfile(Profile profile, Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;


	/**
	 * Removes tags from a profile.
	 * 
	 * <br/><br />Only the UID of the tags are used to perform this operation.
	 * 
	 * <br />If one of these UID is incorrect or doesn't exist (i.e. no tag linked with the current account has this UID), 
	 * an exception of type {@link IllegalFieldException} is thrown to indicate this problem.
	 * Else, each tag of this tag list are removed in the specified profile if it contains this tag.
	 * 
	 * <br /><br />At the end of the call, if the profile is modified, it is returned. Else, <code>null</code> is returned.
	 * 
	 * <br /> <br /> To remove all tags from a profile, call the method {@link #replaceTagListOfProfile(Profile, List)} with an empty list as second parameter.
	 * 
	 * @param profile The profile to be modified.
	 * @param tagList The tags to be removed from the specified profile.
	 * @return The profile modified if this operation succeeds.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
	 * 		<li>{@link IllegalFieldException#TAG_UID UID} of one tag of the list (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if no tag linked with the current account has this UID) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 * 
	 */
	public Profile removeTagsFromProfile(Profile profile, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;

	
	
	/**
	 * Replaces the tag list of a profile by the specified tag list.
	 * 
	 * <br/><br/>Only the UID of the tags are used to perform this operation.
	 * 
	 * <br />If one of these UID is incorrect or doesn't exist (i.e. no tag linked with the current account has this UID), 
	 * an exception of type {@link IllegalFieldException} is thrown to indicate this problem.
	 * 
	 * Else, all tags of the specified profile are removed and all tags of the specified tag list are added in this profile.
	 * 
	 * <br />If a tag is twice or more in the specified tag list, it is added only once in the profile.
	 * 
	 * <br /><br />At the end of the call, this profile modified is returned.
	 * 
	 * @param profile The profile to be modified.
	 * @param tagList The new tag list for the specified profile.
	 * @return The profile modified.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
	 * 		<li>{@link IllegalFieldException#TAG_UID tag UID} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect, {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no tag with this UID, and {@link IllegalFieldException#REASON_VALUE_ALREADY_USED REASON_VALUE_ALREADY_USED} if this tag is twice or more in the specified list) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network service error has occurred.
	 * 
	 * 
	 */
	public Profile replaceTagListOfProfile(Profile profile, List<Tag> tagList) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;

	
	/**
	 * Removes a profile.
	 * @param profile The profile to be removed.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
	 * </ul>
	 * @throws NetworkServiceException If a network error has occurred.
	 */
	public void removeProfile(Profile profile) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException;

	/**
	 * To get a profile by a name.
	 * @param profileName The name of a profile
	 * @return The profile matching with the name <code>profileName</code>.
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws NetworkServiceException If a network service error has occurred.
	 * @throws IllegalFieldException If one field (i.e. one information) is illegal. <br/>
	 * The possible fields with the reason(s) are : <br/>
	 * <ul>
	 * 		<li>{@link IllegalFieldException#PROFILE_NAME Profile name} (reasons {@link IllegalFieldException#REASON_VALUE_INCORRECT REASON_VALUE_INCORRECT} if it is syntactically incorrect and {@link IllegalFieldException#REASON_VALUE_NOT_FOUND REASON_VALUE_NOT_FOUND} if there is no profile which has this name) </li>
	 * </ul>
	 */
	public Profile getProfile(String profileName) throws NotAuthenticatedException, NetworkServiceException, IllegalFieldException;

	/**
	 * To get the profiles linked with the current account.
	 * @return The profiles linked with the current account
	 * @throws NotAuthenticatedException If the authentication is not done.
	 * @throws NetworkServiceException If a network service error has occurred.
	 */
	public List<Profile> getProfiles() throws NotAuthenticatedException, NetworkServiceException;
}
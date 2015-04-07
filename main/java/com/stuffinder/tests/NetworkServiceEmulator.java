package com.stuffinder.tests;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.stuffinder.data.Account;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.FieldVerifier;
import com.stuffinder.exceptions.AccountNotFoundException;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;
import com.stuffinder.interfaces.NetworkServiceInterface;
import static com.stuffinder.exceptions.IllegalFieldException.*;

public class NetworkServiceEmulator implements NetworkServiceInterface
{
	private List<Account> accounts;
	private List<String> passwords;
	
	private Set<Tag> tags;
	
	private Account authenticatedAccount;
	
	private static NetworkServiceEmulator emulator = new NetworkServiceEmulator();
	private int authenticatedAccountIndex;
	
	private NetworkServiceEmulator()
	{
		accounts = new ArrayList<>();
		passwords = new ArrayList<>();
		tags = new HashSet<>();
		
		
		authenticatedAccount = null;
		
		
		Account testAccount = new Account("jdupon", "Jean", "Dupont", "jean.dupont@gmail.com");

		Tag tag1 = new Tag("a1bef83a374", "Home keys");
		Tag tag2 = new Tag("bd3496e342c", "Car key");
		Tag tag3 = new Tag("aefd573fc3e", "Bag");
		Tag tag4 = new Tag("f53ebc87298", "Wallet");
		Tag tag5 = new Tag("e68fa3547cb", "Android tablet");
		Tag tag6 = new Tag("F9:1F:24:D3:1B:D4", "Correct tag");
		Tag tag7 = new Tag("e92fae67acb", "Android smartphone");
		
		tags.add(tag1);
		tags.add(tag2);
		tags.add(tag3);
		tags.add(tag4);
		tags.add(tag5);
		tags.add(tag6);
		tags.add(tag7);

		testAccount.getTags().add(tag1);
		testAccount.getTags().add(tag2);
		testAccount.getTags().add(tag3);
		testAccount.getTags().add(tag4);
		testAccount.getTags().add(tag5);
		testAccount.getTags().add(tag6);
		testAccount.getTags().add(tag7);
		
		
		Profile profile1 = new Profile("profile 1");
		profile1.addTag(tag1);
		profile1.addTag(tag2);
		profile1.addTag(tag3);
		profile1.addTag(tag7);
		

		Profile profile2 = new Profile("profile 2");
		profile2.addTag(tag1);
		profile2.addTag(tag4);
		profile2.addTag(tag5);
		profile2.addTag(tag6);
		profile2.addTag(tag7);

		Profile profile3 = new Profile("profile 2");
		profile3.addTag(tag1);
		profile3.addTag(tag2);
		profile3.addTag(tag3);
		profile3.addTag(tag4);
		profile3.addTag(tag5);
		profile3.addTag(tag6);
		profile3.addTag(tag7);

		testAccount.getProfiles().add(profile1);
		testAccount.getProfiles().add(profile2);


        Account testAccount2 = new Account("jdupouy", "Joe", "Dupouy", "joe.dupouy@gmail.com");

        tag1 = new Tag("identifiant", "Home keys");
        tag2 = new Tag("bd3496e342ce674", "Car key");


        testAccount2.getTags().add(tag1);
        testAccount2.getTags().add(tag2);

        tags.add(tag1);
        tags.add(tag2);

        accounts.add(testAccount);
		passwords.add("123456");

        accounts.add(testAccount2);
        passwords.add("azerty");
	}
	
	public void initNetworkService() throws NetworkServiceException
	{
		
	}

	public void createAccount(Account newAccount, String newPassword) throws IllegalFieldException, NetworkServiceException
	{
		if(newPassword == null)
			throw new IllegalArgumentException("parameter newPassword can't be null.");
		
		if(! FieldVerifier.verifyPassword(newPassword))
			throw new IllegalFieldException(IllegalFieldException.PASSWORD, IllegalFieldException.REASON_VALUE_INCORRECT, newPassword, "Password must contain 6 characters or more.");
		
		if(! FieldVerifier.verifyEMailAddress(newAccount.getEMailAddress()))
			throw new IllegalFieldException(IllegalFieldException.EMAIL_ADDRESS, IllegalFieldException.REASON_VALUE_INCORRECT, newAccount.getEMailAddress());
		
		for(Account tmp : accounts)
			if(tmp.getPseudo().equals(newAccount.getPseudo()))
				throw new IllegalFieldException(IllegalFieldException.PSEUDO, IllegalFieldException.REASON_VALUE_ALREADY_USED, newAccount.getPseudo());
		
		accounts.add(newAccount);
		passwords.add(newPassword);
	}

	public Account authenticate(String pseudo, String password) throws AccountNotFoundException, NetworkServiceException
	{
		Account tmp;
		
		for(int i=0; i < accounts.size(); i++)
		{
			tmp = accounts.get(i);
			if(tmp.getPseudo().equals(pseudo))
			{
				if(passwords.get(i).equals(password))
				{
					authenticatedAccount = tmp;
					authenticatedAccountIndex = i;
					
					return tmp;
				}
				else
					throw new AccountNotFoundException();
			}
		}
		
		throw new AccountNotFoundException();
	}

	public void logOut()
	{
		authenticatedAccount = null;
	}

	public Account getCurrentAccount() throws NotAuthenticatedException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();
		
		return authenticatedAccount;
	}


	public void modifyEMailAddress(String emailAddress) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(! FieldVerifier.verifyEMailAddress(emailAddress))
			throw new IllegalFieldException(IllegalFieldException.EMAIL_ADDRESS, IllegalFieldException.REASON_VALUE_INCORRECT, emailAddress);
		
		authenticatedAccount.setMailAddress(emailAddress);
	}

	public void modifyPassword(String newPassword) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(! FieldVerifier.verifyPassword(newPassword))
			throw new IllegalFieldException(IllegalFieldException.PASSWORD, IllegalFieldException.REASON_VALUE_INCORRECT, newPassword, "Password must contain 6 characters or more.");
		
		passwords.set(authenticatedAccountIndex, newPassword);
	}
	
	

	public List<Tag> getTags() throws NotAuthenticatedException, NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();
		
		return authenticatedAccount.getTags();
	}

	public Tag addTag(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(! FieldVerifier.verifyTagUID(tag.getUid()))
			throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_INCORRECT, tag.getUid());
		
		if(! FieldVerifier.verifyTagName(tag.getObjectName()))
			throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_NAME, IllegalFieldException.REASON_VALUE_INCORRECT, tag.getObjectName());
		
		if(tag.getObjectImageName() != null && tag.getObjectImageName().length() > 0 && ! FieldVerifier.verifyImageFileName(tag.getObjectImageName()))
			throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_IMAGE, IllegalFieldException.REASON_VALUE_INCORRECT, tag.getObjectImageName());
		
		for(Tag tmp : authenticatedAccount.getTags())
			if(tmp.getObjectName().equals(tag.getObjectName()))
				throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_NAME, IllegalFieldException.REASON_VALUE_ALREADY_USED, tag.getObjectName());

		if(! tags.add(tag)) // because the method add() returns true if this operation is successful, false if there is already a tag with the specifed tag UID. 
			throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_ALREADY_USED, tag.getUid());
		
		authenticatedAccount.getTags().add(tag);
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
    public String downloadObjectImage(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(authenticatedAccount == null)
            throw new NotAuthenticatedException();

        simulateRealBehavior();

        if(! FieldVerifier.verifyTagUID(tag.getUid()))
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());

        int index = authenticatedAccount.getTags().indexOf(tag);

        if(index < 0)
            throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());

        String filename = authenticatedAccount.getTags().get(index).getObjectImageName();

        if(filename == null || filename.length() == 0)
            throw new IllegalFieldException(TAG_OBJECT_IMAGE, REASON_VALUE_NOT_FOUND, "");

        return filename;
    }

    public Tag modifyObjectName(Tag tag, String newObjectName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(! FieldVerifier.verifyTagUID(tag.getUid()))
			throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_INCORRECT, tag.getUid());
		
		if(! FieldVerifier.verifyTagName(newObjectName))
			throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_NAME, IllegalFieldException.REASON_VALUE_INCORRECT, newObjectName);

		for(Tag tmp : authenticatedAccount.getTags())
			if(tmp.getObjectName().equals(newObjectName) && ! tmp.getUid().equals(tag.getUid()))
				throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_NAME, IllegalFieldException.REASON_VALUE_ALREADY_USED, newObjectName, "this name is already used for another tag.");
		
		
		int index = authenticatedAccount.getTags().indexOf(tag);
		
		if(index < 0)
			throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_NOT_FOUND, tag.getUid());
		else
		{
			authenticatedAccount.getTags().get(index).setObjectName(newObjectName);
			return authenticatedAccount.getTags().get(index);
		}
	}

	public Tag modifyObjectImage(Tag tag, String newImageFileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(! FieldVerifier.verifyTagUID(tag.getUid()))
			throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_INCORRECT, tag.getUid());

		if(newImageFileName != null && newImageFileName.length() > 0 && ! FieldVerifier.verifyImageFileName(newImageFileName))
			throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_IMAGE, IllegalFieldException.REASON_VALUE_INCORRECT, newImageFileName);
		
		
		int index = authenticatedAccount.getTags().indexOf(tag);
		
		if(index < 0)
			throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_NOT_FOUND, tag.getUid());
		else
		{
			authenticatedAccount.getTags().get(index).setObjectImageName(newImageFileName == null ? "" : newImageFileName);
			return authenticatedAccount.getTags().get(index);
		}
	}
	
	public void removeTag(Tag tag) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(! FieldVerifier.verifyTagUID(tag.getUid()))
			throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_INCORRECT, tag.getUid());
		

		int index = authenticatedAccount.getTags().indexOf(tag);
		
		if(index < 0)
			throw new IllegalFieldException(IllegalFieldException.TAG_UID, IllegalFieldException.REASON_VALUE_NOT_FOUND, tag.getUid());
		else
		{
			Tag tmp = authenticatedAccount.getTags().remove(index);
			
			for(Profile profile : authenticatedAccount.getProfiles())
				profile.getTags().remove(tmp);
			
			tags.remove(tmp);
		}
	}
	
	
	

	public Profile createProfile(String profileName)
			throws NotAuthenticatedException, IllegalFieldException,
			NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profileName))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profileName);
		
		Profile profile = new Profile(profileName);
		
		if(authenticatedAccount.getProfiles().contains(profile))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_ALREADY_USED, profileName);
		
		authenticatedAccount.getProfiles().add(profile);
		
		return profile;
	}

	public Profile createProfile(String profileName, List<Tag> tagList)
			throws NotAuthenticatedException, IllegalFieldException,
			NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profileName))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profileName);
		
		Profile profile = new Profile(profileName);
		
		if(authenticatedAccount.getProfiles().contains(profile))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_ALREADY_USED, profileName);
		
		
		for(Tag tag : tagList)
		{
			if(! FieldVerifier.verifyTagUID(tag.getUid())) // If this tag UID isn't incorrect. 
				throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
			
			int tagIndex = authenticatedAccount.getTags().indexOf(tag);
			
			if(tagIndex < 0) // if no tag of the current account has this UID.
				throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
			else if(! profile.getTags().contains(tag)) // to have this tag at most once.
				profile.getTags().add(authenticatedAccount.getTags().get(tagIndex));
		}
			
		
		authenticatedAccount.getProfiles().add(profile);
		
		return profile;
	}

	public Profile addTagToProfile(Profile profile, Tag tag)
			throws NotAuthenticatedException, IllegalFieldException,
			NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profile.getName()))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());
		
		int index = authenticatedAccount.getProfiles().indexOf(profile);
		
		if(index < 0)
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());
		
		Profile profile1 = authenticatedAccount.getProfiles().get(index);
		

		if(! FieldVerifier.verifyTagUID(tag.getUid())) // if this tag UID isn't incorrect.
			throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
		
		int tagIndex = authenticatedAccount.getTags().indexOf(tag);
		
		if(tagIndex < 0) // if no tag of the current account has this UID.
			throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
		else if(! profile1.getTags().contains(tag)) // to have this tag at most once.
		{
			profile1.getTags().add(authenticatedAccount.getTags().get(tagIndex));
			return profile1;
		}
		else
			return null; // to indicate this profile is not modified.
	}

	public Profile addTagsToProfile(Profile profile, List<Tag> tagList)
			throws NotAuthenticatedException, IllegalFieldException,
			NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profile.getName()))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());
		
		int index = authenticatedAccount.getProfiles().indexOf(profile);
		
		if(index < 0)
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());
		
		Profile profile1 = authenticatedAccount.getProfiles().get(index);
		
		
		Set<Tag> tmp = new HashSet<Tag>();
		
		for(Tag tag : tagList)
		{
			if(! FieldVerifier.verifyTagUID(tag.getUid())) // If this tag UID isn't incorrect. 
				throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
			
			int tagIndex = authenticatedAccount.getTags().indexOf(tag);
			
			if(tagIndex < 0) // if no tag of the current account has this UID.
				throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
			else if(! profile1.getTags().contains(authenticatedAccount.getTags().get(tagIndex)))
				tmp.add(authenticatedAccount.getTags().get(tagIndex));
		}
		
		if(tmp.size() > 0)
		{
			profile1.getTags().addAll(tmp);
			return profile1;
		}
		else
			return null; // because this profile is not modified.
	}

	public Profile removeTagFromProfile(Profile profile, Tag tag)
			throws NotAuthenticatedException, IllegalFieldException,
			NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profile.getName()))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());
		
		int index = authenticatedAccount.getProfiles().indexOf(profile);
		
		if(index < 0)
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());
		
		Profile profile1 = authenticatedAccount.getProfiles().get(index);
		

		if(! FieldVerifier.verifyTagUID(tag.getUid())) // if this tag UID isn't incorrect.
			throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
		
		if(! authenticatedAccount.getTags().contains(tag)) // if no tag of the current account has this UID.
			throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
		
		int tagIndex = profile1.getTags().indexOf(tag);
		
		if(tagIndex >= 0)
		{
			profile1.getTags().remove(tagIndex);
			return profile1;
		}
		else
			return null; // to indicate this profile is not modified.

	}

	public Profile removeTagsFromProfile(Profile profile, List<Tag> tagList)
			throws NotAuthenticatedException, IllegalFieldException,
			NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profile.getName()))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());
		
		int index = authenticatedAccount.getProfiles().indexOf(profile);
		
		if(index < 0)
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());
		
		Profile profile1 = authenticatedAccount.getProfiles().get(index);
		
		
		Set<Tag> tmp = new HashSet<Tag>();
		
		for(Tag tag : tagList)
		{
			if(! FieldVerifier.verifyTagUID(tag.getUid())) // If this tag UID isn't incorrect. 
				throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
			
			int tagIndex = authenticatedAccount.getTags().indexOf(tag);
			
			if(tagIndex < 0) // if no tag of the current account has this UID.
				throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
			else if(profile1.getTags().contains(authenticatedAccount.getTags().get(tagIndex)))
				tmp.add(authenticatedAccount.getTags().get(tagIndex));
		}
		
		if(tmp.size() > 0)
		{
			profile1.getTags().removeAll(tmp);
			return profile1;
		}
		else
			return null; // because this profile is not modified.
	}

	public Profile replaceTagListOfProfile(Profile profile, List<Tag> tagList)
			throws NotAuthenticatedException, IllegalFieldException,
			NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profile.getName()))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());
		
		int index = authenticatedAccount.getProfiles().indexOf(profile);
		
		if(index < 0)
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());
		
		Profile profile1 = authenticatedAccount.getProfiles().get(index);
		
		
		Set<Tag> tmp = new HashSet<Tag>();
		
		for(Tag tag : tagList)
		{
			if(! FieldVerifier.verifyTagUID(tag.getUid())) // If this tag UID isn't incorrect. 
				throw new IllegalFieldException(TAG_UID, REASON_VALUE_INCORRECT, tag.getUid());
			
			int tagIndex = authenticatedAccount.getTags().indexOf(tag);
			
			if(tagIndex < 0) // if no tag of the current account has this UID.
				throw new IllegalFieldException(TAG_UID, REASON_VALUE_NOT_FOUND, tag.getUid());
			else
				tmp.add(authenticatedAccount.getTags().get(tagIndex));
		}
		
		
		profile1.getTags().clear();
		profile1.getTags().addAll(tmp);
		
		return profile1;
	}

	public void removeProfile(Profile profile)
			throws NotAuthenticatedException, IllegalFieldException,
			NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profile.getName()))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profile.getName());
		
		int index = authenticatedAccount.getProfiles().indexOf(profile);

		if(index < 0)
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());
		else
			authenticatedAccount.getProfiles().remove(index);
	}

	public Profile getProfile(String profileName)
			throws NotAuthenticatedException, NetworkServiceException,
			IllegalFieldException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

        simulateRealBehavior();

		if(!FieldVerifier.verifyName(profileName))
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, profileName);
		
		int index = authenticatedAccount.getProfiles().indexOf(new Profile(profileName));

		if(index < 0)
			throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profileName);
		else
			return authenticatedAccount.getProfiles().get(index);
	}


	public List<Profile> getProfiles() throws NotAuthenticatedException, NetworkServiceException
	{
		if(authenticatedAccount == null)
			throw new NotAuthenticatedException();

		return authenticatedAccount.getProfiles();
	}

    /**
     * @return the last update time about account personnal information .
     * @throws com.stuffinder.exceptions.NetworkServiceException
     */
    @Override
    public int getLastPersonnalInformationUpdateTime() throws NetworkServiceException, NotAuthenticatedException {
        return 0;
    }

    /**
     * @return the last update time about tags.
     * @throws com.stuffinder.exceptions.NetworkServiceException
     */
    @Override
    public int getLastTagsUpdateTime() throws NetworkServiceException, NotAuthenticatedException
    {
        if(authenticatedAccount == null)
            throw new NotAuthenticatedException();

        return 0;
    }

    /**
     * @return The last update time about profiles.
     * @throws com.stuffinder.exceptions.NetworkServiceException
     */
    @Override
    public int getLastProfilesUpdateTime() throws NetworkServiceException, NotAuthenticatedException
    {
        if(authenticatedAccount == null)
            throw new NotAuthenticatedException();

        return 0;
    }

    @Override
    public Profile modifyProfileName(Profile profile, String newProfileName) throws NotAuthenticatedException, IllegalFieldException, NetworkServiceException
    {
        if(authenticatedAccount == null)
            throw new NotAuthenticatedException();

        simulateRealBehavior();

        if(!FieldVerifier.verifyName(newProfileName))
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_INCORRECT, newProfileName);

        int index = authenticatedAccount.getProfiles().indexOf(new Profile(profile.getName()));

        if(index < 0)
            throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_NOT_FOUND, profile.getName());

        Profile tmp = authenticatedAccount.getProfiles().get(index);

        for(Profile otherProfile : authenticatedAccount.getProfiles())
            if(otherProfile != tmp && otherProfile.getName().equals(newProfileName))
                throw new IllegalFieldException(PROFILE_NAME, REASON_VALUE_ALREADY_USED, newProfileName);

        tmp.setName(newProfileName);

        return tmp;
    }

    private Random random = new Random();

    private void simulateRealBehavior() throws NetworkServiceException, NotAuthenticatedException {
        try {
            Thread.sleep((long) 100 * (random.nextInt(20) + 4), 0);
        } catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Sleep operation interrupted.");
        }

        if(random.nextInt(15) == 0)
        {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Network service emulator will simulate a network problem.");
            throw new NetworkServiceException("A network error has occured.");
        }

        if(random.nextInt(2) == 0)
        {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Network service emulator will simulate an authentication problem, i.e. the password is wrong.");
            throw new NotAuthenticatedException();
        }
    }

    public static NetworkServiceEmulator getInstance()
	{
		return emulator;
	}

}

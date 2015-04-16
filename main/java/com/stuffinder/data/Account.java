package com.stuffinder.data;

import java.util.ArrayList;
import java.util.List;


public class Account
{
	private String pseudo;
	private String firstName;
	private String lastName;
	private String emailAddress;
	
	private List<Tag> tags;
	private List<Profile> profils;
	
	public Account(String pseudo, String firstName, String lastName, String emailAddress)
	{
		if(pseudo == null)
			throw new NullPointerException();
		
		this.pseudo = pseudo;
		
		setFirstName(firstName);
		setLastName(lastName);
		setMailAddress(emailAddress);
		
		tags = new ArrayList<>();
		profils = new ArrayList<>();
	}

	public List<Tag> getTags()
	{
		return tags;
	}

	public List<Profile> getProfiles()
	{
		return profils;
	}

	public String getPseudo()
	{
		return pseudo;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		if(firstName == null)
			throw new NullPointerException();
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		if(lastName == null)
			throw new NullPointerException();
		this.lastName = lastName;
	}

	public String getEMailAddress()
	{
		return emailAddress;
	}

	public void setMailAddress(String emailAddress)
	{
		if(emailAddress == null)
			throw new NullPointerException();
		
		this.emailAddress = emailAddress;
	}

	public boolean equals(Object obj)
	{
		return (!(obj instanceof Account)) ? false : 
					pseudo.equals(((Account) obj).getPseudo());
	}

	public int hashCode()
	{
		return pseudo.hashCode();
	}
	
}

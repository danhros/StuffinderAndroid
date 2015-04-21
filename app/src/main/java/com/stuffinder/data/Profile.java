package com.stuffinder.data;

import java.util.ArrayList;
import java.util.List;


public class Profile
{
	private List<Tag> tags;
	private String name;
	
	public Profile(String name)
	{
		this.name = name;
		tags = new ArrayList<>();
	}
	
	public String getName()
	{
		return name;
	}



	public List<Tag> getTags()
	{
		return tags;
	}
	
	public boolean addTag(Tag tag)
	{
		return tags.add(tag);
	}
	
	public boolean removeTag(Tag tag)
	{
		return tags.remove(tag);
	}
	
	public void removeAllTags()
	{
		tags.clear();
	}


	public boolean equals(Object obj)
	{
		return (!(obj instanceof Profile)) ? false : 
					name.equals(((Profile) obj).getName());
	}

	public int hashCode()
	{
		return name.hashCode();
	}
	
	
}

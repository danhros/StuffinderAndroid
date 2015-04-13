package com.stuffinder.data;

import java.util.ArrayList;
import java.util.List;

<<<<<<< HEAD
=======

>>>>>>> remotes/master/MutualCodeBranch
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

<<<<<<< HEAD
=======
    public void setName(String name)
    {
        this.name = name;
    }
>>>>>>> remotes/master/MutualCodeBranch


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

<<<<<<< HEAD
    public void setName(String name) {
        this.name = name;
    }
=======
	public int hashCode()
	{
		return name.hashCode();
	}
	
	
>>>>>>> remotes/master/MutualCodeBranch
}

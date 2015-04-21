package com.stuffinder.data;

import java.util.ArrayList;
import java.util.List;

public class Tag
{
	private String uid;
	
	private String objectName;

	private String objectImageName;
	
	private List<Position> lastPositions;
	

	public Tag(String uid, String objectName)
	{
		this(uid, objectName, null);
	}
	
	public Tag(String uid, String objectName, String objectImageName)
	{
		if(uid == null || uid.length() == 0)
			throw new NullPointerException();
		
		this.uid = uid;
		
		setObjectName(objectName);
		setObjectImageName(objectImageName);
		
		lastPositions = new ArrayList<>();
	}
	
	
	public String getUid()
	{
		return uid;
	}
	
	
	public String getObjectName()
	{
		return objectName;
	}
	
	public void setObjectName(String objectName)
	{
		if(objectName == null)
			throw new NullPointerException();
		
		this.objectName = objectName;
	}
	
	
	public String getObjectImageName()
	{
		return objectImageName;
	}

	public void setObjectImageName(String objectImageName)
	{
		this.objectImageName = objectImageName;
	}

	public void addLastPosition(Position p)
	{
		lastPositions.add(p);
	}
	
	public List<Position> getLastPositions()
	{
		return lastPositions;
	}


	public int hashCode()
	{
		return uid.hashCode();
	}

	public boolean equals(Object obj)
	{
		return (!(obj instanceof Tag)) ? false : 
					uid.equals(((Tag) obj).getUid());
	}

    public String toString()
    {
        return objectName;
    }
}

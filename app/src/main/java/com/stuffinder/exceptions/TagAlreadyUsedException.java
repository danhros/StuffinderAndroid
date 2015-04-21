package com.stuffinder.exceptions;

public class TagAlreadyUsedException extends Exception
{
	private static final long serialVersionUID = 5029770280517694800L;

	public TagAlreadyUsedException(String tagId)
	{
		super(tagId);
	}
	
	public String getTagIdNotFound()
	{
		return super.getMessage();
	}

}

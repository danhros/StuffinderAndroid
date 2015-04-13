package com.stuffinder.exceptions;

public class NetworkServiceException extends Exception
{
	private static final long serialVersionUID = 3531267863055303515L;

	public NetworkServiceException(String message)
	{
		super(message);
	}
}

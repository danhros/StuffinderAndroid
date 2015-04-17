package com.stuffinder.exceptions;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkServiceException extends Exception
{
	private static final long serialVersionUID = 3531267863055303515L;

	public NetworkServiceException(String message)
	{
		super(message);
        Logger.getLogger(getClass().getName()).log(Level.WARNING, message);
	}
}

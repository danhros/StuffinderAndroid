package com.stuffinder.data;

public class Position
{
	private long latitude;
	private long longitude;
	
	public Position(long latitude, long longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
	}

	long getLatitude()
	{
		return latitude;
	}

	long getLongitude()
	{
		return longitude;
	}
}

package com.stuffinder.engine;

import com.stuffinder.interfaces.NetworkServiceInterface;

/**
 * This class has the reference to the current network service. <br/>
 * It must be initialized before to use getNetworkService() by using the method setNetworkService(). <br/>
 * For example, to initialize with the emulator : <code>setNetworkService(NetworkServiceEmulator.getInstance());</code>
 *
 * @author Nicolas Thierce
 *@see NetworkServiceInterface
 */
public class NetworkServiceProvider
{

    private static NetworkServiceInterface networkService;

    /**
     * To set the network service to be used. Call this method one time to initialize.
     * @param networkService the network service to set as current.
     */
    public static void setNetworkService(NetworkServiceInterface networkService)
    {
        if(networkService == null)
            throw new NullPointerException();
        else
            NetworkServiceProvider.networkService = networkService;
    }


    /**
     * To get the current network service.
     * @return The current network service, or null if there is no one.
     */
    public static NetworkServiceInterface getNetworkService()
    {
        return networkService;
    }

}

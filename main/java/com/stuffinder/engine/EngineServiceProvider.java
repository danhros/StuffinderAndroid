package com.stuffinder.engine;

/**
 * Created by propriétaire on 15/03/2015.
 */
public class EngineServiceProvider {
    private static EngineService currentEngineService;

    public static void setEngineService(EngineService engineService)
    {
        currentEngineService = engineService;
    }

    public static EngineService getEngineService()
    {
        return currentEngineService;
    }
}

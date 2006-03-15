package org.chungles.frameworks.stateless;

public class StatelessNativeLibraryLoader
{
    // Loads a native library in to the framework        
    private static StatelessNativeLibraryLoader instance;
    
    public static StatelessNativeLibraryLoader getInstance()
    {
        if (instance==null)
            instance=new StatelessNativeLibraryLoader();
        return instance;
    }        
    
    private StatelessNativeLibraryLoader()
    {
        System.loadLibrary("statelessjniloader");        
    }
    
    public native StatelessApplication addApplication(String library);    
}

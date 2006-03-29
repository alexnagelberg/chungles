package org.chungles.frameworks.stateless;

public class StatelessNativeApplication implements StatelessApplication
{
    private static final long serialVersionUID = 3986141267190119198L;
 
    // handle of library (this is so ugly, it hurts)
    private int dlhandle;
    
    native String ngetAppID();
    native String ngetSystemCommand();
    native NativeState nfreeze();
    native void nthaw(NativeState state);
    native boolean nhasEnoughMemory(long size);
    
    public StatelessNativeApplication(int dlhandle)
    {
        this.dlhandle=dlhandle;
    }
    
    private StatelessNativeApplication()
    {        
    }
    
    public String getAppID()
    {
        return ngetAppID();
    }
    
    public String getSystemCommand()
    {
        return ngetSystemCommand();
    }
    
    public State freeze()
    {
        return nfreeze();
    }
    
    public void thaw(State thaw)
    {
        nthaw((NativeState)thaw);
    }
    
    public boolean hasEnoughMemory(long size)
    {
        return nhasEnoughMemory(size);
    }
}

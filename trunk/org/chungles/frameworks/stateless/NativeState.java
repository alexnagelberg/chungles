package org.chungles.frameworks.stateless;

public class NativeState implements State
{
    private static final long serialVersionUID = 6527624469075890265L;
    private long memory;
    private byte[] buffer;
    
    private NativeState()
    {        
    }
    
    public NativeState(long memory, byte[] buffer)
    {
        this.memory=memory;
        this.buffer=buffer;        
    }
    
    public long requiredMemory()
    {
        return memory;
    }
    
    public byte[] getBuffer()
    {
        return buffer;
    }
}

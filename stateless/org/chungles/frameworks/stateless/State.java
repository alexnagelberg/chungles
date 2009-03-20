package org.chungles.frameworks.stateless;

import java.io.Serializable;

public interface State extends Serializable
{	
    // returns amount of memory state needs
    public long requiredMemory();	
}

package org.chungles.frameworks.stateless;
import java.io.*;
import java.util.Enumeration;

public class Test
{
    public static void main(String [] args)
    {       
        StatelessNativeLibraryLoader jni=StatelessNativeLibraryLoader.getInstance();
        StatelessApplication app=jni.addApplication("/home/alex/libtest.so");
        System.out.println(app.getAppID());
    }
}

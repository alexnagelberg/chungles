package org.chungles.core;

public class Util
{
	public static long byteToLong(byte[] in)
    {
        long out=0;
        for (int i=7; i>=0; i--)
        {
                long temp=in[7-i];
                if (temp<0)
                        temp=256+temp;
                out+=temp<<(i*8);
        }
        return out;
    }
    
    public static int byteToInt(byte[] in)
    {
        int out=0;
        for (int i=3; i>=0; i--)
        {
                int temp=in[3-i];
                if (temp<0)
                        temp=256+temp;
                out+=temp<<(i*8);
        }
        return out;
    }
    
	public static byte[] longToBytes(long in)
	{
		byte[] out=new byte[8];
        for (int i=0; i<8; i++)
                out[i]=(byte)((in<<(i*8))>>56);
        return out;
	}
	
	public static byte[] intToBytes(int in)
	{
		byte[] out=new byte[4];
        for (int i=0; i<4; i++)
                out[i]=(byte)((in<<(i*8))>>24);
        return out;
	}
}

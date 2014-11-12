package z.mdb.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.UUID;

public class Utilities 
{
    public static final String[] NAMES={"Mary","Vivian","Alice","Bob","Adjani","Wil","Jack","Delphy","Zhou","Ann"};

    public static String get32CharUUID()
    {
	UUID uuid=UUID.randomUUID();
	String uuidstr=uuid.toString();
	return uuidstr.substring(0, 8)+uuidstr.substring(9, 13)+uuidstr.substring(14,18)+uuidstr.substring(19,23)+uuidstr.substring(24);
	
    } 
    public static String getInvalid32CharUuid()
    {
	return "gggggggggggggggggggggggggggggggg";
    }

    public static  String encode2Ascii(String str)
    {
	if(str==null)return null;
	try
	{
	    return new String(str.getBytes("US-ASCII"),"US-ASCII");
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }
    
    public static int  month2Number(String month)
    {
    	if(month.equals("Jan"))
    	{
    		return 1;
    	}
    	else if(month.equals("Feb"))
    	{
    		return 2;
    	}
    	else if(month.equals("Mar"))
    	{
    		return 3;
    	}
    	else if(month.equals("Apr"))
    	{
    		return 4;
    	}
    	else if(month.equals("May"))
    	{
    		return 5;
    	}
    	else if(month.equals("Jun"))
    	{
    		return 6;
    	}
    	else if(month.equals("Jul"))
    	{
    		return 7;
    	}
    	else if(month.equals("Aug"))
    	{
    		return 8;
    	}
    	else if(month.equals("Sep"))
    	{
    		return 9;
    	}
    	else if(month.equals("Oct"))
    	{
    		return 10;
    	}
    	else if(month.equals("Nov"))
    	{
    		return 11;
    	}
    	else if(month.equals("Dec"))
    	{
    		return 12;
    	}
    	else
    	{
    		return -1;
    	}
    	
    }
    public static void printDebug(String info)
    {
	StringBuffer sb = new StringBuffer();
	StackTraceElement[] stacks = new Throwable().getStackTrace();
	int stacksLen = stacks.length;
	sb.append(stacks[1].getClassName()).append("#"+stacks[1].getLineNumber()).append("."+stacks[1].getMethodName()+"():");
	System.out.println("---"+sb.toString()+info);
    }
    public static void fileLog(String path,String log)
    {
	File file = new File(path);
	try
	{
	    FileWriter w=new FileWriter(file,true);
	    w.write(log+"\r\n");
	    w.flush();
	    w.close();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	}
    }
    public static String randomContent(int maxLen)
    {
	int len=(int)(Math.random()*maxLen);
	String con="";
	char ch;
	for(int i=0;i<len;i++)
	{
	    ch=(char)(32+(126-32)*Math.random());
	    if(ch=='\\')
	    {
		ch='s';
	    }
	    else if(ch=='\'' || ch=='"')
	    {
		ch='q';
	    }
	    

	    con=con+ch;
	}
	return con;
    }
    public static String randomAlpNos(int maxLen)
    {
	int len=(int)(Math.random()*maxLen);
	String con="";
	char ch;
	for(int i=0;i<len;)
	{
	    ch=(char)(127*Math.random());
	   if(ch<48 || (ch>57 && ch<65) || (ch>91 && ch<96) || ch>122)
	   {
	       
	   }
	   else
	   {
	       con=con+ch;
	       i++;
	   }
	}
	return con;
	
    }
    public static String randomNos(int maxLen)
    {
	int len=(int)(Math.random()*maxLen);
	String con="";
	for(int i=0;i<len;i++)
	{
	    con=con+((int)(9*Math.random()));
	}
	Utilities.printDebug("rano:"+con);
	return con;
    }
    public static String getThisMethodName()
    {
	StackTraceElement[] stacks = new Throwable().getStackTrace();
	return stacks[1].getMethodName();
    }
    
    public static String[] getNames(int quan,int width)
    {
	String[] names=new String[quan];
	String name;
	for(int i=0;i<names.length;i++)
	{
	    name="";
	    for(int j=0;j<width;j++)
	    {
		char ch=(char)(Math.random()*(90-65)+65);
		name=name+ch;
	    }
	    for(int k=0;k<i;)
	    {
		if(name.equals(names[k]))
		{
		    name="";
		    for(int j=0;j<width;j++)
		    {
			char ch=(char)(Math.random()*(90-65)+65);
			name=name+ch;
		    }
		    k=0;
		}
		else
		{
		    k++;
		}
	    }
	    names[i]=name;
	}
	return names;
    }

    
}

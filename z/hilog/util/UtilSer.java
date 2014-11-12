package z.hilog.util;

public class UtilSer 
{
	public static String getTraceInfo(int index)
	{ 
        StringBuffer sb = new StringBuffer();   
          
        StackTraceElement[] stacks = new Throwable().getStackTrace(); 
        int stacksLen = stacks.length; 
        sb.append("---ln"+stacks[index].getLineNumber()+":").append(stacks[index].getClassName()).append("."+stacks[index].getMethodName()+"()"); 
        return sb.toString(); 
    }
	public static void printDebug(String info)
	{
		System.out.println(getTraceInfo(2)+":"+info);
	}

}

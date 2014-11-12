package z.mdb.excp;

public class InvalidParameterException extends Exception 
{
    private String clazz;
    private String method;
    public InvalidParameterException(String clazz, String method)
    {
	super();
	this.clazz = clazz;
	this.method = method;
    }
    public String toString()
    {
	return "Exception:"+clazz+"."+method+"()"+" recived invalid parameter";
    }
}

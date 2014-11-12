package z.mdb.excp;

public class IMMReturnedException extends Exception
{
    private String method;
    private String excpValue;
    public IMMReturnedException(String method, String excpValue)
    {
	super();
	this.method = method;
	this.excpValue = excpValue;
    }
    public String toString()
    {
	return "imm returned an exception value:method="+method+"(), excp value="+excpValue;
    }
    
}

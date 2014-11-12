package z.mdb.excp;

public class IMAPCmdNotOkException extends Exception
{
    private String result;

    public IMAPCmdNotOkException(String result)
    {
	super();
	this.result = result;
    }
    
    public String toString()
    {
	return "imap cmd is not ok: result="+result;
    }
}

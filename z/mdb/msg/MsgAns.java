package z.mdb.msg;


public class MsgAns
{
    private String iden;

    private MsgAns(String iden)
    {
	super();
	this.iden = iden;
    }
    public static MsgAns parse(String ans)
    {
	if(ans.equals("ANSED"))
	{
	    return MsgAns.ANSED;
	}
	else if(ans.equals("UNANSED"))
	{
	    return MsgAns.UNANSED;
	}
	else if(ans.equals("IGNORE"))
	{
	    return MsgAns.IGNORE;
	}
	else
	{
	    return null;
	}
    }
    public boolean equals(Object o)
    {
	if (!(o instanceof MsgAns)) return false;
	final MsgAns other=(MsgAns)o;
	if (this.iden.equals(other.iden))
	{
	    return true;
	}
	else
	{
	    return false;
	}
    }
    public String toString()
    {
	return iden;
    }
    public int toInt()
    {
	if(this.equals(MsgAns.ANSED))
	{
	    return 1;
	}
	else if(this.equals(MsgAns.UNANSED))
	{
	    return 0;
	}
	else
	{
	    return -1;
	}
	    
    }
    public final static MsgAns  ANSED=new MsgAns(" ANSWERED ");
    public final static MsgAns  UNANSED=new MsgAns(" UNANSWERED ");
    public final static MsgAns  IGNORE=new MsgAns(" ");
}

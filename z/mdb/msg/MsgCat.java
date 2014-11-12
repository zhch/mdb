package z.mdb.msg;

public class MsgCat
{
    private String desc;

    private MsgCat(String desc)
    {
	super();
	this.desc = desc;
    }

    public String toString()
    {
	return this.desc;
    }
    public static MsgCat parse(String desc)
    {
	if(desc.equals("nor"))
	{
	    return NORMAL_MSG;
	}
	if(desc.equals("del"))
	{
	    return DEL_MSG;
	}
	if(desc.equals("S"))
	{
	    return S_MSG;
	}
	if(desc.equals("X"))
	{
	    return X_MSG;
	}
	if(desc.equals("IS"))
	{
	    return IS_MSG;
	}		    
	if(desc.equals("IX"))
	{
	    return IX_MSG;
	}		    
	if(desc.equals("time"))
	{
	    return TIME_MSG;
	}
	if(desc.equals("dis"))
	{
	    return DIS_MSG;
	}
	else
	{
	    return null;
	}
    }
    public boolean equals(Object o)
    {
	if (!(o instanceof MsgCat)) return false;
	final MsgCat other=(MsgCat)o;
	if (this.desc.equals(other.desc))
	{
	    return true;
	}
	else
	{
	    return false;
	}
    }


    public final static MsgCat  NORMAL_MSG=new MsgCat("nor");
    public final static MsgCat  DEL_MSG=new MsgCat("del");
    public final static MsgCat  IS_MSG=new MsgCat("IS");
    public final static MsgCat  IX_MSG=new MsgCat("IX");
    public final static MsgCat  S_MSG=new MsgCat("S");
    public final static MsgCat  X_MSG=new MsgCat("X");
    public final static MsgCat  TIME_MSG=new MsgCat("time");
    public final static MsgCat  DIS_MSG=new MsgCat("dis");
}

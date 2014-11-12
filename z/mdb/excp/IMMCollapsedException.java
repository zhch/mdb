package z.mdb.excp;

public class IMMCollapsedException extends Exception
{
    private String desc;

    public IMMCollapsedException(String desc)
    {
	super();
	this.desc = desc;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }
    public String toString()
    {
	return "IMM structure collapsed:"+desc;
    }
}

package z.mdb.excp;

public class TxnValidateFailedExcp extends Exception
{
    private String desc;

    public TxnValidateFailedExcp(String desc)
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
	return "txn failed during validate:"+desc;
    }
}

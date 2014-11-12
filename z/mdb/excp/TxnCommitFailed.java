package z.mdb.excp;

public class TxnCommitFailed extends Exception
{
    private String desc;

    public TxnCommitFailed(String desc)
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
	return "txn commit failed:"+desc;
    }
}

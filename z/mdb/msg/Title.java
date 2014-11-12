package z.mdb.msg;

import java.util.StringTokenizer;


public class Title
{
    private MsgCat cat;
    private String name;
    private String txnId;
    private long version;
    
    
    
    public MsgCat getCat()
    {
        return cat;
    }

    public void setCat(MsgCat cat)
    {
        this.cat = cat;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTxnId()
    {
        return txnId;
    }

    public void setTxnId(String txnId)
    {
        this.txnId = txnId;
    }

    public long getVersion()
    {
        return version;
    }

    public void setVersion(long version)
    {
        this.version = version;
    }

    public Title(MsgCat cat, String name, String txnId, long version)
    {
	super();
	this.cat = cat;
	this.name = name;
	this.txnId = txnId;
	this.version = version;
    }
    
    public String weaveSubject()
    {
	return this.degen2SubPattern().weaveSubjectPattern();
    }
    
    public SubjectPattern degen2SubPattern()
    {
	return SubjectPattern.genPattern(cat, name, txnId, version);
    }
    
    /**
     * 从subject文本中构建title对象
     * @param subject
     * @return
     */
    public static Title parse(String subject)
    {
	int deli1;
	int deli2;
	StringTokenizer st;
	String name;
	MsgCat cat;
	String txn;
	long ver;

	/*
	 * 先找出name
	 */
	deli1=subject.indexOf("(");
	deli2=subject.indexOf(")");
	if(deli1<1 || deli1+1>=deli2)
	{
	    return null;
	}
	name=subject.substring(deli1+1,deli2);
	
	/*
	 * name位置找出并parse了之后，首先把name之前剩下的cat parse掉
	 */
	if((cat=MsgCat.parse(subject.substring(0,deli1)))==null)
	{
	    return null;
	}
	
	/*
	 * 用StringTokenizer parse剩下的txn 和version
	 */
	st=new StringTokenizer(subject.substring(deli2+1),"-");
	if(st.countTokens()!=2)
	{
	    return null;
	}
	else
	{
	    txn=st.nextToken();
	    ver=Long.valueOf(st.nextToken());
	}
	
	return new Title(cat,name,txn,ver);

    }	

    
}

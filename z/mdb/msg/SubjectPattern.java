package z.mdb.msg;


/**
 * 对subject pattern的封装，并负责将其编码为subject pattern命令文本的编码
 * subject pattern可以理解为一种不完整的title
 * @author zhou
 *
 */
public class SubjectPattern
{
    /*
     * 0=空状态
     * 1=just like,用字符串直接作为subject pattern
     * 2=型如：cat>
     * 3=型如：cat>namePrefix
     * 4=型如：cat>name<
     * 5=型如：cat>name<txn
     * 6=型如：cat>name<txn-ver
     */
    private int type;
    
    private MsgCat cat;
    private String name;
    private String txnId;
    private long version;
    
    private SubjectPattern()
    {
	this.type=0;
    }
    
    public static SubjectPattern genPatternLike(String justLike)
    {
	SubjectPattern pattern=new SubjectPattern();
	pattern.type=1;
	pattern.name=justLike;
	return pattern;
    }
    public static SubjectPattern genPattern(MsgCat cat)
    {
	SubjectPattern pattern=new SubjectPattern();
	pattern.type=2;
	pattern.cat=cat;
	return pattern;
    }
    public static SubjectPattern genPatternWithTitlePrefix(MsgCat cat,String namePrefix)
    {
	SubjectPattern pattern=new SubjectPattern();
	pattern.type=3;
	pattern.cat=cat;
	pattern.name=namePrefix;
	return pattern;
    }
    public static SubjectPattern genPattern(MsgCat cat,String name)
    {
	SubjectPattern pattern=new SubjectPattern();
	pattern.type=4;
	pattern.cat=cat;
	pattern.name=name;
	return pattern;
    }
    public static SubjectPattern genPattern(MsgCat cat,String name,String txnId)
    {
	SubjectPattern pattern=new SubjectPattern();
	pattern.type=5;
	pattern.cat=cat;
	pattern.name=name;
	pattern.txnId=txnId;
	return pattern;
    }
    public static SubjectPattern genPattern(MsgCat cat,String name,String txnId,long version)
    {
	SubjectPattern pattern=new SubjectPattern();
	pattern.type=6;
	pattern.cat=cat;
	pattern.name=name;
	pattern.txnId=txnId;
	pattern.version=version;
	return pattern;
    }
    public String weaveSubjectPattern()
    {
	if(type==1)
	{
	    return name;
	}
	else if(type==2)
	{
	    return cat.toString()+"(";
	}
	else if(type==3)
	{
	    return cat.toString()+"("+name;
	}
	else if(type==4)
	{
	    return cat.toString()+"("+name+")";
	}
	else if(type==5)
	{
	    return cat.toString()+"("+name+")"+txnId+"-";
	}
	else if(type==6)
	{
	    return cat.toString()+"("+name+")"+txnId+"-"+version;
	}
	else
	{
	    return null;
	}
    }
    public String toString()
    {
	return this.weaveSubjectPattern();
    }
}

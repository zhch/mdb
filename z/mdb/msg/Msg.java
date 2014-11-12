package z.mdb.msg;

/**
 * 负责msg六元组:(cat,name,txn,ver,con,ans)建模，并将其编码为底层命令文本
 * 
 * knows:
 * 
 * don't know:
 * 	1.只负责从六元组到命令文本的编解码，不了解六元组各字段的语义
 * 
 * @author zhou
 *
 */
public class Msg
{
	private long seq;
	private Title title;
	private Content content;
	private MsgAns ansed;
	private long intDate;
	public Msg(Title title, Content content)
	{
		super();
		seq=-1;
		this.title = title;
		this.content = content;
		//	this.ansed=MsgAns.IGNORE;
		this.ansed=null;
		this.intDate=-1;
	}
	public Msg(Title title,long intDate)
	{
		seq=-1;
		this.title=title;
		this.intDate=intDate;
		this.content=null;
		this.ansed=null;
	}
	public Msg(long seq,Title title,Content content)
	{
		this.seq=seq;
		this.title=title;
		this.content=content;
		
	}
	
	
	public long getSeq() {
		return seq;
	}
	public void setSeq(long seq) {
		this.seq = seq;
	}
	public Title getTitle()
	{
		return title;
	}
	public void setTitle(Title title)
	{
		this.title = title;
	}
	public Content getContent()
	{
		return content;
	}
	public void setContent(Content content)
	{
		this.content = content;
	}
	public MsgAns getAnsed()
	{
		return ansed;
	}
	public void setAnsed(MsgAns ansed)
	{
		this.ansed = ansed;
	}

	public long getIntDate()
	{
		return intDate;
	}
	public void setIntDate(long intDate)
	{
		this.intDate = intDate;
	}
	public static Msg parse(String subject,String text)
	{
		Title title=Title.parse(subject);
		Content cont=Content.parse(text);
		return new Msg(title,cont);
	}

}

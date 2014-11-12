package z.mdb.imm;

import java.util.ArrayList;

import z.hilog.AbstLogger;
import z.hilog.InstantLogger;
import z.hilog.LogNode;
import z.hilog.fast.FastInstantLogger;
import z.mdb.excp.IMAPCmdNotOkException;
import z.mdb.imm.imap.IMAPImplement;
import z.mdb.msg.Content;
import z.mdb.msg.Msg;
import z.mdb.msg.MsgAns;
import z.mdb.msg.MsgCat;
import z.mdb.msg.SubjectPattern;
import z.mdb.msg.Title;
import z.mdb.txn.Txn;
import z.mdb.util.Utilities;


/**
 * 具体操作imap协议，包含一些需求决定的操作技巧
 * knows:
 * 	1.用msg包与imap包通讯
 * 	2.不知道 msg各字段的具体含义
 *    	3.folder
 * don't know:
 * 	×暂×未×生×效×1.进行search*系列操作前需要refresh
 * 	2.具体的imap 协议规则，由IMAPImplement类实现
 * @author zhou
 *
 */
public class IMManipulate
{
	private IMAPImplement imap;
	private boolean doLog;//是否开启debug log
	private AbstLogger logger;
	public IMManipulate(String host, int port, String user, String pswd,boolean useSSL)
	{
		this.imap=new IMAPImplement(host, port, user, pswd, useSSL);
		this.doLog=false;
	}
	public IMManipulate(String host, int port, String user, String pswd,boolean useSSL,AbstLogger logger)
	{
		this.imap=new IMAPImplement(host, port, user, pswd, useSSL,logger);
		this.doLog=true;
		this.logger=logger;
	}
	public IMManipulate(String host, int port, String user, String pswd,boolean useSSL,AbstLogger logger,int logDepth)
	{
		if(logDepth>0)
		{
			this.imap=new IMAPImplement(host, port, user, pswd, useSSL,logger,logDepth-1);
		}
		else
		{
			this.imap=new IMAPImplement(host, port, user, pswd, useSSL);
		}
		if(logDepth>=0)
		{
			this.doLog=true;
			this.logger=logger;
		}
		else
		{
			this.doLog=false;
		}
	}
	private String log0(String info1)
	{
		if(doLog)
		{
			return logger.routineStart(info1);
		}
		else
		{
			return null;
		}
	}
	private boolean logt(String stub,String info2)
	{
		if(doLog)
		{
			return logger.routineFinished(stub,info2);
		}
		else
		{
			return false;
		}
	}
	private boolean log(String info)
	{
		if(doLog)
		{
			return logger.routineFinished(logger.routineStart(info),"ROUTINE END");
		}
		else
		{
			return false;
		}
	}
	public boolean open()
	{
		String logOpenMethod=log0("imm.open()");

		boolean retval=imap.open();

		logt(logOpenMethod,retval+"");
		return retval;
	}
	public Txn getTxn()
	{
		return new Txn(this);
	}
	public boolean create(String folder)
	{
		String logCreateMethod=log0("imm.create()");

		boolean retval=imap.create(folder);

		logt(logCreateMethod,retval+"");
		return retval;
	}
	public boolean select(String folder)
	{
		String logSelectMethod=log0("imm.select()");

		boolean retval=imap.select(folder);

		logt(logSelectMethod,retval+"");
		return retval;
	}

	public boolean put(Msg msg)
	{
		String logPutMethod=this.log0("imm.put("+msg.getTitle().weaveSubject()+","+msg.getContent().weaveText()+")");

		String subject=msg.getTitle().weaveSubject();
		String text=msg.getContent().weaveText();
		boolean retval= imap.append(subject, text);

		this.logt(logPutMethod,retval+"");

		return retval;

	}
	public boolean confirm(Title[] titles)
	{
		String logConfirmMethod=log0("imm.confirm("+titles.length+" msgs)");

		String[] subs=new String[titles.length];
		for(int i=0;i<titles.length;i++)
		{
			subs[i]=titles[i].weaveSubject();	    
		}
		boolean retval= imap.flagAnsed(subs);

		logt(logConfirmMethod,"confirm result:"+retval);
		return retval;
	}

	/**
	 * TODO:可以通过在IMAPImplement中增加ignore ans的search命令实现来改进性能
	 * @param pattern
	 * @param ans
	 * @return
	 */
	public boolean expunge(Title title,MsgAns ans)
	{
		String logExpMethod=log0("imm.expunge("+title.weaveSubject()+","+ans.toString()+")");
		boolean retval;
		retval=imap.flagDeled(title.weaveSubject(), ans.toInt());
		logt(logExpMethod,retval+"");
		return retval ;
	}
	public boolean expunge(long[] seqs)
	{
		return imap.flagDeled(seqs);
	}
	public long[] getSeqs(SubjectPattern pattern,MsgAns ans)throws IMAPCmdNotOkException
	{
		long[] retval;
		String logGetSeqs=log0("imm.getSeqs("+pattern.weaveSubjectPattern()+","+ans.toString()+")");
		retval=imap.searchSeqs(pattern.weaveSubjectPattern(), ans.toInt());
		logt(logGetSeqs,"got: "+retval.length);
		return retval;
	}
	public boolean checkIsDeleted(long[] seqs)
	{
		boolean ret;
		String logDels=log0("imm.checkIsDeleted("+seqs.length+")");
		boolean[] dels=imap.checkIsDeleted(seqs);
		ret=false;
		for(int i=0;i<dels.length;i++)
		{
			if(dels[i]==true)
			{
				ret=true;
			}
		}
		logt(logDels,"end");
		return ret;
		
	}
	public Title[] getTitles(SubjectPattern pattern,MsgAns ans)
	{
		String logGetTitles=log0("imm.getTitles("+pattern.weaveSubjectPattern()+","+ans.toString()+")");
		ArrayList<Title> titleList=new ArrayList<Title>();
		Title title;
		String[] subs;
		subs=imap.searchSubs(pattern.weaveSubjectPattern(),ans.toInt());
		if(subs!=null)
		{
			for(int i=0;i<subs.length;i++)
			{
				if((title=Title.parse(subs[i]))!=null)
				{
					titleList.add(title);
				}
			}
		}
		logt(logGetTitles,"got: "+titleList.size());
		return titleList.toArray(new Title[titleList.size()]);
	}
	public Content[] getCons(SubjectPattern pattern,MsgAns ans)
	{
		String logGetConsMethod=log0("imm.getCons("+pattern.weaveSubjectPattern()+","+ans.toString()+")");

		ArrayList<Content> contentList=new ArrayList<Content>();
		Content con;
		String[] txts;
		txts=imap.searchTexts(pattern.weaveSubjectPattern(),ans.toInt());
		for(int i=0;i<txts.length;i++)
		{
			if((con=Content.parse(txts[i]))!=null)
			{
				contentList.add(con);
			}
		}
		logt(logGetConsMethod,contentList.size()+"");
		return contentList.toArray(new Content[contentList.size()]);
	}

	public Msg[] getTitleNCons(SubjectPattern pattern,MsgAns ans)throws IMAPCmdNotOkException
	{
		String getMsgsMethod=log0("imm.getMsgs("+pattern.weaveSubjectPattern()+","+ans.toString()+")");

		ArrayList<Msg> msgList=new ArrayList<Msg>();
		String[] msgs;

		msgs=imap.searchSubNTxts(pattern.weaveSubjectPattern(),ans.toInt());
		if(msgs!=null && msgs.length%2==0)
		{
			for(int i=0;i<msgs.length;i=i+2)
			{
				msgList.add(Msg.parse(msgs[i],msgs[i+1]));
			}
		}

		Msg[] retval= msgList.toArray(new Msg[msgList.size()]);

		logt(getMsgsMethod,retval.length+"");

		return  retval;
	}
	public Msg[] getSeqNTitleNCons(SubjectPattern pattern,MsgAns ans)throws IMAPCmdNotOkException
	{
		Msg[] ret;
		long[] seqs=imap.searchSeqs(pattern.weaveSubjectPattern(), ans.toInt());
		if(seqs!=null && seqs.length>0)
		{
			ret=new Msg[seqs.length];
			String[] msgs=imap.searchSubNTxts(seqs);
			for(int i=0;i<seqs.length;i++)
			{
				ret[i]=new Msg(seqs[i],Title.parse(msgs[2*i]),Content.parse(msgs[2*i+1]));
			}
			return ret;
		}
		else
		{
			return null;
		}
	}
	public long[] getBeings(SubjectPattern pattern,MsgAns ans)throws IMAPCmdNotOkException
	{
		long[] retval=null;
		String logGetBeings=log0("imm.getBeings("+pattern.weaveSubjectPattern()+","+ans.toString()+")");

		retval=imap.searchBeings(pattern.weaveSubjectPattern(),ans.toInt());
		if(retval!=null)
		{
			logt(logGetBeings,retval.length+"");
		}
		else
		{
			logt(logGetBeings,"null");
		}
		return retval;
	}
	public Msg[] getTitleNBeings(SubjectPattern pattern,MsgAns ans)throws IMAPCmdNotOkException
	{
		ArrayList<Msg> msgList=new ArrayList<Msg>();
		String logGetSubNBeings=log0("imm.getMsgs("+pattern.weaveSubjectPattern()+","+ans.toString()+")");
		String[] msgs=imap.searchSubNBeings(pattern.weaveSubjectPattern(), ans.toInt());
		if(msgs!=null && msgs.length%2==0)
		{
			for(int i=0;i<msgs.length;i=i+2)
			{
				msgList.add(new Msg(Title.parse(msgs[i]),Long.parseLong(msgs[i+1])));
			}
		}
		logt(logGetSubNBeings,msgList.size()+"");
		return msgList.toArray(new Msg[msgList.size()]);
	}
	public static void main(String[] args)
	{
		//	try
		//	{
		//	    String[] NAMES={"Mary","Vivian","Alice","Bob","Adjani","Wil","Jack","Delphy","Zhou","Ann"};
		//	    String folder="imm13";
		//	    FastInstantLogger logger=new FastInstantLogger();
		//	    logger.initTable();
		//
		//	    IMManipulate imm=new IMManipulate("127.0.0.1",143,"test3@mdb.test", "testtest",false,logger,0);
		//
		//	    imm.open();
		//	    imm.create(folder);
		//	    imm.select(folder);
		//
		//	    ArrayList<Title> titleList=new ArrayList<Title>();
		//	    Msg msg;
		//	    Title title;
		//	    String txn=Utilities.get32CharUUID();
		//	    for(int i=0;i<NAMES.length;i++)
		//	    {
		//		title=new Title(MsgCat.NORMAL_MSG,NAMES[i],txn,1);	    
		//		imm.put(new Msg(title,Content.parse("100")));
		//		titleList.add(title);
		//	    }
		//	    imm.confirm(titleList.toArray(new Title[titleList.size()]));
		//
		//
		//
		//	    for(int i=0;i<13;i++)
		//	    {
		//		titleList=new ArrayList<Title>();
		//		txn=Utilities.get32CharUUID();    
		//		int from,to,trans,from0,to0;
		//		Msg fromMsg,toMsg;
		//		from=(int)(NAMES.length*Math.random());
		//		to=(int)(NAMES.length*Math.random());
		//		while(to==from)
		//		{
		//		    to=(int)(NAMES.length*Math.random());
		//		}
		//		fromMsg=imm.getMsgs(SubjectPattern.genPattern(MsgCat.NORMAL_MSG, NAMES[from]),MsgAns.ANSED)[0];
		//		from0=Integer.valueOf(fromMsg.getContent().toString());
		//		toMsg=imm.getMsgs(SubjectPattern.genPattern(MsgCat.NORMAL_MSG, NAMES[to]),MsgAns.ANSED)[0];
		//		to0=Integer.valueOf(toMsg.getContent().toString());
		//		trans=(int)(from0*Math.random());
		//
		//		String logOp=logger.routineStart(NAMES[from]+" gives "+NAMES[to]+" "+trans+" yuan");
		//
		//		title=new Title(MsgCat.NORMAL_MSG,NAMES[from],txn,fromMsg.getTitle().getVersion()+1);	    
		//		imm.put(new Msg(title,Content.parse(""+(from0-trans))));
		//		titleList.add(title);
		//		imm.expunge(fromMsg.getTitle(),MsgAns.ANSED);
		//		title=new Title(MsgCat.NORMAL_MSG,NAMES[to],txn,toMsg.getTitle().getVersion()+1);	    
		//		imm.put(new Msg(title,Content.parse(""+(to0+trans))));
		//		titleList.add(title);
		//		imm.expunge(toMsg.getTitle(),MsgAns.ANSED);
		//		imm.confirm(titleList.toArray(new Title[titleList.size()]));
		//
		//		logger.routineFinished(logOp,"succ");
		//	    }
		//	}
		//	catch(Exception e)
		//	{
		//	    e.printStackTrace();
		//	}
	}

}

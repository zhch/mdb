package z.mdb.txn;

import java.util.ArrayList;

import z.hilog.AbstLogger;
import z.mdb.excp.IMAPCmdNotOkException;
import z.mdb.excp.IMMCollapsedException;
import z.mdb.excp.InvalidParameterException;
import z.mdb.excp.TxnCommitFailed;
import z.mdb.excp.TxnValidateFailedExcp;
import z.mdb.imm.IMManipulate;
import z.mdb.msg.Content;
import z.mdb.msg.Msg;
import z.mdb.msg.MsgAns;
import z.mdb.msg.MsgCat;
import z.mdb.msg.SubjectPattern;
import z.mdb.msg.Title;
import z.mdb.util.Utilities;

public class Txn
{
	private String id;
	private Title latch;
	private IMManipulate imm;
	private ArrayList<Msg> readSet;
	private ArrayList<String> writeNames;
	private ArrayList<Content> writeCons;
	private ArrayList<String> delSet;
	private int status;
	private boolean doLog;
	private AbstLogger logger;
	private long latchSleep=50;

	public Txn(IMManipulate imm)
	{
		this.id=Utilities.get32CharUUID();
		this.doLog=false;
		this.imm=imm;
		readSet=new ArrayList<Msg>(); 
		writeNames=new ArrayList<String>();
		writeCons=new ArrayList<Content>();
		delSet=new ArrayList<String>();
		status=0;
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
	public boolean use(String folder)
	{
		String logUse=log0("txn.use("+folder+")");
		boolean retval;
		if(imm.select(folder))
		{
			retval = true;
		}
		else
		{
			retval = false;
		}
		logt(logUse,retval+"");
		return retval;
	}

	public boolean open(String folder)
	{
		String logOpen=log0("txn.open("+folder+")");

		boolean retval;
		if(imm.open())
		{
			if(imm.select(folder))
			{
				retval = true;
			}
			else
			{
				retval = false;
			}
		}
		else
		{
			retval = false;
		}

		logt(logOpen,retval+"");

		return retval;
	}
	private void latchIt()throws IMAPCmdNotOkException
	{
		Title title; 
		Title[] titles;
		long ticket;
		long[] lockSeqs;
		//	Utilities.printDebug("status:"+status);
		latch=new Title(MsgCat.X_MSG,"latch",id,1);
		imm.put(new Msg(latch,Content.parse("mdb latch")));
		ticket=imm.getSeqs(latch.degen2SubPattern(),MsgAns.IGNORE)[0];
		lockSeqs=imm.getSeqs(SubjectPattern.genPattern(MsgCat.X_MSG,"latch"),MsgAns.IGNORE);
		if(lockSeqs!=null || lockSeqs.length>0)
		{
			for(int i=0;i<lockSeqs.length;)
			{
				if(lockSeqs[i]<ticket)
				{
					try
					{
						Thread.sleep(latchSleep);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					i=0;
					lockSeqs=imm.getSeqs(SubjectPattern.genPattern(MsgCat.X_MSG,"latch"),MsgAns.IGNORE);
				}
				else
				{
					i++;
				}
			}
		}
		//	Utilities.printDebug(id+"latched");
	}
	private void unlatch()
	{
		imm.expunge(latch, MsgAns.IGNORE);
		//	Utilities.printDebug(id+"unlatched");
	}
	public Content read(String name)throws IMMCollapsedException,IMAPCmdNotOkException
	{
		String logRead=log0("txn.read("+name+")");
		Content retval=null;
		boolean finished=false;
		for(int i=0;i<delSet.size();i++)
		{
			if(delSet.get(i).equals(name))
			{
				retval= null;
				finished=true;
			}
		}
		if(!finished)
		{
			for(int i=0;i<writeNames.size();i++)
			{
				if(writeNames.equals(name))
				{
					retval=writeCons.get(i);
					finished=true;
				}
			}
		}
		if(!finished)
		{
			for(int i=0;i<readSet.size();i++)
			{
				if(readSet.get(i).getTitle().getName().equals(name))
				{
					retval=readSet.get(i).getContent();
					finished=true;
				}
			}
		}
		if(!finished)
		{
			Msg[] msgs=imm.getSeqNTitleNCons(SubjectPattern.genPattern(MsgCat.NORMAL_MSG, name),MsgAns.IGNORE);
			Msg msg=null;
			long ver=-1;
			if(msgs!=null && msgs.length>0)
			{
				for(int i=0;i<msgs.length;i++)
				{
					if(msgs[i].getTitle().getVersion()>ver)
					{
						msg=msgs[i];
						ver=msg.getTitle().getVersion();
					}
				}
				retval=msg.getContent();
			}
			else 
			{
				msg=new Msg(-1,new Title(MsgCat.NORMAL_MSG, name,id,-1),Content.parse("not found"));
				retval=null;
			}
			readSet.add(msg);


		}
		logt(logRead,retval.weaveText());
		return retval;

	}

	public boolean write(String name,Content con)throws IMMCollapsedException,IMAPCmdNotOkException
	{
		String logWrite=log0("txn.write("+name+","+con.toString()+")");
		boolean retval=false;
		boolean finished=false;
		Msg msg;
		for(int i=0;i<delSet.size();i++)
		{
			if(delSet.get(i).equals(name))
			{
				delSet.remove(i);
				writeNames.add(name);
				writeCons.add(con);
				retval= true;
				finished=true;
			}
		}
		if(!finished)
		{
			for(int i=0;i<writeNames.size();i++)
			{
				if(writeNames.get(i).equals(name))
				{
					writeCons.set(i, con);
					retval=true;
					finished=true;
				}
			}
		}
		if(!finished)
		{
			writeNames.add(name);
			writeCons.add(con);
			retval=true;
		}

		logt(logWrite,retval+"");
		return retval;
	}

	public boolean remove(String name)throws IMAPCmdNotOkException,IMMCollapsedException
	{
		String logRemove=log0("txn.remove("+name+")");

		boolean retval=false;
		boolean finished=false;
		for(int i=0;i<delSet.size();i++)
		{
			if(delSet.get(i).equals(name))
			{
				retval= true;
				finished=true;
			}
		}
		if(!finished)
		{
			delSet.add(name);
			for(int i=0;i<writeNames.size();i++)
			{
				if(writeNames.get(i).equals(name))
				{
					writeNames.remove(i);
					writeCons.remove(i);
					retval=true;
					finished=true;
				}
			}
		}
		logt(logRemove,retval+"");
		return retval;
	}

	public boolean commit()throws TxnValidateFailedExcp,IMAPCmdNotOkException
	{
		String logCommit=log0("txn.commit()");
		Title[] titles;
		ArrayList<Long> seqList=new ArrayList<Long>();
		long[] seqs;
		Title title;
		Msg msg;
		this.latchIt();
		for(int i=0;i<readSet.size();i++)
		{
			if(readSet.get(i).getSeq()!=-1)
			{
				//		Utilities.printDebug("validating:"+readSet.get(i)+",should:"+readSet.get(i).getTitle().weaveSubject());
				seqList.add(readSet.get(i).getSeq());
				//discarded @2012-1-6
				//				titles=imm.getTitles(readSet.get(i).getTitle().degen2SubPattern(),MsgAns.IGNORE);
				//				if(titles==null || titles.length!=1)
				//				{
				//					//		    Utilities.printDebug("failed");
				//					throw new TxnValidateFailedExcp("none or multiple read msg was found, name:"+readSet.get(i).getTitle().getName());
				//				}
				//		Utilities.printDebug("succ");
			}
			else
			{
				Utilities.printDebug("validating:"+readSet.get(i)+", should has nothing");
				titles=imm.getTitles(SubjectPattern.genPattern(MsgCat.NORMAL_MSG,readSet.get(i).getTitle().getName()),MsgAns.IGNORE);
				if(titles!=null && titles.length>0)
				{
					Utilities.printDebug("failed");
					throw new TxnValidateFailedExcp("nothing found when read while some when validate, name:"+readSet.get(i).getTitle().getName());
				}
				Utilities.printDebug("succ");
			}
		}
		seqs=new long[seqList.size()];
		for(int i=0;i<seqList.size();i++)
		{
			seqs[i]=seqList.get(i);
		}
		if(seqs!=null && seqs.length>0)
		{
			if(imm.checkIsDeleted(seqs))
			{
				throw new TxnValidateFailedExcp("read set polluted");
			}
		}
		for(int i=0;i<writeNames.size();i++)
		{
			titles=imm.getTitles(SubjectPattern.genPattern(MsgCat.NORMAL_MSG,writeNames.get(i)),MsgAns.IGNORE);
			if(titles!=null && titles.length>0)
			{
				msg=new Msg(new Title(MsgCat.NORMAL_MSG,writeNames.get(i),id,titles[0].getVersion()+1),writeCons.get(i));
				imm.put(msg);
				imm.expunge(titles[0], MsgAns.IGNORE);
			}
			else
			{
				msg=new Msg(new Title(MsgCat.NORMAL_MSG,writeNames.get(i),id,1),writeCons.get(i));
				imm.put(msg);
			}
		}
		for(int i=0;i<delSet.size();i++)
		{
			titles=imm.getTitles(SubjectPattern.genPattern(MsgCat.NORMAL_MSG,delSet.get(i)),MsgAns.IGNORE);
			if(titles!=null && titles.length>0)
			{
				imm.expunge(titles[0],MsgAns.IGNORE);
			}
		}
		unlatch();
		logt(logCommit,"succ!");
		this.status=3;
		return true;
	}

	public void rollback()
	{
		String logRoll=log0("txn.rollback()");
		this.unlatch();
		logt(logRoll,"end");
	}
	public static void main(String[] args)
	{
		//	try
		//	{
		//	    Txn txn=new Txn("127.0.0.1",143,"test3@mdb.test", "testtest",false);
		//	    txn.open("txn1");
		//	    for(int i=0;i<Utilities.NAMES.length;i++)
		//	    {
		//		txn.write(Utilities.NAMES[i],Content.parse("100"));
		//	    }
		//	    txn.commit();
		//
		//	    for(int i=0;i<1115;i++)
		//	    {
		//		txn=new Txn("127.0.0.1",143,"test3@mdb.test", "testtest",false);
		//		txn.open("txn1");
		//		int from=(int)(Math.random()*Utilities.NAMES.length);
		//		int to=(int)(Math.random()*Utilities.NAMES.length);
		//		while(to==from)
		//		{
		//		    to=(int)(Math.random()*Utilities.NAMES.length);
		//		}
		//		int from0=Integer.parseInt(txn.read(Utilities.NAMES[from]).toString());
		//		int to0=Integer.parseInt(txn.read(Utilities.NAMES[to]).toString());
		//		int trans=(int)(Math.random()*from0);
		//		txn.write(Utilities.NAMES[from],Content.parse((from0-trans)+""));
		//		txn.write(Utilities.NAMES[to],Content.parse((to0+trans)+""));
		//		txn.commit();
		//		Utilities.printDebug(Utilities.NAMES[from]+"("+from0+") -"+trans+"-> "+Utilities.NAMES[to]+"("+to0+")");
		//	    }
		//	}
		//	catch(Exception e)
		//	{
		//	    e.printStackTrace();
		//	}
	}

}

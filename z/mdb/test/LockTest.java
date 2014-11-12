package z.mdb.test;

import java.util.ArrayList;

import z.hilog.AbstLogger;
import z.hilog.InstantLogger;
import z.hilog.LogNode;
import z.hilog.fast.FastInstantLogger;
import z.mdb.imm.IMManipulate;
import z.mdb.lock.LockCat;
import z.mdb.lock.LockService;
import z.mdb.msg.Content;
import z.mdb.msg.Msg;
import z.mdb.msg.MsgAns;
import z.mdb.msg.MsgCat;
import z.mdb.msg.SubjectPattern;
import z.mdb.msg.Title;
import z.mdb.util.Utilities;

public class LockTest extends Thread
{
    private LockService lock;
    private IMManipulate imm;
    private int trials;
    private AbstLogger log;
    private String iden;

    public LockTest(LockService lock, IMManipulate imm, int trial,AbstLogger log,String iden)
    {
	super();
	this.lock = lock;
	this.imm = imm;
	this.trials = trial;
	this.log=log;
	this.iden=iden;
    }


    public void run()
    {
	try
	{
	    int from0=-1,to0=-1,trans=-1;
	    Msg fromMsg,toMsg;
	    ArrayList<Title> titList=new ArrayList<Title>();
	    Title title;
	    int from=(int)(Math.floor((Math.random()*(Utilities.NAMES.length))));
	    int to=(int)(Math.floor((Math.random()*(Utilities.NAMES.length))));
	    while(from==to)
	    {
		to=(int)(Math.floor((Math.random()*(Utilities.NAMES.length))));
	    }
	    //测试用////////////////////////////////////////
	    	    	if(iden.equals("test1"))
	    	    	{
	    	    	    from=4;
	    	    	    to=7;
	    	    	}
	    	    	else
	    	    	{
	    	    	    from=7;
	    	    	    to=8;
	    	    	}
	    //////////////////////////////////////////
	    for(int i=0;i<this.trials;)
	    {
		Utilities.printDebug(i+":from="+from+",to="+to);
		String logTrans = log.routineStart(iden+":"+Utilities.NAMES[from]+" --> "+Utilities.NAMES[to]);
		int  roRes=0;
		if(lock.acquire(LockCat.X,Utilities.NAMES[from]))
		{
		    fromMsg=imm.getMsgs(SubjectPattern.genPattern(MsgCat.NORMAL_MSG,Utilities.NAMES[from]),MsgAns.ANSED)[0];
		    from0=Integer.valueOf(fromMsg.getContent().toString());
		    if(lock.acquire(LockCat.X,Utilities.NAMES[to]))
		    {
			toMsg=imm.getMsgs(SubjectPattern.genPattern(MsgCat.NORMAL_MSG,Utilities.NAMES[to]),MsgAns.ANSED)[0];
			to0=Integer.valueOf(toMsg.getContent().toString());
			trans=(int)(from0*Math.random());

			title=new Title(MsgCat.NORMAL_MSG,fromMsg.getTitle().getName(),lock.getTxnId(),fromMsg.getTitle().getVersion()+1);
			imm.put(new Msg(title,Content.parse((from0-trans)+"")));
			titList.add(title);
			imm.expunge(fromMsg.getTitle(),MsgAns.ANSED);

			title=new Title(MsgCat.NORMAL_MSG,toMsg.getTitle().getName(),lock.getTxnId(),toMsg.getTitle().getVersion()+1);
			imm.put(new Msg(title,Content.parse((to0+trans)+"")));
			titList.add(title);
			imm.expunge(toMsg.getTitle(),MsgAns.ANSED);

			imm.confirm(titList.toArray(new Title[titList.size()]));



			from=(int)(Math.floor((Math.random()*(Utilities.NAMES.length))));
			to=(int)(Math.floor((Math.random()*(Utilities.NAMES.length))));
			while(from==to)
			{
			    to=(int)(Math.floor((Math.random()*(Utilities.NAMES.length))));
			}
			i++;
			roRes=1;
		    }
		    else
		    {
			roRes=-1;
		    }
		}
		else
		{
		    roRes=-2;
		}

		String logClean=log.routineStart(iden+":"+"release locks");
		lock.release();
		log.routineFinished(logClean,iden+":"+"finished");

		titList=new ArrayList<Title>();
		if(roRes==1)
		{
		    log.routineFinished(logTrans,i+" succ: "+from0+" -"+trans+"-> "+to0);
		}
		else if(roRes==-1)
		{
		    log.routineFinished(logTrans,iden+":"+"failed: can not lock to, going to retry ");
		}
		else if(roRes==-2)
		{
		    log.routineFinished(logTrans,iden+":"+"failed: can not lock from, going to retry");
		}


	    }
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	}
    }
    public static void main(String[] args)
    {
	String folder="imm13";
	FastInstantLogger logger1=new FastInstantLogger();
	logger1.initTable();
	FastInstantLogger logger2=new FastInstantLogger();
	IMManipulate imm1=new IMManipulate("127.0.0.1",143,"test3@mdb.test", "testtest",false,logger1);
	imm1.open();
	imm1.select(folder);
	IMManipulate imm2=new IMManipulate("127.0.0.1",143,"test3@mdb.test", "testtest",false,logger2);
	imm2.open();
	imm2.select(folder);

	ArrayList<Title> titleList=new ArrayList<Title>();
	String txn=Utilities.get32CharUUID();
	for(int i=0;i<Utilities.NAMES.length;i++)
	{
	    Title title=new Title(MsgCat.NORMAL_MSG,Utilities.NAMES[i],txn,1);
	    imm2.put(new Msg(title,Content.parse("100")));
	    titleList.add(title);
	}
	imm2.confirm(titleList.toArray(new Title[titleList.size()]));

	LockService lock1=new LockService(imm1,4,Utilities.get32CharUUID(),10000,logger1);
	LockService lock2=new LockService(imm2,4,Utilities.get32CharUUID(),10000,logger2);

	LockTest test1=new LockTest(lock1,imm1,45,logger1,"test1");
	LockTest test2=new LockTest(lock2,imm2,35,logger2,"test2");


	test1.start();
	test2.start();

    }
}

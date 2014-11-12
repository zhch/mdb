package z.mdb.test;

import java.util.Date;

import z.hilog.AbstLogger;
import z.hilog.fast.FastInstantLogger;
import z.mdb.excp.IMAPCmdNotOkException;
import z.mdb.excp.IMMCollapsedException;
import z.mdb.excp.TxnCommitFailed;
import z.mdb.msg.Content;
import z.mdb.txn.Txn;
import z.mdb.util.Utilities;

public class TxnTest extends Thread
{
    private String host;
    private int port;
    private String user;
    private String pswd;
    private int times;
    private String[] names;
    private String folder;
    private boolean isFinished;
    private AbstLogger logger;
    private String iden;


    public TxnTest(String host, int port, String user, String pswd, int times,String[] names, String folder,AbstLogger logger,String iden)
    {
	super();
	this.host = host;
	this.port = port;
	this.user = user;
	this.pswd = pswd;
	this.times = times;
	this.names = names;
	this.folder = folder;
	this.logger=logger;
	this.iden=iden;
	isFinished=false;
    }



    public boolean isFinished()
    {
	return isFinished;
    }

    private int getFrom(int trial,String iden)
    {
	if(iden.equals("test1"))
	{
	    if(trial==0)
	    {
		return 1;
	    }
	    else if(trial==1)
	    {
		return 4;
	    }
	}
	else if(iden.equals("test2"))
	{
	    if(trial==0)
	    {
		return 3;
	    }
	    else if(trial==1)
	    {
		return 2;
	    }
	    else if(trial==2)
	    {
		return 6;
	    }
	    else if(trial==3)
	    {
		return 4;
	    }
	}
	return  (int)(Math.random()*names.length);

    }
    private int getTo(int trial,String iden,int from)
    {
	if(iden.equals("test1"))
	{
	    if(trial==0)
	    {
		return 2;
	    }
	    else if(trial==1)
	    {
		return 5;
	    }
	}
	else if(iden.equals("test2"))
	{
	    if(trial==0)
	    {
		return 2;
	    }
	    else if(trial==1)
	    {
		return 6;
	    }
	    else if(trial==2)
	    {
		return 7;
	    }
	    else if(trial==3)
	    {
		return 8;
	    }
	}
	int to=(int)(Math.random()*names.length);
	while(to==from)
	{
	    to=(int)(Math.random()*names.length);
	}
	return  to;
    }
    public void run()
    {

	Txn txn=null;
	int from=getFrom(0,iden);
	int to=getTo(0,iden,from);
	Date date1,date2;
	for(int i=0;i<times;)
	{
	    String logTrial=logger.routineStart(iden+":("+i+"):"+names[from]+" --> "+names[to]);
	    Utilities.printDebug("target"+i+": "+iden+":("+i+"):"+names[from]+from+" --> "+names[to]+to);
	    String testOp=null;
	    try
	    {

		txn=new Txn(host,port,user, pswd,false,5,logger);
		txn.open(folder);

		date1=new Date();
		testOp=logger.routineStart(iden+" op"+i+" started at "+date1.toLocaleString()); 

		int from0=Integer.parseInt(txn.read(names[from]).toString());

		date2=new Date();
		logger.routineLog("read from finished at "+date2.toLocaleString()+", "+((date2.getTime()-date1.getTime()))+" elapsed");
		//		Utilities.printDebug(iden+":from0 read");

		int to0=Integer.parseInt(txn.read(names[to]).toString());
		date2=new Date();
		logger.routineLog("read to finished at "+date2.toLocaleString()+", "+((date2.getTime()-date1.getTime()))+" elapsed");
		//		Utilities.printDebug(iden+":to0 read");

		int trans=(int)(Math.random()*from0);
		
		date2=new Date();
		logger.routineLog("calc trans finished at "+date2.toLocaleString()+", "+((date2.getTime()-date1.getTime()))+" elapsed");
		txn.write(names[from],Content.parse((from0-trans)+""));

		date2=new Date();
		logger.routineLog("write from finished at "+date2.toLocaleString()+", "+((date2.getTime()-date1.getTime()))+" elapsed");

		//		Utilities.printDebug(iden+":from write");

		txn.write(names[to],Content.parse((to0+trans)+""));

		//		Utilities.printDebug(iden+":to write");
		date2=new Date();
		logger.routineLog("write to finished at "+date2.toLocaleString()+", "+((date2.getTime()-date1.getTime()))+" elapsed");
		txn.commit();
		date2=new Date();
		logger.routineFinished(testOp,"commit finished at "+date2.toLocaleString()+", "+((date2.getTime()-date1.getTime()))+" elapsed");
		logger.routineFinished(logTrial,"succ: "+from0+" -"+trans+"-> "+to0);

		i++;
		from=getFrom(i,iden);
		to=getTo(i,iden,from);
	    }
	    catch(TxnCannotAcquireLockExcp e)
	    {
		logger.routineFinished(testOp,iden+":can not acquire the lock, going to retry");

		logger.routineFinished(logTrial,iden+":can not acquire the lock, going to retry");
		txn.rollback();
	    }
	    catch(IMMCollapsedException e2)
	    {
		logger.routineFinished(testOp,"excp:IMMCollapsedException");
		logger.routineFinished(logTrial,"excp:IMMCollapsedException");
		e2.printStackTrace();
		System.exit(0);
	    }
	    catch(TxnCommitFailed e3)
	    {
		logger.routineFinished(testOp,"excp:TxnCommitFailed");
		logger.routineFinished(logTrial,"excp:TxnCommitFailed");
		e3.printStackTrace();
		System.exit(0);
	    }
	    catch(IMAPCmdNotOkException e4)
	    {
		logger.routineFinished(testOp,"excp:IMAPCmdNotOkException");
		logger.routineFinished(logTrial,"excp:IMAPCmdNotOkException");
		e4.printStackTrace();
		System.exit(0);
	    }
	}
	isFinished=true;
    }
    public static void main(String[] args)
    {
	int player=5;
	int times=20;
	String host="127.0.0.1";
	int port=143;
	String user="test3@mdb.test";
	String pswd="testtest";
	String folder="txn1";
	String[] names=Utilities.getNames(1500,3);
	Date time1,time2;

	FastInstantLogger[] loggers=new FastInstantLogger[player];
	loggers[0]=new FastInstantLogger();
	loggers[0].initTable();
	for(int i=1;i<player;i++)
	{
	    loggers[i]=new FastInstantLogger();
	}

	Txn txn=new Txn(host,port,user,pswd,false,3);
	txn.open(folder);
	try
	{
	    for(int i=0;i<names.length;i++)
	    {
		Utilities.printDebug("writting "+names[i]);
		txn.write(names[i],Content.parse("100"));
	    }
	    txn.commit();

	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	}

	time1=new Date();
	Utilities.printDebug(time1.toLocaleString());
	TxnTest[] tests =new TxnTest[player];
	for(int i=0;i<player;i++)
	{
	    tests[i]=new TxnTest(host, port, user, pswd,times, names, folder,loggers[i],"test"+i);
	    tests[i].start();
	}

	try
	{

	    for(int i=0;i<player;)
	    {
		if(tests[i].isFinished==false)
		{
		    Thread.sleep(50000);
		    i=0;
		}
		else
		{
		    i++;   
		}
	    }

	    time2=new Date();

	    txn=new Txn(host,port,user,pswd,false,3);
	    txn.open(folder);
	    int total=0;
	    for(int i=0;i<names.length;i++)
	    {
		Utilities.printDebug("total:"+total);
		total=total+Integer.parseInt(txn.read(names[i]).toString());
	    }
	    txn.commit();
	    Utilities.printDebug(time2.toLocaleString());
	    Utilities.printDebug("total:"+total+",elapsed:"+((time2.getTime()-time1.getTime())/1000));
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	}

    }
}

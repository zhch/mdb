package z.mdb.test;

import java.util.Date;
import z.hilog.AbstLogger;
import z.hilog.InstantLogger;
import z.hilog.LogNode;
import z.mdb.imm.IMManipulate;
import z.mdb.msg.Content;
import z.mdb.msg.Msg;
import z.mdb.msg.MsgAns;
import z.mdb.msg.MsgCat;
import z.mdb.msg.SubjectPattern;
import z.mdb.msg.Title;
import z.mdb.util.Utilities;

public class IMMProduceConsumeTest extends Thread
{
    private IMManipulate imm;
    private String role;
    private StringBuffer signal;
    private AbstLogger logger;
    private long trials=103;
    private Date lastOpTime;

    public IMMProduceConsumeTest(IMManipulate imm, String role, StringBuffer signal, AbstLogger logger)
    {
	super();
	this.imm = imm;
	this.role = role;
	this.signal = signal;
	this.logger = logger;
    }

    private boolean signaledSinceLast()
    {
	long signalTime=Long.valueOf(signal.toString());
	long lastOp=lastOpTime.getTime();
	while(lastOp>=signalTime)
	{
	    try
	    {
		logger.routineStart(role+" waitting...");
		logger.routineFinished("signal="+signalTime+",last="+lastOp);
		Thread.sleep(100);
	    }
	    catch(Exception e)
	    {
		e.printStackTrace();
		return false;
	    }
	    signalTime=Long.valueOf(signal.toString());
	    lastOp=lastOpTime.getTime();
	}
	return true;

    }
    public void run()
    {
	if(role.equals("producer"))
	{
	    for(long i=0;i<trials;i++)
	    {
		Title title=new Title(MsgCat.NORMAL_MSG,"object-"+i,Utilities.get32CharUUID(),1);

		logger.routineStart("------produce:"+title.weaveSubject());

		boolean opres=imm.put(new Msg(title,Content.parse("object-"+i)));
		lastOpTime=new Date();

		logger.routineFinished(opres+","+lastOpTime.toLocaleString());

		signal.replace(0,signal.length(),lastOpTime.getTime()+"");
		signaledSinceLast();

		logger.routineStart("signaled");

		Title[] titles=imm.getTitles(SubjectPattern.genPatternWithTitlePrefix(MsgCat.NORMAL_MSG,"object"),MsgAns.UNANSED);
		if(titles.length>0)
		{
		    logger.routineFinished("consume check failed,i can still see include but not limited:"+titles[0].weaveSubject());
		    System.exit(0);
		}
		else
		{
		    logger.routineFinished("consume check succeed,going to produce more");
		}

	    }

	}
	else if(role.equals("consumer"))
	{
	    lastOpTime=new Date();
	    
	    for(long i=0;i<trials;i++)
	    {
		signaledSinceLast();
		logger.routineStart("------consume");
		Title[] titles=imm.getTitles(SubjectPattern.genPatternWithTitlePrefix(MsgCat.NORMAL_MSG,"object"),MsgAns.UNANSED);
		if(titles.length!=1)
		{
		    logger.routineFinished("failed");
		    System.exit(0);
		}
		else
		{
		    imm.expunge(titles[0],MsgAns.UNANSED);
		    logger.routineFinished(titles[0].weaveSubject()+" succeeded");
		    lastOpTime=new Date();
		    signal.replace(0,signal.length(),lastOpTime.getTime()+"");
		}
		
	    }
	    
	}
    }
    public static void main(String[] args)
    {
	LogNode log=new LogNode("===root===",null);
	String folder="imm13";
	String logPath="d:\\ll\\immtest.xml";
	InstantLogger logger1=new InstantLogger(log,logPath);
	InstantLogger logger2=new InstantLogger(log,logPath);
	IMManipulate imm1=new IMManipulate("127.0.0.1",143,"test3@mdb.test", "testtest",false,logger1);
	IMManipulate imm2=new IMManipulate("127.0.0.1",143,"test3@mdb.test", "testtest",false,logger2);
	imm1.open();
	imm2.open();
	imm1.create(folder);
	imm2.select(folder);
	imm1.select(folder);
	StringBuffer signal=new StringBuffer("-1");
	IMMProduceConsumeTest test1=new IMMProduceConsumeTest(imm1, "producer",signal,logger1);
	IMMProduceConsumeTest test2=new IMMProduceConsumeTest(imm2, "consumer",signal,logger2);
	test1.start();
	test2.start();
    }

}

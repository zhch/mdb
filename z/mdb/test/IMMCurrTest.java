package z.mdb.test;

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

public class IMMCurrTest extends Thread
{
    private IMManipulate imm;
    private String role;
    private StringBuffer signal;
    private AbstLogger logger;



    public IMMCurrTest(IMManipulate imm, String role, StringBuffer signal,AbstLogger logger)
    {
	super();
	this.imm = imm;
	this.role = role;
	this.signal = signal;
	this.logger = logger;
    }



    public void run()
    {
	if(role.equals("splash"))
	{
	    Title title;
	    Title[] titles;
	    Content[] cons;
	    while(signal.length()<=1);

	    logger.routineStart("splash started");

	    title=new Title(MsgCat.NORMAL_MSG,"splash here","txntxntxn",1);	    
	    if(imm.put(new Msg(title,Content.parse("splash"))))
	    {
		signal.append('s');
		titles=imm.getTitles(SubjectPattern.genPatternWithTitlePrefix(MsgCat.NORMAL_MSG,"splash"),MsgAns.UNANSED);
		for(int i=0;i<titles.length;i++)
		{
		    cons=imm.getCons(titles[i].degen2SubPattern(),MsgAns.UNANSED);
		    logger.routineStart("splash: i can see");
		    logger.routineFinished("("+titles[i].weaveSubject()+","+cons[0].toString()+")");
		}
	    }
	    else
	    {
		Utilities.printDebug("splash put failed");

		logger.routineFinished("splash put failed");

		System.exit(0);
	    }

	    logger.routineFinished("finished successful");
	}
	else if(role.equals("peep"))
	{
	    Title[] titles=null;
	    Title title=new Title(MsgCat.NORMAL_MSG,"spalsh here","txntxntxn",1);
	    Content[] cons;
	    signal.append('p');
	    while(signal.length()<=2)
	    {
		logger.routineLog("peep waitting...");
	    }

	    logger.routineStart("peep started");
	    titles=imm.getTitles(SubjectPattern.genPatternWithTitlePrefix(MsgCat.NORMAL_MSG,"splash"),MsgAns.UNANSED);
	    if(titles==null || titles.length<=0)
	    {
		logger.routineLog("can not see anything");
	    }
	    else
	    {
		for(int i=0;i<titles.length;i++)
		{
		    cons=imm.getCons(titles[i].degen2SubPattern(),MsgAns.UNANSED);
		    logger.routineStart("peep: i can see");
		    logger.routineFinished("("+titles[i].weaveSubject()+","+cons[0].toString()+")");
		}
	    }
	    logger.routineFinished("finished successful");

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
	StringBuffer signal=new StringBuffer("0");
	IMMCurrTest test1=new IMMCurrTest(imm1, "splash",signal,logger1);
	IMMCurrTest test2=new IMMCurrTest(imm2, "peep",signal,logger2);
	test1.start();
	test2.start();
    }
}

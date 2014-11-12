package z.hilog;

import java.util.ArrayList;

import z.hilog.util.UtilSer;
import z.mdb.util.Utilities;

public abstract class AbstLogger
{
    private StubMgr stubs;

    public AbstLogger()
    {
	stubs=new StubMgr();
    }
    abstract public void logAlpha(String info1);
    abstract public boolean logOmega(String info2);
    abstract public String newLogStub();

    public String routineStart(String info1)
    {
	this.logAlpha(info1);
	String stub=this.newLogStub();
	stubs.push(stub,info1);
	return stub;
    }

    public boolean routineFinished(String logStub,String info2)
    {
	if(stubs.hasPendingRoutines()==false)
	{
	    UtilSer.printDebug("invalid hilog behaviour, this is a routine finish with no routine start,info2 = "+info2);
	    new Throwable().printStackTrace();
	    System.exit(0);
	    return false;
	}
	else
	{
	    String pending=stubs.pop(logStub);
	    if(pending==null)
	    {
		return logOmega(info2);
	    }
	    else
	    {
		UtilSer.printDebug("invalid hilog behaviour, this is mismatched routine finish, while the waitting start is \""+pending+"\", info2 = \""+info2+"\"");
		new Throwable().printStackTrace();
		System.exit(0);
		return false;
	    }
	}
    }
    public boolean routineLog(String info)
    {
	return this.routineFinished(this.routineStart(info),"ROUTINE END");
    }


}

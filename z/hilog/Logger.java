package z.hilog;

import z.hilog.util.UtilSer;

public class Logger 
{
    private LogNode curr;
    private int startCount;

    public Logger(LogNode root) 
    {
	super();
	this.curr =root;
	startCount=0;
    }
    public void routineStart(String info)
    {
	this.startCount++;
	this.curr=this.curr.appendOff(info);

    }
    public boolean routineFinished(String info2)
    {
	this.startCount--;
	if(startCount<0)
	{
	    UtilSer.printDebug("错误的hilog行为:start-finish失配，试图输入routine finish信息:"+info2);
	    System.exit(0);
	    return false;
	}
	else
	{
	    this.curr.setInfo2(info2);
	    if(this.curr.getParent()!=null)
	    {
		this.curr=this.curr.getParent();
		return true;
	    }
	    else
	    {
		UtilSer.printDebug("错误的hilog行为:已回溯到日志树顶部，试图输入routine finish信息:"+info2);
		System.exit(0);
		return false;
	    }
	}

    }



}

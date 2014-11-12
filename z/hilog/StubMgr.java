package z.hilog;

import java.util.ArrayList;

public class StubMgr
{
    private ArrayList<String> stubs;
    private ArrayList<String> info1s;
    
    protected StubMgr()
    {
	stubs=new ArrayList<String>();
	info1s=new ArrayList<String>();
    }
    protected void push(String stub,String info1)
    {
	stubs.add(stub);
	info1s.add(info1);
    }
    protected String pop(String stub)
    {
	if(stubs.size()==0)
	{
	    return "there is no more routine pending";
	}
	else
	{
	    if(stubs.remove(stubs.size()-1).equals(stub))
	    {
		info1s.remove(info1s.size()-1);
		return null;
	    }
	    else
	    {
		return info1s.remove(info1s.size()-1);
	    }
	}
    }
    protected boolean hasPendingRoutines()
    {
	if(stubs.size()>0)
	{
	    return true;
	}
	else
	{
	    return false;
	}
    }
}

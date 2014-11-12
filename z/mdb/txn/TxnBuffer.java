package z.mdb.txn;

import java.util.ArrayList;

import z.mdb.msg.Msg;
import z.mdb.msg.MsgCat;

public class TxnBuffer
{
    private ArrayList<Msg> copies;
    private ArrayList<Msg> readSet;
    private ArrayList<Msg> writeSet;
    private ArrayList<Msg> delSet;

    public TxnBuffer()
    {
	copies=new ArrayList<Msg>();
	readSet=new ArrayList<Msg>(); 
	writeSet=new ArrayList<Msg>();
	delSet=new ArrayList<Msg>();
    }
    protected boolean isDeleted(String name)
    {
	for(int i=0;i<delSet.size();i++)
	{
	    if(delSet.get(i).getTitle().getName().equals(name))
	    {
		return true;
	    }
	}
	return false;
    }
    protected Msg contains(String name)
    {
	if(isDeleted(name))
	{
	    return null;
	}
	else
	{
	    Msg msg;
	    for(int i=0;i<writeSet.size();i++)
	    {
		msg=copies.get(i);
		if(msg.getTitle().getCat().equals(cat))
		{
		    if(msg.getTitle().getName().equals(name))
		    {
			return msg;
		    }
		}
	    }
	    for(int i=0;i<dirties.size();i++)
	    {
		msg=dirties.get(i);
		if(msg.getTitle().getCat().equals(cat))
		{
		    if(msg.getTitle().getName().equals(name))
		    {
			return msg;
		    }
		}
	    }
	    return null;
	}
    }
    protected void insert(Msg msg,boolean isDirty)
    {
	remove(msg.getTitle().getCat(),msg.getTitle().getName());
	if(isDirty)
	{
	    dirties.add(msg);
	}
	else
	{
	    copies.add(msg);
	}

    }
    protected boolean remove(MsgCat cat,String name)
    {
	Msg msg;
	for(int i=0;i<copies.size();i++)
	{
	    msg=copies.get(i);
	    if(msg.getTitle().getCat().equals(cat))
	    {
		if(msg.getTitle().getName().equals(name))
		{
		    copies.remove(i);
		    return true;
		}
	    }
	}
	for(int i=0;i<dirties.size();i++)
	{
	    msg=dirties.get(i);
	    if(msg.getTitle().getCat().equals(cat))
	    {
		if(msg.getTitle().getName().equals(name))
		{
		    dirties.remove(i);
		    return true;
		}
	    }
	}
	return false;
    }
    protected Msg[] getDirties()
    {
	return dirties.toArray(new Msg[dirties.size()]);
    }
}

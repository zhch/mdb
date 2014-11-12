package z.hilog;

import java.util.ArrayList;

public class LogNode 
{
	private String info1,info2;
	private LogNode[] children;
	private LogNode parent;
//	private boolean iterated;
	
	
	public LogNode(String info1,LogNode parent) 
	{
		super();
		this.info1 = info1;
		this.info2="CAN NOT REACH THE ROUTINE END";
		this.parent=parent;
		this.children=null;
	}
	
	public synchronized LogNode appendOff(String info1)
	{
		LogNode node=new LogNode(info1,this);
		if(children==null)
		{
		    children=new LogNode[1];
		}
		else
		{
		    LogNode[] newChildren=new LogNode[children.length+1];
		    for(int i=0;i<children.length;i++)
		    {
			newChildren[i]=children[i];
		    }
		    children=newChildren;
		}
		this.children[children.length-1]=node;
		return node;
	}
	public void addChild(LogNode child)
	{
		if(children==null)
		{
		    children=new LogNode[1];
		}
		else
		{
		    LogNode[] newChildren=new LogNode[children.length+1];
		    for(int i=0;i<children.length;i++)
		    {
			newChildren[i]=children[i];
		    }
		    children=newChildren;
		}
		this.children[children.length-1]=child;
	}
	public  void setInfo2(String info2)
	{
	    this.info2=info2;
	}
	public LogNode getParent() 
	{
		return parent;
	}
	
	public String getInfo() 
	{
		return info1+"|"+info2;
	}
	public boolean hasChildren()
	{
		if(this.children!=null && this.children.length>0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public int getChildrenQuan()
	{
		if(this.children==null)
		{
		    return 0;
		}
		else
		{
		    return this.children.length;
		}
	    
	}
	public LogNode getChild(int index)
	{
	    if(children==null)
	    {
		return null;
	    }
	    else
	    {
		if(index>=children.length)
		{
		    return null;
		}
		else
		{
		    return children[index];
		}
	    }

	}
	

}

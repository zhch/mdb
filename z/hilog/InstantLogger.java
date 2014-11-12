package z.hilog;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import z.hilog.util.UtilSer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class InstantLogger extends AbstLogger
{
    private LogNode root;
    private XStream xStream;
    private String path; 
    public InstantLogger(LogNode root,String stablePath)
    {
	super(root);
	this.root=root;
	xStream = new XStream(new DomDriver()); 
	this.path=stablePath;

    }
    @Override
    public void logAlpha(String info1)
    {
	// TODO Auto-generated method stub
	this.curr=this.curr.appendOff(info1);
	try
	{

	    FileOutputStream fos= new FileOutputStream(path);  
	    OutputStreamWriter out= new OutputStreamWriter(fos, "utf-8");  
	    out.write(xStream.toXML(root));
	    out.flush();
	    fos.close();
	    out.close();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	}
    }

    @Override
    public boolean logOmega(String info2)
    {
	// TODO Auto-generated method stub
	this.curr.setInfo2(info2);	
	this.curr=this.curr.getParent();
	try
	{

	    FileOutputStream fos= new FileOutputStream(path);  
	    OutputStreamWriter out= new OutputStreamWriter(fos, "utf-8");  
	    out.write(xStream.toXML(root));
	    out.flush();
	    fos.close();
	    out.close();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	    return false;
	}
	return true;
    }


}

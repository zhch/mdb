package z.hilog.fast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.util.ArrayList;

import z.hilog.HtmlWeaver;
import z.hilog.LogNode;
import z.mdb.util.Utilities;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SlowHTMLWeaver
{
    private String dbURL="jdbc:mysql://127.0.0.1:3306/mdb";//?user=remote&password=rdzxrdzx";
    private static final String dbUsr="root";
    private static final String dbPswd="testtest";
    private String table="fastlog";
    private ComboPooledDataSource cpds;
    private String sql;
    private LogNode root;
    public SlowHTMLWeaver()
    {
	try
	{
	    cpds = new ComboPooledDataSource();
	    cpds.setDriverClass( "com.mysql.jdbc.Driver"); //loads the jdbc driver            
	    cpds.setJdbcUrl(dbURL+"?user="+dbUsr+"&password="+dbPswd);
	    cpds.setUser(dbUsr);                                  
	    cpds.setPassword(dbPswd); 
	    cpds.setTestConnectionOnCheckin(true);
	    cpds.setMaxIdleTime(25200);
	    //	    cpds.setIdleConnectionTestPeriod(100);
	    root=new LogNode("===ROOT===",null);
	    String id="0";
	    sql="select id from "+table+" where parent='"+id+"'";
	    SqlExecuter exe=executeQuery(sql);
	    ResultSet rs=exe.getRs();
	    if(rs.next())
	    {
		id=rs.getString("id");
	    }
	    else
	    {
		Utilities.printDebug("hilog slow html weaver can not find the root");
	    }
	    exe.release();
	    //initRecursively(id,root);
	    initWidelyRecursively(id,root);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
    private void initRecursively(String parentId,LogNode parent)
    {
	Utilities.printDebug("building children of: "+parentId);
	try
	{
	    LogNode node;
	    sql="select * from "+table+" where parent='"+parentId+"' order by iss_seq";
	    SqlExecuter exe=executeQuery(sql);
	    ResultSet rs=exe.getRs();
	    while(rs.next())
	    {
		node=new LogNode(rs.getString("info1"),parent);
		node.setInfo2(rs.getString("info2"));
		parent.addChild(node);
		initRecursively(rs.getString("id"),node);
	    }
	    exe.release();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
    private void initWidelyRecursively(String parentId,LogNode parent)
    {
	Utilities.printDebug("building children of: "+parentId);
	try
	{
	    LogNode node;
	    ArrayList<String> idList=new ArrayList<String>();
	    ArrayList<LogNode> nodeList=new ArrayList<LogNode>();
	    sql="select * from "+table+" where parent='"+parentId+"' order by iss_seq";
	    SqlExecuter exe=executeQuery(sql);
	    ResultSet rs=exe.getRs();
	    while(rs.next())
	    {
		node=new LogNode(rs.getString("info1"),parent);
		node.setInfo2(rs.getString("info2"));
		parent.addChild(node);
		nodeList.add(node);
		idList.add(rs.getString("id"));
	    }
	    exe.release();
	    while(idList.size()>0)
	    {
		initWidelyRecursively(idList.get(0),nodeList.get(0));
		idList.remove(0);
		nodeList.remove(0);
	    }
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
    private SqlExecuter executeQuery(String sql)
    {
	return SqlExecuter.newQueryExecuter(cpds,sql);
    }

    public static void main(String[] args)
    {
	try
	{
	    SlowHTMLWeaver html=new SlowHTMLWeaver();
	    HtmlWeaver weaver=new HtmlWeaver(html.root) ;
	    File file = new File("d:\\ll\\locktest.log"); 
	    FileOutputStream fos= new FileOutputStream(file.getAbsoluteFile()+".htm");  
	    OutputStreamWriter out= new OutputStreamWriter(fos, "utf-8");  
	    out.write(weaver.toHTML());
	    out.flush();
	    fos.close();
	    out.close();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
}

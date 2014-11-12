package z.hilog.fast;

import java.sql.ResultSet;

import z.hilog.AbstLogger;
import z.mdb.util.Utilities;

import com.mchange.v2.c3p0.ComboPooledDataSource;


public class FastInstantLogger extends AbstLogger
{
    //    private String dbURL="jdbc:mysql://115.236.94.220:3306/mdb";//?user=remote&password=rdzxrdzx";
    //    private static final String dbUsr="remote";
    //    private static final String dbPswd="rdzxrdzx";
    private String dbURL="jdbc:mysql://127.0.0.1:3306/mdb";//?user=remote&password=rdzxrdzx";
    private static final String dbUsr="root";
    private static final String dbPswd="testtest";
    private String table="fastlog";
    private ComboPooledDataSource cpds;
    private String sql;
    private String parent;
    private long logSeq;


    public FastInstantLogger()
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
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
	this.parent="000";
	this.logSeq=0;
    }
    private SqlExecuter executeQuery(String sql)
    {
	return SqlExecuter.newQueryExecuter(cpds,sql);
    }
    private SqlExecuter executeManipulate(String sql)
    {
	return SqlExecuter.newPolluteExecuter(cpds,sql);
    }
    private String insert(String info1,String info2)
    {
	String id=Utilities.get32CharUUID();
	if(this.parent.equals("000"))
	{
	    sql="INSERT INTO "+table+" VALUES ('"+id+"',"+System.currentTimeMillis()+",'"+info1+"','"+info2+"','"+this.parent+"')";
	}
	else
	{
	    logSeq++;
	    sql="INSERT INTO "+table+" VALUES ('"+id+"',"+logSeq+",'"+info1+"','"+info2+"','"+this.parent+"')";

	}
	SqlExecuter exe=executeManipulate(sql);
	exe.release();
	return id;
    }
    private void updateInfo2(String info2)
    {
	sql="UPDATE "+table+" SET info2 ='"+info2+"' WHERE id ='"+parent+"'";
	SqlExecuter exe=executeManipulate(sql);
	exe.release();
    }
    private String findParent()
    {
	String result=null;
	sql="SELECT parent FROM "+table+" WHERE id='"+parent+"'";
	try
	{
	    SqlExecuter exe=executeQuery(sql);
	    ResultSet rs=exe.getRs();
	    if(rs.next())
	    {
		result= rs.getString("parent");
	    }
	    exe.release();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
	return result;
    }
    public void logAlpha(String info1)
    {
	parent=insert(info1,"CAN NOT REACH THE ROUTINE END");
    }
    public boolean logOmega(String info2)
    {
	updateInfo2(info2);
	parent=findParent();
	if(parent!=null)
	{
	    return true;
	}
	else
	{
	    return false;
	}
    }
    public String newLogStub()
    {
	return Utilities.get32CharUUID();
    }
    public void initTable()
    {
	sql="INSERT INTO "+table+" VALUES ('000',"+System.currentTimeMillis()+",'ROOT','ROOT','0')";
	SqlExecuter exe=executeManipulate(sql);
	exe.release();

    }
}



























//public String getLeader(String yaid)
//{
//	sql="select db.name from rd_yajy ya ,rd_dblist db where ya.lxdbbh=db.id and ya.id='"+yaid+"'";
//	SqlExecuter exe=executeSql(sql);
//	String result;
//	try
//	{
//	    ResultSet rs=exe.getRs();
//	    if(rs.next())
//	    {
//		result= rs.getString("name");
//	    }
//	    else
//	    {
//		result=null;
//	    }
//	}
//	catch(Exception e)
//	{
//	    e.printStackTrace();
//	    result=null;
//	}
//	exe.release();
//	return result;
//}

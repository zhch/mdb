package z.hilog.fast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.mchange.v2.c3p0.ComboPooledDataSource;


public class SqlExecuter 
{
    /*
     * 1=查询session
     * 2=写session
     */
    private int cat;

    private ComboPooledDataSource cpds;
    private String sql;
    private Connection conn;
    private Statement stmt;
    private ResultSet rs;
    private int polluted;
    private SqlExecuter()
    {

    }
    public static SqlExecuter  newQueryExecuter(ComboPooledDataSource cpds, String sql) 
    {
	SqlExecuter exe=new SqlExecuter();
	exe.cpds = cpds;
	exe.sql = sql;
	exe.cat=1;
	try
	{
	    exe.conn = cpds.getConnection();
	    exe.stmt = exe.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
	    exe.rs=exe.stmt.executeQuery(sql);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
	return exe;
    }
    public static SqlExecuter newPolluteExecuter(ComboPooledDataSource cpds, String sql) 
    {
	SqlExecuter exe=new SqlExecuter();
	exe.cpds = cpds;
	exe.sql = sql;
	exe.cat=2;
	try
	{
	    exe.conn = cpds.getConnection();
	    exe.stmt = exe.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
	    exe.polluted=exe.stmt.executeUpdate(sql);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
	return exe;
    }
    public void release()
    {
	try
	{
	    if(cat==1)
	    {
		rs.close();
	    }
	    stmt.close();
	    conn.close();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
    public ResultSet getRs() 
    {
	if(cat==1)
	{
	    return rs;
	}
	else
	{
	    return null;
	}
    }







}

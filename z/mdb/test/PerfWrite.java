package z.mdb.test;

import java.util.Date;

import z.mdb.imm.IMManipulate;
import z.mdb.msg.Content;
import z.mdb.txn.Txn;
import z.mdb.util.Utilities;

public class PerfWrite extends Thread
{

	public static void main(String[] args)
	{
		String host="127.0.0.1";
		int port=143;
		String user="test3@mdb.test";
		String pswd="testtest";
		String folder="10";
		String[] names=Utilities.getNames(Integer.parseInt(folder),3);
		Date time1,time2=null;
		Txn txn=new IMManipulate(host,port,user,pswd,false).getTxn();
		txn.open(folder);
		time1=new Date();
		try
		{
			for(int i=0;i<names.length;i++)
			{
				txn.write(names[i],Content.parse("100"));
			}
			time2=new Date();
			Utilities.printDebug("ph1 elapsed:"+(time2.getTime()-time1.getTime()));
			txn.commit();
			time2=new Date();
			Utilities.printDebug("ph2 elapsed:"+(time2.getTime()-time1.getTime()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}


		try
		{

			txn=new IMManipulate(host,port,user,pswd,false).getTxn();
			txn.open(folder);
			int total=0;
			for(int i=0;i<names.length;i++)
			{
				total=total+Integer.parseInt(txn.read(names[i]).toString());
			}
			txn.commit();
			Utilities.printDebug("total="+total+",elapsed="+(time2.getTime()-time1.getTime()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}

	}
}

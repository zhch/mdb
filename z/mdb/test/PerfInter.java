package z.mdb.test;

import java.util.Date;

import z.hilog.fast.FastInstantLogger;
import z.mdb.excp.IMAPCmdNotOkException;
import z.mdb.excp.IMMCollapsedException;
import z.mdb.excp.TxnCommitFailed;
import z.mdb.excp.TxnValidateFailedExcp;
import z.mdb.imm.IMManipulate;
import z.mdb.msg.Content;
import z.mdb.txn.Txn;
import z.mdb.util.Utilities;

public class PerfInter extends Thread
{
	private String host;
	private int port;
	private String user;
	private String pswd;
	private int times;
	private String[] names;
	private String folder;
	private IMManipulate imm;
	private boolean[] isFinished;
	private int index;
	private Date time1,time2;
	int[][] froms,tos;

	public PerfInter(String host, int port, String user, String pswd,int times, String[] names, String folder,int[][]froms,int[][]tos,boolean[] isFinished,int index,Date time1,Date time2)
	{
		super();
		this.host = host;
		this.port = port;
		this.user = user;
		this.pswd = pswd;
		this.times = times;
		this.names = names;
		this.folder = folder;
		this.isFinished=isFinished;
		this.index=index;
		imm=new IMManipulate( host, port, user, pswd,false);
		imm.open();
		imm.select(folder);
		this.time1=time1;
		this.time2=time2;
		this.froms=froms;
		this.tos=tos;
	}
	public void run()
	{

		Txn txn=null;
		int from=froms[0][index];
		int to=tos[0][index];
		for(int i=0;i<times;)
		{
			String testOp=null;
			try
			{

				txn=imm.getTxn();

				int from0=Integer.parseInt(txn.read(names[from]).toString());

				int to0=Integer.parseInt(txn.read(names[to]).toString());
				int trans=(int)(Math.random()*from0);
				txn.write(names[from],Content.parse((from0-trans)+""));
				txn.write(names[to],Content.parse((to0+trans)+""));
				txn.commit();
				//		Utilities.printDebug(index+"#("+i+"):"+names[from]+"("+from0+")--"+trans+"-->"+names[to]+"("+to0+"):succ");
				i++;
				if(i<times)
				{
					from=froms[i][index];
					to=tos[i][index];
				}
			}
			catch(TxnValidateFailedExcp e)
			{
				//		Utilities.printDebug(index+"#("+i+"):"+names[from]+","+names[to]+":deadlocked, roll back");
				txn.rollback();
			}
			catch(IMMCollapsedException e2)
			{
				e2.printStackTrace();
				System.exit(0);
			}

			catch(IMAPCmdNotOkException e4)
			{
				e4.printStackTrace();
				System.exit(0);
			}
		}
		time2=new Date();
		isFinished[index]=true;
		Utilities.printDebug(index+" finished!!!");
		audit();
	}
	private void audit()
	{
		try
		{
			for(int i=0;i<isFinished.length;i++)
			{
				if(isFinished[i]==false)
				{
					return;
				}
			}

			Txn txn=imm.getTxn();
			txn.use(folder);
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
	public static void main(String[] args)
	{
		int player=1;
		int times=500;
		String host="127.0.0.1";
		int port=143;
		String user="test3@mdb.test";
		String pswd="testtest";
		String folder="inter";

		String[] names=Utilities.getNames(10,3);
		Date time1,time2=null;
		boolean[] isFinished=new boolean[player];
		int[][] froms=new int[times][player];
		int[][] tos=new int[times][player];
		for(int i=0;i<player;i++)
		{
			for(int j=0;j<times;j++)
			{
				int from=(int)(Math.random()*names.length);
				int to=(int)(Math.random()*names.length);
				while(to==from)
				{
					to=(int)(Math.random()*names.length);
				}
				froms[j][i]=from;
				tos[j][i]=to;
			}
		}
		////////////////////////////////////////////////////////////////////////////////
		Txn txn=new IMManipulate(host,port,user,pswd,false).getTxn();
		txn.open(folder);
		try
		{
			for(int i=0;i<names.length;i++)
			{
				//		Utilities.printDebug("writting"+i+": "+names[i]);
				txn.write(names[i],Content.parse("100"));
			}
			txn.commit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}

		//////////////////////////////////////////////////////////////	
		PerfInter[] inters =new PerfInter[player];
		time1=new Date();
		Utilities.printDebug(time1.toLocaleString());
		for(int i=0;i<player;i++)
		{
			isFinished[i]=false;
			inters[i]=new PerfInter(host, port, user, pswd,times, names, folder,froms,tos,isFinished,i,time1,time2);
			inters[i].start();
		}
	}
}

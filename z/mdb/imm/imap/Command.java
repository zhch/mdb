package z.mdb.imm.imap;

import java.util.ArrayList;
import java.util.StringTokenizer;

import z.mdb.util.Utilities;


/**
 * 封装imap cmd 的weave工作
 * 每个command可能对应多个命令行，用nextLine()方法依次读取
 * @author zhou
 *
 */
public class Command
{
	public static enum FetchTarget {SUB,TXT,SUB_N_TXT,DATE,SUB_N_DATE,FLGS }; 
	public static enum Flag {ANS,DEL}; 
	public static String  fetchToken(FetchTarget tar)
	{
		String tarDesc=null;
		if(tar.compareTo(FetchTarget.SUB)==0)
		{
			tarDesc="BODY[HEADER.FIELDS (SUBJECT)]";
		}
		else if(tar.compareTo(FetchTarget.TXT)==0)
		{
			tarDesc="BODY[TEXT]";
		}
		else if(tar.compareTo(FetchTarget.SUB_N_TXT)==0)
		{
			tarDesc="(BODY[HEADER.FIELDS (SUBJECT)] BODY[TEXT])";
		}
		else if(tar.compareTo(FetchTarget.DATE)==0)
		{
			tarDesc="INTERNALDATE";
		}
		else if(tar.compareTo(FetchTarget.SUB_N_DATE)==0)
		{
			tarDesc="(BODY[HEADER.FIELDS (SUBJECT)] INTERNALDATE)";
		}
		else if(tar.compareTo(FetchTarget.FLGS)==0)
		{
			tarDesc="FLAGS";
		}
		return tarDesc;
	}

	/*
	 * 指明命令类别,含义如下：
	 * 0=空命令
	 * 1=LOGIN
	 * 2=SELECT
	 * 3=CREATE
	 * 4=APPEND
	 * 5=MESSAGE(APPEND命令后续命令)
	 * 6=CLOSE
	 * 7=SEARCH
	 * 8=FETCH
	 * 9=STORE
	 * 10=NOOP
	 */
	private int cmd;

	private String tag;

	/*
	 * 当前command对应的命令行数目
	 */
	private int lineQuan;

	/*
	 * 当前issue的命令行的编号，当lineSeq=lineQuan时全部命令行issue完毕
	 */
	private int lineSeq=0;

	/*
	 * LOGIN 命令用
	 */
	private String user;
	private String pswd;

	/*
	 * SELECT,CREATE,APPEND 命令用
	 */
	private String folder;

	/*
	 * APPEND,MESSAGE 命令用
	 */
	private ArrayList<String> msg;

	/*
	 * SEARCH命令用
	 */
	private String subPattern;
	private int ansed;

	/*
	 * FETCH,STORE命令用
	 */
	private long[] seqs;

	/*
	 * FETCH命令用
	 */
	private FetchTarget tar;

	/*
	 * STORE命令用
	 */
	private Flag flag;

	private Command()
	{
		cmd=0;
	}

	public String getTag()
	{
		return tag;
	}
	private static Command init(int c,String tag,int lineQuan)
	{
		Command cmd=new Command();
		cmd.cmd=c;
		cmd.tag=tag;
		cmd.lineQuan=lineQuan;
		return cmd;
	}
	private static String genSeqList(long[] seqs)
	{

		String seqList="";
		for(int i=0;i<seqs.length-1;i++)
		{
			seqList=seqList+seqs[i]+",";
		}
		seqList=seqList+seqs[seqs.length-1];
		return seqList;

	}
	private String addTag(String cmdWithoutTag)
	{
		return tag+" "+cmdWithoutTag;
	}

	public static Command newLogin(String tag,String user,String pswd)
	{
		Command cmd=new Command();
		cmd.cmd=1;
		cmd.tag=tag;
		cmd.user=user;
		cmd.pswd=pswd;
		cmd.lineQuan=1;
		return cmd;
	}
	public static Command newSelect(String tag,String folder)
	{
		Command cmd=new Command();
		cmd.cmd=2;
		cmd.tag=tag;
		cmd.folder=folder;
		cmd.lineQuan=1;
		return cmd;
	}
	public static Command newCreate(String tag,String folder)
	{
		Command cmd=new Command();
		cmd.cmd=3;
		cmd.tag=tag;
		cmd.folder=folder;
		cmd.lineQuan=1;
		return cmd;
	}

	/**
	 * 一次Append命令生成两个Command对象,一个issue APPEND命令,一个issue msg内容
	 * @param tag1
	 * @param tag2
	 * @param folder
	 * @param subject
	 * @param text
	 * @return
	 */
	public static Command[] newAppend(String tag1,String tag2,String folder,String subject,String text)
	{
		int crlf;

		/*
		 * 生成命令行
		 */
		ArrayList<String> lines=new ArrayList<String>();
		lines.add("Message-ID: <"+Utilities.get32CharUUID()+".of.mdb>");
		lines.add("Subject: "+subject.toString());
		lines.add("MIME-Version: 1.0");
		lines.add("Content-Type: text/plain; charset=US-ASCII");
		lines.add("Content-Transfer-Encoding: 7bit");
		lines.add("");//空行

		/*
		 * 把text转换为命令行
		 */
		while((crlf=text.indexOf("\r\n"))!=-1)
		{
			lines.add(text.substring(0,crlf));
			text=text.substring(crlf+2);
		}
		lines.add(text);

		Command[] cmds=new Command[2];
		Command cmd1=init(4,tag1,1);
		cmd1.folder=folder;
		cmd1.msg=lines;
		cmds[0]=cmd1;

		/*
		 * 命令行的行数就等于刚生成lines的行数
		 */
		Command cmd2=init(5,tag1,lines.size());

		cmd2.msg=lines;

		/*
		 * 返回一个Command数组
		 */
		cmds[0]=cmd1;
		cmds[1]=cmd2;
		return cmds;


	}
	public static Command newClose(String tag)
	{
		return init(6,tag,1);
	}
	public static Command newSearch(String tag,String subPattern,int ansed)
	{
		Command cmd=init(7,tag,1);
		cmd.subPattern=subPattern;
		cmd.ansed=ansed;
		return cmd;
	}
	public static Command newFetch(String tag,long[] seqs,FetchTarget tar)
	{
		if(seqs!=null && seqs.length>0)
		{
			Command cmd=init(8,tag,1);
			cmd.seqs=seqs;
			cmd.tar=tar;	
			return cmd;
		}
		else
		{
			return null;
		}
	}
	public static Command newStore(String tag,Flag flag,long[] seqs )
	{
		if(seqs!=null && seqs.length>0)
		{
			Command cmd=init(9,tag,1);
			cmd.flag=flag;
			cmd.seqs=seqs;
			return cmd;
		}
		else
		{
			return null;
		}
	}
	public static Command newNoop(String tag)
	{
		Command cmd=init(10,tag,1);
		return cmd;
	}
	public boolean hasMoreLines()
	{
		if(lineSeq<lineQuan)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public String nextLine()
	{
		if(lineSeq<lineQuan)
		{
			lineSeq++;
			if(cmd==0)
			{
				return "";
			}
			else if(cmd==1)
			{
				return this.tag+" LOGIN "+this.user+" "+this.pswd;
			}
			else if(cmd==2)
			{
				return this.tag+" SELECT "+folder;
			}
			else if(cmd==3)
			{
				return this.tag+" CREATE "+folder;
			}
			else if(cmd==4)
			{
				int len=0;
				for(int i=0;i<msg.size()-1;i++)
				{
					len=len+msg.get(i).length()+2;
				}
				len=len+msg.get(msg.size()-1).length();
				return tag+" APPEND "+folder+" () {"+len+"}";
			}
			else if(cmd==5)
			{
				return msg.get(lineSeq-1);
			}
			else if(cmd==6)
			{
				return addTag("CLOSE");
			}
			else if(cmd==7)
			{
				String ans;
				if(this.ansed==1)
				{
					ans="ANSWERED";
				}
				else if(this.ansed==0)
				{
					ans="UNANSWERED";
				}
				else
				{
					ans="";
				}
				return addTag("SEARCH UNDELETED "+ans+" SUBJECT \""+subPattern+"\"");
			}
			else if(cmd==8)
			{
				String tarDesc=null;
				String seqList="";
				tarDesc=Command.fetchToken(this.tar);
				seqList=genSeqList(this.seqs);

				return addTag("FETCH "+seqList+" "+tarDesc);


			}
			else if(cmd==9)
			{
				String flagStr="";
				if(flag.compareTo(Command.Flag.DEL)==0)
				{
					flagStr="\\Deleted";
				}
				else if(flag.compareTo(Command.Flag.ANS)==0)
				{
					flagStr="\\Answered";
				}
				return addTag("STORE "+Command.genSeqList(seqs)+" +FLAGS ("+flagStr+")");
			}
			else if(cmd==10)
			{
				return addTag("NOOP");
			}

			return null;
		}
		else
		{
			return null;
		}
	}
}

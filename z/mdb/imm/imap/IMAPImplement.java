package z.mdb.imm.imap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


import z.hilog.AbstLogger;
import z.hilog.InstantLogger;
import z.hilog.LogNode;
import z.mdb.excp.IMAPCmdNotOkException;
import z.mdb.util.Utilities;



/**
 * 实现imap协议用到的部分
 * knows:
 * 	1.邮件由subject、text和ANS组成
 * 	2.msgseq
 * 	3.cmd tag及其维护
 * 	3.用subject pattern搜索
 * 	4.folder
 * don't know:
 * 	1.具体的imap cmd的编码和imap response的解码，分别代由Command类和Response类完成
 * @author zhou
 *
 */
public class IMAPImplement
{
    private String host;
    private int port;
    private String user;
    private String pswd;
    private boolean useSSL;
    private String folder;

    private boolean doLog;//是否开启debug log
    private boolean opened;//是否已打开imap连接

    private Socket socket1;//不使用ssl时的socket
    private SSLSocket socket2;//使用ssl时的socket
    private BufferedReader in;
    private PrintWriter out;
    private AbstLogger logger;

    private long tagSeq;


    public String getHost()
    {
	return host;
    }
    public int getPort()
    {
	return port;
    }
    public String getUser()
    {
	return user;
    }
    public String getPswd()
    {
	return pswd;
    }
    public boolean isUseSSL()
    {
	return useSSL;
    }
    public String getFolder()
    {
	return folder;
    }
    /**
     * 不会真正打开连接
     * @param host
     * @param port
     * @param user
     * @param pswd
     * @param useSSL
     */
    public IMAPImplement(String host, int port, String user, String pswd, boolean useSSL)
    {
	super();
	this.host = host;
	this.port = port;
	this.user = user;
	this.pswd = pswd;
	this.useSSL = useSSL;
	doLog=false;
	tagSeq=1;
	opened=false;
    }
    public IMAPImplement(String host, int port, String user, String pswd, boolean useSSL,AbstLogger logger)
    {
	this(host, port, user, pswd, useSSL);
	this.doLog=true;
	this.logger=logger;
    }
    public IMAPImplement(String host, int port, String user, String pswd, boolean useSSL,AbstLogger logger,int logDepth)
    {
	this(host, port, user, pswd, useSSL);
	if(logDepth>=0)
	{
	    this.doLog=true;
	    this.logger=logger;
	}
	else
	{
	    this.doLog=false;
	}
    }
    private String log0(String info1)
    {
	if(doLog)
	{
	    return logger.routineStart(info1);
	}
	else
	{
	    return null;
	}
    }
    private boolean logt(String stub,String info2)
    {
	if(doLog)
	{
	    return logger.routineFinished(stub,info2);
	}
	else
	{
	    return false;
	}
    }
    private boolean log(String info)
    {
	if(doLog)
	{
	    return logger.routineFinished(logger.routineStart(info),"ROUTINE END");
	}
	else
	{
	    return false;
	}
    }

    public boolean isOpened()
    {
	return opened;
    }



    /**
     * 创建imap连接,在成功连接前将会一直尝试连接
     * @return
     */
    public boolean open()
    {
	while(this.opened==false)//open不成功将会一直尝试
	{
	    try
	    {
		/*
		 * 创建socket
		 */
		if(useSSL==true)
		{
		    SSLSocketFactory f =  (SSLSocketFactory) SSLSocketFactory.getDefault();
		    socket2=(SSLSocket) f.createSocket(host,port);
		    socket2.startHandshake();
		    in= new BufferedReader(new InputStreamReader(socket2.getInputStream()));
		    out = new PrintWriter(socket2.getOutputStream(), true);

		}
		else
		{
		    socket1=new Socket(host,port);
		    in = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
		    out = new PrintWriter(socket1.getOutputStream(), true);
		}

		/*
		 * 调用LOGIN命令
		 */
		Response resp=this.sendCmd(Command.newLogin(nextTag(), user, pswd));
		if(resp.getResult().compareTo(Response.Result.OK)==0)
		{
		    /*
		     * 调用SELECT命令 
		     */
		    resp=this.sendCmd(Command.newSelect(nextTag(),"INBOX"));
		    if(resp.getResult().compareTo(Response.Result.OK)==0)
		    {
			this.folder="INBOX";
			this.opened=true;			
		    }
		    else
		    {
			this.opened=false;
		    }
		}
		else
		{
		    this.opened=false;
		}

	    }
	    catch(Exception e)
	    {
		e.printStackTrace();
	    }
	}
	return this.opened;
    }
    /**
     * 选择进入一个文件夹
     * @param folder
     * @return
     */
    public boolean select(String folder)
    {
	Response resp=sendCmd(Command.newSelect(nextTag(), folder));
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    this.folder=folder;
	    return true;
	}
	else
	{
	    return false;
	}

    }
    public boolean create(String folder)
    {

	Response resp=sendCmd(Command.newCreate(nextTag(), folder));
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    return true;
	}
	else
	{
	    return false;
	}
    }

    public boolean append(String subject,String text)
    {
	Command[] cmds=Command.newAppend(nextTag(),nextTag(),folder, subject, text);
	Response resp=sendCmd(cmds[0]);
	if(resp.getResult().compareTo(Response.Result.PLUS)==0)
	{
	    resp=sendCmd(cmds[1]);
	    if(resp.getResult().compareTo(Response.Result.OK)==0)
	    {
		return true;
	    }
	    else
	    {
		return false;
	    }
	}
	else
	{
	    return false;
	}
    }

    /**
     * 搜索指定条件邮件的数目
     * @param subPattern
     * @param ansed
     * @return -1:出错,非负数：邮件数目
     */
    public int searchQuan(String subPattern,int ansed)
    {
	//	this.reSelect();
	Command cmd=Command.newSearch(nextTag(), subPattern, ansed);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    return resp.parseSearch().length;
	}
	else
	{
	    return -1;
	}

    }
    public long[] searchSeqs(String subPattern,int ansed)throws IMAPCmdNotOkException
    {
	Command cmd=Command.newSearch(nextTag(), subPattern, ansed);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    return resp.parseSearch();
	}
	else
	{
	    throw new IMAPCmdNotOkException(resp.getResult().toString());
	}
    }
    public boolean[] checkIsDeleted(long[] seqs)
    {
	boolean[] ret=new boolean[seqs.length];
	Command cmd=Command.newFetch(nextTag(), seqs,Command.FetchTarget.FLGS);
	Response resp=sendCmd(cmd);
	String[] flags=resp.parseFetch(Command.FetchTarget.FLGS, seqs);		
	for(int i=0;i<flags.length;i++)
	{
	    //			Utilities.printDebug("i="+i+",seq="+seqs[i]+",f="+flags[i]);
	    if(flags[i].indexOf("Deleted")!=-1)
	    {
		ret[i]=true;
	    }
	    else
	    {
		ret[i]=false;
	    }

	}
	return ret;

    }

    /**
     * 按条件搜索邮件，并返回这些邮件的subject
     * @param subPattern
     * @param ansed
     * @return
     */
    public String[] searchSubs(String subPattern,int ansed)
    {
	Command cmd=Command.newSearch(nextTag(), subPattern, ansed);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    long[] seqs=resp.parseSearch();
	    if(seqs!=null && seqs.length>0)
	    {
		cmd=Command.newFetch(nextTag(),seqs,Command.FetchTarget.SUB);
		resp=sendCmd(cmd);
		if(resp.getResult().compareTo(Response.Result.OK)==0)
		{
		    return resp.parseFetch(Command.FetchTarget.SUB,seqs);
		}
		else
		{
		    return null;
		}
	    }
	    else
	    {
		return null;
	    }
	}
	else
	{
	    return null;
	}
    }

    /**
     * 按条件搜索邮件，并返回这些邮件的txt
     * @param subPattern
     * @param ansed
     * @return
     */
    public String[] searchTexts(String subPattern,int ansed)
    {
	Command cmd=Command.newSearch(nextTag(), subPattern, ansed);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    long[] seqs=resp.parseSearch();
	    if(seqs!=null && seqs.length>0)
	    {
		cmd=Command.newFetch(nextTag(),seqs,Command.FetchTarget.TXT);
		resp=sendCmd(cmd);
		if(resp.getResult().compareTo(Response.Result.OK)==0)
		{
		    return resp.parseFetch(Command.FetchTarget.TXT,seqs);
		}
		else
		{
		    return null;
		}
	    }
	    else
	    {
		return null;
	    }
	}
	else
	{
	    return null;
	}
    }
    public String[] searchTexts(long[] seqs)
    {
	Command cmd=Command.newFetch(nextTag(),seqs,Command.FetchTarget.TXT);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    return resp.parseFetch(Command.FetchTarget.TXT,seqs);
	}
	else
	{
	    return null;
	}
    }


    /**
     * 按条件搜索邮件，并返回这些邮件的subject和txt
     * 
     * @param subPattern
     * @param ansed
     * @return index为偶数的元素都是subject,index为奇数的元素都是txt
     */
    public String[] searchSubNTxts(String subPattern,int ansed)throws IMAPCmdNotOkException
    {
	Command cmd=Command.newSearch(nextTag(), subPattern, ansed);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    long[] seqs=resp.parseSearch();
	    if(seqs!=null && seqs.length>0)
	    {
		cmd=Command.newFetch(nextTag(),seqs,Command.FetchTarget.SUB_N_TXT);
		resp=sendCmd(cmd);
		if(resp.getResult().compareTo(Response.Result.OK)==0)
		{
		    return resp.parseFetch(Command.FetchTarget.SUB_N_TXT,seqs);
		}
		else
		{
		    throw new IMAPCmdNotOkException(resp.getResult().toString());
		}
	    }
	    else
	    {
		return new String[0];
	    }
	}
	else
	{
	    throw new IMAPCmdNotOkException(resp.getResult().toString());

	}
    }

    public String[] searchSubNTxts(long[] seqs)throws IMAPCmdNotOkException
    {
	Command cmd=Command.newFetch(nextTag(),seqs,Command.FetchTarget.SUB_N_TXT);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    return resp.parseFetch(Command.FetchTarget.SUB_N_TXT,seqs);
	}
	else
	{
	    throw new IMAPCmdNotOkException(resp.getResult().toString());
	}
    }

    public long[] searchBeings(String subPattern,int ansed)throws IMAPCmdNotOkException
    {
	Command cmd=Command.newSearch(nextTag(), subPattern, ansed);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    long[] seqs=resp.parseSearch();
	    if(seqs!=null && seqs.length>0)
	    {
		cmd=Command.newFetch(nextTag(),seqs,Command.FetchTarget.DATE);
		resp=sendCmd(cmd);
		if(resp.getResult().compareTo(Response.Result.OK)==0)
		{
		    String[] dates=resp.parseFetch(Command.FetchTarget.DATE, seqs);
		    if(dates!=null && dates.length>0)
		    {
			long[] mills=new long[dates.length];
			for(int i=0;i<dates.length;i++)
			{
			    mills[i]=Long.parseLong(dates[i]);
			}
			return mills;
		    }
		    else
		    {
			return null;
		    }
		}
		else
		{
		    throw new IMAPCmdNotOkException(resp.getResult().toString());
		}
	    }
	    else
	    {
		return null;
	    }
	}
	else
	{
	    throw new IMAPCmdNotOkException(resp.getResult().toString());
	}
    }
    public String[] searchSubNBeings(String subPattern,int ansed)throws IMAPCmdNotOkException
    {
	Command cmd=Command.newSearch(nextTag(), subPattern, ansed);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    long[] seqs=resp.parseSearch();
	    if(seqs!=null && seqs.length>0)
	    {
		cmd=Command.newFetch(nextTag(),seqs,Command.FetchTarget.SUB_N_DATE);
		resp=sendCmd(cmd);
		if(resp.getResult().compareTo(Response.Result.OK)==0)
		{
		    String[] retval=resp.parseFetch(Command.FetchTarget.SUB_N_DATE, seqs);
		    return retval;
		}
		else
		{
		    throw new IMAPCmdNotOkException(resp.getResult().toString());
		}
	    }
	    else
	    {
		return null;
	    }

	}
	else
	{
	    throw new IMAPCmdNotOkException(resp.getResult().toString());
	}
    }
    /**
     * 为符合条件的邮件flag deleted
     * 按照subject pattern进行操作，准确的操作某封邮件依赖上层逻辑
     * @param subPattern
     * @param ansed
     * @return	正常操作返回受影响的邮件数
     * 		出错返回-1
     */
    public boolean flagDeled(String subject,int ansed)
    {
	Command cmd=Command.newSearch(nextTag(), subject, ansed);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    long[] seqs=resp.parseSearch();
	    if(seqs!=null && seqs.length>0)
	    {
		cmd=Command.newStore(nextTag(),Command.Flag.DEL,resp.parseSearch());
		resp=sendCmd(cmd);
		if(resp.getResult().compareTo(Response.Result.OK)==0)
		{
		    return true;
		}
		else
		{
		    return false;
		}
	    }
	    else
	    {
		return false;
	    }
	}
	else
	{
	    return false;
	}
    }
    public boolean flagDeled(long[] seqs)
    {
	Command cmd=Command.newStore(nextTag(),Command.Flag.DEL,seqs);
	Response resp=sendCmd(cmd);
	if(resp.getResult().compareTo(Response.Result.OK)==0)
	{
	    return true;
	}
	else
	{
	    return false;
	}
    }
    /**
     * 为符合条件的邮件flag ansed
     * 按照subject pattern进行操作，准确的操作某封邮件依赖上层逻辑     
     * @param subPattern
     * @return	正常操作返回受影响的邮件数
     * 		出错返回-1
     */
    public boolean flagAnsed(String[] subjects)
    {

	Command cmd;
	Response resp;
	ArrayList<Long> seqList=new ArrayList<Long>();
	long[] seqs;

	for(int i=0;i<subjects.length;i++)
	{
	    cmd=Command.newSearch(nextTag(),subjects[i],0);
	    resp=sendCmd(cmd);
	    if(resp.getResult().equals(Response.Result.OK))
	    {
		seqs=resp.parseSearch();
		for(int j=0;j<seqs.length;j++)
		{
		    seqList.add(seqs[j]);
		}
	    }
	    else
	    {
		return false;
	    }
	}
	if(seqList.size()>0)
	{
	    seqs=new long[seqList.size()];
	    for(int i=0;i<seqList.size();i++)
	    {
		seqs[i]=seqList.get(i).longValue();
	    }

	    cmd=Command.newStore(nextTag(),Command.Flag.ANS,seqs);
	    resp=sendCmd(cmd);
	    if(resp.getResult().compareTo(Response.Result.OK)==0)
	    {
		return true;
	    }
	    else
	    {
		return false;
	    }
	}
	else
	{
	    return false;
	}


    }

    /**
     * 申请一个命令tag，每次 issue 命令前都需要申请
     * @return 一个合法的tag
     */
    private String nextTag()
    {
	tagSeq++;
	return "#"+tagSeq;
    }    
    /**
     * issue一条imap命令
     * @param c
     * @return
     */
    private Response sendCmd(Command c)
    {
	//		Utilities.printDebug(c.toString());
	while(c.hasMoreLines())
	{
	    String cmd=c.nextLine();
	    out.println(cmd);
	    //				    	    Utilities.printDebug("*imapout*"+cmd);
	    String logIMAPOut=log0("*imapout*");
	    logt(logIMAPOut,cmd);
	}
	ArrayList<String> lines=new ArrayList<String>() ;
	String line,token;
	Response.Result result;
	String tag=c.getTag();
	int s1,s2;
	while(true)
	{
	    /*
	     * 读取一行response
	     */
	    try
	    {
		line=in.readLine();
		//								Utilities.printDebug("*imap in*"+line);
		String logIMAPIn=log0("*imap in*");
		logt(logIMAPIn,line);

	    }
	    catch(IOException e)
	    {
		e.printStackTrace();
		result=Response.Result.ERR;
		break;
	    }

	    /*
	     * 说明连接断开
	     */
	    if(line==null)
	    {
		result=Response.Result.DISCONN;
		break;
	    }

	    /*
	     * 读到一个空行
	     */
	    if(line.length()==0)
	    {
		lines.add("");
	    }

	    /*
	     * 读到一个plus response,说明服务器在等待下一步命令
	     */
	    else if(line.charAt(0)=='+')
	    {
		result=Response.Result.PLUS;
		break ;
	    }

	    /*
	     * 找到服务器 completion result 行
	     */
	    else if(line.length()>tag.length() && line.substring(0, tag.length()).equals(tag))
	    {
		s1=line.indexOf(" ");
		s2=line.indexOf(" ",s1+1);
		token=line.substring(s1+1,s2);
		if(token.equals("OK"))
		{
		    result=Response.Result.OK;
		}
		else if(token.equals("NO"))
		{
		    result=Response.Result.NO;
		}
		else
		{
		    result=Response.Result.BAD;
		}
		break ;

	    }

	    /*
	     * 一个普通行，保存下来
	     */
	    else
	    {
		lines.add(line);
	    }
	}

	return new Response(result, tag,lines);

    }


    public static void main(String[] args)
    {
	IMAPImplement imap=new IMAPImplement("127.0.0.1",143,"test3@mdb.test", "testtest",false);
	imap.open();
	imap.create("subbeingtest");
	imap.select("subbeingtest");
	imap.append("aaaaaaaa","testtesttest");

	long[] seqs=new long[3];
	seqs[0]=1;
	seqs[1]=2;
	seqs[2]=3;
	try
	{
	    imap.searchSubNTxts("aaaaaaaa",-1);
	    boolean[] res=imap.checkIsDeleted(seqs);
	    for(int i=0;i<res.length;i++)
	    {
		//				Utilities.printDebug(res[i]+"");
	    }

	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
}

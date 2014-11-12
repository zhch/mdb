package z.mdb.imm.imap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

import z.mdb.util.Utilities;

/**
 * 包含一次服务器response的内容
 * @author zhou
 *
 */
public class Response
{
	/*
	 * ERR:出错
	 * DISCONN:连接断开
	 * PLUS:服务器等待下一步命令
	 * OK:successful completion of the associated command
	 * NO:indicates an operational error message from the server
	 * BAD:reports a protocol-level error in the client's command
	 */
	public static enum Result {ERR,DISCONN,PLUS,OK,NO,BAD }; 
	private Result result;

	private String tag;

	/*
	 * server response data
	 */
	private ArrayList<String> lines;


	public Response(Result result, String tag,ArrayList<String> lines)
	{
		super();
		this.result = result;
		this.tag = tag;
		this.lines=lines;
	}
	public Result getResult()
	{
		return result;
	}
	public void setResult(Result result)
	{
		this.result = result;
	}
	public String getTag()
	{
		return tag;
	}
	/**
	 * parse search命令的结果
	 * @return msgseq组成的long数组
	 */
	public long[] parseSearch()
	{
		ArrayList<Long> seqs=new ArrayList<Long>();
		String line;
		Long seq;
		int s;
		/*
		 * 遍历每一个response行
		 */
		for(int i=0;i<lines.size();i++)
		{
			line=lines.get(i);
			/*
			 * 包含SEARCH标记的行符合要求
			 */
			if((s=line.indexOf("* SEARCH"))!=-1)
			{
				line=line.substring(s+8);//去除行首SEARCH标记,剩余以空格分隔的msg seq list

				/*
				 * parse以空格分隔的msg seq list
				 */
				StringTokenizer st=new StringTokenizer(line," ");
				if(st.countTokens()>0)
				{
					while(st.hasMoreTokens())
					{
						seq=Long.parseLong(st.nextToken());
						seqs.add(seq);
					}
				}

			}
		}

		/*
		 * 将结果转换为数组返回
		 */
		long[]ret=new long[seqs.size()];
		for(int i=0;i<seqs.size();i++)
		{
			ret[i]=seqs.get(i).longValue();
		}
		return ret;
	}

	/**
	 * parse literal 字符串
	 * @param beginLn
	 * @param length
	 * @return
	 */
	private String parseLiteral(int beginLn,int length)
	{
		String line=lines.get(beginLn);
		int len=line.length()+2;
		int index=1;
		while(len<length)
		{
			line=line+lines.get(beginLn+index);
			len=len+lines.get(beginLn+index).length()+2;
			index++;
		}
		return line;
	}
	/**
	 * 当单独fetch sub或text时由parseFetch方法调用,相应的parse sub或text
	 * @param tar
	 * @param lineIndex
	 * @return
	 */
	private String parseSubOrTxtFetch(Command.FetchTarget tar,int lineIndex)
	{

		String token=Command.fetchToken(tar);
		int index,index2;
		String line=null;
		String ret=null;

		/*
		 * 检查要求parse的lineIndex是否合法
		 */
		if(lineIndex>=lines.size() || lineIndex<0)
		{
			return null;
		}
		else
		{
			line=lines.get(lineIndex);
		}
		/*
		 * 检查是否有fetch token的标记
		 */
		if((index=line.indexOf(token))!=-1)
		{
			line=line.substring(index+token.length()+1);
			/*
			 * 服务器以literal string的形式返回fetch结果
			 */
			if(line.charAt(0)=='{')
			{
				index=line.indexOf("{");
				index2=line.indexOf("}");
				ret= parseLiteral(lineIndex+1,Integer.parseInt(line.substring(index+1,index2)));
			}
			/*
			 * 服务器以quoted string的形式返回fetch结果
			 */
			else if(line.charAt(0)=='"')
			{
				index=line.indexOf('"');
				index2=line.indexOf('"',index+1);
				ret= line.substring(index+1,index2);
			}

		}

		/*
		 * 当fetch subject时要去掉返回行头部的Subject标志
		 */
		if(ret==null)
		{
			Utilities.printDebug("line without fetch:"+line);
		}
		String subToken="SUBJECT: ";
		if(ret.length()==subToken.length())
		{
			return ret="";
		}
		else if(ret.length()>subToken.length())
		{
			ret=ret.substring(subToken.length());
		}

		return ret;
	}



	private String parseInternalDate(int lineIndex)
	{
		/*
		 * 检查要求parse的lineIndex是否合法
		 */
		String line;
		int index,index2;
		String token;
		String ret=null;
		if(lineIndex>=lines.size() || lineIndex<0)
		{
			return null;
		}
		else
		{
			line=lines.get(lineIndex);
			token=Command.fetchToken(Command.FetchTarget.DATE);
			/*
			 * 检查是否有fetch token的标记
			 */
			if((index=line.indexOf(token))!=-1)
			{
				line=line.substring(index+token.length()+1);
				/*
				 * 服务器以literal string的形式返回fetch结果
				 */
				if(line.charAt(0)=='{')
				{
					Utilities.printDebug("literal");
					index=line.indexOf("{");
					index2=line.indexOf("}");
					ret= parseLiteral(lineIndex+1,Integer.parseInt(line.substring(index+1,index2)));
				}
				/*
				 * 服务器以quoted string的形式返回fetch结果
				 */
				else if(line.charAt(0)=='"')
				{
					index=line.indexOf('"');
					index2=line.indexOf('"',index+1);
					ret= line.substring(index+1,index2);
				}
			}
			StringTokenizer st=new StringTokenizer(ret," ");
			String date=st.nextToken();
			String time=st.nextToken();
			String zone=st.nextToken(); 
			TimeZone timeZone=TimeZone.getTimeZone("GMT"+zone);
			st=new StringTokenizer(date,"-");
			int day=Integer.valueOf(st.nextToken());
			int month=Utilities.month2Number(st.nextToken());
			int year=Integer.valueOf(st.nextToken()); 
			st=new StringTokenizer(time,":");  
			int hour=Integer.valueOf(st.nextToken()); 
			int min=Integer.valueOf(st.nextToken()); 
			int sec=Integer.valueOf(st.nextToken());
			//	    Utilities.printDebug("year="+year+",month="+month+",day="+day+",hour="+hour+",min="+min+",sec="+sec);
			Calendar cal=Calendar.getInstance(timeZone);
			cal.set(year,month, day, hour, min,sec);
			//	    Utilities.printDebug(cal.getTime().getTime()+"");
			return String.valueOf( (cal.getTime().getTime()/1000));
		}
	}
	/**
	 * 当fetch命令需要同时fetch subject和txt时由parseFetch()方法调用
	 * 在找到相应的fetch token之后还是调用parseSubOrTxtFetch()方法进行进一步parse
	 * @param lineIndex
	 * @return 
	 * 		找到并正常parse返回:长度为2的String数组，第一个元素是subject，第二个元素是txt
	 * 		找不到或parse出错返回:null
	 */
	private String[] parseSubAndTxtFetch(int lineIndex)
	{
		String[] ret=new String[2];
		/*
		 * 整个方法做两遍，第一遍parse subject,第二遍parse txt
		 */
		for(int i=0;i<2;i++)
		{
			boolean found;
			String line="";

			Command.FetchTarget tar;

			/*
			 * 第一遍parse subject,第二遍parse txt
			 */
			if(i==0)
			{
				tar=Command.FetchTarget.SUB;
			}
			else
			{
				tar=Command.FetchTarget.TXT;
			}

			/*
			 * 查找出现fetch token的行
			 */
			found=false;
			int step=0;
			for(step=0;lineIndex+step<lines.size();step++)
			{
				line=lines.get(lineIndex+step);
				if(line.indexOf(Command.fetchToken(tar))!=-1)
				{
					found=true;
					break;
				}
			}

			/*
			 * 若找到调用parseSubOrTxtFetch()方法进一步parse
			 * 若找不到则直接返回null
			 */
			if(found==true)
			{
				ret[i]=parseSubOrTxtFetch(tar,lineIndex+step);
				if(ret[i]==null)
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
		return ret;
	}
	private String[] parseSubAndIntdate(int lineIndex)
	{
		String[] ret=new String[2];
		for(int i=0;i<2;i++)
		{
			boolean found;
			String line="";

			Command.FetchTarget tar;

			/*
			 * 第一遍parse subject,第二遍parse date
			 */
			if(i==0)
			{
				tar=Command.FetchTarget.SUB;
			}
			else
			{
				tar=Command.FetchTarget.DATE;
			}
			/*
			 * 查找出现fetch token的行
			 */
			found=false;
			int step=0;
			for(step=0;lineIndex+step<lines.size();step++)
			{
				line=lines.get(lineIndex+step);
				if(line.indexOf(Command.fetchToken(tar))!=-1)
				{
					found=true;
					break;
				}
			}
			if(found==true)
			{
				if(i==0)
				{
					ret[i]=parseSubOrTxtFetch(Command.FetchTarget.SUB,lineIndex+step);
				}
				else
				{
					ret[i]=this.parseInternalDate(lineIndex+step);
				}

				if(ret[i]==null)
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
		return ret;
	}
	private String parseFlags(int lineIndex)
	{
		String ret="";
		String line=lines.get(lineIndex);
		if(line.indexOf("\\Deleted")!=-1)
		{
			ret="Deleted";
		}
		if(line.indexOf("\\Answered")!=-1)
		{
			ret=ret+"|Answered";
		}
		return ret;
	}
	/**
	 * parse fetch命令服务器的response数据，
	 * 每找出一个fetch响应行后：
	 * 		当单独fetch sub或txt时调用parseSubOrTxtFetch进行进一步parse
	 * 		当同时fetch sub和 txt时调用parseSubAndTxtFetch进行进一步parse
	 * @param tar
	 * @return
	 */
	public String[] parseFetch(Command.FetchTarget tar,long[] seqs)
	{
		String line;
		String[] line2=new String[2];
		String[] subs;
		long seq;
		int index;
		boolean check;
		if(tar.compareTo(Command.FetchTarget.SUB_N_TXT)==0 || tar.compareTo(Command.FetchTarget.SUB_N_DATE)==0)
		{
			subs=new String[seqs.length*2]; 
		}
		else
		{
			subs=new String[seqs.length]; 
		}

		/*
		 * 遍历每一个response行
		 */
		for(int i=0;i<lines.size();i++)
		{

			line=lines.get(i);
			check=false;

			if(tar.compareTo(Command.FetchTarget.SUB_N_TXT)==0)
			{
				if(line.indexOf("FETCH")!=-1 && (line.indexOf(Command.fetchToken(Command.FetchTarget.SUB))!=-1 || line.indexOf(Command.fetchToken(Command.FetchTarget.TXT))!=-1))
				{
					check=true;
				}
			}
			else if(tar.compareTo(Command.FetchTarget.SUB_N_DATE)==0)
			{
				if(line.indexOf("FETCH")!=-1 && (line.indexOf(Command.fetchToken(Command.FetchTarget.SUB))!=-1 || line.indexOf(Command.fetchToken(Command.FetchTarget.DATE))!=-1))
				{
					check=true;
				}
			}
			else if(line.indexOf("FETCH")!=-1 && line.indexOf(Command.fetchToken(tar))!=-1)
			{
				check=true;
			}

			if(check)
			{
				seq=Long.valueOf(line.substring(2,line.indexOf("FETCH")-1));
				index=-1;
				for(int j=0;j<seqs.length;j++)
				{
					if(seqs[j]==seq)
					{
						index=j;
						break;
					}
				}
				if(index!=-1)
				{
					if(tar.compareTo(Command.FetchTarget.SUB)==0 || tar.compareTo(Command.FetchTarget.TXT)==0)
					{
						if((line=parseSubOrTxtFetch(tar,i))!=null)
						{
							subs[index]=line;
						}
					}
					else if(tar.compareTo(Command.FetchTarget.DATE)==0)
					{
						if((line=this.parseInternalDate(i))!=null)
						{
							subs[index]=line;
						}
					}
					else if(tar.compareTo(Command.FetchTarget.SUB_N_TXT)==0)
					{
						if((line2=this.parseSubAndTxtFetch(i))!=null)
						{
							subs[2*index]=line2[0];
							subs[2*index+1]=line2[1];
						}
					}
					else if(tar.compareTo(Command.FetchTarget.SUB_N_DATE)==0)
					{
						if((line2=this.parseSubAndIntdate(i))!=null)
						{
							subs[2*index]=line2[0];
							subs[2*index+1]=line2[1];
						}
					}
					else if(tar.compareTo(Command.FetchTarget.FLGS)==0)
					{
						if(((line=this.parseFlags(i))!=null))
						{
							subs[index]=line;
						}
						else
						{
							subs[index]="";
						}
					}

				}

			}
		}
		return subs;
	}

}

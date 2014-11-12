package z.mdb.msg;


public class Content
{
    /*
     * text的行长度,imap协议规定一行不能超过一定的长度
     */
    private static int LINE_WIDTH=990;
    
    private String content;

    public Content(String content)
    {
	super();
	this.content = content;
    }

    /**
     * 将content编码为text
     * 这里只考虑行长度，达到行长度临界之后就将其拆分，插入一个回车换行
     * (认为content本身中不会出现回车换行)
     * @return
     */
    public String weaveText()
    {
	int index=0;
	String text="";
	if(content.length()<=LINE_WIDTH)
	{
	    text= content;
	}
	else
	{
	    while(index+LINE_WIDTH-1<content.length())
	    {
		text=text+content.substring(index,index+LINE_WIDTH)+"\r\n";
		index=index+LINE_WIDTH;
	    }
	    text=text+content.substring(index);
	}
	return text;
    }
    /**
     * 
     * @param text
     * @return
     */
    public static Content parse(String text)
    {
	int index,check;
	String con="";
	check=0;
	while((index=text.indexOf("\r\n"))!=-1)
	{
	    con=con+text.substring(check,index);
	    check=index+2;
	}
	con=con+text.substring(check);
	return new Content(con);
    }
    
    /**
     * 用来转换为供认阅读的字符串
     * @return
     */
    public String toString()
    {
	return this.content;
    }
}

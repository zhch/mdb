package z.hilog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import z.hilog.util.UtilSer;


public class HtmlWeaver 
{
    private int idSeq;
    private LogNode root;
    private int treeIndent=20;
    private ArrayList<ArrayList<Integer>> lysMap;
    private ArrayList<String> colors;


    public HtmlWeaver(LogNode root) 
    {
	super();
	this.root = root;
	this.idSeq=-1;
	lysMap=new ArrayList<ArrayList<Integer>>();
	colors=new ArrayList<String>();
    }


    public int getIdSeq() 
    {
	idSeq++;
	return idSeq;
    }

    private String genLys()
    {

	String html="<script type=\"text/javascript\">\r\n";
	html=html+"var lys=new Array();\r\n";
	for(int i=0;i<lysMap.size();i++)
	{
	    html=html+"lys["+i+"]=new Array();\r\n";
	    for(int j=0;j<lysMap.get(i).size();j++)
	    {
		html=html+"lys["+i+"]["+j+"]="+lysMap.get(i).get(j).toString()+";\r\n";
	    }
	}
	html=html+"</script>\r\n";
	return html;
    }
    /**
     * 在生成LogNode的html文本的同时，设置好lysMap数据结构，为生成js数组lys作好准备
     * @param ly
     * @param id
     */
    private void insertLaysMap(int ly,int id)
    {
	if(lysMap.size()<=ly)
	{
	    for(int i=lysMap.size();i<=ly;i++)
	    {
		lysMap.add(new ArrayList<Integer>());
	    }
	}
	ArrayList<Integer> lyMap=lysMap.get(ly);
	lyMap.add(new Integer(id));
    }
    public String toHTML()
    {
	int id;
	String html="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n";
	html=html+"<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n";
	html=html+"<head>\r\n";

	html=html+"<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />\r\n";
	html=html+"<script type=\"text/javascript\" src=\"funs.js\" ></script>\r\n";
	html=html+"</head>\r\n";
	html=html+"<body>\r\n";
	html=html+"<table cellpadding=0 cellspacing=0 style=\"width:100%\">\r\n";

	for(int i=0;i<root.getChildrenQuan();i++)
	{
	    html=html+recursion2TRs(root.getChild(i),0);
	}

	html=html+"</table>\r\n";
	html=html+this.genLys();
	html=html+"</body>\r\n";
	html=html+"</html>\r\n";
	return html;

    }
    private String recursion2TRs(LogNode node,int layer)
    {
	int id;
	String html,color;
	if(colors.size()-1<layer)
	{
	    for(int i=colors.size()-1;i<=layer;i++)
	    {
		colors.add(" style=\"background-color:rgb("+(int)(Math.random()*(255-150)+140)+","+(int)(Math.random()*(255-150)+140)+","+(int)(Math.random()*(255-150)+140)+");\" ");
	    }
	}
	if(node.hasChildren())
	{
	    html=this.genHtmlTr((id=getIdSeq()),layer,false,node.getInfo(),colors.get(layer));
	    this.insertLaysMap(layer,  id);
	    for(int i=0;i<node.getChildrenQuan();i++)
	    {
		html=html+recursion2TRs(node.getChild(i), layer+1);
	    }
	}
	else
	{
	    html=this.genHtmlTr((id=getIdSeq()),layer,true,node.getInfo(),colors.get(layer));
	    this.insertLaysMap(layer,id);
	}
	return html;
    }
    private String genHtmlTr(int id,int layer,boolean isLeaf,String info,String color)
    {
	String tr="<tr>";
	String style,onclick;
	tr=tr+"<td>";
	if(layer==0)
	{
	    if(isLeaf==true)
	    {
		style="class=\"leaf\"";
		onclick="";
	    }
	    else
	    {
		style="class=\"node\"";
		onclick="onclick=\"nodeOp(this,"+layer+",this.id)\"";
	    }
	}
	else
	{
	    if(isLeaf==true)
	    {
		style="class=\"leaf_hiden\"";
		onclick="";
	    }
	    else
	    {
		style="class=\"node_hiden\"";
		onclick="onclick=\"nodeOp(this,"+layer+",this.id)\"";
	    }
	}
	tr=tr+"<table cellpadding=0 cellspacing=0  "+color+style+" id=\""+id+"\" "+onclick+">";
	tr=tr+"<tr>";
	tr=tr+"<td style=\"width:"+(layer*treeIndent)+"px;\">";
	tr=tr+"</td>";
	tr=tr+"<td>";
	tr=tr+info;
	tr=tr+"</td>";
	tr=tr+"</tr>";
	tr=tr+"</table>";
	tr=tr+"</td>";
	tr=tr+"</tr>\r\n";
	return tr;
    }

    public static void main(String[] args)
    {

	File file = new File("d:\\ll\\immtest.xml");  
	try
	{
	    BufferedReader input = new BufferedReader (new FileReader(file));  
	    StringBuffer buffer = new StringBuffer();  
	    String text; 
	    while((text = input.readLine()) != null)  
	    {
		buffer.append(text +"\n");  
	    }
	    String xml = buffer.toString();
	    UtilSer.printDebug("xml:"+xml);
	    XStream xstream = new XStream(new DomDriver());
	    LogNode root=(LogNode)xstream.fromXML(xml);
	    HtmlWeaver html=new HtmlWeaver(root);
	    FileOutputStream fos= new FileOutputStream(file.getAbsoluteFile()+".htm");  
	    OutputStreamWriter out= new OutputStreamWriter(fos, "utf-8");  
	    out.write(html.toHTML());
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

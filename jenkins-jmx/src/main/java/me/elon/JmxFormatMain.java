package me.elon;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmxFormatMain {
    public static boolean containJmx(String location)
    {
        boolean has = false;
        File f = new File(location);
        File[] files = f.listFiles();
        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".jmx"))
            {
                has = true;
                break;
            }
        }
        return has;
    }
    public static  ArrayList<String> getJmxs(String path){
        ArrayList<String> jmxs=new ArrayList<String>();
        File f = new File(path);
        if (f.isDirectory()){
           File[] files= f.listFiles();
            for (File file:files) {
                if (file.getName().toLowerCase().endsWith(".jmx")){
                    jmxs.add(file.getAbsolutePath());
                }
            }
        }
        return  jmxs;
    }
    public static void writeToNewFile(String data ,String writeTo){
    	File file = new File(writeTo);
        try{
            File pFile = file.getParentFile();
            if (!pFile.exists()) {
				pFile.mkdirs();
			}
            if(!file.exists()){
                file.createNewFile();
            }
            //true = append file
            FileWriter fileWriter = new FileWriter(writeTo,false);
            fileWriter.write(data);
            fileWriter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static String formatJmx(File file){
        String name = file.getName().toLowerCase().replace(".jmx","");
        System.out.println("name:"+name);
        String content = readToString(file.getAbsolutePath());
        StringBuffer sb = new StringBuffer() ;
        Pattern p = Pattern.compile("testclass=\"ThreadGroup\" testname=\"(.*)\" enabled=\"true\">") ;
        Matcher m = p.matcher(content) ;
        while( m.find() ){
            String tmp = m.group(1) ;
            System.out.println("find:"+tmp);
            String rep = String.format("testclass=\"ThreadGroup\" testname=\"[%s-%s]\" enabled=\"true\">",tmp,name);
            String v = rep;
            //注意，在替换字符串中使用反斜线 (\) 和美元符号 ($) 可能导致与作为字面值替换字符串时所产生的结果不同。
            //美元符号可视为到如上所述已捕获子序列的引用，反斜线可用于转义替换字符串中的字面值字符。
            v = v.replace("\\", "\\\\").replace("$", "\\$");
            //替换掉查找到的字符串
            m.appendReplacement(sb, v) ;
        }
        //别忘了加上最后一点
        m.appendTail(sb);
        return  sb.toString();
    }
    public static ArrayList<String> getfolders(ArrayList<String> folders, String location)
    {
        File f = new File(location);
        if (f.isDirectory())
        {
            if (containJmx(location)) {
                folders.add(location);
            }
            File[] files = f.listFiles();
            for (File file : files) {
                if (file.isDirectory())
                {
                    String path = file.getAbsolutePath();
                    getfolders(folders, path);
                }
            }
        }
        return folders;
    }
    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }
    public static  void main(String args[]){
        //String path = args[0];
    	String sourceFolder = "D:\\workspace\\dps-api";
    	String destinationFolderString="E:\\workspace\\dps-api";
    	File file = new File(destinationFolderString);
    	if (!file.isDirectory()) {
			file.mkdirs();
		}
        System.out.println("=================");
        String location = sourceFolder;
        ArrayList<String> folders=new ArrayList<String>();
        getfolders(folders,location);
        for (String folder :folders) {
            System.out.println(folder);
            ArrayList<String> jmxList = getJmxs(folder);
            for (String jmx:jmxList) {
                System.out.println(jmx);
                String data = formatJmx(new File(jmx));
                //System.out.println(data);
                System.out.println("===============================================");
                String newJmxLocation = jmx.replace(sourceFolder, destinationFolderString);
                System.out.println("new Folder - "+newJmxLocation);
                //writeToNewFile(data,new File(System.currentTimeMillis()+".txt"));
                writeToNewFile(data,newJmxLocation);
            }
        }
    }
}

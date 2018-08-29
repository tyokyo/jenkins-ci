package me.elon;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MailTestArgs2 {
	public static String readHtml(String htmlpath){
		StringBuffer content = new StringBuffer();
		FileReader fr;
		try {
			fr = new FileReader(htmlpath);
			int ch = 0;  
			while((ch = fr.read()) != -1){  
				content.append((char)ch);
			}  
			fr.close();  
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		//System.out.println(content.toString());
		return content.toString();
	}  
public static void main(String[] args) throws Exception {
	MailSender mailSender = MailSender.getInstance();
	
	String to = args[0];
	String cc = args[1];
	String subject = args[2];
	String attach =  args[3];
	String html =  args[4];
	
	String mail_host =  args[5];
	String mail_port =  args[6];
	String mail_username =  args[7];
	String mail_password =  args[8];

	MailInfo info = new MailInfo();
	info.setMailHost(mail_host);
	info.setMailPort(mail_port);
	info.setUsername(mail_username);
	info.setPassword(mail_password);
	
/*	info.setMailHost("smtp.163.com");
	info.setMailPort("465");
	info.setUsername("lolopiao@163.com");
	info.setPassword("Baidu@piao");*/
	
	info.setNotifyTo(to);
	info.setNotifyCc(cc);
	info.setSubject(subject);
	
	/*MailInfo info = new MailInfo();
	info.setMailHost("hwsmtp.qiye.163.com");
	info.setMailPort("465");
	info.setUsername("qiang.zhang@ck-telecom.com");
	info.setPassword("zhangqiang@9090");
	info.setNotifyTo("qiang.zhang@ck-telecom.com");
	info.setNotifyCc("tyokyo@126.com;qiang.zhang502502@163.com");
	info.setSubject("java send mail test");*/
	String[] attachments = attach.split(";");
	info.setContent(readHtml(html));
	info.setAttachFileNames(attachments);//添加附件
	//MailInfo mailInfo = mailSender.setMailInfo();
	mailSender.sendHtmlMail(info, 3);
}
}

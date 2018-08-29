package com;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.jmeter.sampler.util.MyX509TrustManager;
/**
 * 以Http协议传输文件
 * 
 * @author mingxue.zhang@163.com
 *
 */
public class HttpPostUtil2 {
	private InputStream responseInStream;
	private ByteArrayOutputStream responseOutStream;
	private OutputStream connOutStream;
	private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();
	private int connCode = -1;
	private URL url;
	private HttpsURLConnection conn;
	private String boundary = null;
	private Map<String, String> textParams = new HashMap<String, String>();
	private Map<String, File> fileparams = new HashMap<String, File>();
	private Map<String, File> headers = new HashMap<String, File>();

	public HttpPostUtil2(String url) throws Exception {
		this.url = new URL(url);
	}

	// 重新设置要请求的服务器地址，即上传文件的地址。
	public void setUrl(String url) throws Exception {
		this.url = new URL(url);
	}

	// 增加一个普通字符串数据到form表单数据中
	public void addTextParameter(String name, String value) {
		textParams.put(name, value);
	}
	
	// 增加一个普通字符串数据到form表单数据中
		public void addHeaders(Map<String, File> headers) {
			this.headers=headers;
		}

	// 增加一个文件到form表单数据中
	public void addFileParameter(String name, File value) {
		fileparams.put(name, value);
	}

	// 清空所有已添加的form表单数据
	public void clearAllParameters() {
		textParams.clear();
		fileparams.clear();
	}
	/**
	 * 发送数据到服务器
	 * 
	 * @return 一个字节包含服务器的返回结果的数组
	 * @throws Exception
	 */
	public byte[] send() throws Exception {
		try {
			initConnection();
			conn.connect();
			connOutStream = new DataOutputStream(conn.getOutputStream());

			writeFileParams(connOutStream);
			writeStringParams(connOutStream);
			writesEnd(connOutStream);
			
			//read response data
			responseInStream = conn.getInputStream();
			connCode = conn.getResponseCode(); 
			System.out.println(connCode);
			//connect success
			if(connCode == HttpsURLConnection.HTTP_OK) {
				
			}else{
				
			}
			responseOutStream = new ByteArrayOutputStream();
			int len;
			byte[] bufferByte = new byte[1024];
			while ((len = responseInStream.read(bufferByte)) != -1) {
				responseOutStream.write(bufferByte, 0, len);
			}
			responseInStream.close();
			connOutStream.close();
			
			conn.disconnect();

			byte[] resultByte = responseOutStream.toByteArray();
			responseOutStream.close();
			return resultByte;
		} catch (SocketTimeoutException e) {
			throw new Exception(e);
		}finally{
			
			if (connOutStream!=null) {
				connOutStream.close();
			}
			if (conn!=null) {
				conn.disconnect();
			}
			if (responseInStream!=null) {
				responseInStream.close();
			}
			if (responseOutStream!=null) {
				responseOutStream.close();
			}
		}
	}

	// 文件上传的connection的一些必须设置
	private void initConnection() throws Exception {
		StringBuffer buf = new StringBuffer("----");
		Random rand = new Random();
		for (int i = 0; i < 15; i++) {
			buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}
		this.boundary = buf.toString();

		conn = (HttpsURLConnection) this.url.openConnection();
		//SSLContext
		TrustManager[] tm = { new MyX509TrustManager() };
		SSLContext sslContext;
		sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, tm, new java.security.SecureRandom());
		//SSLSocketFactory
		SSLSocketFactory ssf = sslContext.getSocketFactory();
		conn.setSSLSocketFactory(ssf);

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setConnectTimeout(3 * 60 * 1000); // 连接超时为10秒
		conn.setRequestMethod("PUT");
		conn.setReadTimeout(60*1000);

		conn.setRequestProperty("Content-Type","application/octet-stream; boundary=" + boundary);

	}

	// 普通字符串数据
	private void writeStringParams(OutputStream out) throws Exception {
		Set<String> keySet = textParams.keySet();
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			String name = it.next();
			String value = textParams.get(name);

			out.write(("--" + boundary + "\r\n").getBytes());
			out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n")
					.getBytes());
			out.write(("\r\n").getBytes());
			out.write((encode(value) + "\r\n").getBytes());
		}
	}

	// 文件数据
	private void writeFileParams(OutputStream out) throws Exception {
		Set<String> keySet = fileparams.keySet();
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			String name = it.next();
			File value = fileparams.get(name);

			out.write(("--" + boundary + "\r\n").getBytes());
			out.write(("Content-Disposition: form-data; name=\"" + name
					+ "\"; filename=\"" + encode(value.getName()) + "\"\r\n")
					.getBytes());
			out.write(("Content-Type: " + getContentType(value) + "\r\n")
					.getBytes());
			out.write(("Content-Transfer-Encoding: " + "binary" + "\r\n")
					.getBytes());

			out.write(("\r\n").getBytes());

			FileInputStream inStream = new FileInputStream(value);
			int bytes = 0;
			byte[] bufferByte = new byte[1024];
			while ((bytes = inStream.read(bufferByte)) != -1) {
				out.write(bufferByte, 0, bytes);
			}
			inStream.close();

			out.write(("\r\n").getBytes());
		}
	}

	// 添加结尾数据
	private void writesEnd(OutputStream out) throws Exception {
		out.write(("--" + boundary + "--" + "\r\n").getBytes());
		out.write(("\r\n").getBytes());
	}

	// 获取文件的上传类型，图片格式为image/png,image/jpg等。非图片为application/octet-stream
	private String getContentType(File f) throws Exception {
		String fileName = f.getName();
		if (fileName.endsWith(".jpg")) {
			return "image/jpeg";
		} else if (fileName.endsWith(".png")) {
			return "image/png";
		}
		if (fileName.endsWith(".mp4")) {
			return "video/mp4";
		}
		return "application/octet-stream";
	}

	// 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
	private String encode(String value) throws Exception {
		return URLEncoder.encode(value, "UTF-8");
	}
	
	public static void main(String  asrg[]){
		String url= "https://sioeye-disney-tmp-test.s3.cn-north-1.amazonaws.com.cn/origin/2a3b52219e874440a489c8270ecb334f/5ed4d10545284c71b3dc1e57b16e75a8/c57905798a01471f9ba6388d12c2a28a/videos/d46d3c44a7334760bec40892929f8521.mp4?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAO7QXSMTQKKQGQWRQ%2F20180705%2Fcn-north-1%2Fs3%2Faws4_request&X-Amz-Date=20180705T050209Z&X-Amz-Expires=900&X-Amz-Signature=43ae4920fe6a52c713722c6da3eb6119f079e4cc051a19202f8843d5d6bdbcb4&X-Amz-SignedHeaders=host%3Bx-amz-acl&x-amz-acl=private";
		String file_png = "D:/soft/Jmeter/apache-jmeter-3.3/data/video/disney/AmusementPark_video74_segment1.mp4";
		try {
			HttpPostUtil2 postUtil = new HttpPostUtil2(url);
			postUtil.addFileParameter("file",new File(file_png) );

			byte[]  resultByte = postUtil.send();
			String resultString=new String(resultByte);
			System.out.println(resultString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
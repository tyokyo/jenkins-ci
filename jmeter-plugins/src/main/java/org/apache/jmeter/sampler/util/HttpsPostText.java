package org.apache.jmeter.sampler.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import net.sf.json.JSONObject;

import org.apache.jmeter.Tool;
import org.apache.jmeter.functions.String2MD5;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsPostText{
	private  Logger logger =LoggerFactory.getLogger(HttpsPostText.class);
	private HTTPSampleResult res;
	private  InputStream postInuptStream=null;
	private  InputStream  postErrorStream=null;
	private  OutputStream connOutStream=null;
	private int connCode = -1;
	private URL url;
	private HttpsURLConnection conn;
	private Map<String, String> textParams = new HashMap<String, String>();
	private Map<String, String> headers = new HashMap<String, String>();

	public Map<String, String> getTextParams() {
		return textParams;
	}

	public void setTextParams(Map<String, String> textParams) {
		this.textParams = textParams;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public HttpsPostText(HTTPSampleResult res,String url) {
		try {
			this.url = new URL(url);
			this.res=res;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 重新设置要请求的服务器地址，即上传文件的地址。
	public void setUrl(String url) throws Exception {
		this.url = new URL(url);
	}
	// 文件上传的connection的一些必须设置
	private void initConnection() throws Exception {
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
		conn.setConnectTimeout(30 * 1000); 
		conn.setRequestMethod("POST");
		conn.setReadTimeout(30*1000);
		setContentLength();
		setRequestHeaders();
	}
	private void setRequestHeaders(){
		Map<String, String> headers=getHeaders();
		Set<String> headerKeys = headers.keySet();
		for (String key : headerKeys) {
			String value = headers.get(key);
			conn.setRequestProperty(key,value.toString());
		}
	}
	private void setContentLength(){
		String paramsData=JSONObject.fromObject(getTextParams()).toString();
		byte[] data = paramsData.getBytes();
		conn.setRequestProperty("Content-Length", String.valueOf(data.length));
	}
	// 普通字符串数据
	private void writeTextParams(OutputStream out) throws Exception {
		Map<Object, Object> parametersMap = Tool.mapToObj(getTextParams());
		String paramsData=JSONObject.fromObject(parametersMap).toString();
		logger.info(paramsData);
		byte[] data = paramsData.getBytes();
		out.write(data);
	}
	private void errorResult(Throwable e, SampleResult res)
	{
		res.setSampleLabel(res.getSampleLabel());
		res.setDataType("text");
		java.io.ByteArrayOutputStream text = new java.io.ByteArrayOutputStream(200);
		e.printStackTrace(new PrintStream(text));
		res.setResponseData(text.toByteArray());
		res.setResponseCode(new StringBuilder().append("Non HTTP response code: ").append(e.getClass().getName()).toString());
		res.setResponseMessage(new StringBuilder().append("Non HTTP response message: ").append(e.getMessage()).toString());
		res.setSuccessful(false);
	}
	/**
	 * 发送数据到服务器
	 * 
	 * @return 一个字节包含服务器的返回结果的数组
	 * @throws Exception
	 */
	public void  send(){
		try {
			initConnection();
			conn.connect();
			connOutStream = new DataOutputStream(conn.getOutputStream());
			writeTextParams(connOutStream);
			
			connCode = conn.getResponseCode(); 
			if(connCode == HttpsURLConnection.HTTP_OK) {
				postInuptStream = conn.getInputStream();
				String response=coverInputStreamResult(postInuptStream);
				res.setResponseData(JsonFormatUtil.formatJson(response),null);
				res.setSuccessful(true);
				doSessonToken(response);
			}else{
				postErrorStream=conn.getErrorStream();
				String response=coverInputStreamResult(postErrorStream);
				res.setResponseData(response,null);
				res.setSuccessful(false);
			}
			
		} catch (Exception e) {
			errorResult(e, res);
			res.sampleEnd();
			logger.warn(e.getMessage());
		}finally{
			try {
				if (connOutStream!=null) {
					connOutStream.close();
				}
				if (conn!=null) {
					conn.disconnect();
				}
				if (postInuptStream!=null) {
					postInuptStream.close();
				}
				if (postErrorStream!=null) {
					postErrorStream.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}
	public static void doSessonToken(String result){
		try {
			JsonParse js = new JsonParse();
			js.setJsonString(result);
			js.setQueryString(".sessiontoken");
			List<Object> skens = js.parse();
			if (skens.size()==1) {
				String sessiontoken =skens.get(0).toString();
				JMeterUtils.getJMeterProperties().put("sessiontoken", sessiontoken);
			}
			//js.print();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private  String coverInputStreamResult(InputStream inputStream) {
		String encode=conn.getContentEncoding();
		if(inputStream!=null){
			String resultData = null; 
			GZIPInputStream gZIPInputStream = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int len = 0;
			try {
				if(encode!=null&&encode.equals("gzip")){
					gZIPInputStream = new GZIPInputStream(inputStream);//gzip
					while((len = gZIPInputStream.read(data)) != -1) {
						byteArrayOutputStream.write(data, 0, len);
					}
				}else{
					while((len = inputStream.read(data)) != -1) {
						byteArrayOutputStream.write(data, 0, len);
					}
				}
			} catch (IOException e) {
				logger.error("Exception",e);
			}finally {			
				try {
					if(byteArrayOutputStream!=null){
						byteArrayOutputStream.close();
					}
					if(gZIPInputStream!=null){
						gZIPInputStream.close();
					}
				} catch (IOException e) {
					logger.error("Exception",e);
				}
			}
			try {
				resultData = new String(byteArrayOutputStream.toByteArray(),"UTF-8");
				byteArrayOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("Exception",e);
			}
			//logger.info(resultData);
			return resultData;
		}else{
			return null;	
		}
	}
	public static void main(String args[]){
		String url ="https://api.siocloud.sioeye.cn/functions/login" ;

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("username","tyokyo@126.com");
		params.put("password",String2MD5.MD5("123456789"));
		params.put("type","app");

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json;charset=utf-8");
		headers.put("X_Sioeye_App_Id", "usYhGBBKDMiypaKFV8fc3kE4");
		headers.put("X_Sioeye_App_Sign_Key", "5f3773d461775804ca2c942f8589f1d6,1476178217671");
		headers.put("X_Sioeye_App_Production", "1");
		
		HTTPSampleResult res = new HTTPSampleResult();
		HttpsPostText postText = new HttpsPostText(res,url);
		postText.setHeaders(headers);
		postText.setTextParams(params);
		postText.send();
	}
}

/**   
 * Copyright © 2020 zmd. All rights reserved.
 * 
 * 功能描述：
 * @Package: com.zmd.reptile 
 * @author: Lin   
 * @date: 2020年9月11日 上午9:54:14 
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.test.PictureAddressUtil;
import com.zmd.test.OrcUtils;
import com.zmd.test.QichaInterfaceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import sun.security.util.Length;


/**   
 * Copyright: Copyright (c) 2020 zmd
 * 
 * @ClassName: OrcTest.java
 * @Description: 该类的功能描述
 *
 * @version: v1.0.0
 * @author: Lin
 * @date: 2020年9月11日 上午9:54:14 
 *
 */
public class OrcTest {
	
	public static final String URL_GETCODE = "https://investorservice.cfmmc.com/";
	public static String request_url = "https://investorservice.cfmmc.com/login.do";
	public static String img_url = "https://investorservice.cfmmc.com/veriCode.do?t="+System.currentTimeMillis();

	private String userID = "000780028186";
	private String password = "84588311";
	private String showSaveCookies = "";
	private static String _imgCode = "";

	//http请求头
	private String Accept="text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
	private String Accept_Encoding="gzip, deflate, br";
	private String Accept_Language="zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2";
	private String Connection="keep-alive";
	private String Content_Type = "application/x-www-form-urlencoded";
	private String Host = "investorservice.cfmmc.com";
	private String Origin = "https://investorservice.cfmmc.com";
	private String Referer = "https://investorservice.cfmmc.com/login.do";
	private String Upgrade_Insecure_Requests = "1";
	private String User_Agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0";

	
	/**
	 * 
	* @Title: generateStdDigitImgage
	* @Description: 获取图片，图形处理并分割成可选模板（1.获取验证码图片库；2.读取上述图片库图片，依次去干扰，切割成模板库，挑选出复合要求的模板归纳入基础库；3.测试验证）
	* @return void    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年9月11日 上午9:56:33
	 */
	@Test
	public void generateStdDigitImgage(){
		for (int i = 0; i < 1; i++) {
			getPicture(URL_GETCODE);
		}
		/*try {
			OrcUtils.generateStdDigitImgage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	/**
	 * 
	* @Title: testVeridateCode
	* @Description: 图片解析测试
	* @return void    返回类型
	* @throws
	* @Description: 账号：000780028186  密码：84588311
	* @author: Lin
	* @date: 2020年9月11日 上午10:01:03
	 */
	@Test
	public void testLogin() throws IOException{
		CloseableHttpResponse httpResponse = null;
		String token = "";
		for (int i = 0; i < 1; i++) {
			String code;
			try {
				code = OrcUtils.getValidateCode(new File(getPicture(img_url))).trim();
				System.out.println(code);
				System.out.println("token1 = "+token);
				httpResponse = login(code,token);

				if(httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200){
					HttpEntity entity = httpResponse.getEntity();
					if (entity != null){
						//获取返回的信息
						String result = EntityUtils.toString(entity);
						//使用jsoup 进行语言转换
						Document doc = Jsoup.parse(result);
						Elements elements = doc.getElementsByClass("error-msg");
						if(elements!=null && elements.size() > 0){
							String text = elements.get(0).text();
							if(text.contains("验证码错误")){
								//重新请求一下登录界面
								HttpUriRequest codeReq = new HttpGet(URL_GETCODE);
								CloseableHttpClient httpClient = declareHttpClientSSL(codeReq.getURI().toString());
								CloseableHttpResponse response = httpClient.execute(codeReq);
								if(response != null && response.getStatusLine().getStatusCode() == 200){
									HttpEntity entity1 = response.getEntity();
									if(entity1 != null){
										String str = EntityUtils.toString(entity1);
										Document document = Jsoup.parse(str);
										token = document.select("input[name='org.apache.struts.taglib.html.TOKEN']").val();
										System.out.println("token = "+token);
										continue;
									}
								}
							}
						}else{
							System.out.println("登录成功");
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public CloseableHttpResponse login(String code,String token) throws IOException{
		//登录参数设置
		List<NameValuePair> para = new ArrayList<NameValuePair>();
		if(StringUtils.isNotBlank(token)){
			para.add(new BasicNameValuePair("org.apache.struts.taglib.html.TOKEN",token));
		}
		para.add(new BasicNameValuePair("showSaveCookies",showSaveCookies));
		para.add(new BasicNameValuePair("userID",userID));
		para.add(new BasicNameValuePair("password","11111111"));
		para.add(new BasicNameValuePair("vericode",code));

		String requestParams = "showSaveCookies=&userID=000780028186&password=84588311&vericode="+code;
		if(StringUtils.isNotBlank(token)){
			requestParams = "org.apache.struts.taglib.html.TOKEN="+token+"&"+requestParams;
		}

		//请求头参数设置
		Map<String,String> header = new HashMap<String, String>();
		header.put("Accept",Accept);
		header.put("Accept-Encoding",Accept_Encoding);
		header.put("Accept-Language",Accept_Language);
		header.put("Connection",Connection);
		//header.put("Content-Length",Content_Length);
		header.put("Content-Type",Content_Type);
		header.put("Host",Host);
		header.put("Origin",Origin);
		header.put("Referer",Referer);
		header.put("Upgrade-Insecure-Requests",Upgrade_Insecure_Requests);
		header.put("User-Agent",User_Agent);

		HttpPost post = new HttpPost(request_url);
		for(String string : header.keySet()){
			post.addHeader(string,header.get(string));
		}
		if (StringUtils.isNotBlank(token)){
			post.addHeader("X-Xsrftoken", token);
		}
		post.setEntity(new StringEntity(requestParams, ContentType.APPLICATION_FORM_URLENCODED));

		//CloseableHttpClient httpClient = declareHttpClientSSL(post.getURI().toString());
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(post);

		//post.releaseConnection();
		return response;
	}

	@Test
	public void login_in(){
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		try {
			for(int i = 0;i<1;i++){
				HttpUriRequest request = new HttpGet(img_url);  //获取验证码
				CloseableHttpClient httpClient = declareHttpClientSSL(request.getURI().toString());
				response = httpClient.execute(request);
				String checkImgUrl="";
				String code="";
				String checkCookies="";
				if(response != null){
					entity = response.getEntity();
					if(entity != null){
						byte[] resource = EntityUtils.toByteArray(entity);
						checkImgUrl = saveJPG(resource);  //保存验证码图片
					}
					/*checkCookies = getCookies(response);
					System.out.println("获取验证码图片Cookie = "+ checkCookies);*/
				}

				code = OrcUtils.getValidateCode(new File(checkImgUrl)).trim();
				System.out.println("验证码 = "+code);

				//模拟登录
				HttpPost loginReq = new HttpPost(request_url);
				//请求头参数设置
				loginReq.setHeader("Accept",Accept);
				loginReq.setHeader("Accept-Encoding",Accept_Encoding);
				loginReq.setHeader("Accept-Language",Accept_Language);
				loginReq.setHeader("Connection",Connection);
				loginReq.setHeader("Content-Type",Content_Type);
				loginReq.setHeader("Host",Host);
				loginReq.setHeader("Origin",Origin);
				loginReq.setHeader("Referer",Referer);
				loginReq.setHeader("Upgrade-Insecure-Requests",Upgrade_Insecure_Requests);
				loginReq.setHeader("User-Agent",User_Agent);
				//设置登陆使用的cookies
				StringBuffer loginCookies = new StringBuffer(checkCookies);
				loginReq.setHeader(new BasicHeader("Cookie",loginCookies.toString()));
				//登录参数设置
				List<NameValuePair> para = new ArrayList<NameValuePair>();
				/*if(StringUtils.isNotBlank(token)){
					para.add(new BasicNameValuePair("org.apache.struts.taglib.html.TOKEN",token));
				}*/
				para.add(new BasicNameValuePair("showSaveCookies",showSaveCookies));
				para.add(new BasicNameValuePair("userID",userID));
				para.add(new BasicNameValuePair("password",password));
				para.add(new BasicNameValuePair("vericode",code));

				loginReq.setEntity(new UrlEncodedFormEntity(para,"utf-8"));
				CloseableHttpClient httpClient1 = declareHttpClientSSL(loginReq.getURI().toString());
				response = httpClient1.execute(loginReq);

				entity = response.getEntity();
				String html = "";
				if(entity != null){
					html = EntityUtils.toString(entity,"UTF-8");
				}
				if(StringUtils.isNotBlank(html)){
					//使用jsoup 进行语言转换
					Document doc = Jsoup.parse(html);
					Elements elements = doc.getElementsByClass("error-msg");
					if(elements != null){
						System.out.println("登录失败======"+elements.get(0).text());
					}else{
						System.out.println("登录成功");
						break;
					}
				}
			}
		}catch (Exception e){
			System.out.println("登录失败"+e);
		}

	}

	@Test
	public void Login(){
		CloseableHttpResponse response = null;
		for(int i=0;i<40;i++){
			try {
				HttpUriRequest request = new HttpGet(URL_GETCODE);  //请求登录页面
				CloseableHttpClient httpClient = declareHttpClientSSL(request.getURI().toString());
				CloseableHttpResponse response1 = httpClient.execute(request);   //执行访问登录页面

				String checkCookies="";
				String checkImgUrl="";
				String code= "";
				String token = "";
				if(response1 != null && response1.getStatusLine().getStatusCode() == 200){
					HttpEntity entity = response1.getEntity();
					if(entity != null){
						String str = EntityUtils.toString(entity);
						Document document = Jsoup.parse(str);
						token = document.select("input[name='org.apache.struts.taglib.html.TOKEN']").val();
						System.out.println("token = "+token);
					}

					//获取Cookie信息
					checkCookies = getCookies(response1);
					System.out.println("获取登录Cookie = "+ checkCookies);

					HttpUriRequest request1 = new HttpGet(img_url);  //获取验证码
					CloseableHttpClient httpClient1 = declareHttpClientSSL(request1.getURI().toString());
					//请求头参数设置
					request1.setHeader("Accept","image/webp,*/*");
					request1.setHeader("Accept-Encoding",Accept_Encoding);
					request1.setHeader("Accept-Language",Accept_Language);
					request1.setHeader("Connection",Connection);
					request1.setHeader("Cookie",checkCookies);
					request1.setHeader("Host",Host);
					request1.setHeader("Referer",Referer);
					request1.setHeader("User-Agent",User_Agent);

					CloseableHttpResponse response2 = httpClient1.execute(request1);
					if(response2 != null && response2.getStatusLine().getStatusCode() == 200){
						HttpEntity entity1 = response2.getEntity();
						byte[] resource = EntityUtils.toByteArray(entity1);
						checkImgUrl = saveJPG(resource);  //保存验证码图片
					}

					code = OrcUtils.getValidateCode(new File(checkImgUrl)).trim();
					System.out.println("验证码 = "+code);

					//模拟登录
					HttpPost loginReq = new HttpPost(request_url);
					//请求头参数设置
					loginReq.setHeader("Accept",Accept);
					loginReq.setHeader("Accept-Encoding",Accept_Encoding);
					loginReq.setHeader("Accept-Language",Accept_Language);
					loginReq.setHeader("Connection",Connection);
					loginReq.setHeader("Content-Type",Content_Type);
					//设置登陆使用的cookies
					StringBuffer loginCookies = new StringBuffer(checkCookies);
					loginReq.setHeader(new BasicHeader("Cookie",loginCookies.toString()));
					loginReq.setHeader("Host",Host);
					loginReq.setHeader("Origin",Origin);
					loginReq.setHeader("Referer",Referer);
					loginReq.setHeader("Upgrade-Insecure-Requests",Upgrade_Insecure_Requests);
					loginReq.setHeader("User-Agent",User_Agent);

					//登录参数设置
					List<NameValuePair> para = new ArrayList<NameValuePair>();
					if(StringUtils.isNotBlank(token)){
						para.add(new BasicNameValuePair("org.apache.struts.taglib.html.TOKEN",token));
					}
					para.add(new BasicNameValuePair("showSaveCookies",showSaveCookies));
					para.add(new BasicNameValuePair("userID",userID));
					para.add(new BasicNameValuePair("password",password));
					para.add(new BasicNameValuePair("vericode",code));

					loginReq.setEntity(new UrlEncodedFormEntity(para,"utf-8"));
					CloseableHttpClient httpClient2 = declareHttpClientSSL(loginReq.getURI().toString());
					response = httpClient2.execute(loginReq);

					entity = response.getEntity();
					String html = "";
					if(entity != null){
						html = EntityUtils.toString(entity,"UTF-8");
					}
					if(StringUtils.isNotBlank(html)){
						//使用jsoup 进行语言转换
						Document doc = Jsoup.parse(html);
						Elements elements = doc.getElementsByClass("error-msg");
						if(elements != null){
							System.out.println("登录失败======"+elements.get(0).text());
						}else{
							System.out.println("登录成功");
							break;
						}
					}
				}
			}catch (Exception e){
				System.out.println("登录失败。。"+e);
			}
		}
	}

	public String getPicture(String picUrl) {
        try {
        	HttpUriRequest codeReq = new HttpGet(picUrl);
			CloseableHttpClient httpClient = declareHttpClientSSL(codeReq.getURI().toString());
			CloseableHttpResponse response = httpClient.execute(codeReq);
			if(response.getStatusLine().getStatusCode() == 200){
				try {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						//获取
						String result1 = EntityUtils.toString(entity);
						//使用jsoup 进行语言转换
						Document doc1 = Jsoup.parse(result1);
						//图片标签
						String string = doc1.getElementById("imgVeriCode").toString();
						//爬取验证码  图片/sso/authimg
						PictureAddressUtil addressUtil = new PictureAddressUtil();
						List<String> list = addressUtil.filePath(string);
						/**
						 * 通过验证码   下载下来   并用tess4j图片识别其中的验证码
						 */
						HttpPost request2 = new HttpPost("https://investorservice.cfmmc.com" + list.get(0) + "");
						HttpResponse response2 = httpClient.execute(request2);

						HttpEntity entity1 = response2.getEntity();
						String str = EntityUtils.toString(entity1);
						Document doc2 = Jsoup.parse(str);
						byte[] resource = EntityUtils.toByteArray(entity1);
						return saveJPG(resource);
					}
				} finally {
					response.close();
				}
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String saveJPG(byte[] resource) throws IOException {
        
        String path = this.getClass().getResource("/").getPath().replace("test-classes", "checkCode");//DOWNLOAD_DIR
        
        File dir = new File(path);
        if(!dir.exists() && dir.mkdir());
        
        Date d = new Date();
        String fileName = path +  d.getTime() + ".jpg";
        File storeFile = new File(fileName);    
        FileOutputStream output = new FileOutputStream(storeFile);    
        //得到网络资源的字节数组,并写入文件    
        output.write(resource);    
        output.close();    
        return fileName;  
    }  
	
	private CloseableHttpClient declareHttpClientSSL(String url) {
		if (url.startsWith("https://")) {
			return sslClient();
		} else {
			return HttpClientBuilder.create().setConnectionManager(QichaInterfaceUtils.httpClientConnectionManager).build();
		}
	}
	
	private CloseableHttpClient sslClient() {
		try {
			TrustManager[] trustAllCerts = new TrustManager[] {
		        new X509TrustManager() {
		            public X509Certificate[] getAcceptedIssuers() {
		                return null;
		            }
		            public void checkClientTrusted(X509Certificate[] certs, String authType) {
		                // don't check
		            }
		            public void checkServerTrusted(X509Certificate[] certs, String authType) {
		                // don't check
		            }
		        }
		    };
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, trustAllCerts, null);
			
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(ctx);
			return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
			
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @Title: getCookies
	 * @Description: 获取验证码返回的cookies
	 * @param @param response
	 * @param @return    参数
	 * @return String    返回类型
	 * @throws
	 *
	 * @author: Lin
	 * @date: 2020年6月22日 上午11:46:20
	 */
	private String getCookies(CloseableHttpResponse response){
		Header[] headers = response.getHeaders("Set-Cookie");
		//组装要抓取的数据页面请求发送的cookie
		StringBuilder sb = new StringBuilder();
		if (headers != null){
			for(int i=0;i<headers.length;i++){
				String cookie=headers[i].getValue();
				String[]cookievalues=cookie.split(";");
				for(int j=0;j<cookievalues.length;j++){
					String[] keyPair=cookievalues[j].split("=");
					String key=keyPair[0].trim();
					String value=keyPair.length>1?keyPair[1].trim():"";
					if(StringUtils.isNotBlank(key)){
						if(key.equals("JSESSIONID")){
							sb.append(key).append("=").append(value).append(";");
						}
						if(key.equals("WMONID")){
							sb.append(key).append("=").append(value).append(";");
						}
					}
				}
			}
		}
		/*sb.append("Hm_lvt_e91cc445fdd1ff22a6e5c7ea9e9d5406=1592795427;")
				.append("Hm_lpvt_e91cc445fdd1ff22a6e5c7ea9e9d5406=1592795427;")
				.append("Hm_lvt_47f485baba18aaaa71d17def87b5f7ec=1592795429;")
				.append("Hm_lpvt_47f485baba18aaaa71d17def87b5f7ec=1592795429;");*/
		System.out.println("===================================当前请求cookie=======================================");
		String str = sb.toString();
		return str.substring(0,str.length()-1);
	}
}

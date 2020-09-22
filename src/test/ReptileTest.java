/**   
 * Copyright © 2020 zmd. All rights reserved.
 * 
 * 功能描述：
 * @Package: com.zmd.reptile 
 * @author: Lin   
 * @date: 2020年6月16日 下午3:05:27 
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.zmd.test.OrcUtils;
import com.zmd.test.QichaInterfaceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**   
 * Copyright: Copyright (c) 2020 zmd
 * 
 * @ClassName: ReptileTest.java
 * @Description: 爬虫测试
 *
 * @version: v1.0.0
 * @author: Lin
 * @date: 2020年6月16日 下午3:05:27 
 *
 */
public class ReptileTest {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

    // 存放比对图片与代表数字的Map
    private static Map<BufferedImage, String> trainMap = new HashMap<BufferedImage, String>();
    
	private final String REQ_ENCODEING_GBK = "GBK";
	private final String REQ_ENCODEING_UTF8 = "UTF-8";
	public static final int SUCCESS_CODE = 200;
	public static final int MOVED_CODE = 302;
	private static String _imgCode = "";
	
	static class ImageFileFilter implements FileFilter {
        private String postfix = ".jpg";
        
        public ImageFileFilter(String postfix) {
            if(!postfix.startsWith("."))
                postfix = "." + postfix;
            
            this.postfix = postfix;
        }
        
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith(postfix);
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
	private String getCookies(CloseableHttpResponse response,String wantKey){
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
					if(StringUtils.isNotBlank(wantKey) && wantKey.equals(key)){
						sb.append(wantKey).append("=").append(value).append(";");
						if("_imgCode".equals(wantKey)){
							_imgCode = value;
						}
					}
				}
			}
		}
		sb.append("Hm_lvt_e91cc445fdd1ff22a6e5c7ea9e9d5406=1592795427;")
			.append("Hm_lpvt_e91cc445fdd1ff22a6e5c7ea9e9d5406=1592795427;")
				.append("Hm_lvt_47f485baba18aaaa71d17def87b5f7ec=1592795429;")
					.append("Hm_lpvt_47f485baba18aaaa71d17def87b5f7ec=1592795429;");
		System.out.println("===================================当前请求cookie=======================================");
		return sb.toString();
	}
	
	/**
	 * 
	* @Title: testOilChem2
	* @Description: 隆众石化测试
	* @param @throws Exception    参数
	* @return void    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月22日 上午11:21:51
	 */
	@Test
	public void testOilChem2() throws Exception{
		
		String getCodeUrl = "https://passport.oilchem.net/member/login/getImgCode?timestamp="+System.currentTimeMillis();//获取验证码
		String checkCodeUrl = "https://passport.oilchem.net/member/login/checkImgCode?code=";//校验验证码
		String loginUrl = "https://passport.oilchem.net/member/login/login";//登陆
		//String dataUrl = "https://www.oilchem.net/";
		String dataUrl = "https://dc.oilchem.net/price_search/list.htm?businessType=3&specificationsId=&regionId=4&memberId=&standard=&productState=&varietiesId=3145&varietiesName=%E6%B1%BD%E6%B2%B9&templateType=5&flagAndTemplate=3-4%3B5-null%3B6-null%3B2-5%3B1-5%3B4-null&channelId=1693&oneName=%E8%83%BD%E6%BA%90&twoName=%E6%88%90%E5%93%81%E6%B2%B9&dateType=0";
		
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		try {
			//1.获取验证码
			HttpUriRequest codeReq = new HttpGet(getCodeUrl);
			CloseableHttpClient httpClient1 = declareHttpClientSSL(codeReq.getURI().toString());
			response = httpClient1.execute(codeReq);
			//获取并保存验证码
			String imgName = "";
			entity = response.getEntity();
			if (entity != null) {
			    byte[] resource = EntityUtils.toByteArray(entity);
			    imgName = saveJPG(resource);
			}
			//解析获取的验证码,作废参数发送至校验验证码
			String code = OrcUtils.getValidateCode(new File(imgName));
			
			//2.校验验证码
			HttpUriRequest checkRequest = new HttpGet(checkCodeUrl + code);
			//设置请求头
			checkRequest.setHeader("Accept", "*/*");
			checkRequest.setHeader("Accept-Encoding", "gzip, deflate, br");
			checkRequest.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
			checkRequest.setHeader("Connection", "keep-alive");
			checkRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36");
			String checkCoolkies = getCookies(response,"_imgCode");
			checkRequest.addHeader(new BasicHeader("Cookie",checkCoolkies));//校验验证码使用请求验证码返回的cookie
			System.out.println("checkRequest校验验证码发送的cookie:"+checkCoolkies);
			CloseableHttpClient httpClient2 = declareHttpClientSSL(checkRequest.getURI().toString());
			response = httpClient2.execute(checkRequest);
			entity = response.getEntity();
			String html = "";
			if (entity != null) {
				html = EntityUtils.toString(entity, REQ_ENCODEING_UTF8);
				if(!"true".equals(html)){
					throw new RuntimeException("验证码校验失败！"); 
				}
			}
			
			if(response!=null && (response.getStatusLine().getStatusCode()==SUCCESS_CODE)){
				//3.校验成功，进行模拟登陆
				HttpPost loginReq = new HttpPost(loginUrl);
				loginReq.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
				loginReq.setHeader("Accept-Encoding", "gzip, deflate, br");
				loginReq.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
				loginReq.setHeader("Connection", "keep-alive");
				loginReq.setHeader("Referer", "https://www.oilchem.net/");
				loginReq.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36");
				//设置登陆使用的cookies
				StringBuffer loginCookies = new StringBuffer(checkCoolkies);
				loginCookies.append("_member_user_tonken_=0;refcheck=ok;refpay=0;refsite=;oilchem_refer_url=;oilchem_land_url=https://www.oilchem.net/;");
				loginReq.addHeader(new BasicHeader("Cookie",loginCookies.toString()));//登陆也使用请求验证码的cookie（和校验验证码一样）
				System.out.println("登陆发送的cookie:"+loginCookies.toString());
				//设置请求参数
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		        BasicNameValuePair username = new BasicNameValuePair("username", "gxgxmy");
    			BasicNameValuePair password = new BasicNameValuePair("password", "4963908eaa34eb2107f519b8dd298188");
    			BasicNameValuePair errorPaw = new BasicNameValuePair("errorPaw", "gx123456");
    			BasicNameValuePair target = new BasicNameValuePair("target", "https://www.oilchem.net/");
    			formparams.add(username);
    			formparams.add(password);
    			formparams.add(errorPaw);
    			formparams.add(target);
		        loginReq.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
				CloseableHttpClient httpClient3 = declareHttpClientSSL(loginReq.getURI().toString());
				response = httpClient3.execute(loginReq);
		        entity = response.getEntity();
		        html = EntityUtils.toString(entity, REQ_ENCODEING_UTF8);
		        if(StringUtils.isNotBlank(html)){
		        	if(html.contains("错误提示")){
		        		throw new RuntimeException("登陆失败！用户名或密码不正确!"); 
		        	}
		        	throw new RuntimeException("登陆失败！"); 
		        }
		        
		        //if(response!=null && (response.getStatusLine().getStatusCode()==SUCCESS_CODE)){
		        	//4.登陆成功，获取数据
		        	HttpUriRequest dataReq = new HttpGet(dataUrl);
		        	dataReq.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
					dataReq.setHeader("Accept-Encoding", "gzip, deflate, br");
					dataReq.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
					dataReq.setHeader("Connection", "keep-alive");
					dataReq.setHeader("Cache-Control", "no-chache");
					dataReq.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36");
		        	StringBuffer dataCookies = new StringBuffer(getCookies(response,"_member_user_tonken_"));
		        	dataCookies.append("refcheck=ok;refpay=0;refsite=;oilchem_refer_url=;oilchem_land_url=https://www.oilchem.net/;")
		        		.append("_user=gxgxmy;_pass=gx123456;_remberId=true;")
		        			.append("_imgCode="+_imgCode+";");
		        	System.out.println("获取数据的cookie:"+dataCookies.toString());
					dataReq.addHeader(new BasicHeader("Cookie",dataCookies.toString()));
					CloseableHttpClient httpClient4 = declareHttpClientSSL(dataReq.getURI().toString());
					response = httpClient4.execute(dataReq);
					
			        entity = response.getEntity();
			        html = EntityUtils.toString(entity, REQ_ENCODEING_UTF8);
			        if(html.contains("退出")){
			        	System.out.println("数据爬取成功！");
			        }
			        if(html.contains("登陆")){
			        	System.out.println("数据爬取失败！");
			        }
			        Document doc = Jsoup.parse(html);
			        String tag = "div.tableList.shows";
			        Elements showDivs = doc.select(tag);
			        for (int i = 0; i < showDivs.size(); i++) {
			        	Elements dataTables = showDivs.get(i).getElementsByTag("table");
			        	System.out.println(dataTables.get(0));
			        }
		        }
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testVeridateCode(){
		String URL_GETCODE = "https://passport.oilchem.net/member/login/getImgCode?timestamp="+System.currentTimeMillis();
		for (int i = 0; i < 20; i++) {
			String code;
			try {
				code = OrcUtils.getValidateCode(new File(getPicture(URL_GETCODE)));
				System.out.println(code);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 *
	* @Title: getPicture
	* @Description: 获取验证码图片
	* @param @param picUrl
	* @param @return    参数
	* @return String    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 下午4:21:37
	 */
	public String getPicture(String picUrl) {
        try {
        	HttpUriRequest codeReq = new HttpGet(picUrl);
			CloseableHttpClient httpClient = declareHttpClientSSL(codeReq.getURI().toString());
			CloseableHttpResponse response = httpClient.execute(codeReq);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    byte[] resource = EntityUtils.toByteArray(entity);
                    return saveJPG(resource);
                }
            } finally {
                response.close();
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
}

package com.zmd.test;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public abstract class QichaInterfaceUtils {
	/**
	 * 企查查http连接池
	 */
	public static PoolingHttpClientConnectionManager httpClientConnectionManager;
	static{
		httpClientConnectionManager = new PoolingHttpClientConnectionManager();
		httpClientConnectionManager.setMaxTotal(100);//设置最大总打开连接数为100
		httpClientConnectionManager.setDefaultMaxPerRoute(20);//设置每条路由的最大并发连接数为20，默认为2。
	}
}

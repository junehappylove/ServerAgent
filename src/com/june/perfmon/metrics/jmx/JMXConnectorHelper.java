package com.june.perfmon.metrics.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * 
 * JMX链接控制器 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:55:13
 */
public class JMXConnectorHelper {
	
	private static final Logger log = LoggingManager.getLoggerForClass();

	/**
	 * 取服务端连接器
	 * 
	 * @param url
	 * @param user
	 * @param pwd
	 * @return
	 * @date 2016年10月25日 下午4:55:56
	 * @writer iscas
	 */
	public MBeanServerConnection getServerConnection(String url, String user, String pwd) {
		try {
			JMXConnector connector = getJMXConnector(url, user, pwd);
			return connector.getMBeanServerConnection();
		} catch (Exception ex) {
			log.error("Failed to get JMX Connector", ex);
			throw new RuntimeException("Failed to get JMX Connector", ex);
		}
	}

	/**
	 * 取服务端连接器
	 * 
	 * @param url
	 * @return
	 * @date 2016年10月25日 下午4:56:38
	 * @writer iscas
	 */
	public MBeanServerConnection getServerConnection(String url) {
		try {
			JMXConnector connector = getJMXConnector(url);
			return connector.getMBeanServerConnection();
		} catch (Exception ex) {
			log.error("Failed to get JMX Connector", ex);
			throw new RuntimeException("Failed to get JMX Connector", ex);
		}
	}

	private JMXConnector getJMXConnector(String url) throws IOException {
		return getJMXConnector(url,null,null);
	}

	private JMXConnector getJMXConnector(String url, String usr, String pwd) throws MalformedURLException, IOException {
		String serviceUrl = "service:jmx:rmi:///jndi/rmi://" + url + "/jmxrmi";
		if ((usr == null) || (usr.trim().length() <= 0) || (pwd == null) || (pwd.trim().length() <= 0)) {
			JMXServiceURL surl = new JMXServiceURL(serviceUrl);
			return JMXConnectorFactory.connect(surl);
		}
		Map<String, Object> envMap = new HashMap<String, Object>();
		envMap.put("jmx.remote.credentials", new String[] { usr, pwd });
		envMap.put("java.naming.security.principal", usr);
		envMap.put("java.naming.security.credentials", pwd);
		return JMXConnectorFactory.connect(new JMXServiceURL(serviceUrl), envMap);
	}
}

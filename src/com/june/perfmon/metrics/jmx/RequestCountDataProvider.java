package com.june.perfmon.metrics.jmx;

import java.io.IOException;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * 
 * jmx中对cpu的使用率数据收集器 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:49:25
 */
class RequestCountDataProvider extends AbstractJMXDataProvider {
	MBeanServerConnection mBeanServerConn;

	public RequestCountDataProvider(MBeanServerConnection mBeanServerConn, boolean diff) throws Exception {
		super(mBeanServerConn, diff);
		this.mBeanServerConn = mBeanServerConn;
	}

	protected String getMXBeanType() {
		return JMXDataConst.JMX_REQUEST_COUNT;
	}

	protected Class<None> getMXBeanClass() {
		return None.class;
	}

	private volatile long last_count = 0;
	
	protected long getValueFromBean(Object bean) {
		long result = 0,ret=0l;
		try {
			Set<ObjectName> smbi = super.getObjectNames(mBeanServerConn);
			for (ObjectName obj : smbi) {
				ObjectName objname = new ObjectName(obj.getCanonicalName());
				long now = (int) mBeanServerConn.getAttribute(objname, "requestCount");
				result = result + now;
			}
			long temp = last_count;
			last_count  = result;
			ret = last_count - temp;
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (AttributeNotFoundException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}
}

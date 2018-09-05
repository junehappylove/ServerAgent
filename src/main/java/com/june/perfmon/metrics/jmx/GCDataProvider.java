package com.june.perfmon.metrics.jmx;

import java.lang.management.GarbageCollectorMXBean;
import javax.management.MBeanServerConnection;

/**
 * 
 * 垃圾回收数据收集器 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:49:25
 */
class GCDataProvider extends AbstractJMXDataProvider {
	public GCDataProvider(MBeanServerConnection mBeanServerConn, boolean diff)
			throws Exception {
		super(mBeanServerConn, diff);
	}

	protected String getMXBeanType() {
		return JMXDataConst.JMX_GC;
	}

	protected Class<GarbageCollectorMXBean> getMXBeanClass() {
		return GarbageCollectorMXBean.class;
	}

	protected long getValueFromBean(Object bean) {
		return ((GarbageCollectorMXBean) bean).getCollectionTime();
	}
}

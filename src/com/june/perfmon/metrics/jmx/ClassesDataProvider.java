package com.june.perfmon.metrics.jmx;

import java.lang.management.ClassLoadingMXBean;
import javax.management.MBeanServerConnection;

/**
 * 
 * class类数据收集器 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:48:48
 */
class ClassesDataProvider extends AbstractJMXDataProvider {
	
	public ClassesDataProvider(MBeanServerConnection mBeanServerConn,
			boolean diff) throws Exception {
		super(mBeanServerConn, diff);
	}

	protected String getMXBeanType() {
		return JMXDataConst.JMX_CLASSES;
	}

	protected Class<ClassLoadingMXBean> getMXBeanClass() {
		return ClassLoadingMXBean.class;
	}

	protected long getValueFromBean(Object bean) {
		return ((ClassLoadingMXBean) bean).getLoadedClassCount();
	}
}


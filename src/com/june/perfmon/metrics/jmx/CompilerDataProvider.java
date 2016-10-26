package com.june.perfmon.metrics.jmx;

import java.lang.management.CompilationMXBean;
import javax.management.MBeanServerConnection;

/**
 * 
 * 编译器数据收集器 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:49:11
 */
class CompilerDataProvider extends AbstractJMXDataProvider {
	public CompilerDataProvider(MBeanServerConnection mBeanServerConn,
			boolean diff) throws Exception {
		super(mBeanServerConn, diff);
	}

	protected String getMXBeanType() {
		return JMXDataConst.JMX_COMPILER;
	}

	protected Class<CompilationMXBean> getMXBeanClass() {
		return CompilationMXBean.class;
	}

	protected long getValueFromBean(Object bean) {
		return ((CompilationMXBean) bean).getTotalCompilationTime();
	}
}

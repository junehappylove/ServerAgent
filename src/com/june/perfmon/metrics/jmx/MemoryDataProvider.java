package com.june.perfmon.metrics.jmx;

import java.lang.management.MemoryMXBean;

import javax.management.MBeanServerConnection;

/**
 * 
 * 内存数据收集器 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:49:44
 */
class MemoryDataProvider extends AbstractJMXDataProvider {
	public static final int TYPE_USED = 0;
	public static final int TYPE_COMMITTED = 1;
	private int type = 0;

	public MemoryDataProvider(MBeanServerConnection mBeanServerConn,
			boolean diff, int aType) throws Exception {
		super(mBeanServerConn, diff);
		this.type = aType;
		this.bytesValue = true;
	}

	protected String getMXBeanType() {
		return JMXDataConst.JMX_MEMORY;
	}

	protected Class<MemoryMXBean> getMXBeanClass() {
		return MemoryMXBean.class;
	}

	protected long getValueFromBean(Object bean) {
		if (this.type == 1) {
			return ((MemoryMXBean) bean).getHeapMemoryUsage().getCommitted();
		}
		return ((MemoryMXBean) bean).getHeapMemoryUsage().getUsed();
	}
}


package com.june.perfmon.metrics.jmx;

import java.lang.management.MemoryPoolMXBean;

import javax.management.MBeanServerConnection;

/**
 * 
 * 内存池数据收集器 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:50:08
 */
class MemoryPoolDataProvider extends AbstractJMXDataProvider {
	public static final int TYPE_USED = 0;
	public static final int TYPE_COMMITTED = 1;
	private int type = 0;

	public MemoryPoolDataProvider(MBeanServerConnection mBeanServerConn, boolean diff, int aType) throws Exception {
		super(mBeanServerConn, diff);
		this.type = aType;
		this.bytesValue = true;
	}

	protected String getMXBeanType() {
		return JMXDataConst.JMX_MEMORY_POOL;
	}

	protected Class<MemoryPoolMXBean> getMXBeanClass() {
		return MemoryPoolMXBean.class;
	}

	protected long getValueFromBean(Object bean) {
		if (this.type == 1) {
			return ((MemoryPoolMXBean) bean).getUsage().getCommitted();
		}
		return ((MemoryPoolMXBean) bean).getUsage().getUsed();
	}
}


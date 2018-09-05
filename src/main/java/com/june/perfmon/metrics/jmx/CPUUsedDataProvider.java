package com.june.perfmon.metrics.jmx;

import com.sun.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;

/**
 * 
 * jmx中对cpu的使用率数据收集器 <br>
 * 返回的数据是乘以1000之后的数据，如 返回数据为  3，表示cpu的使用率为 :0.3%
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:49:25
 */
class CPUUsedDataProvider extends AbstractJMXDataProvider {
	public CPUUsedDataProvider(MBeanServerConnection mBeanServerConn, boolean diff) throws Exception {
		super(mBeanServerConn, diff);
	}

	protected String getMXBeanType() {
		return JMXDataConst.JMX_OS;
	}

	protected Class<OperatingSystemMXBean> getMXBeanClass() {
		return OperatingSystemMXBean.class;
	}

	protected long getValueFromBean(Object bean) {
		// 取cpu的使用率 ，这个使用率是乘以1000之后的了
		double num = ((OperatingSystemMXBean) bean).getProcessCpuLoad();
		String res = String.format("%.3f", num);
		long result = (long) (Double.parseDouble(res) * 1000);
		return result;
	}
}

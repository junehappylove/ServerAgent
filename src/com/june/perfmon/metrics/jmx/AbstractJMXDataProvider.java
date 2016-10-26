package com.june.perfmon.metrics.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * 
 * JMX数据提供器抽象类 <br>
 * jmx的所有数据对外提供，必须都继承此类
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:59:21
 */
public abstract class AbstractJMXDataProvider {
	protected final Set<Object> beans;
	private final boolean isDiff;
	private long prevValue = 0L;
	protected boolean bytesValue = false;

	public AbstractJMXDataProvider(MBeanServerConnection mBeanServerConn,
			boolean diff) throws Exception {
		this.isDiff = diff;
		this.beans = getMXBeans(mBeanServerConn);
	}

	public static AbstractJMXDataProvider getProvider(MBeanServerConnection mBeanServerConn, String type)
			throws Exception {
		if (type.startsWith("gc-")) {
			return new GCDataProvider(mBeanServerConn, true);
		}
		if (type.startsWith("class-")) {
			return new ClassesDataProvider(mBeanServerConn, false);
		}
		if (type.startsWith("compile-")) {
			return new CompilerDataProvider(mBeanServerConn, true);
		}
		if (type.startsWith("memorypool-")) {
			if (type.endsWith("-committed")) {
				return new MemoryPoolDataProvider(mBeanServerConn, false, 1);
			}
			return new MemoryPoolDataProvider(mBeanServerConn, false, 0);
		}
		if (type.startsWith("memory-")) {
			if (type.endsWith("-committed")) {
				return new MemoryDataProvider(mBeanServerConn, false, 1);
			}
			return new MemoryDataProvider(mBeanServerConn, false, 0);
		}
		if(type.startsWith("cpuused-")){
			return new CPUUsedDataProvider(mBeanServerConn, false);
		}
		throw new IllegalArgumentException("Can't define JMX type");
	}

	private Set<Object> getMXBeans(MBeanServerConnection mBeanServerConn)
			throws MalformedObjectNameException, NullPointerException,
			IOException {
		ObjectName gcAllObjectName = new ObjectName(getMXBeanType() + ",*");
		Set<?> gcMXBeanObjectNames = mBeanServerConn.queryNames(gcAllObjectName, null);
		Iterator<?> it = gcMXBeanObjectNames.iterator();
		Set<Object> res = new HashSet<Object>();
		while (it.hasNext()) {
			ObjectName on = (ObjectName) it.next();
			Object mxBean = ManagementFactory.newPlatformMXBeanProxy(mBeanServerConn, on.getCanonicalName(), getMXBeanClass());
			res.add(mxBean);
		}
		return res;
	}

	protected abstract String getMXBeanType();

	protected abstract Class<?> getMXBeanClass();

	protected abstract long getValueFromBean(Object paramObject);

	public boolean isBytesValue() {
		return this.bytesValue;
	}

	public void getValue(StringBuffer res) {
		getValue(res, 1);
	}

	public void getValue(StringBuffer res, int divider) {
		Iterator<Object> it = this.beans.iterator();
		long value = 0L;
		while (it.hasNext()) {
			value += getValueFromBean(it.next());
		}
		if (this.isDiff) {
			if (this.prevValue == 0L) {
				this.prevValue = value;
				value = 0L;
			} else {
				long oldVal = value;
				value -= this.prevValue;
				this.prevValue = oldVal;
			}
		}
		value /= divider;
		System.out.println(value);
		res.append(Long.toString(value));
	}
}

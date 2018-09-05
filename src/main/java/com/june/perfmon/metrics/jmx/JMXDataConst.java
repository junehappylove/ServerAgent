/**
 * 中科方德软件有限公司<br>
 * ServerAgent:com.june.perfmon.metrics.jmx.JMXDataConst.java
 * 日期:2016年10月25日
 */
package com.june.perfmon.metrics.jmx;

/**
 * JMX收集数据的常量定义 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年10月25日 下午4:50:56
 */
public final class JMXDataConst {
	public static final String JMX_CLASSES = "java.lang:type=ClassLoading";
	public static final String JMX_COMPILER = "java.lang:type=Compilation";
	public static final String JMX_GC = "java.lang:type=GarbageCollector";
	public static final String JMX_OS = "java.lang:type=OperatingSystem";
	public static final String JMX_MEMORY = "java.lang:type=Memory";
	public static final String JMX_MEMORY_POOL = "java.lang:type=MemoryPool";
	public static final String JMX_REQUEST_COUNT = "Catalina:type=GlobalRequestProcessor";
	public static final String JMX_BYTE_SEND = "Catalina:type=GlobalRequestProcessor";

}

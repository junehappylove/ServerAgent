package com.june.perfmon.metrics;

import java.util.Hashtable;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.SigarProxy;

import com.june.perfmon.metrics.jmx.JMXConnectorHelper;

public abstract class AbstractPerfMonMetric {
	private static final Logger log = LoggingManager.getLoggerForClass();
	protected static final String PARAMS_DELIMITER = ":";
	protected final SigarProxy sigarProxy;
	private static final Hashtable<String, Integer> unitDividingFactors = new Hashtable<String, Integer>();

	public AbstractPerfMonMetric(SigarProxy aSigar) {
		this.sigarProxy = aSigar;
		unitDividingFactors.put("b", new Integer(1));
		unitDividingFactors.put("kb", new Integer(1024));
		unitDividingFactors.put("mb", new Integer(1048576));
	}

	protected int getUnitDividingFactor(String unit) {
		if (!unitDividingFactors.containsKey(unit)) {
			return 1;
		}
		return ((Integer) unitDividingFactors.get(unit)).intValue();
	}

	/**
	 * 取监控值函数
	 * @param paramStringBuffer
	 * @throws Exception
	 * @date 2016年10月26日 下午6:19:49
	 * @writer iscas
	 */
	public abstract void getValue(StringBuffer paramStringBuffer)
			throws Exception;

	public static AbstractPerfMonMetric createMetric(String metricType, String metricParamsStr, SigarProxy sigarProxy) {
		log.debug("Creating metric: " + metricType + " with params: " + metricParamsStr);
		if (metricType.indexOf(' ') > 0) {
			metricType = metricType.substring(0, metricType.indexOf(' '));
		}
		MetricParamsSigar metricParams = MetricParamsSigar.createFromString(metricParamsStr, sigarProxy);
		AbstractPerfMonMetric metric;
		try {
			if (metricType.equalsIgnoreCase("exec")) {
				metric = new ExecMetric(metricParams);
			} else {
				if (metricType.equalsIgnoreCase("tail")) {
					metric = new TailMetric(metricParams);
				} else {
					if (metricType.equalsIgnoreCase("cpu")) {
						metric = AbstractCPUMetric.getMetric(sigarProxy, metricParams);
					} else {
						if (metricType.equalsIgnoreCase("memory")) {
							metric = AbstractMemMetric.getMetric(sigarProxy, metricParams);
						} else {
							if (metricType.equalsIgnoreCase("swap")) {
								metric = new SwapMetric(sigarProxy, metricParams);
							} else {
								if (metricType.equalsIgnoreCase("disks")) {
									metric = new DiskIOMetric(sigarProxy, metricParams);
								} else {
									if (metricType.equalsIgnoreCase("network")) {
										metric = new NetworkIOMetric(sigarProxy, metricParams);
									} else {
										if (metricType.equalsIgnoreCase("tcp")) {
											metric = new TCPStatMetric(sigarProxy, metricParams);
										} else {
											if (metricType.equalsIgnoreCase("jmx")) {
												metric = new JMXMetric(metricParams, new JMXConnectorHelper());
											} else {
												throw new RuntimeException("No collector object for metric type " + metricType);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (IllegalArgumentException ex) {
			log.error(ex.toString());
			log.error("Invalid parameters specified for metric " + metricType + ": " + metricParams);
			metric = new InvalidPerfMonMetric();
		} catch (RuntimeException ex) {
			log.error("Invalid metric specified: " + metricType, ex);
			metric = new InvalidPerfMonMetric();
		}
		log.debug("Have metric object: " + metric.toString());
		return metric;
	}
}

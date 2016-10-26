package com.june.perfmon.metrics;

import org.hyperic.sigar.SigarProxy;

abstract class AbstractMemMetric extends AbstractPerfMonMetric {
	//private static final Logger log = LoggingManager.getLoggerForClass();
	protected final MetricParamsSigar params;

	public static AbstractMemMetric getMetric(SigarProxy sigar,
			MetricParamsSigar params) {
		if (params.PID >= 0L) {
			return new MemProcMetric(sigar, params);
		}
		return new MemTotalMetric(sigar, params);
	}

	public AbstractMemMetric(SigarProxy aSigar, MetricParamsSigar params) {
		super(aSigar);
		this.params = params;
	}
}
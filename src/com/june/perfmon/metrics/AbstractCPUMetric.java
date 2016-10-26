package com.june.perfmon.metrics;

import org.hyperic.sigar.SigarProxy;

abstract class AbstractCPUMetric extends AbstractPerfMonMetric {
	//private static final Logger log = LoggingManager.getLoggerForClass();
	protected final MetricParamsSigar params;

	public static AbstractCPUMetric getMetric(SigarProxy sigar, MetricParamsSigar params) {
		if (params.PID >= 0L) {
			return new CPUProcMetric(sigar, params);
		}
		return new CPUTotalMetric(sigar, params);
	}

	protected AbstractCPUMetric(SigarProxy aSigar, MetricParamsSigar params) {
		super(aSigar);
		this.params = params;
	}
}

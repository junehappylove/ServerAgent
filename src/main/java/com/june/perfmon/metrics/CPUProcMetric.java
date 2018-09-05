package com.june.perfmon.metrics;

import java.util.Arrays;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

class CPUProcMetric extends AbstractCPUMetric {
	public static final byte PERCENT = 0;
	public static final byte TOTAL = 1;
	public static final byte SYSTEM = 2;
	public static final byte USER = 3;
	public static final String[] types = { "percent", "total", "system", "user" };
	private int type = -1;
	private double prev = -1.0D;

	protected CPUProcMetric(SigarProxy aSigar, MetricParamsSigar params) {
		super(aSigar, params);
		if (params.type.length() == 0) {
			this.type = 0;
		} else {
			this.type = Arrays.asList(types).indexOf(params.type);
			if (this.type < 0) {
				throw new IllegalArgumentException("Invalid process cpu type: " + params.type);
			}
		}
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		ProcCpu cpu = this.sigarProxy.getProcCpu(this.params.PID);
		double val;
		long cur;
		switch (this.type) {
		case 0:
			val = 100.0D * cpu.getPercent();
			break;
		case 1:
			cur = cpu.getTotal();
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 2:
			cur = cpu.getSys();
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 3:
			cur = cpu.getUser();
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		default:
			throw new SigarException("Unknown proc cpu type " + this.type);
		}
		res.append(Double.toString(val));
	}
}

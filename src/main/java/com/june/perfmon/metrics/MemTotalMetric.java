package com.june.perfmon.metrics;

import java.util.Arrays;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

class MemTotalMetric extends AbstractMemMetric {
	public static final byte ACTUAL_FREE = 0;
	public static final byte ACTUAL_USED = 1;
	public static final byte FREE = 2;
	public static final byte FREE_PERCENT = 3;
	public static final byte RAM = 4;
	public static final byte TOTAL = 5;
	public static final byte USED = 6;
	public static final byte USED_PERCENT = 7;
	public static final String[] types = { "actualfree", "actualused", "free",
			"freeperc", "ram", "total", "used", "usedperc" };
	private int type = -1;
	private int dividingFactor = 1;

	public MemTotalMetric(SigarProxy aSigar, MetricParamsSigar params) {
		super(aSigar, params);
		if (params.type.length() == 0) {
			this.type = 7;
		} else {
			this.type = Arrays.asList(types).indexOf(params.type);
			if (this.type < 0) {
				throw new IllegalArgumentException("Invalid total mem type: "
						+ params.type);
			}
		}
		this.dividingFactor = getUnitDividingFactor(params.getUnit());
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		Mem mem = this.sigarProxy.getMem();

		int factor = 1;
		double val;
		switch (this.type) {
		case 0:
			val = mem.getActualFree();
			factor = this.dividingFactor;
			break;
		case 1:
			val = mem.getActualUsed();
			factor = this.dividingFactor;
			break;
		case 2:
			val = mem.getFree();
			factor = this.dividingFactor;
			break;
		case 3:
			val = mem.getFreePercent();
			break;
		case 4:
			val = mem.getRam();
			break;
		case 5:
			val = mem.getTotal();
			factor = this.dividingFactor;
			break;
		case 6:
			val = mem.getUsed();
			factor = this.dividingFactor;
			break;
		case 7:
			val = mem.getUsedPercent();
			break;
		default:
			throw new SigarException("Unknown total mem type " + this.type);
		}
		val /= factor;
		res.append(Double.toString(val));
	}
}

/*
 * Location: C:\Users\dell\Desktop\wjw\ServerAgent-2.2.1\ServerAgent.jar
 * Qualified Name: com.june.perfmon.metrics.MemTotalMetric JD-Core Version:
 * 0.7.0.1
 */
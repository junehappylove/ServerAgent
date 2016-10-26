package com.june.perfmon.metrics;

import java.util.Arrays;

import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.Swap;

class SwapMetric extends AbstractPerfMonMetric {
	public static final byte PAGE_IN = 0;
	public static final byte PAGE_OUT = 1;
	public static final byte FREE = 2;
	public static final byte TOTAL = 3;
	public static final byte USED = 4;
	public static final String[] types = { "pagein", "pageout", "free", "total", "used" };
	private int type = -1;
	private double prev = -1.0D;
	private int dividingFactor = 1;

	public SwapMetric(SigarProxy aSigar, MetricParamsSigar params) {
		super(aSigar);
		if (params.type.length() == 0) {
			this.type = 4;
		} else {
			this.type = Arrays.asList(types).indexOf(params.type);
			if (this.type < 0) {
				throw new IllegalArgumentException("Unknown swap type: " + params.type);
			}
		}
		this.dividingFactor = getUnitDividingFactor(params.getUnit());
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		Swap mem = this.sigarProxy.getSwap();

		int factor = 1;
		double cur;
		double val;
		switch (this.type) {
		case 0:
			cur = mem.getPageIn();
			val = this.prev != -1.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 1:
			cur = mem.getPageOut();
			val = this.prev != -1.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 2:
			val = mem.getFree();
			factor = this.dividingFactor;
			break;
		case 3:
			val = mem.getTotal();
			factor = this.dividingFactor;
			break;
		case 4:
			val = mem.getUsed();
			factor = this.dividingFactor;
			break;
		default:
			throw new SigarException("Unknown swap type " + this.type);
		}
		val /= factor;
		res.append(Double.toString(val));
	}
}

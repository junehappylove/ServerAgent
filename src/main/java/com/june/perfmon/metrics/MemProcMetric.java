package com.june.perfmon.metrics;

import java.util.Arrays;

import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

class MemProcMetric extends AbstractMemMetric {
	public static final byte VIRTUAL = 0;
	public static final byte SHARED = 1;
	public static final byte PAGE_FAULTS = 2;
	public static final byte MAJOR_FAULTS = 3;
	public static final byte MINOR_FAULTS = 4;
	public static final byte RESIDENT = 5;
	public static final String[] types = { "virtual", "shared", "pagefaults",
			"majorfaults", "minorfaults", "resident" };
	private int type = -1;
	private double prev = -1.0D;
	private int dividingFactor = 1;

	public MemProcMetric(SigarProxy aSigar, MetricParamsSigar params) {
		super(aSigar, params);
		if (params.type.length() == 0) {
			this.type = 5;
		} else {
			this.type = Arrays.asList(types).indexOf(params.type);
			if (this.type < 0) {
				throw new IllegalArgumentException("Invalid proc mem type: "
						+ params.type);
			}
		}
		this.dividingFactor = getUnitDividingFactor(params.getUnit());
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		ProcMem mem = this.sigarProxy.getProcMem(this.params.PID);

		int factor = 1;
		double val;
		long cur;
		switch (this.type) {
		case 0:
			val = mem.getSize();
			factor = this.dividingFactor;
			break;
		case 1:
			cur = mem.getShare();
			this.prev = cur;
			val = cur;
			factor = this.dividingFactor;
			break;
		case 2:
			cur = mem.getPageFaults();
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 3:
			cur = mem.getMajorFaults();
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 4:
			cur = mem.getMinorFaults();
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 5:
			cur = mem.getResident();
			this.prev = cur;
			val = cur;
			factor = this.dividingFactor;
			break;
		default:
			throw new SigarException("Unknown proc mem type " + this.type);
		}
		val /= factor;
		res.append(Double.toString(val));
	}
}

/*
 * Location: C:\Users\dell\Desktop\wjw\ServerAgent-2.2.1\ServerAgent.jar
 * Qualified Name: com.june.perfmon.metrics.MemProcMetric JD-Core Version: 0.7.0.1
 */
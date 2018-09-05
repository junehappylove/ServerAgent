package com.june.perfmon.metrics;

import java.util.Arrays;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

class CPUTotalMetric extends AbstractCPUMetric {
	private static final Logger log = LoggingManager.getLoggerForClass();
	public static final byte COMBINED = 0;
	public static final byte IDLE = 1;
	public static final byte IRQ = 2;
	public static final byte NICE = 3;
	public static final byte SOFTIRQ = 4;
	public static final byte STOLEN = 5;
	public static final byte SYSTEM = 6;
	public static final byte USER = 7;
	public static final byte IOWAIT = 8;
	public static final String[] types = { "combined", "idle", "irq", "nice",
			"softirq", "stolen", "system", "user", "iowait" };
	private int type = -1;
	private int coreID = -1;

	protected CPUTotalMetric(SigarProxy aSigar, MetricParamsSigar params) {
		super(aSigar, params);
		if (params.type.length() == 0) {
			this.type = 0;
		} else {
			this.type = Arrays.asList(types).indexOf(params.type);
			if (this.type < 0) {
				throw new IllegalArgumentException("Invalid total cpu type: " + params.type);
			}
		}
		if (params.coreID >= 0) {
			int avail;
			try {
				avail = aSigar.getCpuList().length;
			} catch (SigarException ex) {
				throw new IllegalArgumentException("Cannot get CPU count at this system: " + ex.getMessage());
			}
			if (params.coreID >= avail) {
				throw new IllegalArgumentException("Invalid core ID on this system: " + params.type);
			}
			this.coreID = params.coreID;
		}
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		CpuPerc cpu;
		if (this.coreID < 0) {
			cpu = this.sigarProxy.getCpuPerc();
		} else {
			cpu = this.sigarProxy.getCpuPercList()[this.coreID];
		}
		double val;
		switch (this.type) {
		case 0:
			val = cpu.getCombined();
			break;
		case 1:
			val = cpu.getIdle();
			break;
		case 2:
			val = cpu.getIrq();
			break;
		case 3:
			val = cpu.getNice();
			break;
		case 4:
			val = cpu.getSoftIrq();
			break;
		case 5:
			val = cpu.getStolen();
			break;
		case 6:
			val = cpu.getSys();
			break;
		case 7:
			val = cpu.getUser();
			break;
		case 8:
			val = cpu.getWait();
			break;
		default:
			throw new SigarException("Unknown proc total type " + this.type);
		}
		if (!Double.isNaN(val)) {
			res.append(Double.toString(100.0D * val));
		} else {
			log.warn("Failed to get total cpu metric: " + types[this.type]);
		}
	}
}
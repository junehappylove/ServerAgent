package com.june.perfmon.metrics;

import java.util.Arrays;
import java.util.LinkedList;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

class NetworkIOMetric extends AbstractPerfMonMetric {
	private static final Logger log = LoggingManager.getLoggerForClass();
	public static final byte RX_BYTES = 0;
	public static final byte RX_DROPPED = 1;
	public static final byte RX_ERRORS = 2;
	public static final byte RX_FRAME = 3;
	public static final byte RX_OVERRUNS = 4;
	public static final byte RX_PACKETS = 5;
	public static final byte TX_BYTES = 6;
	public static final byte TX_CARRIER = 7;
	public static final byte TX_COLLISIONS = 8;
	public static final byte TX_DROPPED = 9;
	public static final byte TX_ERRORS = 10;
	public static final byte TX_OVERRUNS = 11;
	public static final byte USED = 12;
	public static final byte SPEED = 13;
	public static final byte TX_PACKETS = 14;
	public static final String[] types = { "bytesrecv", "rxdrops", "rxerr",
			"rxframe", "rxoverruns", "rx", "bytessent", "txcarrier",
			"txcollisions", "txdrops", "txerr", "txoverruns", "used", "speed",
			"tx" };
	private int type = -1;
	private final String[] interfaces;
	private double prev = -1.0D;
	private int dividingFactor = 1;

	public NetworkIOMetric(SigarProxy aSigar, MetricParams params) {
		super(aSigar);
		if (params.type.length() == 0) {
			this.type = 0;
		} else {
			this.type = Arrays.asList(types).indexOf(params.type);
			if (this.type < 0) {
				throw new IllegalArgumentException("Unknown net io type: " + params.type);
			}
		}
		log.debug("Net metric type: " + this.type);

		LinkedList<String> list = new LinkedList<String>();
		if (params.iface.length() != 0) {
			list.add(params.iface);
		} else {
			try {
				list.addAll(Arrays.asList(aSigar.getNetInterfaceList()));
			} catch (SigarException ex) {
				log.warn("Can't get network interfaces list", ex);
			}
		}
		this.interfaces = ((String[]) list.toArray(new String[0]));
		this.dividingFactor = getUnitDividingFactor(params.getUnit());
	}

	static void logAvailableInterfaces(SigarProxy sigar) {
		log.info("*** Logging available network interfaces ***");
		try {
			String[] list = sigar.getNetInterfaceList();
			for (int n = 0; n < list.length; n++) {
				NetInterfaceConfig ifc = sigar.getNetInterfaceConfig(list[n]);
				log.info("Network interface: iface=" + ifc.getName() + " addr=" + ifc.getAddress() + " type=" + ifc.getType());
			}
		} catch (SigarException e) {
			log.debug("Can't get network info", e);
		}
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		double val = 0.0D;

		int factor = 1;
		for (int n = 0; n < this.interfaces.length; n++) {
			NetInterfaceStat usage;
			try {
				usage = this.sigarProxy.getNetInterfaceStat(this.interfaces[n]);
			} catch (SigarException e) {
				log.error("Failed to get interface stat: " + this.interfaces[n], e);
				continue;
			}
			switch (this.type) {
			case 0:
				val += usage.getRxBytes();
				factor = this.dividingFactor;
				break;
			case 1:
				val += usage.getRxDropped();
				break;
			case 2:
				val += usage.getRxErrors();
				break;
			case 3:
				val += usage.getRxFrame();
				break;
			case 4:
				val += usage.getRxOverruns();
				break;
			case 5:
				val += usage.getRxPackets();
				break;
			case 6:
				val += usage.getTxBytes();
				factor = this.dividingFactor;
				break;
			case 7:
				val += usage.getTxCarrier();
				break;
			case 8:
				val += usage.getTxCollisions();
				break;
			case 9:
				val += usage.getTxDropped();
				break;
			case 10:
				val += usage.getTxErrors();
				break;
			case 11:
				val += usage.getTxOverruns();
				break;
			case 12:
				val = usage.getTxPackets();
				break;
			case 13:
				val = usage.getSpeed();
				break;
			case 14:
				val += usage.getTxPackets();
				break;
			default:
				throw new SigarException("Unknown net io type " + this.type);
			}
		}
		switch (this.type) {
		case 13:
			break;
		case 12:
			break;
		default:
			double cur = val;
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
		}
		val /= factor;
		res.append(Double.toString(val));
	}
}

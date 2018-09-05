package com.june.perfmon.metrics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

class DiskIOMetric extends AbstractPerfMonMetric {
	private static final Logger log = LoggingManager.getLoggerForClass();
	public static final byte AVAILABLE = 0;
	public static final byte DISK_QUEUE = 1;
	public static final byte READ_BYTES = 2;
	public static final byte READS = 3;
	public static final byte SERVICE_TIME = 4;
	public static final byte WRITE_BYTES = 5;
	public static final byte WRITES = 6;
	public static final byte FILES = 7;
	public static final byte FREE = 8;
	public static final byte FREE_FILES = 9;
	public static final byte TOTAL = 10;
	public static final byte USE_PERCENT = 11;
	public static final byte USED = 12;
	public static final String[] types = { "available", "queue", "readbytes", "reads", "service", "writebytes",
			"writes", "files", "free", "freefiles", "total", "useperc", "used" };
	private int type = -1;
	private final String[] filesystems;
	private double prev = -1.0D;
	private int dividingFactor = 1;

	public DiskIOMetric(SigarProxy aSigar, MetricParamsSigar params) {
		super(aSigar);
		if (params.type.length() == 0) {
			this.type = 1;
		} else {
			this.type = Arrays.asList(types).indexOf(params.type);
			if (this.type < 0) {
				throw new IllegalArgumentException("Invalid disk io type: " + params.type);
			}
		}
		log.debug("Disk metric type: " + this.type);

		LinkedList<Object> list = new LinkedList<Object>();
		if (params.fs.length() != 0) {
			list.add(params.fs);
		} else {
			getAllDiskFilesystems(aSigar, list);
		}
		this.filesystems = ((String[]) list.toArray(new String[0]));
		this.dividingFactor = getUnitDividingFactor(params.getUnit());
	}

	private void getAllDiskFilesystems(SigarProxy aSigar, LinkedList<Object> list) {
		try {
			FileSystemMap map = aSigar.getFileSystemMap();
			Iterator<?> it = map.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				FileSystem fs = (FileSystem) map.get(key);
				if (fs.getType() == 2) {
					list.add(key);
				}
			}
		} catch (SigarException e) {
			log.warn("Can't get filesystems map", e);
		}
	}

	public static void logAvailableFilesystems(SigarProxy aSigar) {
		log.info("*** Logging available filesystems ***");
		try {
			FileSystemMap map = aSigar.getFileSystemMap();
			Iterator<?> it = map.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				FileSystem fs = (FileSystem) map.get(key);
				log.info("Filesystem: fs=" + fs.toString() + " type=" + fs.getSysTypeName());
			}
		} catch (SigarException e) {
			log.warn("Can't get filesystems map", e);
		}
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		double val = 0.0D;
		long used = 0L;
		long total = 0L;

		int factor = 1;
		for (int n = 0; n < this.filesystems.length; n++) {
			FileSystemUsage usage = this.sigarProxy.getFileSystemUsage(this.filesystems[n]);
			switch (this.type) {
			case 0:
				val += usage.getAvail();
				factor = this.dividingFactor;
				break;
			case 1:
				val += usage.getDiskQueue();
				break;
			case 2:
				val += usage.getDiskReadBytes();
				factor = this.dividingFactor;
				break;
			case 3:
				val += usage.getDiskReads();
				break;
			case 4:
				val += usage.getDiskServiceTime();
				break;
			case 5:
				val += usage.getDiskWriteBytes();
				factor = this.dividingFactor;
				break;
			case 6:
				val += usage.getDiskWrites();
				break;
			case 7:
				val += usage.getFiles();
				factor = this.dividingFactor;
				break;
			case 8:
				val += usage.getFree();
				factor = this.dividingFactor;
				break;
			case 9:
				val += usage.getFreeFiles();
				factor = this.dividingFactor;
				break;
			case 10:
				val += usage.getTotal();
				factor = this.dividingFactor;
				break;
			case 11:
				if (this.filesystems.length > 1) {
					used += usage.getUsed();
					total += usage.getTotal();
				} else {
					val += 100.0D * usage.getUsePercent();
				}
				break;
			case 12:
				val = usage.getUsed();
				factor = this.dividingFactor;
				break;
			default:
				throw new SigarException("Unknown disk I/O type " + this.type);
			}
		}
		double cur;
		switch (this.type) {
		case 2:
			cur = val;
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 3:
			cur = val;
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 4:
			cur = val;
			break;
		case 5:
			cur = val;
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 6:
			cur = val;
			val = this.prev > 0.0D ? cur - this.prev : 0.0D;
			this.prev = cur;
			break;
		case 11:
			if (this.filesystems.length > 1) {
				val = 100.0D * used / total;
			}
			break;
		}
		val /= factor;
		res.append(Double.toString(val));
	}
}
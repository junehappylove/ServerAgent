package com.june.perfmon.metrics;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.ptql.ProcessFinder;

class MetricParamsSigar extends MetricParams {
	private static final Logger log = LoggingManager.getLoggerForClass();
	private final SigarProxy sigar;

	static MetricParamsSigar createFromString(String metricParamsStr, SigarProxy sigarProxy) {
		MetricParamsSigar inst = new MetricParamsSigar(sigarProxy);
		parseParams(metricParamsStr, inst);
		return inst;
	}

	protected MetricParamsSigar(SigarProxy sigarProxy) {
		this.sigar = sigarProxy;
	}

	protected void populateParams(String token, List<String> params)
			throws NumberFormatException {
		if (token.startsWith("name=")) {
			this.PID = getPIDByName(token);
		} else if (token.startsWith("ptql=")) {
			this.PID = getPIDByPTQL(token);
		} else {
			super.populateParams(token, params);
		}
	}

	private long getPIDByName(String token) {
		String name = token.substring(token.indexOf("=") + 1);
		String[] parts = name.split("#");
		try {
			long index = parts.length > 1 ? Long.parseLong(parts[1]) : 0L;
			this.PID = getPIDByProcName(parts[0], index);
			if (this.PID <= 0L) {
				log.warn("Enable to find process from name: " + name);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			log.warn("Error processing token: " + token, e);
			this.PID = -1L;
		} catch (NumberFormatException e) {
			log.warn("Error processing token: " + token, e);
			this.PID = -1L;
		}
		return this.PID;
	}

	private long getPIDByPTQL(String token) {
		String query = token.substring(token.indexOf("=") + 1);
		try {
			@SuppressWarnings("deprecation")
			long[] pids = new ProcessFinder(this.sigar).find(query);
			if (pids.length < 1) {
				log.warn("Unable to find process from query: " + query);
				return -1L;
			}
			return pids[0];
		} catch (SigarException ex) {
			log.warn("Error querying PTQL: " + query, ex);
		}
		return -1L;
	}

	private long getPIDByProcName(String name, long index) {
		int procIndex = 0;
		long[] list;
		try {
			list = this.sigar.getProcList();
		} catch (SigarException ex) {
			log.error("Failed to get process list", ex);
			return -1L;
		}
		for (int n = 0; n < list.length; n++) {
			ProcExe proc;
			try {
				proc = this.sigar.getProcExe(list[n]);
			} catch (SigarException e) {
				log.debug("Can't get process exe for pid " + list[n], e);
				continue;
			}
			String pname = proc.getName().toLowerCase();
			if (pname.endsWith(File.separator + name.toLowerCase())) {
				if (procIndex == index) {
					return list[n];
				}
				procIndex++;
			}
		}
		return -1L;
	}

	static void logAvailableProcesses(SigarProxy sigar) {
		log.info("*** Logging available processes ***");

		long[] list = new long[0];
		try {
			list = sigar.getProcList();
		} catch (SigarException ex) {
			log.error("Failed to get process list", ex);
		}
		HashMap<String, Long> nameMap = new HashMap<String, Long>();
		for (int n = 0; n < list.length; n++) {
			ProcExe proc;
			try {
				proc = sigar.getProcExe(list[n]);
			} catch (SigarException e) {
				log.debug("Can't get process exe for pid " + list[n], e);
				continue;
			}
			StringBuffer str = new StringBuffer("Process: ");
			str.append("pid=").append(list[n]).append(' ');
			String pname = proc.getName()
					.substring(proc.getName().lastIndexOf(File.separator) + 1)
					.toLowerCase();
			str.append("name=").append(pname);
			if (nameMap.containsKey(pname)) {
				Long val = (Long) nameMap.get(pname);
				val = new Long(val.longValue() + 1L);
				nameMap.put(pname, val);
				str.append('#').append(val);
			} else {
				nameMap.put(pname, new Long(0L));
			}
			try {
				String[] args = sigar.getProcArgs(list[n]);
				str.append(" args=");
				join(str, args, " ");
			} catch (SigarException ex) {
				log.debug("Can't get process args for pid " + list[n], ex);
			}
			log.info(str.toString());
		}
	}
}

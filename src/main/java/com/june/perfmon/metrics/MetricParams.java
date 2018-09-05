package com.june.perfmon.metrics;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class MetricParams {
	private static final Logger log = LoggingManager.getLoggerForClass();
	String label = "";
	long PID = -1L;
	int coreID = -1;
	String fs = "";
	String iface = "";
	String[] params = new String[0];
	String type = "";
	String unit = "";

	protected static void parseParams(String metricParams, MetricParams inst)
			throws NumberFormatException {
		String[] tokens = metricParams.split("(?<!\\\\):");

		List<String> params = new LinkedList<String>();
		for (int i = 0; i < tokens.length; i++) {
			inst.populateParams(tokens[i].replaceAll("\\\\:", ":"), params);
		}
		inst.params = ((String[]) params.toArray(new String[0]));
	}

	protected void populateParams(String token, List<String> params)
			throws NumberFormatException {
		if (token.startsWith("pid=")) {
			this.PID = getPIDByPID(token);
		} else if (token.startsWith("iface=")) {
			this.iface = getParamValue(token);
		} else if (token.startsWith("label=")) {
			this.label = getParamValue(token);
		} else if (token.startsWith("fs=")) {
			this.fs = getParamValue(token);
		} else if (token.startsWith("core=")) {
			this.coreID = Integer.parseInt(getParamValue(token));
		} else if (token.startsWith("unit=")) {
			this.unit = getParamValue(token);
		} else {
			params.add(token);
			this.type = token;
		}
	}

	protected static StringTokenizer tokenizeString(String metricParams) {
		return new StringTokenizer(metricParams, ":");
	}

	public static MetricParams createFromString(String metricParams) {
		MetricParams inst = new MetricParams();
		parseParams(metricParams, inst);
		return inst;
	}

	public static String join(StringBuffer buff, Object[] array, String delim) {
		if (buff == null) {
			buff = new StringBuffer();
		}
		boolean haveDelim = delim != null;
		for (int i = 0; i < array.length; i++) {
			buff.append(array[i]);
			if ((haveDelim) && (i + 1 < array.length)) {
				buff.append(delim);
			}
		}
		return buff.toString();
	}

	public String getLabel() {
		return this.label;
	}

	public String getUnit() {
		return this.unit;
	}

	public static String getParamValue(String token) {
		return token.substring(token.indexOf("=") + 1);
	}

	private static long getPIDByPID(String token) {
		long PID;
		try {
			String PIDStr = token.substring(token.indexOf("=") + 1);
			PID = Long.parseLong(PIDStr);
		} catch (ArrayIndexOutOfBoundsException e) {
			log.warn("Error processing token: " + token, e);
			PID = -1L;
		} catch (NumberFormatException e) {
			log.warn("Error processing token: " + token, e);
			PID = -1L;
		}
		return PID;
	}
}

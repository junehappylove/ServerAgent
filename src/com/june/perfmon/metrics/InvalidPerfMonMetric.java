package com.june.perfmon.metrics;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

class InvalidPerfMonMetric extends AbstractPerfMonMetric {
	private static final Logger log = LoggingManager.getLoggerForClass();

	public InvalidPerfMonMetric() {
		super(null);
	}

	@Override
	public void getValue(StringBuffer res) {
		log.debug("Invalid metric stub hit");
		res.append("");
	}
}


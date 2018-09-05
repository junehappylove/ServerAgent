package com.june.perfmon.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.SigarException;

class TailMetric extends AbstractPerfMonMetric {
	private static final Logger log = LoggingManager.getLoggerForClass();
	private String filename;
	private BufferedReader reader;

	public TailMetric(MetricParams params) {
		super(null);
		if (params.params.length == 0) {
			throw new IllegalArgumentException("Cannot tail unspecified file");
		}
		String string = MetricParamsSigar.join(null, params.params, ":");
		log.debug("Tailing file: " + string);
		this.filename = string;
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		String lastLine = "";
		try {
			String line;
			while ((line = getReader().readLine()) != null) {
				log.debug("Read line: " + line);
				lastLine = line;
			}
			res.append(lastLine);
		} catch (IOException e) {
			log.error("Cannot read lines from file: " + this.filename);
		}
	}

	private BufferedReader getReader() throws IOException {
		if (this.reader == null) {
			this.reader = new BufferedReader(new FileReader(new File(
					this.filename)));
		}
		return this.reader;
	}
}

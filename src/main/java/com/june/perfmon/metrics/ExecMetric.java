package com.june.perfmon.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

class ExecMetric extends AbstractPerfMonMetric {
	private static final Logger log = LoggingManager.getLoggerForClass();
	private String[] command;

	public ExecMetric(MetricParams params) {
		super(null);
		if (params.params.length == 0) {
			throw new IllegalArgumentException("Params cannot be null");
		}
		this.command = params.params;
	}

	@Override
	public void getValue(StringBuffer res) throws Exception {
		log.debug("Executing custom script: " + MetricParams.join(null, this.command, " "));
		try {
			Process p = Runtime.getRuntime().exec(this.command);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String e;
			while ((e = stdErr.readLine()) != null) {
				log.error("Error: " + e);
			}
			String lastStr = "";
			String s;
			while ((s = stdInput.readLine()) != null) {
				log.debug("Read proc out line: " + s);
				lastStr = s;
			}
			res.append(lastStr);

			stdErr.close();
			stdInput.close();
			p.destroy();
		} catch (IOException e) {
			log.error("Problems executing: " + this.command[0], e);
		}
	}
}

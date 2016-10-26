package com.june.perfmon.client;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AbstractTransport implements Transport {
	private static final Logger log = LoggingManager.getLoggerForClass();;
	protected final PipedOutputStream pos;
	protected final PipedInputStream pis;
	private String label;

	public AbstractTransport() throws IOException {
		this.pos = new PipedOutputStream();
		this.pis = new PipedInputStream(this.pos);
	}

	public String[] readMetrics() {
		String str = readln();
		return str.split("\t");
	}

	public void setInterval(long interval) {
		log.debug("Setting interval to " + interval);
		try {
			writeln("interval:" + interval);
		} catch (IOException ex) {
			log.error("Error setting interval", ex);
		}
	}

	public void shutdownAgent() {
		log.info("Shutting down the agent");
		try {
			writeln("shutdown");
		} catch (IOException ex) {
			log.error("Error shutting down", ex);
		}
	}

	public void startWithMetrics(String[] metricsArray) throws IOException {
		String cmd = "metrics:";
		for (int n = 0; n < metricsArray.length; n++) {
			cmd = cmd + metricsArray[n].replace('\t', ' ') + "\t";
		}
		log.debug("Starting with metrics: " + cmd);
		writeln(cmd);
	}

	public void disconnect() {
		log.debug("Disconnecting from " + this.label);
		try {
			writeln("exit");
		} catch (IOException ex) {
			log.error("Error during exit", ex);
		}
	}

	public boolean test() {
		try {
			writeln("test");
		} catch (IOException ex) {
			log.error("Failed to send command", ex);
			return false;
		}
		return readln().startsWith("Yep");
	}

	protected String getNextLine(int newlineCount) throws IOException {
		if (newlineCount == 0) {
			return "";
		}
		StringBuffer str = new StringBuffer();
		while (this.pis.available() > 0) {
			int b = this.pis.read();
			if (b == -1) {
				return "";
			}
			if (b == 10) {
				newlineCount--;
				if (newlineCount == 0) {
					log.debug("Read lines: " + str.toString());
					String[] lines = str.toString().split("\n");

					return lines[(lines.length - 1)];
				}
			}
			str.append((char) b);
		}
		return "";
	}

	public String getAddressLabel() {
		return this.label;
	}

	public void setAddressLabel(String label) {
		this.label = label;
	}
}

package com.june.perfmon.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

class StreamTransport extends AbstractTransport {
	private static final Logger log = LoggingManager.getLoggerForClass();;
	private InputStream is;
	private OutputStream os;

	public StreamTransport() throws IOException {
	}

	public void setStreams(InputStream i, OutputStream o) {
		this.is = i;
		this.os = o;
	}

	public String readln() {
		int nlCount = 0;
		try {
			int b;
			while ((b = this.is.read()) >= 0) {
				this.pos.write(b);
				if (b == 10) {
					nlCount++;
					return getNextLine(nlCount);
				}
			}
			return "";
		} catch (IOException ex) {
			if (nlCount > 0) {
				log.error("Error reading next line", ex);
			}
		}
		return "";
	}

	public void writeln(String line) throws IOException {
		this.os.write(line.concat("\n").getBytes());
	}
}

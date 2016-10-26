package com.june.perfmon.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

class NIOTransport extends AbstractTransport {
	private static final Logger log = LoggingManager.getLoggerForClass();;
	private ReadableByteChannel readChannel;
	private WritableByteChannel writeChannel;

	public NIOTransport() throws IOException {
	}

	public void setChannels(ReadableByteChannel reader,
			WritableByteChannel writer) {
		this.readChannel = reader;
		this.writeChannel = writer;
	}

	public void disconnect() {
		super.disconnect();
		try {
			if (this.readChannel.isOpen()) {
				this.readChannel.close();
			}
			if (this.writeChannel.isOpen()) {
				this.writeChannel.close();
			}
		} catch (IOException ex) {
			log.error("Error closing transport", ex);
		}
	}

	public void writeln(String line) throws IOException {
		this.writeChannel.write(ByteBuffer.wrap(line.concat("\n").getBytes()));
	}

	public String readln() {
		ByteBuffer buf = ByteBuffer.allocateDirect(4096);
		int nlCount = 0;
		try {
			this.readChannel.read(buf);
			buf.flip();
			while (buf.position() < buf.limit()) {
				byte b = buf.get();
				this.pos.write(b);
				if (b == 10) {
					nlCount++;
				}
			}
			return getNextLine(nlCount);
		} catch (IOException e) {
			log.error("Problem reading next line", e);
		}
		return "";
	}
}

/*
 * Location: C:\Users\dell\Desktop\wjw\ServerAgent-2.2.1\ServerAgent.jar
 * Qualified Name: com.june.perfmon.client.NIOTransport JD-Core Version: 0.7.0.1
 */
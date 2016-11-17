package com.june.perfmon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.SigarProxy;

import com.june.perfmon.metrics.AbstractPerfMonMetric;

public class PerfMonMetricGetter {
	public static final String TAB = "\t";
	public static final String DVOETOCHIE = ":";
	public static final String NEWLINE = "\n";
	private final PerfMonWorker controller;
	private static final Logger log = LoggingManager.getLoggerForClass();
	private String commandString = "";
	private final SelectableChannel channel;
	private AbstractPerfMonMetric[] metrics = new AbstractPerfMonMetric[0];
	private final SigarProxy sigarProxy;
	private SocketAddress udpPeer;
	private String ip,name;

	public PerfMonMetricGetter(SigarProxy aproxy, PerfMonWorker aController,
			SelectableChannel aChannel) throws IOException {
		this.controller = aController;
		this.channel = aChannel;
		this.sigarProxy = aproxy;
		InetAddress ia=null;
		ia = InetAddress.getLocalHost();
		name = ia.getHostName();
		ip = ia.getHostAddress();
	}

	PerfMonMetricGetter(SigarProxy aproxy, PerfMonWorker aThis,
			DatagramChannel udpServer, SocketAddress remoteAddr)
			throws IOException {
		this(aproxy, aThis, udpServer);
		this.udpPeer = remoteAddr;
	}

	/**
	 * 
	 * @param command
	 * @throws IOException
	 * @date 2016年9月6日 下午4:13:48
	 * @writer iscas
	 */
	private void processCommand(String command) throws IOException {
		log.info("Got command line: " + command);
		String cmdType = command.trim();
		String params = "";
		if (command.indexOf(":") >= 0) {
			cmdType = command.substring(0, command.indexOf(":")).trim();
			params = command.substring(command.indexOf(":") + 1).trim();
		}
		if (cmdType.equals("interval")) {
			this.controller.setInterval(Integer.parseInt(params));
		} else if (cmdType.equals("shutdown")) {
			this.controller.shutdownConnections();
		} else if (cmdType.equals("metrics-single")) {
			setUpMetrics(params.split("\t"));
			ByteBuffer buf = getMetricsLine();
			this.controller.sendToClient(this.channel, buf);
		} else if (cmdType.equals("metrics")) {
			log.info("Starting measures: " + params);
			setUpMetrics(params.split("\t"));

			this.controller.registerWritingChannel(this.channel, this);
		} else if (cmdType.equals("exit")) {
			log.info("Client disconnected");
			synchronized (this.channel) {
				this.metrics = new AbstractPerfMonMetric[0];
				if ((this.channel instanceof SocketChannel)) {
					this.channel.close();
				} else {
					log.info("UDP channel left open to receive data");
				}
			}
			this.controller.notifyDisonnected();
		} else if (cmdType.equals("test")) {
			log.info("Yep, we received the 'test' command");
			if ((this.channel instanceof DatagramChannel)) {
				DatagramChannel udpChannel = (DatagramChannel) this.channel;
				udpChannel.send(ByteBuffer.wrap("Yep\n".getBytes()), this.udpPeer);
			} else {
				((WritableByteChannel) this.channel).write(ByteBuffer.wrap("Yep\n".getBytes()));
			}
		} else if (!cmdType.equals("")) {
			throw new UnsupportedOperationException("Unknown command [" + cmdType.length() + "]: '" + cmdType + "'");
		}
	}

	public void addCommandString(String byteBufferToString) {
		this.commandString += byteBufferToString;
	}

	public boolean processNextCommand() throws IOException {
		log.info("Command line is: " + this.commandString);
		if (this.commandString.indexOf("\n") >= 0) {
			int pos = this.commandString.indexOf("\n");
			String cmd = this.commandString.substring(0, pos);
			this.commandString = this.commandString.substring(pos + 1);
			processCommand(cmd);
			return true;
		}
		return false;
	}

	public ByteBuffer getMetricsLine() throws IOException {
		log.info("Building metrics");
		StringBuffer res = new StringBuffer();
		res.append(name+"\t");
		res.append(ip+"\t");
		synchronized (this.channel) {
			for (int n = 0; n < this.metrics.length; n++) {
				try {
					this.metrics[n].getValue(res);
				} catch (Exception ex) {
					log.error("Error getting metric", ex);
				}
				res.append("\t");
			}
		}
		res.append("\n");

		log.info("Metrics line: " + res.toString());
		return ByteBuffer.wrap(res.toString().getBytes());
	}

	private void setUpMetrics(String[] params) throws IOException {
		synchronized (this.channel) {
			this.metrics = new AbstractPerfMonMetric[params.length];
			String metricParams = "";
			for (int n = 0; n < params.length; n++) {
				String metricType = params[n];
				if (metricType.indexOf(":") >= 0) {
					metricParams = metricType.substring(metricType.indexOf(":") + 1).trim();
					metricType = metricType.substring(0, metricType.indexOf(":")).trim();
				}
				this.metrics[n] = AbstractPerfMonMetric.createMetric(metricType, metricParams, this.sigarProxy);
			}
		}
	}

	public boolean isStarted() {
		return this.metrics.length > 0;
	}
}
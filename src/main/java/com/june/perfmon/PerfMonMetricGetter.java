package com.june.perfmon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.SigarProxy;

import com.june.perfmon.metrics.AbstractPerfMonMetric;

/**
 * 
 * 监控数据采集器 <br>
 * 
 * @author 王俊伟 wjw.happy.love@163.com
 * @date 2016年11月23日 上午11:05:47
 */
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
	private String name;
	private String[] ips;

	public PerfMonMetricGetter(SigarProxy aproxy, PerfMonWorker aController,
			SelectableChannel aChannel) throws IOException {
		this.controller = aController;
		this.channel = aChannel;
		this.sigarProxy = aproxy;
		name = getLocalHostName();
		ips = getAllLocalHostIP();
	}

	private String getLocalHostName(){
		String hostName;
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (UnknownHostException e) {
			hostName = null;
		}
		return hostName;
	}
	private String[] getAllLocalHostIP(){
		String[] ips = null;
		String hostName = getLocalHostName();
		if(hostName!=null){
			InetAddress[] addres;
			try {
				addres = InetAddress.getAllByName(hostName);
				if(addres.length>0){
					ips = new String[addres.length];
					for(int i=0;i<addres.length;i++){
						ips[i] = addres[i].getHostAddress();
					}
				}
			} catch (UnknownHostException e) {
				ips = null;
			}
		}
		return ips;
	}
	
	PerfMonMetricGetter(SigarProxy aproxy, PerfMonWorker aThis,
			DatagramChannel udpServer, SocketAddress remoteAddr)
			throws IOException {
		this(aproxy, aThis, udpServer);
		this.udpPeer = remoteAddr;
	}

	/**
	 * 处理用户命令
	 * @param command
	 * @throws IOException
	 * @date 2016年9月6日 下午4:13:48
	 * @writer iscas
	 */
	private void processCommand(String command) throws IOException {
		log.info("Got command line: " + command);
		String cmdType = command.trim();
		String params = "";
		if (command.indexOf(DVOETOCHIE) >= 0) {
			cmdType = command.substring(0, command.indexOf(DVOETOCHIE)).trim();
			params = command.substring(command.indexOf(DVOETOCHIE) + 1).trim();
		}
		switch (cmdType) {
		case "interval":
			this.controller.setInterval(Integer.parseInt(params));
			break;
		case "shutdown":
			this.controller.shutdownConnections();
			break;
		case "metrics-single":	//只发送一次数据命令
			setUpMetrics(params.split(TAB));
			ByteBuffer buf = getMetricsLine();
			this.controller.sendToClient(this.channel, buf);
			break;
		case "metrics":	//不断发送数据
			log.info("Starting measures: " + params);
			setUpMetrics(params.split(TAB));
			this.controller.registerWritingChannel(this.channel, this);
			break;
		case "exit":
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
			break;
		case "test":
			log.info("Yep, we received the 'test' command");
			if ((this.channel instanceof DatagramChannel)) {
				DatagramChannel udpChannel = (DatagramChannel) this.channel;
				udpChannel.send(ByteBuffer.wrap("Yep\n".getBytes()), this.udpPeer);
			} else {
				((WritableByteChannel) this.channel).write(ByteBuffer.wrap("Yep\n".getBytes()));
			}
			break;
		default:
			throw new UnsupportedOperationException("Unknown command [" + cmdType.length() + "]: '" + cmdType + "'");
		}
	}

	public void addCommandString(String byteBufferToString) {
		this.commandString += byteBufferToString;
	}

	public boolean processNextCommand() throws IOException {
		log.info("Command line is: " + this.commandString);
		if (this.commandString.indexOf(NEWLINE) >= 0) {
			int pos = this.commandString.indexOf(NEWLINE);
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
		for(String ip : ips){//XXX 多网卡情况
			res.append(name + TAB);
			res.append(ip + TAB);
			synchronized (this.channel) {
				for (int n = 0; n < this.metrics.length; n++) {
					try {
						this.metrics[n].getValue(res);
					} catch (Exception ex) {
						log.error("Error getting metric", ex);
					}
					res.append(TAB);
				}
			}
			res.append(NEWLINE);
		}
		log.info("Metrics line: " + res.toString());
		return ByteBuffer.wrap(res.toString().getBytes());
	}

	private void setUpMetrics(String[] params) throws IOException {
		synchronized (this.channel) {
			this.metrics = new AbstractPerfMonMetric[params.length];
			String metricParams = "";
			for (int n = 0; n < params.length; n++) {
				String metricType = params[n];
				if (metricType.indexOf(DVOETOCHIE) >= 0) {
					metricParams = metricType.substring(metricType.indexOf(DVOETOCHIE) + 1).trim();
					metricType = metricType.substring(0, metricType.indexOf(DVOETOCHIE)).trim();
				}
				this.metrics[n] = AbstractPerfMonMetric.createMetric(metricType, metricParams, this.sigarProxy);
			}
		}
	}

	public boolean isStarted() {
		return this.metrics.length > 0;
	}
}
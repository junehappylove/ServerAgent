package com.june.perfmon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;

import com.june.perfmon.metrics.SysInfoLogger;

public class PerfMonWorker implements Runnable {
	private static String version = "1.2.0";
	private static final Logger log = LoggingManager.getLoggerForClass();
	private int tcpPort = 4444;
	private int udpPort = 4444;
	private int exitCode = -1;
	private boolean isFinished = true;
	private final Selector acceptSelector;
	private ServerSocketChannel tcpServer;
	private final Thread writerThread;
	private final Selector sendSelector;
	private DatagramChannel udpServer;
	private final LinkedList<SelectableChannel> tcpConnections = new LinkedList<SelectableChannel>();
	private final Hashtable udpConnections = new Hashtable<SocketAddress, PerfMonMetricGetter>();
	private long interval = 1000L;
	private final SigarProxy sigar;
	private long numConnections = 0L;
	private boolean autoShutdown = false;

	public PerfMonWorker() throws IOException {
		this.acceptSelector = Selector.open();
		this.sendSelector = Selector.open();
		this.writerThread = new Thread(this);
		this.sigar = SigarProxyCache.newInstance(new Sigar(), 500);
	}

	public void setTCPPort(int parseInt) {
		this.tcpPort = parseInt;
	}

	public void setUDPPort(int parseInt) {
		this.udpPort = parseInt;
	}

	public boolean isFinished() {
		return this.isFinished;
	}

	public void processCommands() throws IOException {
		if (this.isFinished) {
			throw new IOException("Worker finished");
		}
		if ((!this.acceptSelector.isOpen()) || ((this.tcpServer == null) && (this.udpServer == null))) {
			throw new IOException("Nothing to do with this settings");
		}
		this.acceptSelector.select();

		Iterator<?> keys = this.acceptSelector.selectedKeys().iterator();
		while (keys.hasNext()) {
			SelectionKey key = (SelectionKey) keys.next();

			keys.remove();
			if (key.isValid()) {
				if (key.isAcceptable()) {
					accept(key);
				} else if (key.isReadable()) {
					try {
						read(key);
					} catch (IOException e) {
						log.error("Error reading from the network layer", e);
						notifyDisonnected();
						key.cancel();
					}
				}
			}
		}
	}

	public int getExitCode() {
		return this.exitCode;
	}

	public void startAcceptingCommands() {
		log.debug("Start accepting connections");
		this.isFinished = false;
		this.writerThread.start();
		boolean started = false;
		try {
			listenUDP();
			started = true;
		} catch (IOException ex) {
			log.error("Can't accept UDP connections", ex);
		}
		try {
			listenTCP();
			started = true;
		} catch (IOException ex) {
			log.error("Can't accept TCP connections", ex);
		}
		if (started) {
			//log.info("JP@GC Agent v" + version + " started");
			log.info("Server Agent v" + version + " started");
		}
	}

	private long getInterval() {
		return this.interval;
	}

	private void listenTCP() throws IOException {
		if (this.tcpPort > 0) {
			log.info("Binding TCP to " + this.tcpPort);
			this.tcpServer = ServerSocketChannel.open();
			this.tcpServer.configureBlocking(false);

			this.tcpServer.socket().bind(new InetSocketAddress(this.tcpPort));
			this.tcpServer.register(this.acceptSelector, 16);
		}
	}

	private void listenUDP() throws IOException {
		if (this.udpPort > 0) {
			log.info("Binding UDP to " + this.udpPort);
			DatagramChannel udp = DatagramChannel.open();
			udp.socket().bind(new InetSocketAddress(this.udpPort));
			udp.configureBlocking(false);
			udp.register(this.acceptSelector, 1);
			udp.register(this.sendSelector, 4);
		}
	}

	private void accept(SelectionKey key) throws IOException {
		log.info("Accepting new TCP connection");
		this.numConnections += 1L;
		SelectableChannel channel = key.channel();
		SelectableChannel tcpConn = ((ServerSocketChannel) channel).accept();
		tcpConn.configureBlocking(false);
		SelectionKey k = tcpConn.register(this.acceptSelector, 1);

		log.debug("Creating new metric getter");
		PerfMonMetricGetter getter = new PerfMonMetricGetter(this.sigar, this,
				tcpConn);
		k.attach(getter);
		this.tcpConnections.add(tcpConn);
	}

	private void read(SelectionKey key) throws IOException {
		PerfMonMetricGetter getter = null;
		ByteBuffer buf = ByteBuffer.allocateDirect(1024);
		if ((key.channel() instanceof SocketChannel)) {
			SocketChannel channel = (SocketChannel) key.channel();
			if (channel.read(buf) < 0) {
				log.info("Closing TCP connection");
				channel.close();
				notifyDisonnected();
				return;
			}
			getter = (PerfMonMetricGetter) key.attachment();
		} else if ((key.channel() instanceof DatagramChannel)) {
			DatagramChannel channel = (DatagramChannel) key.channel();
			SocketAddress remoteAddr = channel.receive(buf);
			if (remoteAddr == null) {
				throw new IOException("Received null datagram");
			}
			synchronized (this.udpConnections) {
				if (!this.udpConnections.containsKey(remoteAddr)) {
					log.info("Connecting new UDP client");
					this.numConnections += 1L;
					this.udpConnections.put(remoteAddr, new PerfMonMetricGetter(this.sigar, this, channel, remoteAddr));
				}
				getter = (PerfMonMetricGetter) this.udpConnections.get(remoteAddr);
			}
		}
		buf.flip();
		log.debug("Read: " + buf.toString());

		getter.addCommandString(byteBufferToString(buf));
		try {
			while (getter.processNextCommand()) {
				log.debug("Done executing command");
			}
		} catch (Exception e) {
			log.error("Error executing command", e);
		}
	}

	public void shutdownConnections() throws IOException {
		log.info("Shutdown connections");
		this.isFinished = true;
		Iterator<SelectableChannel> it = this.tcpConnections.iterator();
		while (it.hasNext()) {
			SelectableChannel entry = (SelectableChannel) it.next();
			log.debug("Closing " + entry);
			entry.close();
			it.remove();
		}
		if (this.udpServer != null) {
			this.udpServer.close();
		}
		if (this.tcpServer != null) {
			this.tcpServer.close();
		}
		this.acceptSelector.close();
		this.sendSelector.close();
	}

	@Override
	public void run() {
		for (;;) {
			if (!this.isFinished) {
				try {
					processSenders();
				} catch (IOException ex) {
					log.error("Error processing senders", ex);
				}
			}
		}
	}

	public void registerWritingChannel(SelectableChannel channel,
			PerfMonMetricGetter worker) throws ClosedChannelException {
		this.sendSelector.wakeup();
		channel.register(this.sendSelector, 4, worker);
	}

	private void processSenders() throws IOException {
		this.sendSelector.select(getInterval());

		long begin = System.currentTimeMillis();

		Iterator<?> keys = this.sendSelector.selectedKeys().iterator();
		
		while (keys.hasNext()) {
			SelectionKey key = (SelectionKey) keys.next();

			keys.remove();
			if (key.isValid()) {
				if (key.isWritable()) {
					try {
						if ((key.channel() instanceof DatagramChannel)) {
							sendToUDP(key);
						} else {
							PerfMonMetricGetter getter = (PerfMonMetricGetter) key.attachment();
							ByteBuffer metrics = getter.getMetricsLine();
							((WritableByteChannel) key.channel()).write(metrics);

						}
					} catch (IOException e) {
						log.error("Cannot send data to TCP network connection", e);
						notifyDisonnected();
						key.cancel();
					}
				}
			}
		}
		long spent = System.currentTimeMillis() - begin;
		if (spent < getInterval()) {
			try {
				Thread.sleep(getInterval() - spent);
			} catch (InterruptedException ex) {
				log.debug("Thread interrupted", ex);
			}
		}
	}

	private void sendToUDP(SelectionKey key) throws IOException {
		synchronized (this.udpConnections) {
			Iterator<SocketAddress> it = this.udpConnections.keySet().iterator();
			while (it.hasNext()) {
				SocketAddress addr = (SocketAddress) it.next();
				PerfMonMetricGetter getter = (PerfMonMetricGetter) this.udpConnections
						.get(addr);
				if (getter.isStarted()) {
					ByteBuffer metrics = getter.getMetricsLine();
					((DatagramChannel) key.channel()).send(metrics, addr);
				}
			}
		}
	}

	private static String byteBufferToString(ByteBuffer bytebuff) {
		byte[] bytearr = new byte[bytebuff.remaining()];
		bytebuff.get(bytearr);
		return new String(bytearr);
	}

	public void setInterval(long parseInt) {
		log.debug("Setting interval to: " + parseInt + " seconds");
		this.interval = (parseInt * 1000L);
	}

	public void logVersion() {
		log.info("JMeter Plugins Agent v" + version);
	}

	public void logSysInfo() {
		SysInfoLogger.doIt(this.sigar);
	}

	public void setAutoShutdown() {
		log.info("Agent will shutdown when all clients disconnected");
		this.autoShutdown = true;
	}

	public void notifyDisonnected() throws IOException {
		this.numConnections -= 1L;
		if (this.autoShutdown) {
			log.debug("Num connections: " + this.numConnections);
		}
		if ((this.numConnections == 0L) && (this.autoShutdown)) {
			log.info("Auto-shutdown triggered");
			shutdownConnections();
		}
	}

	public void sendToClient(SelectableChannel channel, ByteBuffer buf)
			throws IOException {
		if ((channel instanceof DatagramChannel)) {
			synchronized (this.udpConnections) {
				DatagramChannel udpChannel = (DatagramChannel) channel;
				Iterator<SocketAddress> it = this.udpConnections.keySet().iterator();
				while (it.hasNext()) {
					SocketAddress addr = (SocketAddress) it.next();
					if (this.udpConnections.get(addr) == udpChannel) {
						udpChannel.send(buf, addr);
					}
				}
			}
		} else {
			((SocketChannel) channel).write(buf);
		}
	}
}
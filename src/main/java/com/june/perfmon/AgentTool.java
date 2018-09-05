package com.june.perfmon;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ListIterator;
import com.june.cmdtools.AbstractCMDTool;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;

public class AgentTool extends AbstractCMDTool {
	private static final Logger log = LoggingManager.getLoggerForClass();

	protected int processParams(ListIterator<?> args) throws UnsupportedOperationException, IllegalArgumentException {
		LoggingManager.setPriority(Priority.INFO);
		PerfMonWorker worker;
		try {
			worker = getWorker();
		} catch (IOException ex) {
			log.error("Error", ex);
			return 0;
		}
		while (args.hasNext()) {
			String nextArg = (String) args.next();
			log.debug("Arg: " + nextArg);
			if (nextArg.equalsIgnoreCase(TCP_PORT)) {
				if (!args.hasNext()) {
					throw new IllegalArgumentException("Missing TCP Port no");
				}
				worker.setTCPPort(Integer.parseInt((String) args.next()));
			} else if (nextArg.equals(LOG_LEVEL)) {
				args.remove();
				String loglevelStr = (String) args.next();
				LoggingManager.setPriority(loglevelStr);
			} else if (nextArg.equalsIgnoreCase(INTERVAL)) {
				if (!args.hasNext()) {
					throw new IllegalArgumentException("Missing interval specification");
				}
				worker.setInterval(Long.parseLong((String) args.next()));
			} else if (nextArg.equalsIgnoreCase(UDP_PORT)) {
				if (!args.hasNext()) {
					throw new IllegalArgumentException("Missing UDP Port no");
				}
				worker.setUDPPort(Integer.parseInt((String) args.next()));
			} else if (nextArg.equals(AUTO_SHUTDOWN)) {
				args.remove();
				worker.setAutoShutdown();
			} else if (nextArg.equals(SYS_INFO)) {
				args.remove();
				worker.logSysInfo();
			} else if (nextArg.equals(AGENT_VERSION)) {
				args.remove();
				worker.logVersion();
			} else {
				throw new UnsupportedOperationException("Unrecognized option: " + nextArg);
			}
		}
		try {
			worker.startAcceptingCommands();
			while (!worker.isFinished()) {
				worker.processCommands();
			}
		} catch (IOException e) {
			log.error("Error", e);
			return 0;
		}
		return worker.getExitCode();
	}

	protected void showHelp(PrintStream os) {
		os.println("Options for tool 'PerfMon': [ " 
				+ "--tcp-port <port no> " 
				+ "--udp-port <port no> "
				+ "--interval <seconds> " 
				+ "--loglevel <debug|info|warn|error>" 
				+ "--sysinfo " 
				+ "--auto-shutdown]");
	}

	protected PerfMonWorker getWorker() throws IOException {
		return new PerfMonWorker();
	}
}

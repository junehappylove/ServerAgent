package com.june.cmdtools;

import java.io.PrintStream;
import java.util.ListIterator;

public abstract class AbstractCMDTool {

	public final static String[] ONs = { "on", "1", "yes", "true" };
	public final static int OK = 1;
	public final static int NO = 0;
	public final static int SUCCESS = 0;
	public final static int FAILED = -1;

	public final static String TCP_PORT = "--tcp-port";
	public final static String LOG_LEVEL = "--loglevel";
	public final static String INTERVAL = "--interval";
	public final static String UDP_PORT = "--udp-port";
	public final static String AUTO_SHUTDOWN = "--auto-shutdown";
	public final static String SYS_INFO = "--sysinfo";
	public final static String AGENT_VERSION = "--agent-version";
	

	protected int getLogicValue(String string) {
		if (string.equalsIgnoreCase(ONs[0])) {
			return OK;
		}
		if (string.equalsIgnoreCase(ONs[1])) {
			return OK;
		}
		if (string.equalsIgnoreCase(ONs[2])) {
			return OK;
		}
		if (string.equalsIgnoreCase(ONs[3])) {
			return OK;
		}
		return NO;
	}

	protected abstract int processParams(ListIterator<?> paramListIterator)
			throws UnsupportedOperationException, IllegalArgumentException;

	protected abstract void showHelp(PrintStream paramPrintStream);
}

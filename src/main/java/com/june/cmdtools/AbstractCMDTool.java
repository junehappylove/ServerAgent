package com.june.cmdtools;

import java.io.PrintStream;
import java.util.ListIterator;

public abstract class AbstractCMDTool {
	
	protected int getLogicValue(String string) {
		if (string.equalsIgnoreCase("on")) {
			return 1;
		}
		if (string.equalsIgnoreCase("1")) {
			return 1;
		}
		if (string.equalsIgnoreCase("yes")) {
			return 1;
		}
		if (string.equalsIgnoreCase("true")) {
			return 1;
		}
		return 0;
	}

	protected abstract int processParams(ListIterator<?> paramListIterator)
			throws UnsupportedOperationException, IllegalArgumentException;

	protected abstract void showHelp(PrintStream paramPrintStream);
}

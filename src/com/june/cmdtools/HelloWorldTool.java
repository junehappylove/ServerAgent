package com.june.cmdtools;

import java.io.PrintStream;
import java.util.ListIterator;

public class HelloWorldTool extends AbstractCMDTool {
	protected int processParams(ListIterator<?> args)
			throws UnsupportedOperationException, IllegalArgumentException {
		System.out.println("Hello, World!");
		return 0;
	}

	protected void showHelp(PrintStream os) {
		os.println("This tool just prints 'Hello, World!'");
	}
}

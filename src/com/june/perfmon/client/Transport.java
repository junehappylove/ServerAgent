package com.june.perfmon.client;

import java.io.IOException;

public abstract interface Transport {
	public abstract void disconnect();

	public abstract String[] readMetrics();

	public abstract String readln();

	public abstract void setInterval(long paramLong);

	public abstract void shutdownAgent();

	public abstract void startWithMetrics(String[] paramArrayOfString)
			throws IOException;

	public abstract boolean test();

	public abstract void writeln(String paramString) throws IOException;

	public abstract String getAddressLabel();

	public abstract void setAddressLabel(String paramString);
}

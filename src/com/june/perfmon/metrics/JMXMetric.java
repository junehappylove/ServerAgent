package com.june.perfmon.metrics;

import javax.management.MBeanServerConnection;
import com.june.perfmon.metrics.jmx.AbstractJMXDataProvider;
import com.june.perfmon.metrics.jmx.JMXConnectorHelper;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class JMXMetric extends AbstractPerfMonMetric {
	
	private static final Logger log = LoggingManager.getLoggerForClass();
	private final AbstractJMXDataProvider dataProvider;
	private int dividingFactor = 1;

	public JMXMetric(MetricParams params, JMXConnectorHelper jmxHelper) {
		super(null);
		String url = "localhost:4711";//4711	8181
		String user = "";
		String pwd = "";
		for (int i = 0; i < params.params.length; i++) {
			if (params.params[i].startsWith("url=")) {
				url = MetricParams.getParamValue(params.params[i]);
			} else if (params.params[i].startsWith("user=")) {
				user = MetricParams.getParamValue(params.params[i]);
			} else if (params.params[i].startsWith("password=")) {
				pwd = MetricParams.getParamValue(params.params[i]);
			}
		}
		if (url.isEmpty()) {
			throw new IllegalArgumentException("'url' parameter required for metric type 'jmx'");
		}
		MBeanServerConnection mBeanServerConn = jmxHelper.getServerConnection(url, user, pwd);
		try {
			this.dataProvider = AbstractJMXDataProvider.getProvider(mBeanServerConn, params.type);
		} catch (Exception ex) {
			log.error("Failed to get MX Bean data provider", ex);
			throw new RuntimeException("Failed to get MX Bean data provider", ex);
		}
		this.dividingFactor = getUnitDividingFactor(params.getUnit());
	}

	@Override
	public void getValue(StringBuffer res) throws Exception {
		if (this.dataProvider.isBytesValue()) {
			this.dataProvider.getValue(res, this.dividingFactor);
		} else {
			this.dataProvider.getValue(res);
		}
	}
}

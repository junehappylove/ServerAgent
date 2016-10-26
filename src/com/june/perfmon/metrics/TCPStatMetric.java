package com.june.perfmon.metrics;

import java.util.Arrays;

import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

class TCPStatMetric extends AbstractPerfMonMetric {
	public static final byte BOUND = 0;
	public static final byte CLOSE = 1;
	public static final byte CLOSE_WAIT = 2;
	public static final byte CLOSING = 3;
	public static final byte ESTAB = 4;
	public static final byte FIN_WAIT1 = 5;
	public static final byte FIN_WAIT2 = 6;
	public static final byte IDLE = 7;
	public static final byte INBOUND_TOTAL = 8;
	public static final byte LAST_ACK = 9;
	public static final byte LISTEN = 10;
	public static final byte OUTBOUND_TOTAL = 11;
	public static final byte SYN_RECV = 12;
	public static final byte TIME_WAIT = 13;
	public static final String[] types = { "bound", "close", "close_wait", "closing", "estab", "fin_wait1", "fin_wait2",
			"idle", "inbound", "last_ack", "listen", "outbound", "syn_recv", "time_wait" };
	private int type = -1;

	public TCPStatMetric(SigarProxy aSigar, MetricParams params) {
		super(aSigar);
		if (params.type.length() == 0) {
			this.type = 4;
		} else {
			this.type = Arrays.asList(types).indexOf(params.type);
			if (this.type < 0) {
				throw new IllegalArgumentException("Unknown TCP type: " + params.type);
			}
		}
	}

	@Override
	public void getValue(StringBuffer res) throws SigarException {
		NetStat stat = this.sigarProxy.getNetStat();
		double val;
		switch (this.type) {
		case 0:
			val = stat.getTcpBound();
			break;
		case 1:
			val = stat.getTcpClose();
			break;
		case 2:
			val = stat.getTcpCloseWait();
			break;
		case 3:
			val = stat.getTcpClosing();
			break;
		case 4:
			val = stat.getTcpEstablished();
			break;
		case 5:
			val = stat.getTcpFinWait1();
			break;
		case 6:
			val = stat.getTcpFinWait2();
			break;
		case 7:
			val = stat.getTcpIdle();
			break;
		case 8:
			val = stat.getTcpInboundTotal();
			break;
		case 9:
			val = stat.getTcpLastAck();
			break;
		case 10:
			val = stat.getTcpListen();
			break;
		case 11:
			val = stat.getTcpOutboundTotal();
			break;
		case 12:
			val = stat.getTcpSynRecv();
			break;
		case 13:
			val = stat.getTcpTimeWait();
			break;
		default:
			throw new SigarException("Unknown tcp type " + this.type);
		}
		res.append(Double.toString(val));
	}
}


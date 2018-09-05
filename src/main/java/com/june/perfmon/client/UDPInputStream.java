package com.june.perfmon.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

class UDPInputStream extends InputStream {
	private final DatagramSocket socket;
	private final DatagramPacket packet;
	private ByteBuffer data;

	public UDPInputStream(DatagramSocket sock) {
		this.socket = sock;

		byte[] buffer = new byte[4096];
		this.packet = new DatagramPacket(buffer, 4096);
	}

	public int read() throws IOException {
		if (this.data == null) {
			this.socket.receive(this.packet);
			byte[] packetData = this.packet.getData();
			this.data = ByteBuffer.wrap(packetData, this.packet.getOffset(),
					this.packet.getLength());
		}
		if (this.data.position() >= this.data.limit()) {
			this.data = null;
			return read();
		}
		return this.data.get();
	}
}

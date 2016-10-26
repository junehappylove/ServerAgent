package com.june.perfmon.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class UDPOutputStream extends OutputStream {
	private final DatagramSocket socket;
	ByteArrayOutputStream bos;

	public UDPOutputStream(DatagramSocket sock) {
		this.socket = sock;
		this.bos = new ByteArrayOutputStream();
	}

	public void write(int i) throws IOException {
		this.bos.write(i);
		if (i == 10) {
			byte[] data = this.bos.toByteArray();
			this.bos.reset();
			DatagramPacket packet = new DatagramPacket(data, data.length);
			this.socket.send(packet);
		}
	}
}

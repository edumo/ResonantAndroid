package processing.test.udpbroadcast;
import processing.core.*;
import processing.data.*;
import processing.opengl.*;

import java.io.*;
import java.net.*;

//import android.view.MotionEvent;
//import android.view.KeyEvent;
//import android.graphics.Bitmap;

import java.io.*;
import java.util.*;

public class UDPBroadcast extends PApplet {

	UDPReceiver udpReceiver;
	UDPSender udpSender;
	CheckReceiving checkReceiving;

	boolean server = false;

	int MAX_PACKET_SIZE = 4;

	int r, g, b, a;

	public void setup() {

		if (udpReceiver == null) {
			udpReceiver = new UDPReceiver();
			Thread thread = new Thread(udpReceiver);
			thread.start();
		}

		if (udpSender == null) {
			udpSender = new UDPSender();
			Thread thread = new Thread(udpSender);
			thread.start();
		}

		if (checkReceiving == null) {
			checkReceiving = new CheckReceiving(udpReceiver);
			Thread thread = new Thread(checkReceiving);
			 thread.start();
		}

		size(1024, 768);

		frameRate(60);

		r = 255;
	}

	public void draw() {

		if (server) {
			r += 1;
			if (r > 255)
				r = 0;

			sendData(r, g, b, a);
		}
		receiveData();

		text("" + frameRate, width / 2, height / 2);

	}

	private void sendData(int r, int g, int b, int a) {
		byte[] rgba = new byte[4];

		rgba[1] = (byte) r;
		rgba[2] = (byte) g;
		rgba[3] = (byte) b;
		rgba[0] = (byte) a;

		udpSender.sendMessage(rgba);

	}

	private void receiveData() {
		byte[] rgba = udpReceiver.getMessage();

		int r = 0x0000FF & rgba[1];
		int g = 0x0000FF & rgba[2];
		int b = 0x0000FF & rgba[3];
		int a = 0x0000FF & rgba[0];

		background(r, g, b, a);

		// println(r + " " + g + " " + b + " " + a);
		// println();
		// println();
		// println();
		// println();

		// background(0);

		fill(255);
		textAlign(CENTER);
		textSize(30);
		// text("Test Luz\n" + r + " - " + g + " - " + b + " - " + a, width / 2,
		// height / 2);
	}

	/**
	 * Esta clase revisa que nos e calle todo el mundo
	 * 
	 * @author edumo
	 * 
	 */

	class CheckReceiving implements Runnable {

		UDPReceiver receiver;

		boolean waitting4Control = false;
		long getControlNow = 0;

		public CheckReceiving(UDPReceiver receiver) {
			super();
			this.receiver = receiver;
		}

		@Override
		public void run() {

			// PRIMERO COMPROBAMOS QUE NO EXISTA NINGÚN SERVER

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while (true) {
				long now = new Date().getTime() - 1000;

				if (!server) {
					if (receiver.lastReceive < now) {
						// Si no recibimos de nadie desde hace un tiempo
						// intentamos
						// tomar el control

						if (waitting4Control == false) {
							println("Vamos a por el control en breve");
							waitting4Control = true;
							getControlNow = new Date().getTime()
									+ (long) random(1000);
						} else {
							if (new Date().getTime() > getControlNow) {
								println("TOMAMOS EL CONTROL!!!!!!");
								server = true;
								waitting4Control = false;
								getControlNow = 0;
							}
						}
					} else {
						if (waitting4Control) {
							waitting4Control = false;
							getControlNow = 0;
						}
					}
				}

				// AHORA COMPROBAMOS QUE NO HAYA VARIOS SERVERS

			}
		}

	}

	class UDPReceiver implements Runnable {

		DatagramSocket socket;
		byte[] message;
		int MAX_PACKET_SIZE = 4;

		long lastReceive = 0;

		LimitedQueue<String> lastNames = new LimitedQueue<String>(10);

		String[] names = null;

		boolean waitting4Stop = false;
		long waitting4StopMoment = 0;

		public void run() {

			message = new byte[MAX_PACKET_SIZE];
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				// Keep a socket open to listen to all the UDP trafic that is
				// destined for this port

				println("init");
				if (socket == null) {
					InetAddress address = InetAddress.getByName("0.0.0.0");
					socket = new DatagramSocket(12000, address);
					socket.setBroadcast(true);
				}

				while (true) {
					// System.out.println(getClass().getName()
					// + ">>>Ready to receive broadcast packets!");

					// Receive a packet
					byte[] recvBuf = new byte[MAX_PACKET_SIZE];
					DatagramPacket packet = new DatagramPacket(recvBuf,
							recvBuf.length);
					socket.receive(packet);

					InetSocketAddress remote = (InetSocketAddress) packet
							.getSocketAddress();

					lastNames.add(remote.getAddress().getHostAddress());

					names = lastNames.toArray(new String[lastNames.size()]);
					// println(hostName);
					// synchronized (message) {

					lastReceive = new Date().getTime();
					message = recvBuf.clone();
//					println("REEEEEEEEEEECIBIIMOOOSSSS de "
//							+ remote.getHostString());

					if (isMultipleSources()) {
						if (server) {
							if (!waitting4Stop) {
								waitting4Stop = true;
								waitting4StopMoment = new Date().getTime()
										+ (long) random(1000);
								println("tenemos varias fuentes, y una somos nosotros, vamos a parar en un rato");
							} else {
								if (new Date().getTime() > waitting4StopMoment) {
									println("hemos parado como servidor");
									server = false;
								}
							}
							// nos preparamos para
						}
					} else {
						if (waitting4Stop) {
							waitting4Stop = false;
							waitting4StopMoment = 0;
						}
					}
					// }

					// Packet received

				}
			} catch (IOException ex) {
				// Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE,
				// null, ex);
				ex.printStackTrace();
			}
		}

		private boolean isMultipleSources() {

			if (names != null && names.length > 0) {
				String lastServer = names[0];
				if (lastServer != null)
					for (int i = 1; i < names.length; i++) {
						if (names[i] != null && !lastServer.equals(names[i])) {
							return true;
						}
					}
			}

			return false;
		}

		public byte[] getMessage() {
			return message;
		}

	}

	class UDPSender implements Runnable {

		DatagramSocket socket;
		byte[] message;
		int MAX_PACKET_SIZE = 4;
		InetAddress address;

		public void run() {

			message = new byte[MAX_PACKET_SIZE];

			try {
				// Keep a socket open to listen to all the UDP trafic that is
				// destined for this port
				address = InetAddress.getByName("192.168.1.255");
				socket = new DatagramSocket();
				socket.setBroadcast(true);

				while (true) {
					// System.out.println(getClass().getName()
					// + ">>>Ready to receive broadcast packets!");

					// Receive a packet

					if (server) {

						byte[] recvBuf = message;

						// socket.receive(packet);

						DatagramPacket packet = new DatagramPacket(recvBuf,
								recvBuf.length, address, 12000);

						socket.send(packet);
					}

					// Enumeration<NetworkInterface> interfaces =
					// NetworkInterface
					// .getNetworkInterfaces();
					// while (interfaces.hasMoreElements()) {
					// NetworkInterface networkInterface = interfaces
					// .nextElement();
					//
					// if (networkInterface.isLoopback()
					// || !networkInterface.isUp()) {
					// continue; // Don't want to broadcast to the loopback
					// // interface
					// }
					//
					// for (InterfaceAddress interfaceAddress : networkInterface
					// .getInterfaceAddresses()) {
					// InetAddress broadcast = interfaceAddress
					// .getBroadcast();
					// if (broadcast == null) {
					// continue;
					// }
					//
					// // Send the broadcast package!
					// try {
					// DatagramPacket packet = new DatagramPacket(
					// recvBuf, recvBuf.length, broadcast,
					// 12000);
					// socket.send(packet);
					// } catch (Exception e) {
					// }
					//
					// System.out.println(getClass().getName()
					// + ">>> Request packet sent to: "
					// + broadcast.getHostAddress()
					// + "; Interface: "
					// + networkInterface.getDisplayName());
					// }
					// }

					// socket.send(packet);
					// println("send");

					// synchronized (message) {
					// message = recvBuf.clone();
					// }

					// Packet received

					// System.out.println( packet.getData());
				}
			} catch (IOException ex) {
				// Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE,
				// null, ex);
				ex.printStackTrace();
			}
		}

		public void sendMessage(byte[] newMsg) {
			message = newMsg;
		}

	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "--bgcolor=#666666",
				"--hide-stop", "UDPBroadcast" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}

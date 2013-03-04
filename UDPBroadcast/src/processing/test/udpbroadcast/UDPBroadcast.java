package processing.test.udpbroadcast;

import processing.core.*;
import processing.data.*;
import processing.opengl.*;

import java.io.*;
import java.net.*;

import android.view.MotionEvent;
import android.view.KeyEvent;
import android.graphics.Bitmap;
import java.io.*;
import java.util.*;

import org.cometd.examples.CometdDemo;

public class UDPBroadcast extends PApplet {

	UDPReceiver udpReceiver;
	
	CometdDemo cometdDemo;
	
	public void setup() {

		udpReceiver = new UDPReceiver();
		Thread thread = new Thread(udpReceiver);
		thread.start();
		
		cometdDemo = new CometdDemo(this);

	}

	public void draw() {
		byte[] rgba = udpReceiver.getMessage();
		
		int r = 0x0000FF & rgba[1];
		int g = 0x0000FF & rgba[2];
		int b = 0x0000FF & rgba[3];
		int a = 0x0000FF & rgba[0];
		
		background(r, g, b, a);
		
//		println(r + " " + g + " " + b + " " + a);
//		println();
//		println();
//		println();
//		println();
		
		fill(255);
		textAlign(CENTER);
		textSize(30);
		text("Test Luz\n" + r + " - " + g + " - " + b + " - " + a, width/2, height/2);
		
		
	}

	class UDPReceiver implements Runnable {

		DatagramSocket socket;
		byte[] message;
		int MAX_PACKET_SIZE = 4;

		public void run() { 

			message = new byte[MAX_PACKET_SIZE];

			try {
				// Keep a socket open to listen to all the UDP trafic that is
				// destined for this port
				socket = new DatagramSocket(12000,
						InetAddress.getByName("0.0.0.0"));
				socket.setBroadcast(true);

				while (true) {
//					System.out.println(getClass().getName()
//							+ ">>>Ready to receive broadcast packets!");

					// Receive a packet
					byte[] recvBuf = new byte[MAX_PACKET_SIZE];
					DatagramPacket packet = new DatagramPacket(recvBuf,
							recvBuf.length);
					socket.receive(packet);
					
//					synchronized (message) {
						message = recvBuf.clone();
//					}

					// Packet received
					
//					System.out.println( packet.getData());

				}
			} catch (IOException ex) {
				// Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE,
				// null, ex);
				ex.printStackTrace();
			}
		}

		public byte[] getMessage() {
			return message;
		}

	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--hide-stop", "UDPBroadcast_PC"  });
	}

	
}

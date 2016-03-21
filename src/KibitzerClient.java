import java.net.*;
import java.io.*;
import java.util.*;

public class KibitzerClient {
	
	static DatagramPacket sPacket;
	static DatagramPacket rPacket;
	static MulticastSocket multisocket;
	
	
	public static void sendPing(DatagramSocket socket, InetAddress ia) throws IOException {
		Integer whoAmI = 1;
		String isI = whoAmI.toString();
		byte [] buf = isI.getBytes();
		sPacket = new DatagramPacket(buf, buf.length, ia, 2018);
		socket.send(sPacket);	
	}
	
	public static byte[] getPing(DatagramSocket socket) throws IOException {
		byte[] rbuf = new byte[1024];
		rPacket = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(rPacket);
		byte[] data = rPacket.getData();
		return data;
	}
	
	public static GameInformation deserializer(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		GameInformation info = null;
		ObjectInputStream is = new ObjectInputStream(in);
		info = (GameInformation) is.readObject();
		is.close();
		return info;
	}
	

	public static void joinMulticast (String MCIP, int pn) throws IOException {
		multisocket = new MulticastSocket (pn);
		multisocket.setTimeToLive (1);
		InetAddress group = InetAddress.getByName(MCIP);
		multisocket.joinGroup (group);
	}
	
	public static void sMulticast(InetAddress MCIP, int pn, String msg) throws IOException {
		DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.length(), MCIP, pn);
		multisocket.send(sendPacket);
	}
	public static String rMulticast() throws IOException {
		String socketString = null; /* string from socket */
		// get their responses!
		//byte[] buf is a byte array from the socket
		byte[] newbuf = new byte[1000];
		DatagramPacket recv = new DatagramPacket(newbuf, newbuf.length);
		multisocket.receive(recv);
		socketString = new String(recv.getData(), 0, recv.getLength());
		return socketString;
	}
	
	public static void main(String [] args) throws Exception {
		System.out.println("Welcome to the chatroom");
		DatagramSocket socket = new DatagramSocket();
		InetAddress ia = InetAddress.getByName("localhost");
		sendPing(socket, ia);
		byte[] data = getPing(socket);
		GameInformation info = deserializer(data);	
		joinMulticast(info.getKIP(), info.getKPN());
		InetAddress group = InetAddress.getByName(info.getKIP());
		String letter = "";
		while (true) {
			String reply = rMulticast();
			if (reply.contains("fromP2:")) {
				reply = reply.replace("fromP2: ", "");
				letter = reply;
			}
			else if (reply.contains("mtrue")) {
				reply = "You guessed " + letter + ". You got lucky";
				sMulticast(group, info.getKPN(), reply);
				System.out.println(rMulticast());
			}
			else if (reply.contains("mfalse")) {
				reply = "You guessed " + letter + ". HAHAHAH";
				sMulticast(group, info.getKPN(), reply);
				System.out.println(rMulticast());
			}
			else if (reply.contains("vtrue")) {
				reply = "Can't believe you won...";
				sMulticast(group, info.getKPN(), reply);
				System.out.println(rMulticast());
				break;
			}
			else if (reply.contains("vfalse")) {
				reply = "I knew you would lose";
				sMulticast(group, info.getKPN(), reply);
				System.out.println(rMulticast());
				break;
			}
		}
		System.exit(0);
	}
}

import java.net.*;
import java.io.*;
import java.util.*;

public class HangmanClientWordChooser {

	static DatagramPacket sPacket;
	static DatagramPacket rPacket;
	static MulticastSocket multisocket;

	public static void sendPing(DatagramSocket socket, InetAddress ia) throws IOException {
		String name = "Aniket";
		byte[] buf = new byte[name.length()];
		buf = name.getBytes();
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

	public static void sendMulticast(InetAddress MCIP, int pn, String msg) throws IOException {
		DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.length(), MCIP, pn);
		multisocket.send(sendPacket);
	}
	public static String receiveMulticast() throws IOException {
		String socketString = null; /* string from socket */
		// get their responses!
		//byte[] buf is a byte array from the socket
		byte[] newbuf = new byte[1000];
		DatagramPacket recv = new DatagramPacket(newbuf, newbuf.length);
		multisocket.receive(recv);
		socketString = new String(recv.getData(), 0, recv.getLength());
		System.out.println(socketString);
		return socketString;
	}

	public static boolean amIP1(String s) {
		return (s.equals("One"));
	}

	public static void player1Stuff(InetAddress MCIP, int pn) throws IOException {
		String myWord = "homie";
		ArrayList<Character> charList = new ArrayList<Character>();      
		for(int i = 0; i<myWord.length();i++) {
			charList.add(myWord.charAt(i));
		}
		sendMulticast(MCIP, pn, myWord);
		receiveMulticast();
		int wrongGuesses = 0;
		while (wrongGuesses < 6) {
			String letterGuess = receiveMulticast();
			if (charList.contains(letterGuess)) {
				sendMulticast(MCIP, pn, "That letter is in there!");
			}
			else {
				sendMulticast(MCIP, pn, "Bad guess");
				wrongGuesses++;
			}
			charList.remove(letterGuess);
		}

	}

	public static void main(String [] args) throws Exception {
		DatagramSocket socket = new DatagramSocket();
		InetAddress ia = InetAddress.getByName("localhost");
		//Sending the server a ping.
		sendPing(socket, ia);
		//Receiving the GameInformation object from the server
		byte[] data = getPing(socket);
		GameInformation info = deserializer(data);		
		//Finding a way to deserialize the GameInformation object

		joinMulticast(info.getMCIP(), info.getPN());

		InetAddress group = InetAddress.getByName(info.getMCIP());
		player1Stuff(group, info.getPN());
	}
}

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HangmanClient {

	static DatagramPacket sPacket;
	static DatagramPacket rPacket;
	static MulticastSocket multisocket;
	static int PORT;
	public static GameInformation info = null;

	public static void sendPing(DatagramSocket socket, InetAddress ia) throws IOException {
		//		Integer whoAmI = 0;
		//		String isI = whoAmI.toString();
		//		byte [] buf = isI.getBytes();
		//		sPacket = new DatagramPacket(buf, buf.length, ia, 2018);
		//		socket.send(sPacket);	
		Integer whoAmI = 0;
		String isI = whoAmI.toString();
		byte[] data2send = isI.getBytes();
		sPacket = new DatagramPacket(data2send, data2send.length, ia, 2018);
		socket.send(sPacket);
	}

	public static byte[] getPing(DatagramSocket socket) throws IOException {
		byte[] rbuf = new byte[1024];
		rPacket = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(rPacket);
		byte[] data = rPacket.getData();
		return data;
	}
	
	public static Runnable getStringPing (DatagramSocket socket) throws IOException {
		socket.receive(rPacket);
		byte[] data = rPacket.getData();
		String disBdata = new String(data, 0, data.length);
		disBdata = disBdata.trim();
		System.out.println(disBdata);
		return null;
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

	public static void sMulticast(GameInformation info, InetAddress MCIP, int pn, String msg) throws IOException {
		DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.length(), MCIP, pn);
		
		multisocket.send(sendPacket);
		InetAddress group2 = InetAddress.getByName(info.getKIP());
		DatagramPacket sendPacket2 = new DatagramPacket(msg.getBytes(), msg.length(),group2, info.getKPN());
		multisocket.send(sendPacket2);

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

	public static boolean amIP1(GameInformation u) {
		return (u.getPlayer1pn() == PORT); 
	}

	public static void hangmanP1(InetAddress MCIP, int pn, DatagramSocket socket) throws IOException, InterruptedException {
		System.out.println("Welcome to hangman. Enter the word you want guessed");
		Scanner scan = new Scanner(System.in);
		String guess = scan.nextLine();
		sMulticast(info, MCIP, pn, "fromP1: We're ready to play. P2 make a guess");
		int wrongGuesses = 0;
		ArrayList<String> guessList = new ArrayList<String>();
		for (int i = 0; i<guess.length(); i++) {
			char c = guess.charAt(i);
			guessList.add(""+c);
		}
		String g = "fromP1: We're ready to play.";
		while (true) {
			while (true) {
				g = rMulticast();
				g = g.replace("fromP2: ", "");
				if (!g.contains("fromP1:")) {
					break;
				}
			}
			System.out.println("Player 2 guessed the word: " + g);
			if (guessList.contains(g)) {
				guessList.remove(g);
				if (guessList.isEmpty()) {
					sMulticast(info, MCIP, pn, "fromP1: vtrue");
					break;
				}
				System.out.println("He guessed correctly");
				sMulticast(info, MCIP, pn, "fromP1: mtrue");
				guessList.remove(g);
			}
			else if (!guessList.contains(g)) {
				System.out.println("He guessed terrible");
				sMulticast(info, MCIP, pn, "fromP1: mfalse");
				wrongGuesses++;
			}
			if (wrongGuesses > 5) {
				sMulticast(info, MCIP, pn, "fromP1: vfalse");
				break;
			}
			System.out.println(guessList);
			System.out.println(wrongGuesses);
		}
		System.out.println("game over");
	}

	public static void hangmanP2(InetAddress MCIP, int pn) throws IOException {
		System.out.println(rMulticast());
		System.out.println("You can mess up six times");
		String reply;
		int wrongGuesses = 0;
		boolean gotWord = false;
		Scanner scan = new Scanner(System.in);
		while ((wrongGuesses <6) && (gotWord == false)) {
			String g = scan.nextLine();
			sMulticast(info, MCIP, pn, "fromP2: "+g);
			while (true) {
				reply = rMulticast();
				if (!reply.contains("fromP2:")) {
					break;
				}
			}
			if (reply.contains("mtrue")) {
				System.out.println("You guessed correctly. Attaboi");
			}
			else if (reply.contains("mfalse")) {
				System.out.println("You guessed incorrectly");
				wrongGuesses++;
			}
			else if (reply.contains("vtrue")) {
				System.out.println("You got the word");
				break;
			}
			else if(reply.contains("vfalse")) {
				System.out.println("You lost");
				break;
			}
			if (wrongGuesses < 6) {
				System.out.println("What is your next guess?");
			}
		}
		if (wrongGuesses>= 6) {
			System.out.println("You lost");
		}
		else {
			System.out.println("You win");
		}
	}

	public static void main(String [] args) throws Exception {
		DatagramSocket socket = new DatagramSocket();
		PORT = socket.getLocalPort();
		InetAddress ia = InetAddress.getByName("localhost");
		//Sending the server a ping.
		sendPing(socket, ia);
		//Receiving the GameInformation object from the server
		byte[] data = getPing(socket);
		info = deserializer(data);
		//Finding a way to deserialize the GameInformation object
		joinMulticast(info.getMCIP(), info.getPN());
		InetAddress group = InetAddress.getByName(info.getMCIP());
		//		hangManStuff(group, info.getPN());
		if (amIP1(info)) {
			System.out.println("Player 1 has connected to the server");
		}
		else {
			System.out.println("Player 2 has connected to the server");
		}
		if (amIP1(info)) {
			hangmanP1(group, info.getPN(), socket);
		}
		else {
			hangmanP2(group, info.getPN());
		}
	}
}

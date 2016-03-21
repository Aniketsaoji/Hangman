import java.util.*;
import java.io.IOException;
import java.net.*;

public class HangmanServer {

	public static ArrayList<GameInformation> listOfGames = new ArrayList<GameInformation>();
	public static DatagramSocket socket = null;
	public static int totalNumberOfPlayers = 0;
	public static String MCIP = "239.255.255.255";
	public static String KMCIP = "239.255.245.255";
	public static int pn = 8000;
	public static int kpn = 7000;
	public static byte[] rbuf = new byte[1024];
	public static DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
	public static String name;

	public static String getPacket(DatagramSocket socket) throws IOException {	
		socket.receive(packet);
		byte[] test = packet.getData();
		name = new String(test, 0, test.length);
		name = name.trim();
		return name;
	}

	public static GameInformation setupPlayerOneOrTwo() {
		GameInformation u = null;
		if (totalNumberOfPlayers%2 == 0) {
			u = new GameInformation();
			u.initialize(MCIP, pn);
			u.setKIP(KMCIP);
			u.setKPN(kpn);
			u.addPlayer();
			System.out.println("The number of players is:" + u.numberOfPlayers());
			u.setPlayer1IP(packet.getAddress());
			//			UUID id = UUID.randomUUID();
			//			u.setPlayer1Name(id.toString());
			u.setPlayer1pn(packet.getPort());
			pn++;
		}
		else {
			for (GameInformation g : listOfGames) {
				if (g.numberOfPlayers() == 1) {
					u = g;
					u.addPlayer();
					System.out.println("The number of players is:" + u.numberOfPlayers());
					u.setPlayer2IP(packet.getAddress());
					u.setPlayer2pn(packet.getPort());
				}
			}
		}
		return u;
	}

	public static void main(String [] args) throws IOException, InterruptedException {
		socket = new DatagramSocket(2018);
		while (true) {
			//Setting up the socket to get players
			String name = (getPacket(socket));

			//Somebody just connected to the server
			/*
			 * If this is odd player, create new game.
			 * If he is an even player, link him to existing game
			 * Existing game has only one player in it.
			 */
			if (name.equals("0")) {
				GameInformation newGame = setupPlayerOneOrTwo();
				listOfGames.add(newGame);
				// Thread off gameInfo back to the Client
				NewGameThread newGT = new NewGameThread(packet, newGame);
				Thread newT = new Thread(newGT);
				newT.start();
				newT.join();
				totalNumberOfPlayers++;
			}
			else if (name.equals("1")) {
				GameInformation sentGame = listOfGames.get(0);
				NewGameThread newGT = new NewGameThread(packet, sentGame);
				Thread newT = new Thread(newGT);
				newT.start();
				newT.join();
			}
			//new port number
		}
	}

}

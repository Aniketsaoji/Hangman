import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SendKibitzerThread implements Runnable{

	public DatagramSocket socket = null;
	public BufferedReader in = null;
	public GameInformation info;
	public DatagramPacket packet;
	static MulticastSocket multisocket;
	public static int pn;


	public SendKibitzerThread(DatagramSocket socket, int pn) throws IOException {
		this.socket = socket;
		this.pn = pn;
	}

	public static void joinMulticast (String MCIP, int pn) throws IOException {
		multisocket = new MulticastSocket (pn);
		multisocket.setTimeToLive (1);
		InetAddress group = InetAddress.getByName(MCIP);
		multisocket.joinGroup (group);
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

	@Override
	public void run() {
		DatagramPacket rPacket, sPacket;
		byte[] rbuf = new byte[1024];
		rPacket = new DatagramPacket(rbuf, rbuf.length);
		try {
			socket.receive(rPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String disBdata = new String(rbuf, 0, rbuf.length);
		disBdata = disBdata.trim();
		System.out.println(disBdata);
		System.out.println(rPacket.getPort());
		System.out.println(rPacket.getAddress().toString());
		InetAddress group;
		try {
			joinMulticast("239.255.255.255", pn);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true) {
			try {
				String s = rMulticast();
				if (s.contains("fromP2: ")) {
					s = s.replace("fromP2: ", "");
					System.out.println(s);
					byte[] data2send = s.getBytes();
					sPacket = new DatagramPacket(data2send, data2send.length, rPacket.getAddress(), rPacket.getPort());
					socket.send(sPacket);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// TODO Auto-generated method stub

	}

}

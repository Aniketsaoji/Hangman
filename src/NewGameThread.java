import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class NewGameThread implements Runnable{
	
	public DatagramSocket socket = null;
	public BufferedReader in = null;
	public GameInformation info;
	public DatagramPacket packet;
	
	public NewGameThread(DatagramPacket packet, GameInformation info) throws IOException {
		this.info = info;
		this.packet = packet;
		socket = new DatagramSocket();
	}

	public void run() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(info);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		byte [] data = outputStream.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
		try {
			socket.send(sendPacket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	}
	

}

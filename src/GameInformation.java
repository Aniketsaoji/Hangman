import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;

public class GameInformation implements Serializable{
	private String multicastIP;
	private int portNumber;
	private int kibitzPN;
	private String kibitzIP;
	boolean isDone;
	private int numberOfPlayers = 0;
	private InetAddress player1IP = null;
	private InetAddress player2IP = null;
	private String player1Name = "";
	private String player2Name = "";
	private int player1pn = 0;
	private int player2pn = 0;
	
	public void initialize(String mcip, int pn) {
		this.multicastIP = mcip;
		this.portNumber = pn;
		this.isDone = false;
	}
	
	public void finished() {
		this.isDone = true;
	}
	
	public void addPlayer() {
		this.numberOfPlayers++;
	}
	
	public void setPlayer1IP(InetAddress i) {
		this.player1IP = i;
	}

	public void setPlayer2IP(InetAddress i) {
		this.player2IP = i;
	}
	public int numberOfPlayers() {
		return numberOfPlayers;
	}
	
	public boolean amIPlayer1 (InetAddress i) {
		if (this.player1IP.equals(i))
			return true;
		else {
			return false;
		}
	}
	
	public boolean amIPlayer2 (InetAddress i) {
		if (this.player2IP.equals(i))
			return true;
		else {
			return false;
		}
	}
	
	public String getMCIP () {
		return this.multicastIP;
	}
	
	public int getPN () {
		return this.portNumber;
	}
	
	public void setPlayer1Name(String s) {
		this.player1Name = s;
	}
	
	public void setPlayer2Name(String s) {
		this.player2Name = s;
	}
	
	public String getPlayer1Name() {
		return player1Name;
	}
	
	public String getPlayer2Name() {
		return player2Name;
	}
	
	public int getPlayer1pn() {
		return player1pn;
	}
	
	public int getPlayer2pn() {
		return player2pn;
	}
	
	public void setPlayer1pn(int pn) {
		this.player1pn = pn;
	}
	
	public void setPlayer2pn(int pn) {
		this.player2pn = pn;
	}
	
	public void setKIP (String KIP) {
		this.kibitzIP = KIP;
	}
	
	public String getKIP () {
		return this.kibitzIP;
	}
	
	public void setKPN (int pn) {
		this.kibitzPN = pn;
	}
	
	public int getKPN () {
		return this.kibitzPN;
	}
}


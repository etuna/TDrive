package Follower;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MasterConnection implements Runnable {

	// Default Values
	public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.36";
	public static final int DEFAULT_SERVER_PORT = 9999;
	//public static final int DEFAULT_DATAGRAM_UPLOAD_PORT = 6000;
	//public static final int DEFAULT_DATAGRAM_DOWNLOAD_PORT = 7000;
	//public static final int DEFAULT_SYNC_PORT = 5000;

	// Variables
	private String masterAddress;
	private int masterPort;
	public static Socket socket;
	public static DatagramSocket dataSocket;
	public static BufferedReader br; // Input Stream
	public static PrintWriter pw; // Output Stream
	public static MasterConnection master_connection;
	public boolean masterConnectionRun = true;
	public Scanner scanner;

	public static MasterConnection getInstance() {
		if (master_connection == null) {
			master_connection = new MasterConnection(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT);
			return master_connection;
		} else {
			return master_connection;
		}
	}

	public MasterConnection(String address, int port) {
		masterAddress = address;
		masterPort = port;
	}

	public boolean Connect() {

		try {
			socket = new Socket(masterAddress, masterPort);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream());
			scanner = new Scanner(System.in);
			System.out.println("Connection Successful, address:" + masterAddress + ", port :" + masterPort);
			return true;

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean Disconnect() {

		try {
			br.close();
			pw.close();
			socket.close();
			System.out.println("Connection Closed. Address: " + masterAddress + ", port:" + masterPort);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String message;
		while (masterConnectionRun) {

			try {
				System.out.println("Master Connection is running. To quit, type quit please.");
				message = scanner.nextLine();
				if (message.equalsIgnoreCase("quit")) {
					masterConnectionRun = false;
					Main.syncThread.Disconnect();
					Main.uploadThread.Disconnect();
					Main.downloadThread.Disconnect();
					System.out.println("Disconnected.");
					Thread.sleep(1000);
					Main.master_connection.Disconnect();
					System.exit(1);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}

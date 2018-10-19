package Follower;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class UploadThread implements Runnable {
	public MasterConnection master_connection;
	public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.36";
	public int DEFAULT_UPLOAD_SOCKET_PORT = 6001;
	public int DEFAULT_UPLOAD_DATASOCKET_PORT = 6002;
	public Socket uploadSocket;
	public static DatagramSocket dataSocket;
	private BufferedReader br;
	private PrintWriter pw;
	private Follower follower;
	public boolean uploadRun = true;
	public boolean connected= false;

	public UploadThread() {
		master_connection = Main.master_connection;
		follower= Main.follower;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(uploadRun) {
			if(connected) {
				try {
				uploadFiles(Main.syncFiles);
				System.out.println("Follower: Upload Thread is sleeping...");
				Thread.sleep(3000);
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			
			
		}

	}

	public void Start() {
		System.out.println("Upload Thread has been started.");
	}

	public boolean Connect() {

		try {
			uploadSocket = new Socket(DEFAULT_SERVER_ADDRESS, DEFAULT_UPLOAD_SOCKET_PORT);
			dataSocket = new DatagramSocket();
			br = new BufferedReader(new InputStreamReader(uploadSocket.getInputStream()));
			pw = new PrintWriter(uploadSocket.getOutputStream());

			System.out.println("Connection Successful, address:" + DEFAULT_SERVER_ADDRESS + ", port :"
					+ DEFAULT_UPLOAD_SOCKET_PORT);
			connected= true;
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
			uploadSocket.close();
			System.out.println(
					"Connection Closed. Address: " + DEFAULT_SERVER_ADDRESS + ", port:" + DEFAULT_UPLOAD_SOCKET_PORT);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean uploadFile(File f) throws IOException {

		try {
			System.out.println("File uploading... File name:" + f.getName());
			
			FileInputStream fileInputStream = new FileInputStream(f);
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);

			int fileSize = (int) f.length();

			byte[] data = new byte[fileSize];
			byte[] incomingData = new byte[1024];

			int read = 0;
			int numRead = 0;

			while (read < data.length && (numRead = dataInputStream.read(data, read, data.length - read)) >= 0) {
				read += numRead;
			}

			DatagramPacket datagramPacket = new DatagramPacket(data, data.length, new InetSocketAddress("192.168.1.36", DEFAULT_UPLOAD_DATASOCKET_PORT));
			dataSocket.send(datagramPacket);

			System.out.println("File " + f.getName() + " has been sent to the master.");

			//DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			//ataSocket.receive(incomingPacket);
			//String response = "" + incomingPacket.getData().toString();
			//System.out.println("Response from Master: " + response);

			fileInputStream.close();
			dataInputStream.close();
			//dataSocket.close();
			System.out.println("File upload complete. File sent:" + f.getName());
			return true;

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Error occured during the file upload!");
		return false;
	}

	public boolean uploadFiles(ArrayList<SyncPair<Integer, String>> SyncFiles) throws IOException {

		ArrayList<String> uploadList = new ArrayList<String>();

		for (SyncPair p : SyncFiles) {
			if (p.operation == 2) {
				uploadList.add((String) p.file);
			}
		}

		String message;
		for (String file : uploadList) {

			// Tell the file to the master
			message = file;
			pw.println(message);
			pw.flush();

			// Send the size
			File mFile = new File(""+follower.DESKTOP_PATH+"/GoogleDrive/"+file);
			int fileSize = (int)mFile.length();

			pw.println(""+fileSize);
			pw.flush();
			
			// Download the file
			uploadFile(mFile);
		}

		return true;
	}
}

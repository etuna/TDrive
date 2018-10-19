package Master;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class UploadFollowerThread implements Runnable {
	public FollowerConnection follower_connection;
	public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.38";
	public int DEFAULT_UPLOAD_SOCKET_PORT = 6001;
	public int DEFAULT_UPLOAD_DATASOCKET_PORT = 6002;
	public Socket uploadSocket;
	public static DatagramSocket dataSocket;
	private BufferedReader br;
	private PrintWriter pw;
	private Master master;
	public boolean uploadRun = true;
	public ServerSocket serverSocket;
	public boolean connected = false;

	public UploadFollowerThread() throws IOException {
		follower_connection = Main.follower_connection;
		master= Main.master;
		serverSocket = new ServerSocket(DEFAULT_UPLOAD_SOCKET_PORT);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		listenAndAccept();

	}
	
	
	
	public boolean listenAndAccept() {
		
		try {
			/*
			 * Casts a server socket to an ordinary socket
			 */
			while(!connected) {
				uploadSocket = serverSocket.accept();
				dataSocket = new DatagramSocket(DEFAULT_UPLOAD_DATASOCKET_PORT);
				if(uploadSocket!=null && uploadSocket!=null) {
					connected = true;
				}
			}
			
			System.out.println("A connection was established with a FOLLOWER on the address of "
					+ uploadSocket.getRemoteSocketAddress());
			br = new BufferedReader(new InputStreamReader(uploadSocket.getInputStream()));
			pw = new PrintWriter(uploadSocket.getOutputStream());

			while (true) {
				
				String filename = br.readLine();
				int fileSize = Integer.parseInt(br.readLine());
				
				try {
					System.out.println("File downloading from FOLLOWER... File name:" + filename);
					

					byte[] data = new byte[fileSize];
					String path = System.getProperty("user.home") + "/Desktop/Master/GoogleDrive/" + filename;

					DatagramPacket datagramPacket = new DatagramPacket(data, fileSize);
					dataSocket.receive(datagramPacket);

					File newFile = new File(path);
					FileOutputStream fileOutputStream = new FileOutputStream(newFile);
					fileOutputStream.write(data);
					fileOutputStream.flush();
					fileOutputStream.close();
					System.out.println("File " + filename + " successfully recieved from FOLLOWER.");

					return true;

				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}

		} catch (Exception e) {
			// e.printStackTrace();
			System.err.println("Exception on listen and accept function on reading the line");
		} finally {
			try {
				System.out.println("Closing the connection");
				if (br != null) {
					br.close();
					System.out.println(" Socket Input Stream Closed");
				}

				if (pw != null) {
					pw.close();
					System.out.println("Socket Out Closed");
				}
				if (uploadSocket != null) {
					uploadSocket.close();
					System.out.println("Socket Closed");
				}

			} catch (IOException ie) {
				System.out.println("Socket Close Error");
			}
		} // end finally
		
		return true;
	}
	

	public void Start() {
		System.out.println("Upload Thread has been started.");
	}
	
}

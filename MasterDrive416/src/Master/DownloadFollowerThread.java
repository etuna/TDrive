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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DownloadFollowerThread implements Runnable {

	public FollowerConnection follower_connection;
	public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.33";
	public int DEFAULT_DOWNLOAD_SOCKET_PORT = 7001;
	public int DEFAULT_DOWNLOAD_DATASOCKET_PORT = 7002;
	public ServerSocket serverSocket;
	public Socket downloadSocket;
	public static DatagramSocket dataSocket;
	private BufferedReader br;
	private PrintWriter pw;
	public boolean downloadRun = true;
	public boolean connected = false;

	public DownloadFollowerThread() throws IOException {
		follower_connection = Main.follower_connection;
		serverSocket = new ServerSocket(DEFAULT_DOWNLOAD_SOCKET_PORT);
	}

	public void Start() {
		System.out.println("Download Thread has started with a FOLLOWER...");
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
				downloadSocket = serverSocket.accept();
				dataSocket = new DatagramSocket(DEFAULT_DOWNLOAD_DATASOCKET_PORT);
				if(downloadSocket != null && dataSocket!=null) {
					connected= true;
				}
			}
			
			
			System.out.println("A connection was established with a FOLLOWER on the address of "
					+ downloadSocket.getRemoteSocketAddress());
			br = new BufferedReader(new InputStreamReader(downloadSocket.getInputStream()));
			pw = new PrintWriter(downloadSocket.getOutputStream());

			while (true) {
				
				String line = br.readLine();
				File fileToSend = findFile(line);
				int fileSize = (int)fileToSend.length();
				pw.println(Integer.toString(fileSize));
				pw.flush();
				System.out.println("Client " + downloadSocket.getRemoteSocketAddress() + " requested file : " + line +" size :"+fileSize);

				sendFileToFollower(line,fileSize);
				
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
				if (downloadSocket != null) {
					downloadSocket.close();
					System.out.println("Socket Closed");
				}

			} catch (IOException ie) {
				System.out.println("Socket Close Error");
			}
		} // end finally
		return true;
	}

	public boolean sendFileToFollower(String filename,int fileSize) throws IOException {

		try {

			File fileToSend = findFile(filename);
			FileInputStream fileInputStream = new FileInputStream(fileToSend);			
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			
			byte[] data = new byte[fileSize];
			System.out.println("File is being sent... File name:" + filename);
			

			int read = 0;
			int numRead = 0;
			
			while(read < data.length && (numRead = dataInputStream.read(data, read, data.length - read)) >= 0) {
				read += numRead;
			}
						
			DatagramPacket datagramPacket = new DatagramPacket(data,data.length, new InetSocketAddress("192.168.1.38",DEFAULT_DOWNLOAD_DATASOCKET_PORT));
			dataSocket.send(datagramPacket);
			System.out.println("File " + filename + " successfully sent.");

			return true;

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public File findFile(String filename) {

		String path = System.getProperty("user.home") + "/Desktop/Master/GoogleDrive/" + filename;
		File file = new File(path);
		return file;
	}

}

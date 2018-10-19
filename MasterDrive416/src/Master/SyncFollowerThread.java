package Master;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SyncFollowerThread implements Runnable {

	public static final int DEFAULT_SYNC_SOCKET_PORT = 5000;
	public static final int DEFAULT_SYNC_DATASOCKET_PORT = 5001;
	public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.38";
	
	public FollowerConnection follower_connection;
	public Master master;
	public ArrayList<String> currentFiles, localCurrentFiles;
	public  ArrayList<SyncPair<Integer, String>> syncFilesFollower;
	private BufferedReader br;
	private PrintWriter pw;
	private ServerSocket serverSocket;
	private Socket syncSocket;
	private DatagramSocket dataSocket;
	public boolean syncRun = true;
	public boolean connected = false;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
			listenAndAccept();
	}

	public SyncFollowerThread() throws IOException {
	
		master = Main.master;
		syncFilesFollower = Main.syncFilesFollower;
		follower_connection = Main.follower_connection;
		serverSocket = new ServerSocket(DEFAULT_SYNC_SOCKET_PORT);
		localCurrentFiles = new ArrayList<String>();
	}

	public void Start() {
		System.out.println("Sync Thread has been started.");
	}
	
	public ArrayList<String> getLocalCurrentFiles(){
		System.out.println("Master : Getting local current files");
		File Folder = new File(master.DESKTOP_PATH+"/Master/GoogleDrive");
		System.out.println("Master : Getting local current files after connecting to the folder");
		File[] listOfFiles = Folder.listFiles();
		
		for(int i = 0; i<listOfFiles.length; i++) {
			localCurrentFiles.add(listOfFiles[i].getName());
		}
		System.out.println("Master : Local current files are "+localCurrentFiles.toString());
		return localCurrentFiles;
	}
	

	
	
	public boolean listenAndAccept() {
		try {
			/*
			 * Casts a server socket to an ordinary socket
			 */
			while(!connected) {
				syncSocket = serverSocket.accept();
				if(syncSocket != null) {
					connected = true;
				}
			}
			
			System.out.println("A connection was established with a FOLLOWER on the address of "
					+ syncSocket.getRemoteSocketAddress());
			br = new BufferedReader(new InputStreamReader(syncSocket.getInputStream()));
			pw = new PrintWriter(syncSocket.getOutputStream());

			while (true) {
				
				String line = br.readLine(); //currentFiles
				
				String message="";
				System.out.println("Master: Follower requested "+line);
				ArrayList<String> localCurrentFiles = getLocalCurrentFiles();
				for(String filename : localCurrentFiles ) {
					message += filename+"#";
				}
				
				if(message.equals("")) {
					System.out.println("Master : No files found in the folder Master/GoogleDrive");
				} else {
					
				
				message = message.substring(0,message.length()-1);
				System.out.println("Master: Current files"+message);
				pw.println(message);
				pw.flush();
				System.out.println("Master:Client " + syncSocket.getRemoteSocketAddress() + " requested current files : " + message);
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
				if (syncSocket != null) {
					syncSocket.close();
					System.out.println("Socket Closed");
				}

			} catch (IOException ie) {
				System.out.println("Socket Close Error");
			}
		} // end finally
		return true;
	}
	

}

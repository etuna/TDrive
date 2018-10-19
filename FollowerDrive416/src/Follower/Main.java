package Follower;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	//Default Values
	public static final String DEFAULT_MASTER_IP = "192.168.1.36";
	public static final int DEFAULT_MASTER_PORT = 9999;
	
	//Variables
	public static Follower follower;
	public static String MasterIP;
	public static int MasterPort;
	static SyncThread syncThread;
	static UploadThread uploadThread;
	static DownloadThread downloadThread;
	public static MasterConnection master_connection;
	public static ArrayList<SyncPair<Integer, String>> syncFiles;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		Init();
		
		ExecutorService service = Executors.newFixedThreadPool(10);
		
		
		//Sync Thread
		syncThread = new SyncThread();
		syncThread.Start();
		syncThread.Connect();
		//syncThread.run();
		
		//Download Thread
		downloadThread = new DownloadThread();
		downloadThread.Start();
		downloadThread.Connect();
		//downloadThread.run();
		
		//Upload Thread
		uploadThread = new UploadThread();
		uploadThread.Start();
		uploadThread.Connect();
		//uploadThread.run();
				
		
		service.execute(syncThread);
		service.execute(downloadThread);
		service.execute(uploadThread);
		
		
	}
	
	
	public static boolean Init() {
		
		follower = Follower.getInstance();
		syncFiles = new ArrayList<SyncPair<Integer, String>>();
		setupMaster(DEFAULT_MASTER_IP, DEFAULT_MASTER_PORT);
		return true;
	}
	
	public static void setupMaster(String address, int port) {
		MasterIP = address;
		MasterPort = port;
		master_connection = MasterConnection.getInstance();
		master_connection.Connect();
	}
	
	
	
	

}

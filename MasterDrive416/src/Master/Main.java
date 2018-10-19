package Master;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	public static APIConnection API_connection;
	public static FollowerConnection follower_connection;
	public static Master master;
	public static ArrayList<SyncPair<Integer, String>> syncFilesFollower;
	public static ArrayList<SyncPair<Integer, String>> syncFilesMaster;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		java.util.concurrent.ExecutorService service = Executors.newFixedThreadPool(10);
		
		
		API_connection= APIConnection.getInstance();
		API_connection.Connect();
		follower_connection = FollowerConnection.getInstance();
		
		master = Master.getInstance();
		MasterAPIThread apiThread =  new MasterAPIThread();
		syncFilesMaster = apiThread.syncFiles;
		apiThread.Start();
		//apiThread.run();
		
		SyncFollowerThread syncFollowerThread = new SyncFollowerThread();
		syncFollowerThread.Start();
		//syncFollowerThread.run();
		
		DownloadFollowerThread downloadFollowerThread = new DownloadFollowerThread();
		downloadFollowerThread.Start();
		//downloadFollowerThread.run();
		
		UploadFollowerThread uploadFollowerThread= new UploadFollowerThread();
		uploadFollowerThread.Start();
		
		service.execute(apiThread);
		service.execute(syncFollowerThread);
		service.execute(downloadFollowerThread);
		service.execute(uploadFollowerThread);
		
		
		
	}
	
	

}

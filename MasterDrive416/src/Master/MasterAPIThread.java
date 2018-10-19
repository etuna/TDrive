package Master;

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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class MasterAPIThread implements Runnable{
	public String command;
	public ArrayList<String> filesInDrive;
	public ArrayList<String> currentFiles, localCurrentFiles;
	public  ArrayList<SyncPair<Integer, String>> syncFiles;
	public static int DEFAULT_API_SOCKET_PORT = 1200;
	public static int DEFAULT_API_UPLOADDATASOCKET_PORT = 1201;
	public static int DEFAULT_API_DOWNLOADDATASOCKET_PORT = 1202;
	public static final String DEFAULT_SERVER_ADDRESS = "localhost";
	public Socket socket;
	public DatagramSocket downloadDataSocket, uploadDataSocket;
	private BufferedReader br;
	private PrintWriter pw;
	public APIConnection api_connection;
	public int nextOp = 0;
	public boolean masterApiRun = true;
	
	
	public MasterAPIThread() {
		api_connection  = APIConnection.getInstance();
		socket = api_connection.socket;
		downloadDataSocket = api_connection.downloadDataSocket;
		uploadDataSocket = api_connection.uploadDataSocket;
		br = api_connection.br;
		pw = api_connection.pw;
		syncFiles = new ArrayList<SyncPair<Integer, String>>();
		localCurrentFiles = new ArrayList<String>();
	}
	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(masterApiRun) {
			try {
				Operate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		api_connection.Disconnect();
	}

	
	public void Start() {
		System.out.println("Master : API Thread has started running...");
	}
	
	public void setNextOp() {
		/*
		 * 0 for sync
		 * 1 for download
		 * 2 for upload
		 * 3 for remove
		 */
		nextOp = (nextOp + 1) %3;
	}
	
	public boolean Operate() throws IOException, InterruptedException {
		
		if(nextOp==0) { //Sync
			//Send command to API
			pw.println("currentfiles");
			pw.flush();
			//Get files as string
			String currentFilesDrive = br.readLine();
			compareFiles(getFilesInDrive(currentFilesDrive), getLocalCurrentFiles());
			setNextOp();
			System.out.println("Master : API Sync Complete.");
			Thread.sleep(3000);
			
			
		}else if(nextOp == 1) { //Upload
			System.out.println("Master: API Upload Started");
			//Send command to API
			pw.println("upload");
			pw.flush();
			//Upload files
			uploadFiles(syncFiles);
			setNextOp();
			System.out.println("Master: API Upload Complete");
			Thread.sleep(10000);
			
		}else if(nextOp == 2) { //Download
			//Send command to API
			System.out.println("Master: API Download Started");
			pw.println("download");
			pw.flush();
			
			String filesToDownload = stringifyDownload();
			System.out.println("in operation:"+filesToDownload);
			pw.println(filesToDownload);
			pw.flush();
			setNextOp();
			if(filesToDownload.equalsIgnoreCase("updated")) {
				System.out.println("Master: API Download Complete");
				Thread.sleep(10000);
			}else {
				downloadFiles(filesToDownload);
			System.out.println("Master: API Download Complete");
			Thread.sleep(10000);
			}
			
						
		}else if(nextOp == 3){ //Remove
			//TODO
			//Remove file
		}else {
			//TODO else?
			masterApiRun = false;
		}
		
		
		
		return true;
	}
	
	
	public boolean downloadFiles(String files) throws IOException {
		
		String[] filesForSize = files.split("#");
		for(int i =0 ; i<filesForSize.length; i++) {
			String filename = br.readLine();
			String filesize = br.readLine();
			System.out.println("Master :"+filename+" with size "+filesize+" being downloaded...");
			downloadFile(filename,filesize);
		}
		
		
		
		return true;
	}
	
	public boolean downloadFile(String filename, String fSize) throws IOException {
		
		int fileSize = Integer.parseInt(fSize);
		
		try {
			System.out.println("File downloading... File name:" + filename);
			//downloadDataSocket = new DatagramSocket(DEFAULT_API_DOWNLOADDATASOCKET_PORT);

			byte[] data = new byte[fileSize];
			String path = System.getProperty("user.home") + "/Desktop/Master/GoogleDrive/" + filename;

			DatagramPacket datagramPacket = new DatagramPacket(data, fileSize);
			downloadDataSocket.receive(datagramPacket);

			File newFile = new File(path);
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			fileOutputStream.write(data);
			fileOutputStream.flush();
			fileOutputStream.close();
			System.out.println("MASTER:File " + filename + " successfully recieved from API.");
			//pw.println("complete");
			//pw.flush();

			return true;

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return false;
	}
	
	public ArrayList<String> getFilesInDrive(String files) {
	
		ArrayList<String> filesDrive = new ArrayList<String>();
		
		String[] mFiles = files.split("#");
		for(int i=0; i<mFiles.length; i++) {
			filesDrive.add(mFiles[i]);
		}
		
		return filesDrive;
	}
	
	public ArrayList<String> getLocalCurrentFiles(){
		
		File Folder = new File(Main.master.DESKTOP_PATH+"/Master/GoogleDrive");
		File[] listOfFiles = Folder.listFiles();
		
		if(listOfFiles == null) {
			return localCurrentFiles;
		}
		
		for(int i = 0; i<listOfFiles.length; i++) {
			localCurrentFiles.add(listOfFiles[i].getName());
		}
		
		return localCurrentFiles;
		
	}
	
	
	public ArrayList<SyncPair<Integer, String>> compareFiles(ArrayList<String> currentFiles,ArrayList<String> localCurrentFiles) {
		syncFiles.clear();

		//sync follower
		ArrayList<String> oldFiles = Main.master.files;
		
		this.currentFiles = currentFiles;

		for (String f : currentFiles) {
			if (!localCurrentFiles.contains(f)) {
				syncFiles.add(new SyncPair(2, f)); //Download
			}
		}
		for (String f : oldFiles) {
			if (!currentFiles.contains(f)) {
				syncFiles.add(new SyncPair(0, f)); //Remove
			}
		}
		for(String f: localCurrentFiles) {
			if(!currentFiles.contains(f)) {
				syncFiles.add(new SyncPair(1,f)); //Upload
			}
		}
		
		Main.master.files = localCurrentFiles;
		return syncFiles;
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

			DatagramPacket datagramPacket = new DatagramPacket(data, data.length, Inet4Address.getLocalHost(),
					DEFAULT_API_UPLOADDATASOCKET_PORT);
			uploadDataSocket.send(datagramPacket);

			System.out.println("Master:File " + f.getName() + " has been sent to the Drive.");

			//DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			//downloadDataSocket.receive(incomingPacket);
			//String response = "" + incomingPacket.getData().toString();
			//System.out.println("Response from API: " + response);

			fileInputStream.close();
			dataInputStream.close();
			System.out.println("Master:File upload complete. File sent:" + f.getName());
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
			if (p.operation == 1) {
				uploadList.add((String) p.file);
			}
		}

		String filenames="empty";
		
		for (String file : uploadList) {
				if(filenames.equals("empty")) {
					filenames="";
				}
			// Tell the file to the master
			filenames += file+"#";
			
		}
		
		String filesizes = "0";
		for(String file:uploadList) {
			if(filenames.equals("0")) {
				filenames="";
			}
			File mFile = new File(""+Main.master.DESKTOP_PATH+"/Master/GoogleDrive/"+file);
			int fileSize = (int)mFile.length();
			filesizes +=fileSize+"#";
		}
		
		if(filenames.equals("empty")) {
			pw.println("empty");
			pw.flush();
			pw.println("empty");
			pw.flush();
			return true;
		}
		
		filenames = filenames.substring(0, filenames.length());
		filesizes = filesizes.substring(0, filesizes.length());
		
		pw.println(filenames);
		pw.flush();
		pw.println(filesizes);
		pw.flush();
		
		
		// Download the file
		for(String f:uploadList) {
			uploadFile(new File(""+Main.master.DESKTOP_PATH+"/Master/GoogleDrive/"+f));
		}
		
		

		return true;
	}
	
	public String stringifyDownload() {

		return getDownloadFiles();
		
	}
	
	
	public String getDownloadFiles(){
		ArrayList<String> files = new ArrayList<String>();
		
		for (SyncPair p : syncFiles) {
			if (p.operation == 2) {
				files.add((String) p.file);
			}
		}

		String message = "";
		for (String file : files) {

			// Tell the file to the master
			message += file+"#";

		}
		if(files.isEmpty()) {
			message="updated";
		}else {
			message = message.substring(0,message.length()-1);
		}
		
		System.out.println("get download files :"+message);
		return message;
	}
	
	
	
}

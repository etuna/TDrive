import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.util.ByteStreams;
import com.google.api.client.util.IOUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class APIThread implements Runnable {

	public String command;
	public ArrayList<String> filesInDrive;
	public static int DEFAULT_API_SOCKET_PORT = 1200;
	public static int DEFAULT_API_UPLOADDATASOCKET_PORT = 1201;
	public static int DEFAULT_API_DOWNLOADDATASOCKET_PORT = 1202;
	public ServerSocket serverSocket;
	public Socket socket;
	public DatagramSocket downloadDataSocket, uploadDataSocket;
	public static QuickConnect quickConnect;
	public Drive service;
	private BufferedReader br;
	private PrintWriter pw;
	public boolean downloadFileComplete = true;

	public APIThread() throws IOException {
		quickConnect = QuickConnect.quickConnect;
		serverSocket = new ServerSocket(DEFAULT_API_SOCKET_PORT);
		service = quickConnect.service;
	}

	public ArrayList<String> getFilesInDrive() {
		ArrayList<String> files = new ArrayList<String>();

		try {
			FileList result = service.files().list().setPageSize(10).setFields("nextPageToken, files(id, name)")
					.execute();
			List<File> mfiles = result.getFiles();

			for (File f : mfiles) {
				files.add(f.getName());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return files;
	}

	public String filesToString(ArrayList<String> files) {

		String filesString = "";

		for (String file : files) {
			filesString += file + "#";

		}

		return filesString;

	}

	public int understandCommand(String command) {
		if (command.equalsIgnoreCase("currentfiles")) { // Get current files
			return 0;
		} else if (command.equalsIgnoreCase("upload")) { // Upload time
			return 1;
		} else if (command.equals("download")) { // Download time
			return 2;
		} else { // THERE IS A REMOVE!!!
			return 3;
		}
	}

	public boolean listenAndAccept() {
		try {
			/*
			 * Casts a server socket to an ordinary socket
			 */
			boolean connected = false;
			while(!connected) {
				socket = serverSocket.accept();
				if(socket  != null) {
					connected = true;
				}
			}
				uploadDataSocket = new DatagramSocket(DEFAULT_API_UPLOADDATASOCKET_PORT);
				downloadDataSocket = new DatagramSocket();
				System.out.println("API:A connection was established with a MASTER on the address of "
						+ socket.getRemoteSocketAddress());
				
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				pw = new PrintWriter(socket.getOutputStream());
				
			
				while (true) {
			//socket = serverSocket.accept();
			//uploadDataSocket = new DatagramSocket(DEFAULT_API_UPLOADDATASOCKET_PORT);
			//downloadDataSocket = new DatagramSocket();
			//System.out.println("API:A connection was established with a MASTER on the address of "
			//		+ socket.getRemoteSocketAddress());
			
			//br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//pw = new PrintWriter(socket.getOutputStream());
		
					
				String command = br.readLine();
				downloadDataSocket = new DatagramSocket();
				if(command.equalsIgnoreCase("quit")) {
					System.out.println("API is being closed...");
					Thread.sleep(3000);
					System.exit(1);
				}
				
				System.out.println("API :Master " + socket.getRemoteSocketAddress() + " command : " + command);
				answer(command);
				System.out.println("API : Answered.");

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
				if (socket != null) {
					socket.close();
					System.out.println("Socket Closed");
				}

			} catch (IOException ie) {
				System.out.println("Socket Close Error");
			}
		} // end finally
		return true;
	}

	public boolean answer(String command) throws IOException {

		int operation = understandCommand(command);
		String filesString;
		String filesSizeString;
		switch (operation) {
		case 0: // current files
			pw.println(filesToString(getFilesInDrive()));
			pw.flush();
			break;
		case 1: // Upload
			filesString = br.readLine();
			filesSizeString = br.readLine();
			if(filesString.equals("empty")) {
				break;
			}
			Upload(filesString, filesSizeString);
			break;
		case 2: // Download
			filesString = br.readLine();
			if(filesString.equalsIgnoreCase("updated")) {
				break;
			}
			Download(filesString);
			break;
		case 3:// Remove
			filesString = br.readLine();
			Remove(filesString);
			break;
		}

		return false;
	}

	public boolean Download(String filesString) throws IOException {
		String[] files = filesString.split("#");

		for (int i = 0; i < files.length; i++) {
			//if(!downloadFileComplete) {
			//	while(!downloadFileComplete) {
			//		//wait
			//	}
			//System.out.println("files i ::::"+files[i]);
				sendFile(files[i]);
			//	downloadFileComplete = false;
			//} else {
			//	sendFile(files[i]);
			//	downloadFileComplete = false;
			//}
			
		}
		return true;
	}
	
	
	public boolean Upload(String filesString, String filesSizeString) throws NumberFormatException, IOException {
	
		String[] filesArray = filesString.split("#");
		String[] filesSizeStringArray = filesSizeString.split("#");
		for(int i = 0; i<filesSizeStringArray.length; i++) {
			recieveFile(filesArray[i], Integer.parseInt(filesSizeStringArray[i]));
		}
		System.out.println("API : Upload Complete.");
		return true;
	}
	
	public boolean Remove(String filesString) throws IOException {
		String[] filesArray = filesString.split("#");
		String[] ids = findIDs(filesString);
		
		for(int i=0; i<ids.length; i++) {
			deleteFile(ids[i]);
		}
		System.out.println("API: Remove complete.");
		return true;
	}
	
	public boolean deleteFile(String ID) {
		try {
		      service.files().delete(ID).execute();
		    } catch (IOException e) {
		      System.out.println("An error occurred: " + e);
		    }
		return true;
	}
	
	
	public String[] findIDs(String filesString) throws IOException {
		String[] filesArray = filesString.split("#");
		String[] IDs = new String[filesArray.length];
		
		FileList result = service.files().list().execute();
		List<File> files = result.getFiles();
		int idIndex=0;
		for(int i=0; i<filesArray.length;i++) {
			String filename = filesArray[i];
			
			for(File f:files) {
				if(f.getName().equalsIgnoreCase(filename)) {
					IDs[idIndex]= f.getId();
					idIndex++;
				}
			}
		}		
		return IDs;
	}
	
	
	public boolean recieveFile(String filename, int fileSize) throws IOException {
		try {
			System.out.println("API:File recieving... File name:" + filename);
			//downloadDataSocket = new DatagramSocket(DEFAULT_DOWNLOAD_DATASOCKET_PORT);

			byte[] data = new byte[fileSize];
			//String path = System.getProperty("user.home") + "/Desktop/GoogleDrive/" + filename;

			DatagramPacket datagramPacket = new DatagramPacket(data, fileSize);
			uploadDataSocket.receive(datagramPacket);
			
			
			
			String path = System.getProperty("user.home") + "/Desktop/tempDrive";
			java.io.File newFile = new java.io.File(path);
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			fileOutputStream.write(data);
			fileOutputStream.flush();
			fileOutputStream.close();
			System.out.println("File " + filename + " successfully recieved.");

			
			File fileMetadata = new File();
			fileMetadata.setName(filename);
			
			String ex = filename.substring(filename.lastIndexOf("."), filename.length()-1);
			FileContent mediaContent;
			if(ex.equalsIgnoreCase("pdf") || ex.equalsIgnoreCase("jpg") || ex.equalsIgnoreCase("png")) {
				 mediaContent= new FileContent("image/jpeg", newFile);
			} else if(ex.equalsIgnoreCase("pptx")) {
				mediaContent = new FileContent("application/pdf", newFile);
			}else {
				mediaContent= new FileContent("text/plain", newFile);
			}
			
			
			File file = service.files().create(fileMetadata, mediaContent)
			    .setFields("id")
			    .execute();
			
			newFile.delete();
			
			return true;

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	

	public boolean sendFile(String filename) throws IOException {
		return prepareFileAndSend(filename);
	}

	public boolean prepareFileAndSend(String filename) throws IOException {

		FileList result = service.files().list().execute();
		List<File> files = result.getFiles();
		//System.out.println("here 1 filename:"+filename);
		if (files == null || files.size() == 0) {
			System.out.println("No files found.");
		} else {

			for (File file : files) {
				//System.out.println("here 2");
				if (!file.getName().equalsIgnoreCase(filename)) {
					//System.out.println("here 3");
					if(file.getName().contains(".")) {
						String realName = file.getName();
						String subs=file.getName().substring(0, file.getName().lastIndexOf("."));
						file.setName(realName);
						if(subs.equalsIgnoreCase(filename)) {
							
						}else {
							continue;
						}
					}else {
						continue;
					}
			
				}
				//System.out.println("here 4 filename:" + file.getName());
				String fname = file.getName();
				String ex = fname.substring(fname.lastIndexOf(".") + 1);

				try {
					//System.out.println("here 5");
					Files f = service.files();
					HttpResponse httpResponse = null;
					if (ex.equalsIgnoreCase("xlsx")) {
						httpResponse = f
								.export(file.getId(),
										"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
								.executeMedia();

					} else if (ex.equalsIgnoreCase("docx")) {
						httpResponse = f
								.export(file.getId(),
										"application/vnd.openxmlformats-officedocument.wordprocessingml.document")
								.executeMedia();
					} else if (ex.equalsIgnoreCase("pptx")) {
						httpResponse = f
								.export(file.getId(),
										"application/vnd.openxmlformats-officedocument.presentationml.presentation")
								.executeMedia();

					} else 
						//(ex.equalsIgnoreCase("pdf") || ex.equalsIgnoreCase("jpg") || ex.equalsIgnoreCase("png")) {
					{
						Get get = f.get(file.getId());
						httpResponse = get.executeMedia();

					}
					//System.out.println("here 6");
					if (null != httpResponse) {
						System.out.println("http res not null");
						InputStream instream = httpResponse.getContent();
						int fileSize = instream.available();
						System.out.println("File name and size:"+file.getName()+"  "+fileSize);
						pw.println(file.getName());
						pw.flush();
						//pw.println(""+fileSize);
						//pw.flush();
						try {
							int l;
							byte[] data = new byte[fileSize];

							int read = 0;
							int numRead = 0;
							//ArrayList<String> toStringArrayList = new ArrayList<String>();
							//BufferedReader myBr = new BufferedReader(new InputStreamReader(instream));
							//String insLine;
							//while((insLine = myBr.readLine())!=null) {
							//	toStringArrayList.add(insLine);
							//}
							//String myFile="";
							
							//for(String s:toStringArrayList) {
							//	myFile +=s+System.lineSeparator();
								
							//}
							byte[] mData = com.google.common.io.ByteStreams.toByteArray(instream);
							int len = mData.length;
							pw.println(""+len);
							pw.flush();

							byte[] myData = new byte[len];
							while (read < myData.length
									&& (numRead = instream.read(myData, read, myData.length - read)) >= 0) {
								read += numRead;
							}
						

							DatagramPacket datagramPacket = new DatagramPacket(mData, mData.length,
									Inet4Address.getLocalHost(), DEFAULT_API_DOWNLOADDATASOCKET_PORT);
							downloadDataSocket.send(datagramPacket);
							//String line = "";
							//while((line = br.readLine()).equals("")) {
							//	// wait
							//}
							
							System.out.println("File " + file.getName() + " has been sent to the master from API.");

							break;
						} finally {

							instream.close();

						}
					} else {
						System.out.println("http response null");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				//break;
			}
		}
		return true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
			listenAndAccept();
	}

	
	public void Start() {
		System.out.println("API HAS STARTED...");
	}
}

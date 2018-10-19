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

public class FollowerConnection {

	//Default Values
	public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.36";
	public static final int DEFAULT_SERVER_PORT = 9999;
	public static final int DEFAULT_DATAGRAM_PORT = 6666;
	
	//Variables
	private String followerAddress;
	private int followerPort;
    private ServerSocket serverSocket;
	public static Socket socket;
	public static DatagramSocket dataSocket;
	public static BufferedReader br; //Input Stream
	public static PrintWriter pw; //Output Stream
	public static FollowerConnection follower_connection;
	public boolean connected = false;
	
	public static FollowerConnection getInstance() throws IOException {
		if(follower_connection == null) {
			follower_connection = new FollowerConnection(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT);
			return follower_connection;
		} else {
			return follower_connection;
		}
	}
	
	
	public FollowerConnection(String address, int port) throws IOException {
		followerAddress= address;
		followerPort = port;
		serverSocket = new ServerSocket(DEFAULT_SERVER_PORT);
		System.out.println("Oppened up a server socket on " + port);
	}
	
	
	
	public boolean listenAndAccept() {
		
		
        try
        {
            /*
            Casts a server socket to an ordinary socket
             */
        	while(!connected) {
        		   socket = serverSocket.accept();
        		   if(socket!=null) {
        			   connected=true;
        		   }
        	}
         
            System.out.println("A connection was established with a FOLLOWER on the address of " + socket.getRemoteSocketAddress());
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream());

            //String line = br.readLine();
          //  while (line.compareTo("QUIT") != 0)
          //  {

                //pw.println(line);
              //  pw.flush();
                //System.out.println("Client " + socket.getRemoteSocketAddress() + " sent : " + line);
               // line = br.readLine();
               // if(line.equalsIgnoreCase("quit")) {
               // 	Disconnect();
            //    }
           // }

        }
        catch (Exception e)
        {
            //e.printStackTrace();
            System.err.println("Exception on listen and accept function on reading the line");
        } finally
        {
            try
            {
                System.out.println("Closing the connection");
                if (br != null)
                {
                    br.close();
                    System.out.println(" Socket Input Stream Closed");
                }

                if (pw != null)
                {
                    pw.close();
                    System.out.println("Socket Out Closed");
                }
                if (socket != null)
                {
                    socket.close();
                    System.out.println("Socket Closed");
                }

            }
            catch (IOException ie)
            {
                System.out.println("Socket Close Error");
            }
        }//end finally
		return true;
	}
	
	
	public boolean Connect() {
		
		try {
			socket = new Socket(followerAddress, followerPort);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream());
			
			System.out.println("Connection Successful, address:"+followerAddress+", port :"+followerPort);
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
			System.out.println("Connection Closed. Address: "+followerAddress+", port:"+followerPort);
			System.exit(1);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}
	
	
	public boolean sendFile(File f) throws IOException {
		
		
		try {
			System.out.println("File uploading... File name:"+f.getName());
			dataSocket = new DatagramSocket();
			FileInputStream fileInputStream = new FileInputStream(f);			
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			
			int fileSize = (int) f.length();
			
			byte[] data = new byte[fileSize];
			byte[] incomingData = new byte[1024];
			
			int read = 0;
			int numRead = 0;
			
			while(read < data.length && (numRead = dataInputStream.read(data, read, data.length - read)) >= 0) {
				read += numRead;
			}
			
			DatagramPacket datagramPacket = new DatagramPacket(data, data.length, Inet4Address.getLocalHost(), DEFAULT_DATAGRAM_PORT);
			dataSocket.send(datagramPacket);
			
			System.out.println("File "+f.getName()+" has been sent to the master.");
			
			DatagramPacket incomingPacket =  new DatagramPacket(incomingData, incomingData.length);
			dataSocket.receive(incomingPacket);
			String response = ""+incomingPacket.getData().toString();
			System.out.println("Response from Master: "+response);
			
			fileInputStream.close();
			dataInputStream.close();
			dataSocket.close();
			System.out.println("File upload complete. File sent:"+f.getName());
			return true;
						
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Error occured during the file upload!");
		return false;
	}
	
	public boolean receiveFile(String filename, int fileSize) throws IOException {
		
		try {
			System.out.println("File downloading... File name:"+filename);
			dataSocket = new DatagramSocket();
			
			byte[] data = new byte[fileSize];
			String path = System.getProperty("user.home") + "/Desktop/GoogleDrive/"+filename;
					
			DatagramPacket datagramPacket = new DatagramPacket(data, fileSize);
			dataSocket.receive(datagramPacket);
			
			File newFile = new File(path);
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			fileOutputStream.write(data);
			fileOutputStream.flush();
			fileOutputStream.close();
			System.out.println("File "+filename+" successfully recieved.");
			
			return true;
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return false;
	}
	
	
}

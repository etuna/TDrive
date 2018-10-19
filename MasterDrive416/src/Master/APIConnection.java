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

public class APIConnection {


	//Variables
	private String serverAddress;
	private int serverPort;
	public static int DEFAULT_API_SOCKET_PORT = 1200;
	public static int DEFAULT_API_UPLOADDATASOCKET_PORT = 1201;
	public static int DEFAULT_API_DOWNLOADDATASOCKET_PORT = 1202;
	public static final String DEFAULT_SERVER_ADDRESS = "localhost";
	public Socket socket;
	public DatagramSocket downloadDataSocket, uploadDataSocket;
	public static BufferedReader br;
	public static  PrintWriter pw;
	public static APIConnection api_connection;
	public static boolean apiRun = true;
	
	public static APIConnection getInstance() {
		if(api_connection == null) {
			api_connection = new APIConnection(DEFAULT_API_SOCKET_PORT);
			return api_connection;
		} else {
			return api_connection;
		}
	}
	
	
	public APIConnection(int port) {
		serverPort = port;
	}
	
	
	public boolean Connect() {

		try {
			apiRun = true;
			socket = new Socket(DEFAULT_SERVER_ADDRESS,DEFAULT_API_SOCKET_PORT);
			downloadDataSocket = new DatagramSocket(DEFAULT_API_DOWNLOADDATASOCKET_PORT);
			uploadDataSocket = new DatagramSocket();
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream());

			System.out.println("MASTER : API Connection Successful, address:" + DEFAULT_SERVER_ADDRESS + ", port :"
					+ DEFAULT_API_SOCKET_PORT);
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
			apiRun=false;
			br.close();
			pw.close();
			socket.close();
			downloadDataSocket.close();
			downloadDataSocket.close();
			System.out.println("Connection Closed. Address: " + DEFAULT_SERVER_ADDRESS + ", port:"
					+ DEFAULT_API_SOCKET_PORT);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
		
	
}

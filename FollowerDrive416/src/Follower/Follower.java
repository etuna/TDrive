package Follower;
import java.io.File;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Follower {

	public static final String DESKTOP_PATH = System.getProperty("user.home") + "/Desktop";
	
	public String IP;
	public static Follower follower;
	public boolean inited = false;
	public static ArrayList<String> files;
	
	
	public static Follower getInstance() {
		if(follower!=null) {
			return follower;
		}else {
			follower= new Follower();
			return follower;
		}
	}
	
	public boolean Init() {
		
		try {
			//Machine's IP
			IP = Inet4Address.getLocalHost().getHostAddress();
		
			
			//Creating Directory on desktop
			makeDir("GoogleDrive", DESKTOP_PATH);
			files = new ArrayList<String>();
			//First Sync
			firstSync();
			
			//First init called
			inited = true;
			
			return true;
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	
	
	
	public Follower() {
		
		Init();
	}
	
	public boolean makeDir(String folderName, String path) {
		
		File dir = new File(path);
		dir.mkdir();
		
		return true;
	}
	
	public boolean firstSync() {
		
		
		return false;
	}
	
}

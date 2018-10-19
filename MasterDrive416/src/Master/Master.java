package Master;
import java.io.File;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Master {

	public static final String DESKTOP_PATH = System.getProperty("user.home") + "/Desktop";
	
	public String IP;
	public static Master master;
	public boolean inited = false;
	public static ArrayList<String> files;
	
	
	public static Master getInstance() {
		if(master!=null) {
			return master;
		}else {
			master= new Master();
			return master;
		}
	}
	
	public boolean Init() {
		
		try {
			//Machine's IP
			IP = Inet4Address.getLocalHost().getHostAddress();
		
			files = new ArrayList<String>();
			//Creating Directory on desktop
			makeDir("Master", DESKTOP_PATH);
			makeDir("GoogleDrive", DESKTOP_PATH+"/Master");
			
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
	
	
	
	
	public Master() {
		
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

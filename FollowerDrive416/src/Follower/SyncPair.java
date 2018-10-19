package Follower;

public class SyncPair<Integer, String> {
	
	
	
	public int operation;//0 for remove, 1 for download, 2 for upload
	public String file;

	public SyncPair(int operation, String file) {
	this.operation = operation;
	this.file = file;
	}

}

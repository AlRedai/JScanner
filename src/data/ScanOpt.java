package data;

public class ScanOpt {
	private static int millisec;
	private static int nanosec;
	private static boolean range = true;
	private static boolean kill  = false;
	
	protected void setDelay(int msec) {
		millisec = msec;
	}
	
	public int getDelay() {
		return millisec;
	}
	
	protected void setDelay(int msec, int nsec) {
		millisec = msec;
		nanosec = nsec;
	}
	
	public int[] getNDelay() {
		return new int [] { millisec, nanosec };
	}
	
	protected void setRangeTek(boolean value) {
		range = value;
	}
	
	public boolean getRangeTek() {
		return range;
	}
	
	protected void setKill(boolean value) {
		kill = value;
	}
	
	public boolean getKill() {
		return kill;
	}
	
}

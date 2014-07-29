package data;

import java.util.ArrayList;

public class DataTarget extends ScanOpt {
	private static ArrayList<Integer> openPort = new ArrayList<Integer>();
	private static int closedPort;
	private static int filteredPort;
	private static String protocol = "tcp";
	private static String target;
	private static String hostname;
	private static int portMin;
	private static int portMax;

	public String getTarget() {
		return target;
	}

	public void setTarget(String ip) {
		target = ip;
	}

	public void setPortMin(String port) {
		portMin = Integer.parseInt(port);
	}
	
	public int getPortMin() {
		return portMin;
	}
	
	public void setPortMax(String port) {
		portMax = Integer.parseInt(port);
	}
	
	public  int getPortMax() {
		return portMax;
	}
	
	public void setProtocol(String proto) {
		protocol = proto;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public void setOpenPort(int port) {
		openPort.add(port);
	}
	
	public ArrayList<?> getOpenPort() {
		return openPort;
	}
	
	public void removeOpenPort() {
		openPort.clear();
	}
	
	public int getClosedPort() {
		return closedPort;
	}
	
	public void setClosedPort() {
		closedPort++;
	}
	
	public int getFilteredPort() {
		return filteredPort;
	}
	
	public void setFilteredPort() {
		filteredPort++;
	}
	
	public void resetPortData() {
		openPort.clear();
		closedPort = 0;
		filteredPort = 0;
	}
	
	public void setHostname(String host) {
		hostname = host;
	}
	
	public String getHostname() {
		return " "+hostname;
	}
}

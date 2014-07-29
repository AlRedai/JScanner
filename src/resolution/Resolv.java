package resolution;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import scanner.JScanner;
import data.DataTarget;
import error.ErrorMessage;

public class Resolv extends DataTarget {
	private static ArrayList<String> ip = new ArrayList<String>(); 
	private final String CONNECT_TIME_OUT = "connect timed out";
	private final String CONNECT_REFUSED  = "Connection refused";
	private SocketAddress sockaddr;
	private Socket socket;
	private String target;
	private String localAddr;
	
	public Resolv() {
		this.localAddr = getLanIp();
	}
	
	public Resolv(String ip) { 
		this.target = ip;
	}
	
	public ArrayList<?> getAllIp() {
		return ip;
	}
	
	private void ipType(InetAddress addr) {
		if(addr.isLoopbackAddress()) {
			setDelay(0, 10);
			return;
		}
		if(addr.equals(getLanIp())) {
			setDelay(0, 100);
			return;
		}
		if(addr.isSiteLocalAddress()) {
			setDelay(5);
			return;
		}
		setDelay(10);	
	}
	
	private String getLanIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	            	InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
	                	String ipAddress=inetAddress.getHostAddress().toString();
	                    return ipAddress;
	                }
	            }
	        }
		} catch (SocketException ex) { 
			ex.printStackTrace();
		}
	    return null; 
	}
	
	public String getLocalAddr() {
		return localAddr;
	}
	
	public void ipAllResolv(String host) {
		try {
			ip.clear();
			for(InetAddress addr : InetAddress.getAllByName(host)) {
				ip.add(addr.getHostAddress()+" ["+addr.getCanonicalHostName()+"]");			
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		JScanner.panePrint(JScanner.output, "\n", new Font("monospaced", Font.BOLD, 17), Color.LIGHT_GRAY);
	}
	
	public boolean ipResolver() {
		String ipAddr = "";
		try {
			InetAddress inetAddr = InetAddress.getByName(target);
			byte[] addr = inetAddr.getAddress();	
			for(int i=0; i<addr.length; i++) {
				if(i>0)
					ipAddr += ".";
				ipAddr += addr[i] & 0xFF;
			}
			ipType(inetAddr);
			setHostname(inetAddr.getHostName());
			sockaddr = new InetSocketAddress(ipAddr, 1);
			socket = new Socket();
			socket.connect(sockaddr, 3500);
		} catch (NoRouteToHostException e) {
			ErrorMessage error = new ErrorMessage("No route to host " + target);
			error.EMessage();
			return false;
		} catch (UnknownHostException e) {
			ErrorMessage error = new ErrorMessage("Unknown host " + target);
			error.EMessage();
			return false;
		} catch (IOException e) {
			if(e.getMessage().equals(CONNECT_REFUSED) || e.getMessage().equals(CONNECT_TIME_OUT)) {
				setTarget(ipAddr);
				return true;
			} else {
				ErrorMessage error = new ErrorMessage("I/O Error " + target);
				error.EMessage();
				return false;
			}
		} 
		setTarget(ipAddr);
		return true;
	}

}

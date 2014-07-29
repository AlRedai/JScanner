package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Services  {
	public final static String PORTDEF = System.getProperty("user.dir") + "/ports.defs";
	public final static int NUDP = 231;
	public final static int NTCP =  318;
	
	public String getService(String proto, int port) throws IOException {
		String line = null;
		File portlist = new File(PORTDEF);
		@SuppressWarnings("resource")
		BufferedReader buff = new BufferedReader(new FileReader(portlist));
		while((line = buff.readLine()) != null) {
			if(line.indexOf(proto) >= 0) {
				String[] reg = line.split(new String("\\s+"));
				String[] ports = reg[1].split(new String("[/]"));
				int n = Integer.parseInt(ports[0]);
				if(n == port) {
					return reg[0];
				}
			}
		}
		return "Service Not Found.";
	}	

}



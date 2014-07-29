package method.tcp;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import data.DataTarget;
import scanner.JScanner;
import services.Services;


class TcpConnect extends Thread {
	private static DataTarget data = new DataTarget();
	private int port;
	private String host;
	
	public TcpConnect(int port, String host) {
		this.port = port;
		this.host = host;
	}
	
	public void run() {
		try {
			SocketAddress sockaddr = new InetSocketAddress(host, port);
			Socket socket = new Socket();
			socket.connect(sockaddr, 100);
			JScanner.panePrint(JScanner.output, JScanner.trueIcon);
			JScanner.panePrint(JScanner.output, "  port "+port+"\t", new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
			JScanner.panePrint(JScanner.output, "OPEN\n", new Font("monospaced", Font.BOLD, 17), Color.GREEN);
			data.setOpenPort(port);
			socket.close();
		} catch (IOException io) {
			if(io.getMessage().equals("Connection refused")) {
				data.setClosedPort();
				//JScanner.output.append(" - port "+port+" is CLOSED"+"\n");
			}
			if(io.getMessage().equals("connect timed out")) {
				data.setFilteredPort();
				//JScanner.output.append(" - port "+port+" is FILTERED"+"\n");
			}
			JScanner.output.setCaretPosition(JScanner.output.getText().length());
		}
	}
}

public class Tcp extends JScanner {

	private static final long serialVersionUID = 1L;
	private static DataTarget data = new DataTarget();
	
	public static int rangeTcpScan() throws InterruptedException  {	
		int delay[] = data.getNDelay();
		boolean ndelay = false;
		if(delay[0] > 0 || delay[1] > 0) {
			ndelay = true;
		}
		resetBar();
		for(int i=data.getPortMin(); i<=data.getPortMax(); i++) {
			updateBar(i);
			Thread tcp = new Thread(new TcpConnect(i, data.getTarget()));
			tcp.start();
			//tcp.join();
			if(ndelay) {
				Thread.sleep(delay[0], delay[1]);
			} else { 
				Thread.sleep(data.getDelay());
			}
		}

		finishBar("Finish!");
		return 0;
	}
	
	public static int listTcpScan() throws InterruptedException, IOException {
		File portlist = new File(Services.PORTDEF);
		BufferedReader buff = new BufferedReader(new FileReader(portlist));
		String line = null;
		int delay[] = data.getNDelay();
		boolean ndelay = false;
		int i=0;
		if(delay[0] > 0 || delay[1] > 0) {
			ndelay = true;
		}
		resetBar();
		while((line = buff.readLine()) != null) {
			if(line.indexOf("tcp") >= 0) {
				String[] reg = line.split(new String("\\s+"));
				reg = reg[1].split(new String("[/]"));
				Thread tcp = new Thread(new TcpConnect(Integer.parseInt(reg[0]), data.getTarget()));
				tcp.start();
				if(ndelay) {
					Thread.sleep(delay[0], delay[1]);
				} else { 
					Thread.sleep(data.getDelay());
				}
				updateBar(i, reg[0]);
				i++;
			}
		}
		finishBar("Finish!");
		buff.close();
		return 0;
	}


}




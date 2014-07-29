package method.udp;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import data.DataTarget;
import scanner.JScanner;
import services.Services;

class UdpConnect extends Thread {
    public static Logger logger = Logger.getLogger(UdpConnect.class.getName());
    private static DataTarget data = new DataTarget();
    private String host;
    private int port;
	
    public UdpConnect(int port, String host) {
		this.port = port;
		this.host = host;
	}
	
	public void run() {
		DatagramSocket udpsock = null;
		byte[] udata = host.getBytes();
		
		try {
			udpsock = new DatagramSocket();
			udpsock.setSoTimeout(1000);
			udpsock.setTrafficClass(0x04|0x10);
			udpsock.connect(new InetSocketAddress(host, port));
			udpsock.send(new DatagramPacket(udata, udata.length));
			while(true) {
				byte[] receive = new byte[4096];
				DatagramPacket packet = new DatagramPacket(receive, 4096);
				udpsock.receive(packet);
				if(packet != null && packet.getData() != null) {
					logger.info(new String(packet.getData()));
					byte[] bs = packet.getData();
					for(int i=0; i<bs.length; i++) {
						logger.info(bs[i] + "");
					}
					JScanner.panePrint(JScanner.output, JScanner.trueIcon);
					JScanner.panePrint(JScanner.output, "  port "+port+"\t", new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
					JScanner.panePrint(JScanner.output, "OPEN\n", new Font("monospaced", Font.BOLD, 17), Color.GREEN);
					break;
				}
			}
		} catch (Exception e) {
			if(e.getMessage().equals("ICMP Port Unreachable")) {
				//JScanner.output.append(" - port " + port + " is CLOSED|FILTERED\n");
			}
			if(e.getMessage().equals("Receive timed out")) {
				JScanner.panePrint(JScanner.output, JScanner.trueIcon);
				JScanner.panePrint(JScanner.output, "  port "+port+"\t", new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
				JScanner.panePrint(JScanner.output, "OPEN\n", new Font("monospaced", Font.BOLD, 17), Color.GREEN);
				//JScanner.output.append(" + port " + port + "\t is OPEN|FILTERED\n");
				data.setOpenPort(port);
			}
		} finally {
			udpsock.close();
		}
		JScanner.output.setCaretPosition(JScanner.output.getText().length());
	}
}

public class Udp extends JScanner {
	
	private static final long serialVersionUID = 1L;
	private static DataTarget data = new DataTarget();

	public static int rangeUdpScan() throws InterruptedException  {
		resetBar();
		for(int i=data.getPortMin(); i<=data.getPortMax(); i++) {
			updateBar(i);
			Thread udp = new Thread(new UdpConnect(i, data.getTarget()));
			udp.start();
			//udp.join();
			Thread.sleep(data.getDelay());
		}
		finishBar("Finish!");
		return 0;
	}
	
	public static int listUdpScan() throws InterruptedException, IOException {
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
			if(line.indexOf("udp") >= 0) {
				String[] reg = line.split(new String("\\s+"));
				reg = reg[1].split(new String("[/]"));
				Thread tcp = new Thread(new UdpConnect(Integer.parseInt(reg[0]), data.getTarget()));
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


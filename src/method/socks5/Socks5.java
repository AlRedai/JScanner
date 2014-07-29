package method.socks5;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import data.DataTarget;
import scanner.JScanner;
import services.Services;

class Socks5Connect extends Thread {
	private final static byte TOR_CONNECT   = (byte)0x01;
	//private final static byte TOR_RESOLV    = (byte)0xF0;
	private final static byte SOCKS_VERSION = (byte)0x05;
	private final static byte SOCKS_DELIM   = (byte)0x00;
	
	//rivate final static byte[] SOCKS_INIT_RESOLV = { SOCKS_VERSION, TOR_RESOLV, SOCKS_DELIM };
	private final static byte[] SOCKS_INIT_SCAN = { SOCKS_VERSION, TOR_CONNECT, SOCKS_DELIM };
	//private final static byte[] SOCKS_INIT = { 0x05, 0x01, 0x00 };
	
	private static DataTarget data = new DataTarget();
	private int port;
	private String host;
	
	private static byte[] intToNByteArray(int myInt, int n) {   
        byte[] myBytes = new byte[n];
        for (int i = 0; i < n; ++i) {
            myBytes[i] = (byte)((myInt >> ((n-i-1)*8))&0xff);
        }
        return myBytes;
    }
	
	public Socks5Connect(int port, String host) {
		this.port = port;
		this.host = host;
	}
	
	public void run() {
		try {
			SocketAddress sockaddr = new InetSocketAddress("127.0.0.1", 9050);
			Socket socket = new Socket();	
			socket.connect(sockaddr);
			socket.setSoTimeout(1000);
			DataOutputStream send = new DataOutputStream(socket.getOutputStream());
			DataInputStream recv = new DataInputStream(socket.getInputStream());
			send.write(SOCKS_INIT_SCAN);
			send.flush();
			byte[] response = new byte[2];
			recv.read(response);
			if(response[0] != 0x05 && response[1] != 0x00) {
				System.out.println("Socks5 init session error!");
			}
			byte[] target = host.getBytes("utf-8");
			byte[] request = new byte[7 + target.length];
			request[0] = 0x05; 
	        request[1] = 0x01;
	        request[2] = 0x00;
	        request[3] = 0x03; 
			request[4] = intToNByteArray(target.length, 1)[0];
			System.arraycopy(target, 0, request, 5, target.length);
			byte[] portArray = intToNByteArray(port, 2);
			System.arraycopy(portArray, 0, request, 5+target.length, 2);
			send.write(request);
			send.flush();
			response = new byte[4];
			recv.read(response);
			if(response[0] == 0x05 && response[1] == 0x00) {
				JScanner.panePrint(JScanner.output, JScanner.trueIcon);
				JScanner.panePrint(JScanner.output, " port "+port+"\t", new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
				JScanner.panePrint(JScanner.output, "OPEN\n", new Font("monospaced", Font.BOLD, 17), Color.GREEN);
				//JScanner.output.append(" + port "+port+"\t is OPEN"+"\n");
				data.setOpenPort(port);
			}
			if(response[0] == 0x05 && response[1] == 0x01) {
				JScanner.panePrint(JScanner.output, " port "+port, new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
				JScanner.panePrint(JScanner.output, "\t\tCLOSED\n", new Font("monospaced", Font.BOLD, 13), Color.RED);
				//JScanner.output.append(" + port "+port+"\t is CLOSED"+"\n");
			}
			if(response[0] == 0x05 && response[1] == 0x06) {
				JScanner.panePrint(JScanner.output, " port "+port, new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
				JScanner.panePrint(JScanner.output, "\t\tFILTERED\n", new Font("monospaced", Font.BOLD, 13), Color.RED);
				//JScanner.output.append(" + port "+port+"\t is FILTERED"+"\n");
			}
			socket.close();
		} catch(IOException e) {
			JScanner.panePrint(JScanner.output, "port "+port, new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
			JScanner.panePrint(JScanner.output, "\t\tFILTERED\n", new Font("monospaced", Font.BOLD, 13), Color.RED);
			//JScanner.output.append(" + port "+port+" is FILETRED"+"\n");
		}
		JScanner.output.setCaretPosition(JScanner.output.getText().length());
	}
}


public class Socks5 extends JScanner {

	private static final long serialVersionUID = 1L;
	private static DataTarget data = new DataTarget();
	
	public static int rangeSocks5Scan() throws InterruptedException  {
		resetBar();
		for(int i=data.getPortMin(); i<=data.getPortMax(); i++) {
			scanstat.setText("Scanned port " + i);
			pbar.setValue(i);
			Thread socks5 = new Thread(new Socks5Connect(i, data.getTarget()));
			socks5.start();
			Thread.sleep(200);
		}
		finishBar("Finish!");
		return 0;
	}
	
	public static int listSocks5Scan() throws InterruptedException, IOException {
		File portlist = new File(Services.PORTDEF);
		BufferedReader buff = new BufferedReader(new FileReader(portlist));
		String line = null;
		//int delay[] = data.getNDelay();
		//boolean ndelay = false;
		int i=0;
		/*
		if(delay[0] > 0 || delay[1] > 0) {
			ndelay = true;
		}
		*/
		resetBar();
		while((line = buff.readLine()) != null) {
			if(line.indexOf("tcp") >= 0) {
				String[] reg = line.split(new String("\\s+"));
				reg = reg[1].split(new String("[/]"));
				Thread tcp = new Thread(new Socks5Connect(Integer.parseInt(reg[0]), data.getTarget()));
				tcp.start();
				/*
				if(ndelay) {
					Thread.sleep(delay[0], delay[1]);
				} else { 
					Thread.sleep(data.getDelay());
				}
				*/
				Thread.sleep(200);
				updateBar(i, reg[0]);
				i++;
			}
		}
		finishBar("Finish!");
		buff.close();
		return 0;
	}
}


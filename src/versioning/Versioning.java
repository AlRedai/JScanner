package versioning;

import java.util.Iterator;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class Versioning {
	private ArrayList<Integer> portList;
	private ArrayList<String> serviceList = new ArrayList<String>();
	private String HTTP = "HEAD / HTTP/1.0\r\n\r\n\r\n";
	private String HTTP_OK = "HTTP/1.1 200 OK";
	private String HTTP_ERR = "400 Bad Request";
	private boolean conn = true;
	//private boolean grab = false;
	private Selector selector;
	private String host;
	private static String version;
	private int port;
	
	public Versioning(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public Versioning(String host, ArrayList<Integer> port) {
		this.host = host;
		this.portList = new ArrayList<Integer>(port);
	}
	
	public void listVersion() {
		for(Integer ports : portList) {
			this.port = ports;
			System.out.println(ports);
			Version();
			setListVersion(getVersion());
			System.out.println(getVersion());
		}
	}
	
	public void Version()  {
		conn = true;
		SocketChannel channel;
		
		try {
			selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);

			channel.register(selector, SelectionKey.OP_CONNECT);
			channel.connect(new InetSocketAddress(host, port));
			int i=0;
			while (conn){
				if(i == 2) {
					channel.register(selector, SelectionKey.OP_WRITE);
				}
				selector.select(1000);
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

				while (keys.hasNext()){
					SelectionKey key = keys.next();
					keys.remove();
					if (!key.isValid()) {
						continue;
					}
					if (key.isConnectable()){
						connect(key);
					}	
					if (key.isWritable()){
						write(key, HTTP);
					}
					if (key.isReadable()){
						read(key);
					}
				}
				i++;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			close();
		}
	}
	
	private void close(){
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void read (SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(1000);
		readBuffer.clear();
		int length;
		try {
			length = channel.read(readBuffer);
		} catch (IOException e){
			key.cancel();
			channel.close();
			setVersion("N.D");
			conn = false;
			return;
		} 
		if (length == -1){
			channel.close();
			key.cancel();
			setVersion("N.D.");
			conn = false;
			return;
		}
		readBuffer.flip();
		byte[] buff = new byte[length];
		readBuffer.get(buff, 0, length);
		grabSt(new String(buff));
		//setVersion(new String(buff));
		key.interestOps(SelectionKey.OP_WRITE);
		conn = false;
	}

	private void write(SelectionKey key, String payload) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		channel.write(ByteBuffer.wrap(payload.getBytes()));
		key.interestOps(SelectionKey.OP_READ);
	}

	private void connect(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		if (channel.isConnectionPending()){
			channel.finishConnect();
		}
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
	}
	
	private void setVersion(String vers) {
		version = vers;
	}
	
	public String getVersion() {
		return version;
	}
	
	private void setListVersion(String str) {
		serviceList.add(str); 
	}
	
	public ArrayList<String> getListVersion() {
		return serviceList;
	}
	
	private String grabSt(String response) {
		if(response.indexOf(HTTP_OK) >= 0 && response.indexOf("Server:") >= 0) {
			return response;
		}
		if(response.indexOf(HTTP_ERR) >= 0) {
			setVersion("N.d.");
			return new String("N.d");
		}
		setVersion(response);
		return response;
	}
	
}

package method.tcp;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import javax.swing.SwingWorker;

import data.DataTarget;
import scanner.JScanner;
import services.Services;


public class TaskTcp extends SwingWorker<Void, Void> {
	private static DataTarget data = new DataTarget();
	private static Services service = new Services();
	
	@Override
	public Void doInBackground() throws IOException {	
		int ret = 1;
		JScanner.panePrint(JScanner.output, "\n\n", new Font("monospaced", Font.BOLD, 17), Color.LIGHT_GRAY);
		try {
			if(JScanner.range.isSelected()) {
				while(isCancelled() || ret != 0) {
					ret = Tcp.rangeTcpScan();
				}
			} else {
				while(isCancelled() || ret != 0) {
					ret = Tcp.listTcpScan();
				}
			}

		} catch (InterruptedException e) { 
			try {
				Thread.sleep(100);
			} catch (InterruptedException s) { }
			JScanner.panePrint(JScanner.output, "\n[*] Scan stop! [*]\n", new Font("monospaced", Font.PLAIN, 13), Color.RED);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) { }
		if(!data.getOpenPort().isEmpty()) {
			JScanner.panePrint(JScanner.output, "\n PORT\tSTATE\tSERVICE\n\n", new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
			for(int i=0; i<data.getOpenPort().size(); i++) {
				JScanner.panePrint(JScanner.output, " "+data.getOpenPort().get(i), new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
				JScanner.panePrint(JScanner.output, "\tOPEN\t", new Font("monospaced", Font.BOLD, 17), Color.GREEN);
				JScanner.panePrint(JScanner.output, service.getService(data.getProtocol(), (Integer)data.getOpenPort().get(i))+"\n", new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
				//Banner banner = new Banner(opt.getHost(), (Integer) opt.getOpenPort().get(i));
				//banner.GrabVersion();

			}
			JScanner.panePrint(JScanner.output, "\n[*] STATISTICS: OPEN "+data.getOpenPort().size()+"  CLOSED "+data.getClosedPort()+"  FILTERED "+data.getFilteredPort(),  new Font("monospaced", Font.PLAIN, 13), Color.LIGHT_GRAY);
			data.resetPortData();
		}
		JScanner.scan.setEnabled(true);
		JScanner.stop.setEnabled(false);
		return null;
	}
}

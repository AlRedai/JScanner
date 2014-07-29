package scanner;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JSeparator;

import data.DataTarget;
import resolution.Resolv;
import services.Services;
import method.tcp.TaskTcp;
import method.udp.TaskUdp;
import method.socks5.TaskTor;

public class JScanner extends JFrame {

	private static final long serialVersionUID = 1L;
	private static JCheckBoxMenuItem tcp, udp, tor;
	protected static JTextField host;
	private static JTextField portMin;
	private static JTextField portMax;
	
	public static DefaultMutableTreeNode rootIp = new DefaultMutableTreeNode(" Resolution");
	public static JTree tree = new JTree(rootIp);
	
	protected static JProgressBar pbar;
	protected static JLabel scanstat;
	
	public static JCheckBoxMenuItem range;
	public static JTextPane output;
	public static StyledDocument doc;
	public static JButton scan;
	public static JButton stop;
	
	private TaskTcp taskTcp;
	private TaskUdp taskUdp;
	private TaskTor taskTor;
	
	private static DataTarget data = new DataTarget();
	
	public final static Icon trueIcon = new ImageIcon(System.getProperty("user.dir")+"/true.png");
	public final static Icon falseIcon = new ImageIcon(System.getProperty("user.dir")+"/false.jpeg");
	
	public JScanner() {
		initScan();
	}
	
	private Component setSepV() {
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);  
        Dimension d = separator.getPreferredSize();  
        d.height = getPreferredSize().height;  
        separator.setPreferredSize(d);
        return separator;
	}
	
	protected static void resetBar() {
		pbar.setValue(0);
		pbar.setString(null);
		pbar.setStringPainted(true);
		if(range.isSelected())
			pbar.setMaximum(data.getPortMax()-data.getPortMin());
		if(!range.isSelected() && data.getProtocol().equals("tcp")){
			pbar.setMaximum(Services.NTCP);
		}
		if(!range.isSelected() && data.getProtocol().equals("udp")){
			pbar.setMaximum(Services.NUDP);
		}
		if(!range.isSelected() && data.getProtocol().equals("tor")){
			pbar.setMaximum(Services.NTCP);
		}
			
	}
	
	protected static void finishBar(String Msg) {
		pbar.setString(Msg);	
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scanstat.setText(" Scanned port");
		pbar.setValue(0);
		pbar.setString("I'm ready...");
	}
	
	protected static void updateBar(int value) {
		scanstat.setText("Scanned port " + value);
		pbar.setValue(value);
	}
	
	protected static void updateBar(int value, String port) {
		scanstat.setText("Scanned port " + port);
		pbar.setValue(value);
	}
	
	public static void panePrint(JTextPane pane, Icon icon) {
		StyleContext context = new StyleContext();
		Style labelStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
	    Image img = ((ImageIcon) icon).getImage();  
	    Image newimg = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);  
	    Icon newIcon = new ImageIcon(newimg);  
		JLabel label = new JLabel(newIcon);
		StyleConstants.setComponent(labelStyle, label);
		try { 
        	doc.insertString(doc.getLength(), "ignored", labelStyle); 
        }
        catch (BadLocationException e){}
	}
	
	public static void panePrint(JTextPane pane, String str, Font font, Color color) {
		MutableAttributeSet mutable = pane.getInputAttributes();
		StyleConstants.setFontFamily(mutable, font.getFamily());
		StyleConstants.setFontSize(mutable, font.getSize());
        StyleConstants.setForeground(mutable, color);
        try { 
        	doc.insertString(doc.getLength(), str, mutable); 
        }
        catch (BadLocationException e){}
	
	}
	
	public final void initScan() {
		//GetOpt opt = new GetOpt();
		JMenuBar menu = new JMenuBar();
		JMenu main = new JMenu("Main");
		main.setMnemonic(KeyEvent.VK_M);
		JMenuItem exit = new JMenuItem("Exit");
		exit.setMnemonic(KeyEvent.VK_E);
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		main.add(exit);
		menu.add(main);
		
		JMenu option = new JMenu("Option");
		option.setMnemonic(KeyEvent.VK_O);
		tcp = new JCheckBoxMenuItem("Tcp");
		udp = new JCheckBoxMenuItem("Udp");
		tor = new JCheckBoxMenuItem("Tor");
		tcp.addActionListener(new TcpMethod());
		udp.addActionListener(new UdpMethod());
		tor.addActionListener(new TorMethod());
		tcp.setState(true);
		udp.setState(false);
		tor.setState(false);
		option.add(tcp);
		option.add(udp);
		option.add(tor);
		
		menu.add(option);
		setJMenuBar(menu);
		
		JPanel inputhost = new JPanel();
		JLabel lab = new JLabel("Target");
		host    = new JTextField(new Resolv().getLocalAddr(), 25);
		portMin = new JTextField("1",5);
		portMax = new JTextField("100",5);
		range   = new JCheckBoxMenuItem("Range");
	    scan    = new JButton("Scan");
	    
	    portMin.addKeyListener(PortControlInput);
	    portMin.setHorizontalAlignment(JTextField.RIGHT);
	    
	    portMax.addKeyListener(PortControlInput);
	    portMax.setHorizontalAlignment(JTextField.RIGHT);
	    
	    scan.addActionListener(new ScanListener());
	    
	    range.setState(true);
		range.addActionListener(new RangeListener());

		inputhost.add(lab);
		inputhost.add(host);
		inputhost.add(setSepV());
		inputhost.add(portMin);
		inputhost.add(setSepV());
		inputhost.add(portMax);
		inputhost.add(setSepV());
		inputhost.add(range);
		inputhost.add(setSepV());
		inputhost.add(scan);
		add(inputhost, "North");

		JPanel panel = new JPanel(new BorderLayout());
		output = new JTextPane();
	    doc = (StyledDocument)output.getDocument();
	    output.setEditable(false);
	    output.setForeground(Color.LIGHT_GRAY);
	    //output.setBackground(new Color(0,0,128));
	    output.setBackground(Color.BLACK);
	    output.setFont(new Font("Arial", Font.TRUETYPE_FONT, 12));
	    output.setMinimumSize(new Dimension(200, 320));
	    DefaultCaret caret = (DefaultCaret)output.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    panel.add(new JScrollPane(output));
	    panel.setBorder(new EmptyBorder(new Insets(20, 20, 5, 20)));

	    add(panel,"Center");
	    
	    JPanel south = new JPanel();
	    JButton clear = new JButton("Clear");
	    stop = new JButton("Stop");
	    stop.setEnabled(false);
	    clear.addActionListener(new ClearListener());
	    stop.addActionListener(new StopListener());
	    south.add(clear, BorderLayout.CENTER);
	    south.add(stop);
	        
	    JPanel stats = new JPanel();
	    stats.setLayout(new BorderLayout());
	    stats.setPreferredSize(new Dimension(400,20));
	    scanstat = new JLabel(" Scanned port");
		
	    pbar = new JProgressBar();
	    //pbar.setMaximum(opt.getPortMax() - opt.getPortMin() - 1);
	    pbar.setMaximum(100);
		pbar.setValue(0);
	    pbar.setStringPainted(true);
	    pbar.setPreferredSize(new Dimension(545, 20));		
	    pbar.setOrientation(SwingConstants.HORIZONTAL);

	    stats.add(scanstat);
	    stats.add(pbar, BorderLayout.EAST);
	   
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, south, stats);
		add(split, "South");

		setTitle("JScanner v1.0");
		setSize(700, 650);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	class TcpMethod implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			data.setProtocol("tcp");
			udp.setState(false);
			tor.setState(false);
		}
	}
	
	class UdpMethod implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			data.setProtocol("udp");
			tcp.setState(false);
			tor.setState(false);
		}
	}
	
	class TorMethod implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			data.setProtocol("tor");
			tcp.setState(false);
			udp.setState(false);
		}
	}
	
	class RangeListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(JScanner.range.getState()) {
				JScanner.portMax.setEnabled(true);
				JScanner.portMin.setEnabled(true);
			} else {
				JScanner.portMax.setEnabled(false);
				JScanner.portMin.setEnabled(false);
			}
		}	
	}
	
	class ScanListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			data.setPortMin(portMin.getText());
			data.setPortMax(portMax.getText());
			data.setTarget(host.getText());
			Resolv resolv = new Resolv(data.getTarget());
			if(resolv.ipResolver()) {
				scan.setEnabled(false);
				stop.setEnabled(true);
				output.setText(null);
				panePrint(output, "[*] Start "+data.getProtocol()+" scan on "+host.getText()+" [", new Font("monospaced", Font.BOLD, 17), Color.LIGHT_GRAY);
				panePrint(output, data.getTarget(), new Font("monospaced", Font.BOLD, 17), Color.MAGENTA);
			    panePrint(output, "] [*]\n", new Font("monospaced", Font.BOLD, 17), Color.LIGHT_GRAY);
				resolv.ipAllResolv(host.getText());
				rootIp.removeAllChildren();
				rootIp.setUserObject(data.getHostname());
				tree.removeAll();
				int i=0;
				for(i=0; i<resolv.getAllIp().size(); i++) {
					rootIp.add(new DefaultMutableTreeNode(resolv.getAllIp().get(i)));
				}
	            UIManager.put("Tree.rendererFillBackground", true);
			    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		        renderer.setLeafIcon(null);
		        renderer.setClosedIcon(null);
		        renderer.setOpenIcon(null);
		        renderer.setBackground(Color.BLACK);
		        renderer.setBackgroundNonSelectionColor(Color.BLACK);
		        renderer.setTextSelectionColor(Color.BLUE);
		        renderer.setTextNonSelectionColor(Color.BLUE);
		        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		        tree.setBackground(Color.BLACK);
		        tree.setOpaque(true);
		        model.reload();
		        output.insertComponent(tree);
		       
				if(data.getProtocol().equals("tcp")) {
					(taskTcp = new TaskTcp()).execute();
				}
				if(data.getProtocol().equals("udp")) {
					(taskUdp = new TaskUdp()).execute();
				}
				if(data.getProtocol().equals("tor")) {
					(taskTor = new TaskTor()).execute();
				}
			} 
		}
	}
	
	KeyListener PortControlInput = new KeyListener() {
		@Override
		public void keyPressed(KeyEvent e) { 
		}
		
		@Override
		public void keyReleased(KeyEvent e) { 
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			if(!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
				e.consume();
			}
		}
	};
	
	class ClearListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			output.setText(null);
		}
	}
	
	class StopListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			stop.setEnabled(false);
			scan.setEnabled(true);
			if(data.getProtocol().equals("tcp"))
				taskTcp.cancel(true);
			if(data.getProtocol().equals("udp"))
				taskUdp.cancel(true);
			if(data.getProtocol().equals("tor"))
				taskTor.cancel(true);
			finishBar("Stopped!");
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JScanner scan = new JScanner();
				scan.setVisible(true);
			}
		});
	}

}

package error;

import javax.swing.JOptionPane;

public class ErrorMessage {
	private String message;
	
	public ErrorMessage(String msg) {
		this.message = msg;
	}
	
	public void EMessage() {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void WMessage() {
		JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void IMessage() {
		JOptionPane.showMessageDialog(null, message, "Question", JOptionPane.INFORMATION_MESSAGE);
	}
	

}

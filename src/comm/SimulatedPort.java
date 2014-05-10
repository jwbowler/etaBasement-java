package comm;

import jssc.SerialPort;


public class SimulatedPort extends SerialPort {
	public SimulatedPort() {
		super("");
	}
	
	public boolean writeByte(byte b) {
		System.out.println("Sending: " + formatByte(b));
		return true;
	}

	public boolean writeBytes(byte[] data) {
		System.out.println("Sending: " + formatBytes(data));
		return true;
	}
	
	public static String formatByte(byte b) {
		String hex = Integer.toHexString(b);
		if (hex.length() > 2)
			hex = hex.substring(hex.length() - 2);
		if (hex.length() < 2)
			hex = "0" + hex;
		return "0x" + hex;
	}
	
	public static String formatBytes(byte[] data) {
		String str = "";
		if (data != null) {
			for (byte b : data) {
				str += formatByte(b) + " ";
			}
		}
		return str;
	}
}

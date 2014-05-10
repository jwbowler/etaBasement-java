package comm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortException;

public class Transmitter extends Thread {
	
	private static final int DELAY = 10;
	
	public enum SerialPortType {
		LINUX, SIMULATION, WINDOWS
	}
	
	private SerialPort serialPort;
	private List<Transmittable> transmittingObjs = new ArrayList<Transmittable>();
	private ByteArrayOutputStream message = new ByteArrayOutputStream();
	
	public void connect(SerialPortType portType) {
		String port = "";
		
		// Windows: auto-connect to COM port
		if (portType == SerialPortType.WINDOWS) {
			port = "COM";
		}

		// Linux: auto-connect to ACM port
		else if (portType == SerialPortType.LINUX) {
			port = "/dev/ttyUSB";
		}

		// Simulation mode
		else if (portType == SerialPortType.SIMULATION) {
			System.err.println("RUNNING IN SIMULATION MODE.");
			serialPort = new SimulatedPort();
			return;
		}
		
		connect(port);
	}
	
	void connect(String port) {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					finalize();
				} catch (Throwable e) {
					e.printStackTrace();
				}
		    }
		});

		// Auto-connect to port of given type "port"
		int i = 0;
		for (i = 0; i < 20; i++) {
			try {
				serialPort = new SerialPort(port + i);
				serialPort.openPort();
				serialPort.setParams(9600, 8, 1, 0);
				break;
			} catch (SerialPortException ex) {
			}
		}
		if (i == 20) {
			System.err.println("Failed to auto-connect to serial port of type \"" + port + "\"");
			System.exit(0);
		}

		System.out.println("Connected to serial port: " + port + i);
	}

	public void addMessageSegment(Transmittable transmittable) {
		transmittingObjs.add(transmittable);
	}
	
	public void run() {
		while (true) {
			message.reset();
			message.write(255); // frame sync
			for (Transmittable t: transmittingObjs) {
				try {
					t.getPacket().writeTo(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				serialPort.writeBytes(message.toByteArray());
			} catch (SerialPortException e) {
				System.err.println("Exception while writing to serial port");
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

import java.awt.FlowLayout;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class Main {
	
	static JFrame frame;
	
	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		
		final SpectrumView spectrumView = new SpectrumView();
		SpectrumAnalyzer spectrumAnalyzer = new SpectrumAnalyzer(createStream(), spectrumView);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame = new JFrame();
				frame.setLayout(new FlowLayout());
				frame.add(spectrumView);
				frame.pack();
				frame.setVisible(true);
			}
		});
		
		spectrumAnalyzer.start();
	
	}
	
	private static AudioInputStream createStream() {		
		AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		
		TargetDataLine line = null;
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open();
		} catch (LineUnavailableException e) {
			System.err.println("Audio line is unavailable");
			System.exit(1);
		}
		line.start();
		return new AudioInputStream(line);
	}
	
//	private static void printMixerInfo() {
//		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
//		
//		for (Mixer.Info info: mixerInfos){
//			Mixer m = AudioSystem.getMixer(info);
//			System.out.println("Mixer: " + info.getName());
//			
//			System.out.println("Source lines:");
//			Line.Info[] lineInfos = m.getSourceLineInfo();
//			for (Line.Info lineInfo: lineInfos){
//				System.out.println("\t" + lineInfo);
//			}
//			
//			System.out.println("Target lines:");
//			lineInfos = m.getTargetLineInfo();
//			for (Line.Info lineInfo: lineInfos){
//				System.out.println("\t" + lineInfo);
//			}
//			System.out.println();
//		}
//	}
}

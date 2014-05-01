import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class Main {
	
	static JFrame frame;
	
	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		
		final SpectrumView spectrumView = new SpectrumView();
		SpectrumAnalyzer spectrumAnalyzer = new SpectrumAnalyzer(createStream());
		
		spectrumAnalyzer.attachConsumer(spectrumView);
		
		final ArrayList<Letter> letters = new ArrayList<>();
		letters.add(new Letter(0, 15, 65));
		letters.add(new Letter(0, 60, 140));
		letters.add(new Letter(0, 120, 255));
		
		for (Letter l : letters)
			spectrumAnalyzer.attachConsumer(l);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame = new JFrame();
				frame.getContentPane().setBackground(Color.black);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				frame.setLayout(new FlowLayout());
				frame.add(spectrumView);
				
				
				JPanel vis = new JPanel(new GridLayout(1,3));
				for (Letter l : letters)
					vis.add(l.getIntensityVis());
				
				JPanel controllers = new JPanel(new GridLayout(0,1));
				for (Letter l : letters)
					controllers.add(l.getControllerPanel());

				JPanel zbt = new JPanel(new GridLayout(0,1));
				vis.setBackground(Color.BLACK);
				controllers.setBackground(Color.BLACK);
				zbt.add(vis);
				zbt.add(controllers);
				frame.add(zbt);
				
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

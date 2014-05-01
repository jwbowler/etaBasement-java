import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JPanel;

import utilities.RollingAverage;
import utilities.Utilities;


public class ZBT implements SpectrumConsumer{
	int DELAY_MILLIS = 20;
	int RUNNING_AVG_TIME = 10000;
	RollingAverage rollingAvg = new RollingAverage(RUNNING_AVG_TIME/DELAY_MILLIS);

	ArrayList<Letter> letters = new ArrayList<>();
	public ZBT() {
		letters.add(new Letter(0.25, 0.5, 15, 65));
		letters.add(new Letter(0.1, .7, 60, 140));
		letters.add(new Letter(0.1, .5, 120, 255));
	}
		
	@Override
	public void updateSpectrum(double[] spectrumData) {
		double sum = Utilities.fftSum(0, 256, spectrumData);
		
		for (Letter l : letters) {
			l.setPrescale(rollingAvg.update(sum));
			l.updateSpectrum(spectrumData);
		}
	}
	
	public JPanel getPanel() {
		JPanel vis = new JPanel(new GridLayout(1,3, 100, 0));
		for (Letter l : letters) {
			JPanel subPanel = new JPanel();
			subPanel.setBackground(Color.BLACK);;
			subPanel.add(l.getOutputControlPanel());
			subPanel.add(l.getIntensityVis());
			vis.add(subPanel);
		}
			
		JPanel controllers = new JPanel(new GridLayout(0,1));
		for (Letter l : letters)
			controllers.add(l.getFrequencyControllerPanel());

		JPanel zbt = new JPanel(new GridLayout(0,1,0,20));
		vis.setBackground(Color.BLACK);
		controllers.setBackground(Color.BLACK);
		zbt.setBackground(Color.BLACK);
		zbt.add(vis);
		zbt.add(controllers);
		
		return zbt;
	}
}

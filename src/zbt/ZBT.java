package zbt;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import main.SpectrumConsumer;
import utilities.RollingAverage;
import utilities.Utilities;


public class ZBT implements SpectrumConsumer{
	int DELAY_MILLIS = 15;
	int RUNNING_AVG_TIME = 60000;
	RollingAverage rollingAvg = new RollingAverage(RUNNING_AVG_TIME/DELAY_MILLIS);

	ArrayList<Letter> letters = new ArrayList<>();
	public ZBT() {
		letters.add(new Letter(0.25, 0.5, 10, 55, 0.0f, "Z"));
		letters.add(new Letter(0.1, .7, 55, 130, 0.33f, "B"));
		letters.add(new Letter(0.1, .5, 120, 255, 0.66f, "T"));
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
		JPanel letterVis = new JPanel(new GridLayout(1, 3, 0, 0));
		letterVis.setBackground(Color.BLACK);
		for (Letter l : letters) {
			letterVis.add(l.getLetterVis());
		}
		
		JPanel controllers = new JPanel(new GridLayout(0, 1, 0, 5));
		controllers.setBackground(Color.BLACK);
		for (Letter l : letters)
			controllers.add(l.getFrequencyControllerPanel());

		JPanel ampVis = new JPanel(new GridLayout(1, 3, 50, 0));
		ampVis.setBackground(Color.BLACK);
		for (Letter l : letters) {
			JPanel subPanel = new JPanel();
			subPanel.setBackground(Color.BLACK);;
			subPanel.add(l.getOutputControlPanel());
			subPanel.add(l.getIntensityVis());
			ampVis.add(subPanel);
		}
		
		JPanel zbt = new JPanel();
		zbt.setLayout(new BoxLayout(zbt, BoxLayout.Y_AXIS));
		zbt.setBackground(Color.BLACK);
		zbt.add(letterVis);
		ampVis.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0));
		zbt.add(ampVis);
		zbt.add(controllers);
		
		return zbt;
	}
}

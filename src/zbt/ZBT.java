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

	ArrayList<Letter> letters = new ArrayList<>();
	public ZBT() {
		letters.add(new Letter(0.25, 0.5, 0, 50, 0.0f, "Z"));
		letters.add(new Letter(0.1, .7, 40, 120, 0.33f, "B"));
		letters.add(new Letter(0.1, .5, 90, 255, 0.66f, "T"));
	}
		
	@Override
	public void updateSpectrum(double[] spectrumData) {
		for (Letter l : letters) {
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

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JPanel;


public class ZBT implements SpectrumConsumer{
	int DELAY_MILLIS = 20;
	int RUNNING_AVG_TIME = 10000;
	double rollingAvg = 0;
	LinkedList<Double> pastVals = new LinkedList<>();
	ArrayList<Letter> letters = new ArrayList<>();
	public ZBT() {
		letters.add(new Letter(0, 15, 65));
		letters.add(new Letter(0, 60, 140));
		letters.add(new Letter(0, 120, 255));
	}
		
	@Override
	public void updateSpectrum(double[] spectrumData) {
		double sum = 0;
		for (int i = 0; i<256; i++)
			sum += spectrumData[i];

		pastVals.addLast(sum);
		if (pastVals.size() * DELAY_MILLIS > RUNNING_AVG_TIME)
			rollingAvg -= pastVals.removeFirst()/pastVals.size();
		else
			rollingAvg *= (pastVals.size()-1.0)/pastVals.size();
		
		rollingAvg += sum/pastVals.size();
		
		for (Letter l : letters) {
			l.setPrescale(rollingAvg);
			l.updateSpectrum(spectrumData);
		}
	}
	
	public JPanel getPanel() {
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
		
		return zbt;
	}
}

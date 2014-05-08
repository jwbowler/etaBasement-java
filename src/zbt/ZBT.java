package zbt;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import main.SpectrumAnalyzer;
import main.SpectrumConsumer;
import main.SpectrumView;


public class ZBT implements SpectrumConsumer{
	ArrayList<Letter> letters = new ArrayList<>();
	SpectrumView specView = new SpectrumView();
	JPanel panel;
	
	public ZBT() {
		for (String l : new String[]{"Z", "B", "T"})
			letters.add(new Letter(l, LetterParameters.Presets.TREND_STANDARD));
		
		panel = createPanel();
	}
	
	public void setPreset(LetterParameters.Presets preset) {
		for (Letter l : letters) {
			l.setParams(LetterParameters.getParameters(preset, l.letter));
			// stupid hacks below
			l.setParams(LetterParameters.getParameters(preset, l.letter));
			l.setParams(LetterParameters.getParameters(preset, l.letter));
		}
	}
		
	@Override
	public void updateSpectrum(double[] spectrumData) {
		for (Letter l : letters) {
			l.updateSpectrum(spectrumData);
			specView.updateSpectrum(spectrumData);
		}
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	private JPanel createPanel() {
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
		
		letterVis.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
		zbt.add(letterVis);	
		zbt.add(new ZBTController());
		ampVis.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0));
		zbt.add(ampVis);
		zbt.add(controllers);
		
		JPanel specPanel = new JPanel(new GridLayout(1,1));	
		specPanel.setBackground(Color.BLACK);
		specPanel.setBorder(BorderFactory.createEmptyBorder(5, 6, 0, 6));

		specView.setPreferredSize(new Dimension(0,150));
		specView.enableFlip(true);
		specPanel.add(specView);
		zbt.add(specPanel);

		
		
		return zbt;
	}
	
	private class ZBTController extends JPanel {
		public ZBTController() {
			super();
			
			this.setLayout(new GridLayout(1,1));
			
			final JComboBox<LetterParameters.Presets> presets = new JComboBox<>(LetterParameters.Presets.values());
			presets.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setPreset(presets.getItemAt(presets.getSelectedIndex()));
				}
			});
			
			this.add(presets);
		}
	}
}

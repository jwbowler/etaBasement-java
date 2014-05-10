package zbt;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import comm.Transmittable;

import slider.GradientGenerator;
import slider.RangeSlider;
import zbt.LetterParameters.ColorParamMode;
import main.SpectrumAnalyzer;
import main.SpectrumConsumer;
import main.SpectrumView;


public class ZBT implements SpectrumConsumer, Transmittable {
	ArrayList<Letter> letters = new ArrayList<>();
	SpectrumView specView = new SpectrumView();
	ZBTController controller;
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
	
	@Override
	public synchronized ByteArrayOutputStream getPacket() throws IOException {
		ByteArrayOutputStream packet = new ByteArrayOutputStream();
		for (Letter l : letters) {
			packet.write(l.getTransmittableColorData());
		}
		return packet;
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
		zbt.add(controller = new ZBTController());
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
		private ColorParamMode mode = ColorParamMode.SINGLE;
		
		JComboBox<LetterParameters.Presets> presets;
		RangeSlider hueSlider = new RangeSlider();
		RangeSlider satSlider = new RangeSlider();
		public ZBTController() {
			super();
			
			this.setLayout(new GridLayout(1,1));
			
			presets = new JComboBox<>(LetterParameters.Presets.values());
			presets.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setPreset(presets.getItemAt(presets.getSelectedIndex()));
					setMode(letters.get(0).getParams());
				}
			});
			
			hueSlider.setOrientation(RangeSlider.HORIZONTAL);
			hueSlider.setBackground(Color.BLACK);
			hueSlider.setFocusable(false);
			hueSlider.setMinimum(0);
			hueSlider.setMaximum(255);
			hueSlider.setGradientColor(new GradientGenerator.HsvGradientGenerator());
	        updateSlider(hueSlider, 30, 70);
	        
			satSlider.setOrientation(RangeSlider.HORIZONTAL);
			satSlider.setBackground(Color.BLACK);
			satSlider.setFocusable(false);
			satSlider.setMinimum(0);
			satSlider.setMaximum(255);
			satSlider.setGradientColor(new GradientGenerator.SaturationGradientGenerator(0.3f));
	        updateSlider(satSlider, 30, 70);

	        hueSlider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                for (Letter l : letters) {
	                	LetterParameters p = l.getParams();
	                	switch (mode) {
	                	case RANGE:
	                		if (p.hueFlip) {
				                p.hueEnd = hueSlider.getValue()/255.0;
				                p.hueStart = hueSlider.getUpperValue()/255.0;
	                		} else {
				                p.hueStart = hueSlider.getValue()/255.0;
				                p.hueEnd = hueSlider.getUpperValue()/255.0;
	                		}
			                break;
	                	case SINGLE:
			                p.hueStart = hueSlider.getUpperValue()/255.0;
			                satSlider.setGradientColor(new GradientGenerator.SaturationGradientGenerator((float)p.hueStart));
	                	}
	                }
	            }
	        });
	        
	        satSlider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	            	for (Letter l : letters) {
	                	LetterParameters p = l.getParams();
	                	switch (mode) {
	                	case RANGE:
			                break;
	                	case SINGLE:
			                p.hueEnd = 1.0 - satSlider.getUpperValue()/255.0;
	                	}
	                }
	            }
	        });
		    
	        this.setMode(LetterParameters.getParameters(presets.getItemAt(presets.getSelectedIndex()), "Z"));			
		}
		
		public void setMode(LetterParameters p) {
			this.mode = p.colorParam;
			this.removeAll();
			switch (mode) {
        	case RANGE:
    		    this.setLayout(new GridLayout(2,1));
    		    this.add(hueSlider);
    			this.add(presets);
    			if (p.hueFlip)
    				updateSlider(hueSlider, (int)(p.hueEnd*255), (int)(p.hueStart*255));
    			else
    				updateSlider(hueSlider, (int)(p.hueStart*255), (int)(p.hueEnd*255));

    			break;
        	case SINGLE:
    		    this.setLayout(new GridLayout(3,1));
    		    this.add(hueSlider);
    		    this.add(satSlider);
    			this.add(presets);      
    			updateSlider(hueSlider, 0, (int)(p.hueStart*255));
    			updateSlider(satSlider, 0, (int)((1.0 - p.hueEnd)*255));
    		}
			updateUI();
		}

		public void updateSlider(RangeSlider slider, int low, int high) {
	        slider.setValue(low);
	        slider.setUpperValue(high);
	        slider.setValue(low); //actually necessary if default upper < fMin
		}
	}
}

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import slider.RangeSlider;
import utilities.Utilities;

public class Letter implements SpectrumConsumer {

	private double outMin, outMax, prescale = 1E7;
	private String letter;
	private int fMin, fMax;
	private float hue;
	
	ArrayList<AmpVis> ampVis = new ArrayList<>();
	ArrayList<LetterVis> letterVis = new ArrayList<>();
	
	public Letter(double outMin, double outMax, int fMin, int fMax, float hue, String letter) {
		this.outMin = outMin;
		this.outMax = outMax;
		this.fMin = fMin;
		this.fMax = fMax;
		this.hue = hue;
		this.letter = letter;
	}
	
	public void setPrescale(double prescale) {
		this.prescale = prescale;
	}

	@Override
	public void updateSpectrum(double[] spectrumData) {
		hue += 0.001f;
		double amp = score(spectrumData);
		amp /= prescale;
		double out = Math.min(1.0, Math.max(0, (amp-outMin)/(outMax-outMin)));

		for (AmpVis v : ampVis)
			v.update(amp, out);
		
		for (LetterVis v : letterVis)
			v.update(out);
	}

	public double score(double[] fft) {
		double sum = 0;
		for (int i = fMin; i < fMax; i++)
			sum += fft[i];

		return sum;
	}
	
	public JPanel getLetterVis() {
		LetterVis vis = new LetterVis();
		letterVis.add(vis);
		return vis;
	}

	private class LetterVis extends JPanel {
		private double out;
		
		public LetterVis() {
			setPreferredSize(new Dimension(150, 150));
		}

		public void update(double out) {
			this.out = out;
			repaint();
		}

		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);

			Color BG_COLOR = Color.black;
			Color BAR_COLOR = Color.white;

			Graphics2D g = (Graphics2D) graphics;
	    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int size = Math.min(this.getWidth(), this.getHeight());
			g.setFont(new Font("Arial Black", Font.BOLD, (int)Math.round(size*(4.0/3.2))));
			g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			g.setColor(BG_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
			//g.setColor(Color.BLUE);
			//g.fillRect((getWidth()-size)/2, (getHeight()-size)/2, size, size);

			//g.setColor(new Color(255,255,255,(int)(255*out)));
			g.setColor(Utilities.colorFromHSBA(hue, 0.75f, 1.0f, (float)out));
			g.drawString(letter, (getWidth()-size)/2, getHeight() - (getHeight()-size)/2);
		}
	}

	public JPanel getIntensityVis() {
		AmpVis vis = new AmpVis();
		ampVis.add(vis);
		return vis;
	}

	private class AmpVis extends JPanel {
		private double amp, out;
		
		public AmpVis() {
			setPreferredSize(new Dimension(80, 200));
		}

		public void update(double amp, double out) {
			this.amp = amp;
			this.out = out;
			repaint();
		}

		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);

			Color BG_COLOR = Color.black;
			Color BAR_COLOR = Color.white;

			Graphics2D g = (Graphics2D) graphics;
			g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			g.setColor(BG_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(BAR_COLOR);

			int height = (int)Math.round(getHeight() * amp);
			//g.setColor(new Color(255,255,255,(int)(255*out)));
			//g.fillRect(5, getHeight()-height,getWidth()-10, height);
			g.setColor(BAR_COLOR);
			g.drawRect(5, getHeight()-height,getWidth()-10, height);
			
			
			/*
			g.setColor(new Color(0,0,0,128));
			g.fillRect(0, getHeight()-(int)(getHeight()*(outMin)), getWidth(), (int)(getHeight()*(outMin)));
			g.fillRect(0, 0, getWidth(), (int)(getHeight()*(1-outMax)));
			*/
			g.setColor(new Color(0,255,0,128));
			g.drawLine(0, getHeight()-(int)(getHeight()*(outMin)), getWidth(), getHeight()-(int)(getHeight()*(outMin)));
			g.setColor(new Color(255,0,0,128));
			g.drawLine(0, (int)(getHeight()*(1-outMax)), getWidth(), (int)(getHeight()*(1-outMax)));
		}
	}
	
	public JPanel getOutputControlPanel() {
		return new OutputControllerPanel();
	}
	
	private class OutputControllerPanel extends JPanel {		
		public OutputControllerPanel() {
			//setPreferredSize(new Dimension(80, 200));
		    RangeSlider rangeSlider = new RangeSlider();
		    rangeSlider.setOrientation(RangeSlider.VERTICAL);
		    this.setBackground(Color.BLACK);
		    rangeSlider.setBackground(Color.BLACK);
		    rangeSlider.setFocusable(false);
		    rangeSlider.setMinimum(0);
	        rangeSlider.setMaximum(100);
	        rangeSlider.setValue((int)(outMin*100));
	        rangeSlider.setUpperValue((int)(outMax*100));
	        rangeSlider.setValue((int)(outMin*100)); //actually necessary if default upper < fMin
	        
		    rangeSlider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                RangeSlider slider = (RangeSlider) e.getSource();
	                outMin = slider.getValue()/100.0;
	                outMax = slider.getUpperValue()/100.0;
	            }
	        });
		    
		    this.setLayout(new GridLayout(1,1));
		    this.add(rangeSlider);
		}
	}
	
	public JPanel getFrequencyControllerPanel() {
		return new FrequencyControllerPanel();
	}
	
	private class FrequencyControllerPanel extends JPanel {		
		public FrequencyControllerPanel() {
			//setPreferredSize(new Dimension(80, 200));
		    RangeSlider rangeSlider = new RangeSlider();
		    rangeSlider.setOrientation(RangeSlider.HORIZONTAL);
		    this.setBackground(Color.BLACK);
		    rangeSlider.setBackground(Color.BLACK);
		    rangeSlider.setFocusable(false);
		    rangeSlider.setMinimum(0);
	        rangeSlider.setMaximum(255);
	        rangeSlider.setValue(fMin);
	        rangeSlider.setUpperValue(fMax);
	        rangeSlider.setValue(fMin); //actually necessary if default upper < fMin
	        
		    rangeSlider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                RangeSlider slider = (RangeSlider) e.getSource();
	                fMin = slider.getValue();
	                fMax = slider.getUpperValue();
	            }
	        });
		    
		    this.setLayout(new GridLayout(1,1));
		    this.add(rangeSlider);
		}
	}
}

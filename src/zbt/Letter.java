package zbt;
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

import main.SpectrumConsumer;
import slider.RangeSlider;
import utilities.RollingAverage;
import utilities.Utilities;

public class Letter implements SpectrumConsumer {
	private double outMinSet, outMaxSet;
	private double outMin, outMax;
	private String letter;
	private int fMin, fMax;
	private float hue;
	
	ArrayList<AmpVis> ampVis = new ArrayList<>();
	ArrayList<LetterVis> letterVis = new ArrayList<>();
	
	RollingAverage intensityAvg = new RollingAverage(6000/15);

	RollingAverage colorLongAvg = new RollingAverage(30000/15);
	RollingAverage colorShortAvg = new RollingAverage(4000/15);
	
	RollingAverage totalLongAvg = new RollingAverage(30000/15);
	
	public Letter(double outMinSet, double outMaxSet, int fMin, int fMax, float hue, String letter) {
		this.outMinSet = outMinSet;
		this.outMaxSet = outMaxSet;
		this.fMin = fMin;
		this.fMax = fMax;
		this.hue = hue;
		this.letter = letter;
	}

	@Override
	public void updateSpectrum(double[] spectrumData) {
		totalLongAvg.update(Utilities.fftSum(0, 256, spectrumData));
		
		double amp = Utilities.fftSum(fMin, fMax, spectrumData);
		
		//totalAvg.update(Utilities.fftSum(0, 256, spectrumData)/prescale);
		double prelog = amp;
		amp = Math.log(amp)/Math.log(2.5); //.5 to 2
		amp = Math.max(-1.0, Math.min(10.0, amp)); //cap
		amp = (amp + 1.0);
		amp = prelog;
		//System.out.println(preamp + " " + amp);

		colorLongAvg.update(amp);
		colorShortAvg.update(amp);
		intensityAvg.update(amp);
		
		double min = intensityAvg.getPercentile(0.05);
		outMin = intensityAvg.getPercentile(outMinSet);
		outMin = Math.max(min+.05, outMin);
		outMax = intensityAvg.getPercentile(outMaxSet);
		outMax = Math.max(outMin+.05, outMax);
	
		double out = Math.min(1.0, Math.max(0, (amp-outMin)/(outMax-outMin)));
		out = Math.pow(2, out)-1.0;
		
		double hue = colorShortAvg.getValue()/colorLongAvg.getValue();
		
		hue = Math.log(hue)/Math.log(1.25); //.5 to 2
		hue = Math.max(-1.0, Math.min(1.0, hue)); //cap
		hue = .75 - (.75*(hue*.5+.5));
		
		//hue = Math.max(0, hue-.75)*2.0f;
		//hue *= 2.0;
		//hue = Math.max(0, 0.75 - hue);
		
		this.hue = (float)hue;
		
		//this.hue += 0.001f;
		

		for (AmpVis v : ampVis)
			v.update(amp, out);
		
		for (LetterVis v : letterVis)
			v.update(out);
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
			double percent = 0.5;
			float sat = (float)Math.min(1.0, (1-out)/(1-percent));
			float val = (float)Math.min(1.0, out/(percent));
			
			g.setColor(Utilities.colorFromHSBA(hue, sat, val, 1.0f));
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
			
			double scale = 1/(totalLongAvg.getValue()*1.25);

			Color BG_COLOR = Color.black;
			Color BAR_COLOR = Color.white;

			Graphics2D g = (Graphics2D) graphics;
			g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			g.setColor(BG_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(BAR_COLOR);

			int height = (int)Math.round(getHeight() * amp * scale);
			//g.setColor(new Color(255,255,255,(int)(255*out)));
			//g.fillRect(5, getHeight()-height,getWidth()-10, height);
			g.setColor(BAR_COLOR);
			g.drawRect(5, getHeight()-height,getWidth()-10, height);
			
			
			/*
			g.setColor(new Color(0,0,0,128));
			g.fillRect(0, getHeight()-(int)(getHeight()*(outMin)), getWidth(), (int)(getHeight()*(outMin)));
			g.fillRect(0, 0, getWidth(), (int)(getHeight()*(1-outMax)));
			*/
			g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

			g.setColor(new Color(0,255,0,128));
			int minY = (int)Math.round(getHeight()*outMin * scale);
			int maxY = (int)Math.round((getHeight()*(1.0-outMax * scale)));
			g.drawLine(0, getHeight()-minY, getWidth(), getHeight()-minY);
			g.setColor(new Color(255,0,0,128));
			g.drawLine(0, maxY, getWidth(), maxY);
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
	        rangeSlider.setValue((int)(outMinSet*100));
	        rangeSlider.setUpperValue((int)(outMaxSet*100));
	        rangeSlider.setValue((int)(outMinSet*100)); //actually necessary if default upper < fMin
	        
		    rangeSlider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                RangeSlider slider = (RangeSlider) e.getSource();
	                outMinSet = slider.getValue()/100.0;
	                outMaxSet = slider.getUpperValue()/100.0;
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

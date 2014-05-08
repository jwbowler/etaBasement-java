package zbt;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.SpectrumConsumer;
import slider.RangeSlider;
import utilities.RollingAverage;
import utilities.Utilities;

public class Letter implements SpectrumConsumer {
	LetterParameters p;
	
	public final String letter;
	
	private double outMin, outMax;
	private double amp, out;
	private Color outputColor;
	
	AmpVis ampVis;
	LetterVis letterVis;
	OutputControllerPanel outControl;
	FrequencyControllerPanel freqControl;
	
	int DELAY_MILLIS = 20;
	RollingAverage intensityAvg = new RollingAverage(6000/DELAY_MILLIS);
	RollingAverage colorLongAvg = new RollingAverage(30000/DELAY_MILLIS);
	RollingAverage colorShortAvg = new RollingAverage(4000/DELAY_MILLIS);
	RollingAverage totalLongAvg = new RollingAverage(10000/DELAY_MILLIS);
	
	public Letter(String l, LetterParameters.Presets preset) {
		this.letter = l;
		this.p = LetterParameters.getParameters(preset, l);
		
		this.letterVis = new LetterVis();
		this.ampVis = new AmpVis();
		
		this.outControl = new OutputControllerPanel();
		this.freqControl = new FrequencyControllerPanel();
	}
	
	@Override
	public void updateSpectrum(double[] spectrumData) {
		amp = Utilities.fftSum(p.fMin, p.fMax, spectrumData);
		
		totalLongAvg.update(Utilities.fftSum(0, 256, spectrumData));
		colorLongAvg.update(amp);
		colorShortAvg.update(amp);
		intensityAvg.update(amp);

		double min = intensityAvg.getPercentile(0.05);
		outMin = intensityAvg.getPercentile(p.outMinPercent);
		outMin = Math.max(min*1.05, outMin);
		outMax = intensityAvg.getPercentile(p.outMaxPercent);
		outMax = Math.max(outMin*1.05, outMax);
		
		out = Math.min(1.0, Math.max(0, (amp-outMin)/(outMax-outMin)));
		out = Math.pow(2, out)-1.0;
		
		outputColor = generateColor();

		ampVis.update();
		letterVis.update();
	}
	
	public LetterParameters getParams() {
		return p;
	}
	
	public void setParams(LetterParameters p) {
		this.p = p;
		outControl.updateSlider();
		freqControl.updateSlider();
	}
	
	public Color generateColor() {
		Color c;
		
		switch (p.colorPattern){
		case TREND:
			double trend = colorShortAvg.getValue()/colorLongAvg.getValue();
			trend = Math.log(trend)/Math.log(1.25); //.5 to 2
			trend = Math.max(-1.0, Math.min(1.0, trend)); //cap
			trend = trend *0.5 + .5;
			
			double hue = 0;
			if (p.hueFlip) {
				double range = p.hueStart - p.hueEnd;
				if (p.hueEnd > p.hueStart)
					range = p.hueStart - (p.hueEnd - 1.0);
				hue = p.hueStart - (range*trend);	
			} else {
				double range = p.hueEnd - p.hueStart;
				if (p.hueStart > p.hueEnd)
					range = p.hueEnd - (p.hueStart - 1.0);
				hue = p.hueStart + (range*trend);	
			}

			double percent = 0.5;
			float sat = (float)Math.min(1.0, (1-out)/(1-percent));
			float val = (float)Math.min(1.0, out/(percent));
			
			//System.out.println("TREND: " + hue + " " + sat + " " + val);

			c = Utilities.colorFromHSBA((float)hue, sat, val, 1.0f);
			break;
		case SOLID:
			c = Utilities.colorFromHSBA((float)p.hueStart, (float)p.hueEnd, (float)out, 1.0f);
			break;
		default:
			c = Utilities.colorFromHSBA(1.0f, 0.0f, (float)out, 1.0f);
		}
		
		return c;
	}
	
	public JPanel getLetterVis() {
		return letterVis;
	}

	private class LetterVis extends JPanel {		
		Color BG_COLOR = Color.black;
		
		public LetterVis() {
			setPreferredSize(new Dimension(150, 150));
		}

		public void update() {
			repaint();
		}

		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);

			Graphics2D g = (Graphics2D) graphics;
	    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int size = Math.min(this.getWidth(), this.getHeight());
			g.setFont(new Font("Arial Black", Font.BOLD, (int)Math.round(size*(4.0/3.2))));
			g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

			g.setColor(BG_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());

			
			g.setColor(outputColor);
			g.drawString(letter, (getWidth()-size)/2, getHeight() - (getHeight()-size)/2);
		}
	}

	public JPanel getIntensityVis() {
		return ampVis;
	}

	private class AmpVis extends JPanel {		
		public AmpVis() {
			setPreferredSize(new Dimension(80, 200));
		}

		public void update() {
			repaint();
		}

		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			
			double scale = 1/(totalLongAvg.getValue()*1.33);

			Color BG_COLOR = Color.black;
			Color BAR_COLOR = Color.white;

			Graphics2D g = (Graphics2D) graphics;
			g.setColor(BG_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			// amp bar
			int height = (int)Math.round(getHeight() * amp * scale);
			g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			g.setColor(BAR_COLOR);
			g.drawRect(5, getHeight()-height,getWidth()-10, height);
			

			// percentile bars
			int minY = (int)Math.round(getHeight()*outMin * scale);
			int maxY = (int)Math.round((getHeight()*(1.0-outMax * scale)));

			g.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			g.setColor(new Color(0,255,0,128));
			g.drawLine(0, getHeight()-minY, getWidth(), getHeight()-minY);
			g.setColor(new Color(255,0,0,128));
			g.drawLine(0, maxY, getWidth(), maxY);
		}
	}
	
	public JPanel getOutputControlPanel() {
		return outControl;
	}
	
	private class OutputControllerPanel extends JPanel {
		RangeSlider rangeSlider = new RangeSlider();
		
		public OutputControllerPanel() {
		    this.setBackground(Color.BLACK);

		    rangeSlider.setOrientation(RangeSlider.VERTICAL);
		    rangeSlider.setBackground(Color.BLACK);
		    rangeSlider.setFocusable(false);
		    rangeSlider.setMinimum(0);
	        rangeSlider.setMaximum(100);
	        updateSlider();
	        
		    rangeSlider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                RangeSlider slider = (RangeSlider) e.getSource();
	                p.outMinPercent = slider.getValue()/100.0;
	                p.outMaxPercent = slider.getUpperValue()/100.0;
	            }
	        });
		    
		    this.setLayout(new GridLayout(1,1));
		    this.add(rangeSlider);
		}
		
		public void updateSlider() {
	        rangeSlider.setValue((int)(p.outMinPercent*100));
	        rangeSlider.setUpperValue((int)(p.outMaxPercent*100));
	        rangeSlider.setValue((int)(p.outMinPercent*100)); //actually necessary if default upper < fMin
		}
	}
	
	public JPanel getFrequencyControllerPanel() {
		return freqControl;
	}
	
	private class FrequencyControllerPanel extends JPanel {	
	    RangeSlider rangeSlider = new RangeSlider();

		public FrequencyControllerPanel() {
		    this.setBackground(Color.BLACK);

		    rangeSlider.setOrientation(RangeSlider.HORIZONTAL);
		    rangeSlider.setBackground(Color.BLACK);
		    rangeSlider.setFocusable(false);
		    rangeSlider.setMinimum(0);
	        rangeSlider.setMaximum(255);
	        updateSlider();
	        
		    rangeSlider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                RangeSlider slider = (RangeSlider) e.getSource();
	                p.fMin = slider.getValue();
	                p.fMax = slider.getUpperValue();
	            }
	        });
		    
		    this.setLayout(new GridLayout(1,1));
		    this.add(rangeSlider);
		}
		
		public void updateSlider() {
	        rangeSlider.setValue(p.fMin);
	        rangeSlider.setUpperValue(p.fMax);
	        rangeSlider.setValue(p.fMin); //actually necessary if default upper < fMin
		}
	}
}

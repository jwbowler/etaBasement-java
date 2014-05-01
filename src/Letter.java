import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import slider.RangeSlider;

public class Letter implements SpectrumConsumer {

	private double outMin, outMax, prescale = 1E7;
	private int fMin, fMax;
	
	ArrayList<IntensityVis> visualizations = new ArrayList<>();
	
	public Letter(double outMin, double outMax, int fMin, int fMax) {
		this.outMin = outMin;
		this.outMax = outMax;
		this.fMin = fMin;
		this.fMax = fMax;
	}
	
	public void setPrescale(double prescale) {
		this.prescale = prescale;
	}

	@Override
	public void updateSpectrum(double[] spectrumData) {
		double score = score(spectrumData);
		score /= prescale;
		for (IntensityVis v : visualizations)
			v.update(score);
	}

	public double score(double[] fft) {
		double sum = 0;
		for (int i = fMin; i < fMax; i++)
			sum += fft[i];

		return sum;
	}

	public JPanel getIntensityVis() {
		IntensityVis vis = new IntensityVis();
		visualizations.add(vis);
		return vis;
	}

	private class IntensityVis extends JPanel {
		private double score;
		
		public IntensityVis() {
			setPreferredSize(new Dimension(80, 200));
		}

		public void update(double score) {
			this.score = score;
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

			int amp = (int)Math.round(getHeight() * score);
			int out = (int)(Math.min(255, Math.max(0, (score-outMin)*255/(outMax-outMin))));
			System.out.println(out);
			g.setColor(new Color(255,255,255,out));
			g.fillRect(5, getHeight()-amp,getWidth()-10, amp);
			g.setColor(BAR_COLOR);
			g.drawRect(5, getHeight()-amp,getWidth()-10, amp);
			
			
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

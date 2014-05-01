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

	private double offset, prescale = 1E7;
	private int fMin, fMax;
	
	ArrayList<IntensityVis> visualizations = new ArrayList<>();

	public Letter(double offset, int fMin, int fMax) {
		this.offset = offset;
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
			g.setColor(BG_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(BAR_COLOR);

			for (int i = 0; i < 256; i++) {
				int amp = (int)Math.round(getHeight() * score);
				g.drawRect(5, getHeight()-amp,getWidth()-10, amp);
			}
		}
	}
	
	public JPanel getControllerPanel() {
		return new ControllerPanel();
	}
	
	private class ControllerPanel extends JPanel {		
		public ControllerPanel() {
			//setPreferredSize(new Dimension(80, 200));
			this.setLayout(new GridLayout(0,1));
			
		    RangeSlider rangeSlider = new RangeSlider();
		    rangeSlider.setOrientation(RangeSlider.HORIZONTAL);
		    rangeSlider.setBackground(Color.black);
		    rangeSlider.setFocusable(false);
		    rangeSlider.setPreferredSize(new Dimension(250, rangeSlider.getPreferredSize().height));
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
		    
		    this.add(rangeSlider);
		}
	}
}

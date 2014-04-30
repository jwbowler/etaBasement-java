import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;


public class SpectrumView extends JPanel {
	
	private double[] spectrumData;
	
	private static final Color BG_COLOR = new Color(0, 0, 0, 255);
	private static final Color BAR_COLOR = new Color(255, 255, 255, 255);
	private static final int DEFAULT_SIZE_W = 1024;
	private static final int DEFAULT_SIZE_H = 500;
	
	public SpectrumView() {
		setPreferredSize(new Dimension(DEFAULT_SIZE_W, DEFAULT_SIZE_H));
	}
	
	public void updateSpectrum(double[] spectrumData) {
		this.spectrumData = spectrumData;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
	    super.paintComponent(graphics);

	    Graphics2D g = (Graphics2D) graphics;
	    g.setColor(BG_COLOR);
	    g.fillRect(0, 0, DEFAULT_SIZE_W, DEFAULT_SIZE_H);
	    g.setColor(BAR_COLOR);
	    
	    synchronized (spectrumData) {
		    for (int i = 0; i < 256; i++) {
		    	int val = (int) spectrumData[i] / 2000;
			    g.fillRect(i*4, 500 - val, 4, val);
		    }
	    }
	}
}
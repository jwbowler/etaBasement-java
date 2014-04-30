import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import javax.swing.JPanel;


public class SpectrumView extends JPanel implements SpectrumConsumer{
	
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
	    	int[] xpoints = new int[258];
	    	int[] ypoints = new int[258];
		    for (int i = 0; i < 256; i++) {
		    	int val = (int) spectrumData[i] / 2000;
		    	xpoints[i] = i*4;
		    	ypoints[i] = 500-val;
		    	//g.fillRect(i*4, 500 - val, 4, val);
		    
		    	int val_next = (int) spectrumData[i+1] / 2000;
		    	//g.drawLine(i*4, 500-val, (i+1)*4, 500-val_next);
		    }
		    xpoints[256] = 256*4;
		    xpoints[257] = 0;
		    ypoints[256] = 500;
		    ypoints[257] = 500;
	    	Polygon p = new Polygon(xpoints, ypoints, 258);

	    	g.setColor(new Color(0,140,170));
	    	g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	    	g.fillPolygon(p);
	    	g.setColor(Color.WHITE);
	    	g.drawPolygon(p);
	    }
	}
}
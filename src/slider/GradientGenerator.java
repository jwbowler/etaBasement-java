package slider;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;

import utilities.Utilities;

public interface GradientGenerator {
	public Paint getGradient(float startX, float startY, float endX, float endY);
	
	public class SaturationGradientGenerator implements GradientGenerator{
	    private final Color[] BASE = new Color[7];
	    
	    public SaturationGradientGenerator(float hue) {
	    	for (int i = 0; i< BASE.length; i++) {
	    		BASE[i] = Utilities.colorFromHSBA(hue, 1f - i/6f, 1f, 1f);
	    	}
	    }
	    
	    public LinearGradientPaint getGradient(float startX, float startY, float endX, float endY) {
	    	 float[] pts = { 0f, 1f/6, 2f/6, 3f/6, 4f/6, 5f/6, 1f };  
	         LinearGradientPaint linGrad = new LinearGradientPaint(startX, startY, endX, endY, pts, BASE );
	         return linGrad;
	    }
		
	}
	
	public class HsvGradientGenerator implements GradientGenerator{
	    private static final Color H0 = new Color( 255, 0, 0 );  
	    private static final Color H1 = new Color( 255, 255, 0 );  
	    private static final Color H2 = new Color( 0, 255, 0 );  
	    private static final Color H3 = new Color( 0, 255, 255 );  
	    private static final Color H4 = new Color( 0, 0, 255 );  
	    private static final Color H5 = new Color( 255, 0, 255 );  
	      
	    private static final Color[] BASE = { H0, H1, H2, H3, H4, H5, H0 };  
	    public LinearGradientPaint getGradient(float startX, float startY, float endX, float endY) {
	    	 float[] pts = { 0f, 1f/6, 2f/6, 3f/6, 4f/6, 5f/6, 1f };  
	         LinearGradientPaint linGrad = new LinearGradientPaint(startX, startY, endX, endY, pts, BASE );
	         return linGrad;
	    }
		
	}
}

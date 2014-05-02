package utilities;

import java.awt.Color;

public class Utilities {
	public static double fftSum(double[] fft) {
		return fftSum(0, fft.length, fft);
	}
	
	public static double fftSum(int start, int end, double[] fft) {
		double sum = 0;
		for (int i = start; i<end; i++)
			sum += fft[i];
		return sum;
	}
	
	public static Color colorFromHSBA(float h, float s, float b, float a) {
		int rgb = Color.HSBtoRGB(h, s, b);
		int alpha = (int)Math.round(255*a);
		rgb &= 0x00FFFFFF;
		rgb |= (alpha<<24);
		return new Color(rgb, true);
	}
}

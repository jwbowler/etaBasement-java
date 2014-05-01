package utilities;

public class Utilities {
	public static double fftSum(double[] fft) {
		return fftSum(0, fft.length, fft);
	}
	
	public static double fftSum(int start, int end, double[] fft) {
		double sum = 0;
		for (int i = 0; i<256; i++)
			sum += fft[i];
		return sum;
	}
}

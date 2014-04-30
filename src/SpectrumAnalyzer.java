import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;


public class SpectrumAnalyzer extends Thread {

	public static final int FFT_INPUT_SIZE = 4096; // number of samples that go into the FFT machine
												   // (the number of buckets in the frequency spectrum is half this number)
	
	private static final int MIN_NUM_SAMPLES_IN_UPDATE = 128; // wait until there are at least this many new samples in the
														  // audio input stream before FFTing

	private AudioInputStream stream;
	private DoubleFFT_1D fftMachine = new DoubleFFT_1D(FFT_INPUT_SIZE);
	private SpectrumView view;

	private byte audioBuf8[] = new byte[FFT_INPUT_SIZE * 2]; // raw audio data, viewed as bytes
	private ShortBuffer audioBuf16 = ByteBuffer.wrap(audioBuf8).order(ByteOrder.BIG_ENDIAN).asShortBuffer(); // raw audio data, viewed as 16-bit samples
	private double fftBuf[] = new double[FFT_INPUT_SIZE]; // audio data converted to an array of doubles (FFT processes this in-place)
	
	public SpectrumAnalyzer(AudioInputStream stream, SpectrumView view) {
		this.stream = stream;
		this.view = view;
	}

	@Override
	public void run() {
		
		int count = 0;
		while (true) {
			
			try {
				int bytesRead = 0;
				int available = stream.available();
				
				if (available < FFT_INPUT_SIZE) {
					// rotate the buffer
					int numBytesToRead = Math.max(MIN_NUM_SAMPLES_IN_UPDATE, available);
					int offset = FFT_INPUT_SIZE - numBytesToRead;
					System.arraycopy(audioBuf8, numBytesToRead, audioBuf8, 0, offset);
					bytesRead = stream.read(audioBuf8, offset, numBytesToRead);
					assert(bytesRead == numBytesToRead);
				} else {
					// rewrite the whole buffer with the most recent bytes
					stream.skip(available - FFT_INPUT_SIZE);
					bytesRead = stream.read(audioBuf8, 0, FFT_INPUT_SIZE);
				}
//				System.out.println(available + " : " + bytesRead);
				
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			// Convert from 16-bit int to double
			for (int i = 0; i < FFT_INPUT_SIZE; i++) {
				fftBuf[i] = audioBuf16.get(i);
			}
			
			// Perform FFT (real -> complex)
			fftMachine.realForward(fftBuf);
			
			// Turn the complex output into magnitudes (see the realForward() doc)
			fftBuf[0] = Math.abs(fftBuf[0]);
			for (int i = 1; i < FFT_INPUT_SIZE / 2; i++) {
				fftBuf[i] = Math.sqrt(fftBuf[2*i]*fftBuf[2*i] + fftBuf[2*i + 1]*fftBuf[2*i + 1]);
			}
			
			// Give the new data to the view
			view.updateSpectrum(Arrays.copyOf(fftBuf, FFT_INPUT_SIZE));
			
//			System.out.println(count++);
		}
	}
	
}
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;


public class SpectrumAnalyzer extends Thread {
	
	private static final int NUM_FFT_INPUT_SAMPLES = 4096; // number of samples that go into the FFT machine
	private static final int NUM_FFT_INPUT_BYTES = NUM_FFT_INPUT_SAMPLES * 2;
	private static final int MIN_NUM_SAMPLES_IN_UPDATE = 128; // wait until there are at least this many new samples in the
														  // audio input stream before FFTing

	private AudioInputStream stream;
	private DoubleFFT_1D fftMachine = new DoubleFFT_1D(NUM_FFT_INPUT_SAMPLES);
	private SpectrumView view;

	private byte audioBuf8[] = new byte[NUM_FFT_INPUT_BYTES]; // raw audio data, viewed as bytes
	private ShortBuffer audioBuf16 = ByteBuffer.wrap(audioBuf8).order(ByteOrder.BIG_ENDIAN).asShortBuffer(); // raw audio data, viewed as 16-bit samples
	private double fftBuf[] = new double[NUM_FFT_INPUT_SAMPLES * 2]; // audio data converted to an array of doubles (FFT processes this in-place)
	
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
				
				if (available < NUM_FFT_INPUT_BYTES) {
					// rotate the buffer
					int numBytesToRead = Math.max(MIN_NUM_SAMPLES_IN_UPDATE, available);
					int offset = NUM_FFT_INPUT_BYTES - numBytesToRead;
					System.arraycopy(audioBuf8, numBytesToRead, audioBuf8, 0, offset);
					bytesRead = stream.read(audioBuf8, offset, numBytesToRead);
					assert(bytesRead == numBytesToRead);
				} else {
					// rewrite the whole buffer with the most recent bytes
					stream.skip(available - NUM_FFT_INPUT_BYTES);
					bytesRead = stream.read(audioBuf8, 0, NUM_FFT_INPUT_BYTES);
				}
//				System.out.println(available + " : " + bytesRead);
				
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			// Convert from 16-bit int to double
			for (int i = 0; i < NUM_FFT_INPUT_SAMPLES; i++) {
				fftBuf[i] = audioBuf16.get(i);
			}
			
			// Perform FFT (real -> complex)
			fftMachine.realForward(fftBuf);
			
			// Turn the complex output into magnitudes (see the realForward() doc)
			fftBuf[0] = Math.abs(fftBuf[0]);
			for (int i = 1; i < NUM_FFT_INPUT_SAMPLES; i++) {
				fftBuf[i] = Math.sqrt(fftBuf[2*i]*fftBuf[2*i] + fftBuf[2*i + 1]*fftBuf[2*i + 1]);
			}
			
			// Give the new data to the view
			view.updateSpectrum(Arrays.copyOf(fftBuf, NUM_FFT_INPUT_SAMPLES));
			
//			System.out.println(count++);
		}
	}
	
}
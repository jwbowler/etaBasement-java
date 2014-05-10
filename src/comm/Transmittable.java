package comm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface Transmittable {
	public ByteArrayOutputStream getPacket() throws IOException;
}
package zbt;

public class LetterParameters {
	public enum Presets {TREND_STANDARD, TREND_REDS, TREND_BLUES, WHITE_BASS}
	
	public enum ColorPattern {TREND, SOLID};
		
	public int fMin, fMax;
	public double outMinPercent, outMaxPercent;
	public double hueStart, hueEnd;
	public boolean hueFlip;
	public ColorPattern colorPattern;
	
	public static LetterParameters getParameters(Presets preset, String l) {
		switch (preset) {
		case TREND_STANDARD:
			return trendStandard(l);
		case TREND_REDS:
			return trendReds(l);
		case TREND_BLUES:
			return trendBlues(l);
		case WHITE_BASS:
			return whiteBass(l);
		default:
			return null;
		}
	}
	
	public LetterParameters setFreq(int fMin, int fMax) {
		this.fMin = fMin;
		this.fMax = fMax;
		return this;
	}
	
	public LetterParameters setOut(double outMinPercent, double outMaxpercent) {
		this.outMinPercent = outMinPercent;
		this.outMaxPercent = outMaxpercent;
		return this;
	}
	
	public LetterParameters setColorParams(ColorPattern colorPattern, double hueStart, double hueEnd, boolean hueFlip) {
		this.colorPattern = colorPattern;
		this.hueStart = hueStart;
		this.hueEnd = hueEnd;
		this.hueFlip = hueFlip;
		return this;
	}

	public static LetterParameters trendStandard(String letter) {
		LetterParameters p = new LetterParameters();
		p.setColorParams(ColorPattern.TREND, 0.75, 0.0, true);
		
		switch (letter) {
		case "Z":
			p.setFreq(5, 40).setOut(0.5, 0.99);
			break;
		case "B":
			p.setFreq(35, 110).setOut(0.25, 0.99);
			break;
		case "T":
			p.setFreq(90, 255).setOut(0.25, 0.99);
			break;
		}
		
		return p;
	}
	
	public static LetterParameters trendReds(String letter) {
		return trendStandard(letter).setColorParams(ColorPattern.TREND, 0.16, 0.0, true);
	}
	
	public static LetterParameters trendBlues(String letter) {
		return trendStandard(letter).setColorParams(ColorPattern.TREND, 0.45, 0.72, false);
	}
	
	public static LetterParameters whiteBass(String letter) {
		LetterParameters p = new LetterParameters();
		p.setColorParams(ColorPattern.SOLID, 0.75, 0.0, true);
		p.setFreq(5, 40);
		p.setOut(0.5, 0.99);
		return p;
	}
}

package gem.ui.board_panel.board_image;

public class RGBA {
	public final int red;
	public final int green;
	public final int blue;
	public final float normalizedAlpha;
	public RGBA(int r, int g, int b, float normalizedAlpha) {
		if(r < 0 || r > 255
			|| g < 0 || g > 255
			|| b < 0 || b > 255
			|| normalizedAlpha < 0 || normalizedAlpha > 1.0f) {
			throw new IllegalArgumentException("Red, green, or blue value was out of range.");
		} else {
			red  = r;
			green = g;
			blue = b;
			this.normalizedAlpha = normalizedAlpha;
		}
	}
}
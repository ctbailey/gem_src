package gem.ui.board_image;

import java.awt.Image;

public interface IBoardImageFilter {
	Image processPixels(Image previousImage, float normalizedOpacity);
}

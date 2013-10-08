package gem.ui.board_panel.board_image;

import gem.ui.IMapImageLoadedListener;
import gem.ui.UserDidNotConfirmException;
import gem.ui.board_panel.board_image.ImageRenderer.ImageDisplaySettings;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class ChainBoardImageSource extends AbstractBoardImageSource implements IImageUpdatedListener {
	private float preferredOpacity;
	private static final float DEFAULT_PREFERRED_OPACITY = 1.0f;
	
	private UserInterfaceManager uiManager;
	
	private List<IStateRenderer> stateRenderers;
	private List<IBoardImageFilter> imageFilters;
	
	public ChainBoardImageSource() {
		this(DEFAULT_PREFERRED_OPACITY);
	}
	public ChainBoardImageSource(float normalizedPreferredOpacity) {
		this(normalizedPreferredOpacity, new ArrayList<IStateRenderer>(), new ArrayList<IBoardImageFilter>());
	}
	public ChainBoardImageSource(float normalizedPreferredOpacity, List<IStateRenderer> stateRenderers, List<IBoardImageFilter> imageFilters) {
		if(normalizedPreferredOpacity < 0 || normalizedPreferredOpacity > 1) {
			throw new IllegalArgumentException("Normalized opacity not between 0 and 1.");
		}
		if(stateRenderers == null) {
			throw new IllegalArgumentException("ChainBoardImageSource doesn't accept null state renderer lists.");
		}
		if(imageFilters == null) {
			throw new IllegalArgumentException("ChainBoardImageSource doesn't accept null image filter lists.");
		}
		preferredOpacity = normalizedPreferredOpacity;
		this.stateRenderers = stateRenderers;
		this.imageFilters = imageFilters;
		uiManager = new UserInterfaceManager();
	}
	
	public void requestImageUpdate() {
		// An idea for an optimization to prevent
		// multiple image updates based on a change
		// to the same event source. E.g., if two
		// listeners are listening to the board state changes.
		
		//if(!calculatingImage
		//	|| currentCompositingIndex > indexOfRequester) {
		setBoardImage(calculateImage());
	}
	@Override
	public void refreshBoardImage() {
		for(IStateRenderer renderer : stateRenderers) {
			renderer.refreshImage();
		}
	}
	
	@Override
	protected Image calculateImage() {
		Image compositeImage = runStateRenderers(preferredOpacity);
		return runImageFilters(compositeImage, preferredOpacity);
	}
	private Image runStateRenderers(float preferredOpacity) {
		Image[] images = new Image[stateRenderers.size()];
		for(int i = 0; i < stateRenderers.size(); i++) {
			images[i] = stateRenderers.get(i).getLatestImage();
		}
		return combineImages(images);
	}
	private Image combineImages(Image[] images) {
		// TODO: Throw an exception if they're not all the same size?
		Image composite = new BufferedImage(images[0].getWidth(null), images[0].getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = composite.getGraphics();
		for(Image i : images) {
			g.drawImage(i, 0, 0, null);
		}
		return composite;
	}
	private Image runImageFilters(Image compositeOfStateRenderers, float preferredOpacity) {
		Image outputOfLast = compositeOfStateRenderers;
		for(IBoardImageFilter filter : imageFilters) {
			outputOfLast = filter.processPixels(outputOfLast, preferredOpacity);
		}
		return outputOfLast;
	}

	public void setPreferredOpacity(float newPreferredOpacity) {
		preferredOpacity = newPreferredOpacity;
	}
	public float getPreferredOpacity() {
		return preferredOpacity;
	}
	
	public boolean addBoardStateRenderer(IStateRenderer generator) {
		boolean addPixelGenerator = !stateRenderers.contains(generator);
		if(addPixelGenerator) {
			addBoardStateRenderer(stateRenderers.size(), generator);
		}
		return addPixelGenerator;
	}
	public boolean addWhileRemovingSpuriousBoardStateRenderers(IStateRenderer newRenderer) {
		List<IStateRenderer> spuriousRenderers = new ArrayList<IStateRenderer>(stateRenderers.size());
		for(IStateRenderer existingRenderer : stateRenderers) {
			if(newRenderer.makesSpurious(existingRenderer)) {
				spuriousRenderers.add(existingRenderer);
				existingRenderer.wasMadeSpurious(newRenderer);
			}
		}
		for(IStateRenderer spurious : spuriousRenderers) {
			stateRenderers.remove(spurious);
		}
		
		return addBoardStateRenderer(newRenderer);
	}
	public boolean addBoardStateRenderer(int index, IStateRenderer generator) {
		boolean addPixelGenerator = !stateRenderers.contains(generator);
		if(addPixelGenerator) {
			stateRenderers.add(index, generator);
			generator.addImageUpdatedListener(this);
		}
		return addPixelGenerator;
	}
	public boolean removeBoardStateRenderer(IStateRenderer generator) {
		generator.wasMadeSpurious(null);
		generator.removeImageUpdatedListener(this);
		return stateRenderers.remove(generator);
	}
	public IStateRenderer removeBoardStateRenderer(int index) {
		IStateRenderer r = stateRenderers.remove(index);
		if(r != null) { r.removeImageUpdatedListener(this); }
		return r;
	}
	public IStateRenderer getBoardStateRenderer(int index) {
		return stateRenderers.get(index);
	}
	public int indexOf(IStateRenderer generator) {
		return stateRenderers.indexOf(generator);
	}
	public List<IStateRenderer> getAllBoardStateRenderers() {
		List<IStateRenderer> copy = new ArrayList<IStateRenderer>(stateRenderers.size());
		for(IStateRenderer generator : stateRenderers) {
			copy.add(generator);
		}
		return copy;
	}
	
	public boolean addBoardImageFilter(IBoardImageFilter filter) {
		boolean addPixelFilter = !stateRenderers.contains(filter);
		if(addPixelFilter) {
			imageFilters.add(filter);
		}
		return addPixelFilter;
	}
	public boolean addBoardImageFilter(int index, IBoardImageFilter filter) {
		boolean addPixelFilter = !stateRenderers.contains(filter);
		if(addPixelFilter) {
			imageFilters.add(index, filter);
		}
		return addPixelFilter;
	}
	public boolean removeBoardImageFilter(IBoardImageFilter filter) {
		return imageFilters.remove(filter);
	}
	public IBoardImageFilter removeBoardImageFilter(int index) {
		return imageFilters.remove(index);
	}
	public IBoardImageFilter getBoardImageFilter(int index) {
		return imageFilters.get(index);
	}
	public int indexOf(IBoardImageFilter filter) {
		return imageFilters.indexOf(filter);
	}
	public List<IBoardImageFilter> getAllBoardImageFilters() {
		List<IBoardImageFilter> copy = new ArrayList<IBoardImageFilter>(imageFilters.size());
		for(IBoardImageFilter filter : imageFilters) {
			copy.add(filter);
		}
		return copy;
	}
	
	public static ChainBoardImageSource getDefault() {
		ChainBoardImageSource defaultImageSource = new ChainBoardImageSource();
		defaultImageSource.addBoardStateRenderer(new BinaryStateRenderer(1.0f));
		return defaultImageSource;
	}
	
	// User interface
	private class UserInterfaceManager {
		private List<JMenuItem> menuItems;
		
		public List<JMenuItem> getMenuItems() {
			menuItems = new ArrayList<JMenuItem>(10);
			
			JCheckBoxMenuItem linkAgeAndOpacityMenuItem = new JCheckBoxMenuItem("Link age and opacity");
				linkAgeAndOpacityMenuItem.addActionListener(new LinkAgeAndOpacityListener(linkAgeAndOpacityMenuItem));
				linkAgeAndOpacityMenuItem.setSelected(false);
				menuItems.add(linkAgeAndOpacityMenuItem);

			JMenu mapDisplayOptionsMenu = new JMenu("Map display options:");
			
				ButtonGroup mapDisplayOptionsRBGroup = new ButtonGroup();
				JCheckBoxMenuItem showMapMenuItem = new JCheckBoxMenuItem("Show Map"); // Instantiated here so it can be passed to mapListener in the constructor
				MapUIListener mapListener = new MapUIListener(showMapMenuItem);
				
				JRadioButton stretchMapToFitBoardRB = new JRadioButton("Stretch map to fit board (may distort map)");
				stretchMapToFitBoardRB.addActionListener(mapListener.stretchMapListener);
				mapDisplayOptionsRBGroup.add(stretchMapToFitBoardRB);
				stretchMapToFitBoardRB.setSelected(true); // this radio button is selected by default
				
				JRadioButton scaleMapToFitBoardRB = new JRadioButton("Scale map to fill board (certain parts of the map may not show)");
				scaleMapToFitBoardRB.addActionListener(mapListener.scaleMapListener);
				mapDisplayOptionsRBGroup.add(scaleMapToFitBoardRB);
				
				mapDisplayOptionsMenu.add(stretchMapToFitBoardRB);
				mapDisplayOptionsMenu.add(scaleMapToFitBoardRB);
				
				menuItems.add(mapDisplayOptionsMenu);
			
			JMenuItem loadMapItem = new JMenuItem("Load Map");
				loadMapItem.addActionListener(mapListener.loadMapListener);
				menuItems.add(loadMapItem);
			
			// showMapMenuItem
				showMapMenuItem.addItemListener(mapListener.showMapListener);
				showMapMenuItem.setSelected(false);
				menuItems.add(showMapMenuItem);
				
			JCheckBoxMenuItem highlightIncomingInfluenceMenuItem = new JCheckBoxMenuItem("Highlight incoming influence");
				highlightIncomingInfluenceMenuItem.addItemListener(new HighlightIncomingInfluenceListener());
				menuItems.add(highlightIncomingInfluenceMenuItem);
			
				JCheckBoxMenuItem highlightOutgoingInfluenceMenuItem = new JCheckBoxMenuItem("Highlight outgoing influence");
				highlightOutgoingInfluenceMenuItem.addItemListener(new HighlightOutgoingInfluenceListener());
				menuItems.add(highlightOutgoingInfluenceMenuItem);
			
			return menuItems;
		}

		private class HighlightIncomingInfluenceListener implements ItemListener {
			private IncomingInfluenceCellHighlighter highlighter;
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					highlighter = new IncomingInfluenceCellHighlighter(); // Defaults to highlighting them as a light blue
					addBoardStateRenderer(highlighter);
				} else {
					removeBoardStateRenderer(highlighter);
				}
			}
		}
		private class HighlightOutgoingInfluenceListener implements ItemListener {
			private OutgoingInfluenceCellHighlighter highlighter;
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					highlighter = new OutgoingInfluenceCellHighlighter(); // Defaults to highlighting them as a light blue
					addBoardStateRenderer(highlighter);
				} else {
					removeBoardStateRenderer(highlighter);
				}
			}
		}
	}
	public List<JMenuItem> getMenuItems() {
		return uiManager.getMenuItems();
	}
	
	// UI listeners
	private class LinkAgeAndOpacityListener implements ActionListener {
		private final JCheckBoxMenuItem menuItem;
		public LinkAgeAndOpacityListener(JCheckBoxMenuItem menuItem) {
			this.menuItem = menuItem;
		}
		public void actionPerformed(ActionEvent ev) {
			JCheckBoxMenuItem source = (JCheckBoxMenuItem)ev.getSource();
			setLinkAgeToAlpha(source.isSelected());
		}
		private void setLinkAgeToAlpha(boolean linkAgeAndAlpha) {
			if(linkAgeAndAlpha) {
				try {
					LinkAgeToOpacityStateRenderer newImageSource = LinkAgeToOpacityStateRenderer.createInstanceFromUserInput();
					addWhileRemovingSpuriousBoardStateRenderers(newImageSource);
				} catch(UserDidNotConfirmException ex) {
					menuItem.setSelected(false);
				}
			} else {
				addWhileRemovingSpuriousBoardStateRenderers(new BinaryStateRenderer(1.0f));
			}
		}
	}
	private class MapUIListener {
		private static final float PREFERRED_OPACITY_WHEN_SHOW_MAP = 0.8f;
		private static final float PREFERRED_OPACITY_WHEN_NOT_SHOW_MAP = 1.0f;
		private StretchMapToFitBoardListener stretchMapListener;
		private ScaleMapToFillBoardListener scaleMapListener;
		private LoadMapListener loadMapListener;
		private JCheckBoxMenuItem showMapCheckBox;
			private ShowMapListener showMapListener;
		private ImageRenderer mapRenderer;
		private BufferedImage map;
		
		public MapUIListener(JCheckBoxMenuItem showMapCheckBox) {
			stretchMapListener = new StretchMapToFitBoardListener();
			scaleMapListener = new ScaleMapToFillBoardListener();
			loadMapListener = new LoadMapListener();
			mapRenderer = new ImageRenderer(null, 1.0f);
			this.showMapCheckBox = showMapCheckBox;
			showMapListener = new ShowMapListener();
		}
		private class StretchMapToFitBoardListener implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				mapRenderer.setStretchOrScale(ImageDisplaySettings.STRETCH_TO_FIT_BOARD);
			}
		}
		private class ScaleMapToFillBoardListener implements ActionListener {
			public void actionPerformed(ActionEvent ev) {
				mapRenderer.setStretchOrScale(ImageDisplaySettings.SCALE_TO_FILL_BOARD);
			}
		}
		private class LoadMapListener implements ActionListener, IMapImageLoadedListener {
			public void actionPerformed(ActionEvent ev) {
				gem.ui.MapWizard wizard = new gem.ui.MapWizard();
				wizard.addMapDisplayListener(this);
			}
			@Override
			public void mapImageLoaded(BufferedImage map) {
				mapRenderer.setImage(map);
				showMapCheckBox.setSelected(true);
			}
		}
		private class ShowMapListener implements ItemListener {
			public void itemStateChanged(ItemEvent e) {
				setShowMap(e.getStateChange() == ItemEvent.SELECTED);
			}
			public void setShowMap(boolean showMap) {
				if(showMap) {
					mapRenderer.setImage(map);
					setPreferredOpacity(PREFERRED_OPACITY_WHEN_SHOW_MAP);
				} else {
					mapRenderer.setImage(null);
					setPreferredOpacity(PREFERRED_OPACITY_WHEN_NOT_SHOW_MAP);
				}
			}
		}
		
	}
	
}

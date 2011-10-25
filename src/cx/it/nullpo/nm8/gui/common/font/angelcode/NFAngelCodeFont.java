package cx.it.nullpo.nm8.gui.common.font.angelcode;

import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cx.it.nullpo.nm8.gui.framework.NFFont;
import cx.it.nullpo.nm8.gui.framework.NFGraphics;
import cx.it.nullpo.nm8.gui.framework.NFImage;
import cx.it.nullpo.nm8.gui.framework.NFSystem;
import cx.it.nullpo.nm8.util.NUtil;

/**
 * NFFont implementation of AngelCodeFont fnt file.
 */
public class NFAngelCodeFont implements NFFont {
	private static final long serialVersionUID = 1130076147839926948L;

	/** Log */
	private Log log = LogFactory.getLog(NFAngelCodeFont.class);

	/** NFSystem */
	protected NFSystem sys;

	/** fnt file info */
	protected AngelCodeMainInfo info;

	/** Font Images (Currently only the first one will be used) */
	protected NFImage[] images;

	/** Character image cache */
	protected Map<Integer, NFImage> mapImageCharCache = new HashMap<Integer, NFImage>();

	/**
	 * Constructor
	 */
	public NFAngelCodeFont() {
	}

	/**
	 * Create a font by using a premade AngelCodeMainInfo.
	 * @param sys NFSystem
	 * @param info AngelCodeMainInfo
	 * @throws IOException When the font image load fails
	 */
	public NFAngelCodeFont(NFSystem sys, AngelCodeMainInfo info) throws IOException {
		init(sys, info);
	}

	/**
	 * Create a font by using the fnt file path.
	 * @param sys NFSystem
	 * @param fntFilename Filename of fnt file
	 * @throws IOException When the fnt file or font image load fails
	 */
	public NFAngelCodeFont(NFSystem sys, String fntFilename) throws IOException {
		URL url = NUtil.getURL(fntFilename);
		List<String> list = NUtil.getStringListFromURLE(url);
		AngelCodeMainInfo info = new AngelCodeMainInfo(list);
		init(sys, info);
	}

	/**
	 * Create a font by using the fnt file URL.
	 * @param sys NFSystem
	 * @param url URL of fnt file
	 * @throws IOException When the fnt file or font image load fails
	 */
	public NFAngelCodeFont(NFSystem sys, URL url) throws IOException {
		List<String> list = NUtil.getStringListFromURLE(url);
		AngelCodeMainInfo info = new AngelCodeMainInfo(list);
		init(sys, info);
	}

	/**
	 * Init this font.
	 * @param sys NFSystem
	 * @param info AngelCodeMainInfo
	 * @throws IOException When the font image load fails
	 */
	protected void init(NFSystem sys, AngelCodeMainInfo info) throws IOException {
		this.sys = sys;
		this.info = info;

		int pages = info.getPages();
		if(pages == 0) {
			throw new IllegalArgumentException("This fnt file does not contain any pages.");
		} else if(pages >= 2) {
			log.warn("This fnt file contains " + info.getPages() + " pages, but we can only use the first page for now.");
		}

		images = new NFImage[pages];

		// Load the images
		for(int i = 0; i < pages; i++) {
			String filename = info.getPageInfoList().get(i).getFileName();
			log.trace("Loading a font image file from '" + filename + "'");

			try {
				images[i] = sys.loadImage(NUtil.getURL(filename));
			} catch (IOException e) {
				throw new IOException("Can't load font image file '" + filename + "' for page #" + i, e);
			}

			log.trace("Successfully loaded a font image file from '" + filename + "'");
		}
	}

	public Font getFont() {
		return null;
	}

	public int getSize() {
		return info.getSize();
	}

	public boolean isBold() {
		return info.isBold();
	}

	public boolean isItalic() {
		return info.isItalic();
	}

	public boolean isGlyphLoadingRequired() {
		return false;
	}

	public void addGlyphs(String text) {
	}

	public boolean loadGlyphs() {
		return true;
	}

	public int getStringWidth(String str) {
		int advance = 0;
		Map<Integer, AngelCodeCharInfo> map = info.getPageInfoList().get(0).getCharInfoMap();
		for(int i = 0; i < str.length(); i++) {
			int c = (int)str.charAt(i);
			AngelCodeCharInfo ci = map.get(c);
			if(ci != null) {
				advance += ci.getXadvance();
			}
		}
		return advance;
	}

	public int getStringHeight(String str) {
		int height = 0;
		Map<Integer, AngelCodeCharInfo> map = info.getPageInfoList().get(0).getCharInfoMap();
		for(int i = 0; i < str.length(); i++) {
			int c = (int)str.charAt(i);
			AngelCodeCharInfo ci = map.get(c);
			if(ci != null) {
				if(ci.getHeight() > height) height = ci.getHeight();
			}
		}
		return height;
	}

	public int getLineHeight() {
		return info.getLineHeight();
	}

	public void drawString(NFGraphics g, String str, int x, int y) {
		int x2 = x;

		Map<Integer, AngelCodeCharInfo> map = info.getPageInfoList().get(0).getCharInfoMap();
		for(int i = 0; i < str.length(); i++) {
			int c = (int)str.charAt(i);
			AngelCodeCharInfo ci = map.get(c);
			if(ci != null) {
				if(ci.getWidth() > 0 && ci.getHeight() > 0) {
					int dx = x2 + ci.getXoffset();
					int dy = y + ci.getYoffset();

					NFImage subImage = mapImageCharCache.get(ci.getId());
					if(subImage == null) {
						subImage = images[0].getSubImage(ci.getX(), ci.getY(), ci.getWidth(), ci.getHeight());
						mapImageCharCache.put(ci.getId(), subImage);
					}

					g.drawImage(
						subImage,
						dx, dy, dx+ci.getWidth(), dy+ci.getHeight(),
						0, 0, ci.getWidth(), ci.getHeight()
					);
				}

				x2 += ci.getXadvance();
			}
		}
	}
}

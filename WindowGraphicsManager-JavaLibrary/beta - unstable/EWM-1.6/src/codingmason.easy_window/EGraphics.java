package codingmason.easy_window;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;

public class EGraphics {
	private Color color = Color.black, background = Color.gray;
	private Font font = new Font("", 0, 20);
	private BufferedImage img;
	private EWindow canvas;
	private Graphics g;
	private boolean render = true;
	private int width, height;
	public int[] pixels;

	public EGraphics(EWindow canvas, int width, int height) {
		this.canvas = canvas;
		setSize(width, height);
	}
	
	// CIRCLE
	@Deprecated
	public void drawCircle(int x, int y, int r) {
		
	}
	public void fillCirlce(int x, int y, int r) {
		fillOval(x-r, y-r, r*2, r*2);
	}
	@Deprecated
	public void drawOval(int x, int y, int width, int height) {
	}
	public void fillOval(int x, int y, int width, int height) {
		Ellipse2D ell = new Ellipse2D.Double();
		ell.setFrame(x, y, width, height);
		for(int xx = 0; xx < width; xx++) {
			for(int yy = 0; yy < height; yy++) {
				if(ell.contains(xx+x, yy+y)) drawPixel(xx+x, yy+y);
			}
		}
	}
	
	// POLYGON
	public void drawPolygon(Polygon p) {
		if(p == null) return;
		if(p.npoints < 1) return;
		for(int i = 0; i < p.npoints-1; i++)
			drawLine(p.xpoints[i], p.ypoints[i], p.xpoints[i+1], p.ypoints[i+1]);
		drawLine(p.xpoints[p.npoints-1], p.ypoints[p.npoints-1], p.xpoints[0], p.ypoints[0]);
	}
	public void fillPolygon(Polygon p) {
		if(p == null) return;
		if(p.npoints < 1) return;
		int max = Integer.MAX_VALUE;
		int xl = max, xm = -max, yl = max, ym = -max;
		for(int i = 0; i < p.npoints; i++) {
			int x = p.xpoints[i];
			int y = p.ypoints[i];
			if(x < xl) xl = x;
			if(x > xm) xm = x;
			if(y < yl) yl = y;
			if(y > ym) ym = y;
		}
		if(xl == max || xm == -max || yl == max || ym == -max)
			return;
		for(int x = xl; x < xm+1; x++) {
			for(int y = yl; y < ym+1; y++) {
				if(p.contains(x, y))
					drawPixel(x, y);
			}
		}
	}
	
	// RECT
	public void drawRect(int x, int y, int width, int height) {
		drawLine(x, y, x+width, y);
		drawLine(x+width, y, x+width, y+height);
		drawLine(x+width, y+height, x, y+height);
		drawLine(x, y+height, x, y);
	}
	public void fillRect(int x, int y, int width, int height) {
		for(int xp = 0; xp < width; xp++) {
			for(int yp = 0; yp < height; yp++) {
				drawPixel(x+xp, y+yp);
			}
		}
	}
	
	// PIXELS
	public void drawPixel(int x, int y) {
		if(!inBounds(this, x, y))
			return;
		pixels[x+y*width] = color.getRGB();
	}
	public void drawLine(int x0, int y0, int x1, int y1) {
		int dx = Math.abs(x1-x0);
		int dy = Math.abs(y1-y0);
		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;
		int err = dx-dy, e2;
		while(true) {
			drawPixel(x0, y0);
			if(x0 == x1 & y0 == y1)
				break;
			e2 = 2*err;
			if(e2 > -dy) {
				err -= dy;
				x0 += sx;
			}
			if(e2 < dx) {
				err += dx;
				y0 += sy;
			}
		}
	}
	public void drawString(String str, int x, int y, boolean fill) {
		FontRenderContext frc = getGraphics().getFontMetrics().getFontRenderContext();
		GlyphVector vector = font.createGlyphVector(frc, str);
		PathIterator iterator = vector.getOutline().getPathIterator(frc.getTransform());
		float[] floats = new float[6];
		ArrayList<Polygon> polys = new ArrayList<>();
	    Polygon polygon = new Polygon();
	    while (!iterator.isDone()) {
	        int type = iterator.currentSegment(floats);
	        int xx = (int) floats[0];
	        int yy = (int) floats[1];
	        if(type != PathIterator.SEG_CLOSE) {
	        	if(type == PathIterator.SEG_MOVETO) {
	        		polys.add(polygon);
	        		polygon = new Polygon();
	        	}
	        	polygon.addPoint(xx+x, yy+y);
	        }
	        iterator.next();
	    }
	    polys.add(polygon);
	    for(Polygon p : polys) {
	    	if(fill) fillPolygon(p);
	    	else drawPolygon(p);
	    }
	}
	public void render() {
		if(!render) {
			render = true;
			return;
		}
		g.drawImage(img, 0, 0, null);
	}
	public void clear() {
		Arrays.fill(pixels, background.getRGB());
	}
	public void drawImage(BufferedImage img, int x, int y) {
		drawImage(img, x, y, 1, 1);
	}
	public void drawImage(BufferedImage img, int x, int y, double scaleX, double scaleY) {
		int w = img.getWidth(), h = img.getHeight();
		int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		for(int xx = 0; xx < w; xx++) {
			for(int yy = 0; yy < h; yy++) {
				setColor(new Color(pixels[xx+yy*w]));
				fillRect((int)(x+xx*scaleX), (int)(y+yy*scaleY), (int)scaleX, (int)scaleY);
			}
		}
	}
	
	// GETTERS
	public int getPixel(int x, int y) {
		if(!inBounds(this, x, y))
			return -1;
		return pixels[x+y*width];
	}
	public Color getColor() {
		return color;
	}
	public Font getFont() {
		return font;
	}
	public Color getBackground() {
		return background;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public EWindow getCanvas() {
		return canvas;
	}
	public Graphics getGraphics() {
		return g;
	}
	
	// SETTERS
	public void setColor(Color color) {
		this.color = color;
	}
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		img = new BufferedImage(width, height, 1);
	    pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
	    clear();
	}
	public void setFont(Font font) {
		this.font = font;
	}
	public void loadSettings(Graphics g) {
		color = g.getColor();
		font = g.getFont();
	}
	public void setBackground(Color color) {
		background = color;
	}
	public void setGraphics(Graphics g) {
		this.g = g;
	}
	public void dontRender(boolean renderNow) {
		if(renderNow) render();
		render = false;
	}
	
	// STATIC
	private static boolean inBounds(EGraphics g, int x, int y) {
		return (x >= 0 & x < g.width & y >= 0 & y < g.height);
	}
}

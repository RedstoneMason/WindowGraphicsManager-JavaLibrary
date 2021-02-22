package window.code_mason;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JFrame;

public class Window {
	private JFrame frame = new JFrame();
	private String renderMethodName = "render", tickMethodName = "tick";
	private Method renderMethod = null, tickMethod = null;
	private Canvas canvas;
	private boolean renderError, tickError, canvasError, renderBackground=true;
	
	private long lastTime = System.nanoTime();
	private long timer = System.currentTimeMillis();
	private int tps = 20;
	private double delta = 0;
	private int frames = 0;
	
	private int width, height, x, y;
	
	private Color background = Color.black;
	
	private int fps, tps_;
	private double mspt, mspf;
	
	// CONSTRUCTOR
	public Window(Component c, int x, int y, int width, int height, String title) {
		try {
			canvas = (Canvas)c;
		} catch(ClassCastException e) {
			e.printStackTrace();
			canvasError = true;
			return;
		}
		this.width = width;
		this.height = height;
		frame.add(c);
		frame.setLocation(x, y);
		frame.setSize(width, height);
		frame.setTitle(title);
		frame.setDefaultCloseOperation(3);
		frame.setVisible(true);
		Toolkit.getDefaultToolkit().setDynamicLayout(false);
	}
	
	// PUBLIC
	public void tick() {
		if(tickError || canvasError)
			return;
		try {
			if(tickMethod == null)
				tickMethod = canvas.getClass().getMethod(tickMethodName);
			tickMath();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			tickError = true;
		}
	}
	public void render() {
		if(renderError || canvasError)
			return;
		try {
			if(renderMethod == null)
				renderMethod = canvas.getClass().getMethod(renderMethodName, Graphics.class);
			renderMath();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			renderError = true;
		}
	}
	public void maximize() {
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	
	// PRIVATE
	private void tickMath() {
		long msStart = System.currentTimeMillis();
		try {
			double ns = 1000000000 / tps;
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta > 0) {
				tickMethod.invoke(canvas);
				delta--;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			tickError = true;
		} finally {
			mspt = System.currentTimeMillis() - msStart;
		}
	}
	private void renderMath() {
		long msStart = System.currentTimeMillis();
		x = frame.getX();
		y = frame.getY();
		width = frame.getWidth();
		height = frame.getHeight();
		try {
			BufferStrategy bs = canvas.getBufferStrategy();
			if(bs == null) {
				canvas.createBufferStrategy(3);
				return;
			}
			Graphics g = bs.getDrawGraphics();
			if(renderBackground) {
				g.setColor(background);
				g.fillRect(0, 0, width, height);
			}
			renderMethod.invoke(canvas, g);
			g.dispose();
			bs.show();
			frames++;
			if (System.currentTimeMillis() - timer > 100) {
				timer += 1000;
				fps = frames;
				frames = 0;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			renderError = true;
		} finally {
			mspf = System.currentTimeMillis() - msStart;
		}
	}
	
	// SETTERS
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
		frame.setLocation(x, y);
	}
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		frame.setSize(width, height);
	}
	public void setTitle(String title) {
		frame.setTitle(title);
	}
	public void setRenderMethod(String path) {
		this.renderMethodName = path;
	}
	public void setTickMethod(String path) {
		this.tickMethodName = path;
	}
	public void setBackground(Color color) {
		this.background = color;
	}
	public void setGoalTPS(int tps) {
		this.tps = tps;
	}
	public void setWidth(int width) {
		this.width = width;
		this.frame.setSize(width, height);
	}
	public void setHeight(int height) {
		this.height = height;
		this.frame.setSize(width, height);
	}
	public void setX(int x) {
		this.x = x;
		this.frame.setLocation(x, y);
	}
	public void setY(int y) {
		this.y = y;
		this.frame.setLocation(x, y);
	}
	public void setRenderBackground(boolean renderBackground) {
		this.renderBackground = renderBackground;
	}

	// GETTERS
	public double getMSPT() {
		return this.mspt;
	}
	public double getMSPF() {
		return this.mspf;
	}
	public int getFPS() {
		return this.fps;
	}
	public int getTPS() {
		return this.tps_;
	}
	public int getWidth() {
		return this.width;
	}
	public int getHeight() {
		return this.height;
	}
	public int getX() {
		return this.x;
	}
	public int getY() {
		return this.y;
	}
}

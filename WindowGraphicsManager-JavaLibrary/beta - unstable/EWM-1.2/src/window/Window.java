package window;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Window implements KeyListener, Runnable {
	private JFrame frame = new JFrame();
	private String renderMethodName = "render", tickMethodName = "tick", savePath = System.getProperty("user.home")+"/Desktop/CodeMason/window-1.txt";
	private Method renderMethod = null, tickMethod = null;
	private Canvas canvas;
	private boolean renderBackground=true, reqTFullScreen, running, autoSave=true, load=true;
	private Color background = Color.black;
	private ArrayList<Integer> errors = new ArrayList<Integer>();
	private int fps, tps = 20, width, height, x, y, fullScreen, saveTimer;
	private double mspt, mspf;
	
	// CONSTRUCTOR
	public Window(Component c) {
		try {
			canvas = (Canvas)c;
		} catch(ClassCastException e) {
			e.printStackTrace();
			errors.add(1);
			return;
		}
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		x = ss.width/2-500;
		y = ss.height/2-375;
		width = 1000;
		height = 750;
		canvas.addKeyListener(this);
		frame.add(c);
		frame.setSize(width, height);
		frame.setTitle("Window - 1.2");
		frame.setLocation(x, y);
		frame.setDefaultCloseOperation(3);
		frame.setVisible(true);
		Toolkit.getDefaultToolkit().setDynamicLayout(false);
		canvas.requestFocus();
	}
	
	// PUBLIC
	public void start() {
		if(running)
			return;
		if(load)
			load();
		Thread r = new Thread(this);
		r.start();
	}
	public void maximize() {
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	public void load() {
		File save = new File(savePath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(save));
			int x = Integer.parseInt(br.readLine());
			int y = Integer.parseInt(br.readLine());
			int w = Integer.parseInt(br.readLine());
			int h = Integer.parseInt(br.readLine());
			int ext = Integer.parseInt(br.readLine());
			int state = Integer.parseInt(br.readLine());
			int fs = Integer.parseInt(br.readLine());
			frame.setLocation(x, y);
			frame.setSize(w, h);
			frame.setExtendedState(ext);
			frame.setState(state);
			width = w;
			height = h;
			this.x = x;
			this.y = y;
			if(fs == 1)
				setFullScreen(true);
			this.fullScreen = fs;
			br.close();
		} catch (IOException | NullPointerException | NumberFormatException e) {
			e.printStackTrace();
		}
	}
	public void run() {
		if(running)
			return;
		running = true;
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		double delta = 0;
		int frames = 0;
		while(running) {
			double ns = 1000000000 / tps;	
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta > 0) {
				tick();
				delta--;
			}
			render();
			frames++;
			if(System.currentTimeMillis() - timer > 1000) {
				timer+=1000;
				fps = frames;
				frames = 0;
			}
		}
	}
	public void save() {
		save(false);
	}
	
	// PRIVATE
	private void tick() {
		long msStart = System.currentTimeMillis();
		try {
			if(autoSave)
				saveTimer++;
			if(saveTimer > 20) {
				save(false);
				saveTimer = 0;
			}
			if(errors.contains(1) || errors.contains(2))
				return;
			if(tickMethod == null)
				tickMethod = canvas.getClass().getMethod(tickMethodName);
			tickMethod.invoke(canvas);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | 
				NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			errors.add(2);
		} finally {
			mspt = System.currentTimeMillis() - msStart;
		}
	}
	private void render() {
		if(frame.getExtendedState() == 0 & frame.getState() == 0 & fullScreen != 1) {
			width = frame.getWidth();
			height = frame.getHeight();
			x = frame.getX();
			y = frame.getY();
		}
		if(reqTFullScreen) {
			setFullScreen(!isFullScreen());
			reqTFullScreen = false;
			canvas.requestFocus();
		}
		long msStart = System.currentTimeMillis();
		try {
			BufferStrategy bs = canvas.getBufferStrategy();
			if(bs == null) {
				canvas.createBufferStrategy(3);
				return;
			}
			Graphics g = bs.getDrawGraphics();
			if(renderBackground) {
				g.setColor(background);
				g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
			}
			if(errors.contains(1) || errors.contains(3)) {
				g.dispose();
				bs.show();
				return;
			}
			if(renderMethod == null)
				renderMethod = canvas.getClass().getMethod(renderMethodName, Graphics.class);
			renderMethod.invoke(canvas, g);
			g.dispose();
			bs.show();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | 
				NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			errors.add(3);
		} finally {
			mspf = System.currentTimeMillis() - msStart;
		}
	}
	private void save(boolean att) {
		File save = new File(savePath);
		try {
			FileWriter fw = new FileWriter(save);
			fw.write(x + "\r" + y + "\r" + width + "\r" + height + 
					"\r" + frame.getExtendedState() + "\r" + frame.getState() + "\r" + 
					fullScreen);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			if(!att)
				createSaveFile();
		}
	}
	private void createSaveFile() {
		try {
			Files.createDirectory(Paths.get(savePath.substring(0, savePath.lastIndexOf("/"))));
			save(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// SETTERS
	public void setLocation(int x, int y) {
		frame.setLocation(x, y);
	}
	public void setSize(int width, int height) {
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
	public void setRenderBackground(boolean renderBackground) {
		this.renderBackground = renderBackground;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	public void setDimensions(int x, int y, int width, int height) {
		frame.setLocation(x, y);
		frame.setSize(width, height);
	}
	public void setFullScreen(boolean fullScreen) {
		if(fullScreen) {
			this.fullScreen = 1;
			frame.setExtendedState(6);
			frame.dispose();
		    frame.setUndecorated(true);
		    frame.setVisible(true);
		} else {
			this.fullScreen = 0;
			frame.setExtendedState(0);
			frame.dispose();
		    frame.setUndecorated(false);
		    frame.setVisible(true);
		    frame.setSize(width, height);
		    frame.setLocation(x, y);
		}
	}
	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}
	public void dontLoad() {
		this.load = false;
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
		return this.tps;
	}
	public int getW() {
		return this.frame.getWidth();
	}
	public int getH() {
		return this.frame.getHeight();
	}
	public int getX() {
		return this.frame.getX();
	}
	public int getY() {
		return this.frame.getY();
	}
	public boolean isFullScreen() {
		return (this.fullScreen == 1);
	}

	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_F11)
			reqTFullScreen = true;
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}

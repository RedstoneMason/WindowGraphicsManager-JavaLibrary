package codingmason.easy_window;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JFrame;

public class EWindow implements Runnable {
	private static final int WAITING = 0, STARTING = 1, RUNNING = 2;
	private boolean F11, load, fullScreen;
	private int fps, tps, mspt, mspf, setTps = 20, width, height, x, y, runState;
	private JFrame frame = new JFrame();
	private String savePath = null;
	private MethodInvoke[] methods = new MethodInvoke[3];
	private Canvas canvas = new Canvas();
	private EGraphics eg;
	private InputListener input;

	// CONSTRUCTOR
	public EWindow(Object parentClass) {
		String path = "";
		if(parentClass != null) path = parentClass.getClass().getName();
		methods[0] = new MethodInvoke(path+".render", parentClass, EGraphics.class);
		methods[1] = new MethodInvoke(path+".tick", parentClass);
		methods[2] = new MethodInvoke(path+".windowClosed", parentClass);
		methods[2].printErrors(false);
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		x = ss.width/2-500;
		y = ss.height/2-375;
		width = 1000;
		height = 750;
		frame.add(canvas);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("EasyWindow "+EW.VERSION+" - "+EW.CREATOR);
		frame.setSize(width, height);
		frame.setLocation(x, y);
		frame.setVisible(true);
		input = new InputListener(canvas, parentClass);
		input.printErrors(false);
		input.addOutput(this);
		input.printErrors(false);
		Toolkit.getDefaultToolkit().setDynamicLayout(false);
		eg = new EGraphics(this, getContentWidth(), getContentHeight());
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				save();
				methods[2].invoke();
				runState = WAITING;
			}
		});
	}
	
	// PUBLIC
	public void run() {
		if(runState == RUNNING) return;
		if(runState == WAITING) {
			Thread r = new Thread(this);
			runState = STARTING;
			r.start();
			return;
		}
		runState = RUNNING;
		if(this.load) load();
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		double delta = 0;
		int frames = 0, ticks = 0;
		while(runState == RUNNING) {
			double ns = 1000000000/setTps;
			long now = System.nanoTime();
			delta += (now-lastTime)/ns;
			lastTime = now;
			while(delta > 0) {
				tick();
				ticks++;
				delta--;
			}
			render();
			frames++;
			if(System.currentTimeMillis()-timer > 1000) {
				timer += 1000;
				fps = frames;
				tps = ticks;
				frames = 0;
				ticks = 0;
			}
		}
		System.exit(0);
	}
	public void save() {
		if(savePath == null) return;
		File save = new File(savePath);
		File saveDir = save.getParentFile();
		try {
			if(!saveDir.exists()) Files.createDirectory(Paths.get(saveDir.getPath()));
			DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(save)));
			for(int i : getProperties()) dos.writeInt(i);
			dos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void load() {
		if(savePath == null) return;
		File save = new File(savePath);
		try {
			DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(save)));
			int[] properties = new int[7];
			for(int i = 0; i < properties.length; i++) properties[i] = dis.readInt();
			dis.close();
			loadProperties(properties);
		} catch(IOException | NullPointerException | NumberFormatException e) {
			e.printStackTrace();
		}
	}

	// PRIVATE
	private int[] getProperties() {
		return new int[] {x, y, width, height, frame.getExtendedState(), frame.getState(), fullScreen ? 1 : 0};
	}
	private void loadProperties(int[] properties) {
		if(properties.length < 7) return;
		int x = properties[0];
		int y = properties[1];
		int w = properties[2];
		int h = properties[3];
		int ext = properties[4];
		int state = properties[5];
		int fs = properties[6];
		frame.setLocation(x, y);
		frame.setSize(w, h);
		frame.setExtendedState(ext);
		frame.setState(state);
		if(fs == 1) setFullScreen(true);
		this.width = w;
		this.height = h;
		this.x = x;
		this.y = y;
		this.fullScreen = (fs == 1);
	}
	private void tick() {
		long msStart = System.currentTimeMillis();
		methods[1].invoke();
		mspt = (int)(System.currentTimeMillis()-msStart);
	}
	private void render() {
		long msStart = System.currentTimeMillis();
		int cwidth = getContentWidth();
		int cheight = getContentHeight();
		if(eg.getWidth() != cwidth || eg.getHeight() != cheight) {
			eg.setSize(cwidth, cheight);
		}
		if(frame.getExtendedState() == 0 & frame.getState() == 0 & !fullScreen) {
			x = frame.getX();
			y = frame.getY();
			width = frame.getWidth();
			height = frame.getHeight();
		}
		if(F11) {
			setFullScreen(!isFullScreen());
			F11 = false;
			canvas.requestFocus();
		}
		BufferStrategy bs = canvas.getBufferStrategy();
		if(bs == null) {
			canvas.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		eg.setGraphics(g);
		methods[0].invoke(eg);
		eg.render();
		g.dispose();
		bs.show();
		mspf = (int)(System.currentTimeMillis()-msStart);
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
	public void setTPS(int tps) {
		this.setTps = tps;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
		this.load = true;
	}
	public void setFullScreen(boolean fullScreen) {
		if(fullScreen) {
			frame.setExtendedState(6);
			frame.dispose();
			frame.setUndecorated(true);
			frame.setVisible(true);
		} else {
			frame.setExtendedState(0);
			frame.dispose();
			frame.setUndecorated(false);
			frame.setVisible(true);
			frame.setSize(width, height);
			frame.setLocation(x, y);
		}
		this.fullScreen = fullScreen;
	}

	// GETTERS
	public boolean isFullScreen() {
		return fullScreen;
	}
	public double getMSPT() {
		return mspt;
	}
	public double getMSPF() {
		return mspf;
	}
	public int getFPS() {
		return fps;
	}
	public int getSetTPS() {
		return setTps;
	}
	public int getTPS() {
		return tps;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getX() {
		return frame.getX();
	}
	public int getY() {
		return frame.getY();
	}
	public int getWindowWidth() {
		return frame.getWidth();
	}
	public int getWindowHeight() {
		return frame.getHeight();
	}
	public int getContentWidth() {
		return frame.getContentPane().getSize().width;
	}
	public int getContentHeight() {
		return frame.getContentPane().getSize().height;
	}
	public JFrame getFrame() {
		return frame;
	}
	public Canvas getCanvas() {
		return canvas;
	}
	public EGraphics getEGraphics() {
		return eg;
	}
	public InputListener getInput() {
		return input;
	}
	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_F11) F11 = true;
	}
}
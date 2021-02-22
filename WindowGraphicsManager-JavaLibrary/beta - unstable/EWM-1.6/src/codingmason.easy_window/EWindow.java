package codingmason.easy_window;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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

public class EWindow implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener, Runnable {
	public static final int WAITING=0, STARTING=1, RUNNING=2;
	private JFrame frame = new JFrame();
	private String savePath = "";
	private boolean[] keys = new boolean[65535];
	private boolean[] buttons = new boolean[20];
	private boolean reqTFullScreen, autoSave, load, fullScreen, inFocus;
	private int fps, tps = 20, width, height, x, y, saveTimer, runState, mouseX, mouseY;
	private double mspt, mspf;
	private ArrayList<MethodInvoke> methods = new ArrayList<>();
	private Canvas canvas = new Canvas();
	private EGraphics eg;

	// CONSTRUCTOR
	public EWindow(Object parentClass) {
		String path = "";
		if(parentClass != null)
			path = parentClass.getClass().getName();
		methods.add(new MethodInvoke(path+".tick", parentClass));
		methods.add(new MethodInvoke(path+".render", parentClass, EGraphics.class));
		methods.add(new MethodInvoke(path+".keyPressed", parentClass, KeyEvent.class));
		methods.add(new MethodInvoke(path+".keyReleased", parentClass, KeyEvent.class));
		methods.add(new MethodInvoke(path+".keyTyped", parentClass, KeyEvent.class));
		methods.add(new MethodInvoke(path+".mouseWheelMoved", parentClass, MouseWheelEvent.class));
		methods.add(new MethodInvoke(path+".mouseDragged", parentClass, MouseEvent.class));
		methods.add(new MethodInvoke(path+".mouseMoved", parentClass, MouseEvent.class));
		methods.add(new MethodInvoke(path+".mousePressed", parentClass, MouseEvent.class));
		methods.add(new MethodInvoke(path+".mouseReleased", parentClass, MouseEvent.class));
		methods.add(new MethodInvoke(path+".mouseClicked", parentClass, MouseEvent.class));
		methods.add(new MethodInvoke(path+".mouseEntered", parentClass, MouseEvent.class));
		methods.add(new MethodInvoke(path+".mouseExited", parentClass, MouseEvent.class));
		methods.add(new MethodInvoke(path+".focusGained", parentClass, FocusEvent.class));
		methods.add(new MethodInvoke(path+".focusLost", parentClass, FocusEvent.class));
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		x = ss.width/2-500;
		y = ss.height/2-375;
		width = 1000;
		height = 750;
		frame.add(canvas);
		frame.setDefaultCloseOperation(3);
		frame.setTitle("Window 1.6");
		frame.setSize(width, height);
		frame.setLocation(x, y);
		frame.setVisible(true);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addFocusListener(this);
		Toolkit.getDefaultToolkit().setDynamicLayout(false);
		canvas.requestFocus();
		eg = new EGraphics(this, getContentWidth(), getContentHeight());
	}

	// PUBLIC
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
			this.fullScreen = (fs == 1);
			br.close();
		} catch(IOException | NullPointerException | NumberFormatException e) {
			e.printStackTrace();
		}
	}
	public void run() {
		if(runState == RUNNING)
			return;
		if(runState == WAITING) {
			Thread r = new Thread(this);
			runState = STARTING;
			r.start();
			return;
		}
		runState = RUNNING;
		if(this.load)
			load(); 
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		double delta = 0;
		int frames = 0;
		while(runState == RUNNING) {
			double ns = 1000000000/tps;
			long now = System.nanoTime();
			delta += (now-lastTime)/ns;
			lastTime = now;
			while(delta > 0) {
				tick();
				delta--;
			}
			render();
			frames++;
			if(System.currentTimeMillis()-timer > 1000) {
				timer += 1000;
				fps = frames;
				frames = 0;
			}
		}
	}
	public void save() {
		File save = new File(savePath);
		File saveDir = save.getParentFile();
		if(!saveDir.exists()) {
			System.out.println("Created directory at '"+saveDir+"'");
			createDirectory(saveDir);
		}
		if(!save.exists())
			System.out.println("Created save at '"+save+"'");
		try {
			FileWriter fw = new FileWriter(save);
			fw.write(x+"\r"+y+"\r"+width+"\r"+height+"\r"+frame.getExtendedState()+"\r"+frame.getState()+"\r"+(fullScreen ? 1 : 0));
			fw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// PRIVATE
	private void tick() {
		long msStart = System.currentTimeMillis();
		if(autoSave) {
			saveTimer++;
			if(saveTimer > 20) {
				save();
				saveTimer = 0;
			}
		}
		methods.get(0).invoke();
		mspt = System.currentTimeMillis()-msStart;
	}
	private void render() {
		int cwidth = getContentWidth();
		int cheight = getContentHeight();
		if(eg.getWidth() != cwidth || eg.getHeight() != cheight) {
			eg.setSize(cwidth, cheight);
		}
		if(frame.getExtendedState() == 0 & frame.getState() == 0 & !fullScreen) {
			x = frame.getX();
			y = frame.getY();
			this.width = frame.getWidth();
			this.height = frame.getHeight();
		}
		if(reqTFullScreen) {
			setFullScreen(!isFullScreen());
			reqTFullScreen = false;
			canvas.requestFocus();
		}
		long msStart = System.currentTimeMillis();
		BufferStrategy bs = canvas.getBufferStrategy();
		if(bs == null) {
			canvas.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		eg.setGraphics(g);
		methods.get(1).invoke(eg);
		eg.render();
		g.dispose();
		bs.show();
		mspf = System.currentTimeMillis()-msStart;
	}
	private void createDirectory(File path) {
		try {
			Files.createDirectory(Paths.get(path.getPath()));
		} catch(IOException e) {
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
	public void setTPS(int tps) {
		this.tps = tps;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
		this.autoSave = true;
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
	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
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
	public int getWidth() {
		return this.width;
	}
	public int getHeight() {
		return this.height;
	}
	public int getX() {
		return this.frame.getX();
	}
	public int getY() {
		return this.frame.getY();
	}
	public boolean isFullScreen() {
		return this.fullScreen;
	}
	public JFrame getFrame() {
		return this.frame;
	}
	public Canvas getCanvas() {
		return this.canvas;
	}
	public boolean keyIsPressed(int keyCode) {
		if(keyCode >= 0 & keyCode < keys.length)
			return keys[keyCode];
		return false;
	}
	public boolean buttonIsPressed(int button) {
		if(button >= 0 & button < buttons.length)
			return buttons[button];
		return false;
	}
	public boolean isInFocus() {
		return inFocus;
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
	public int getMouseX() {
		return mouseX;
	}
	public int getMouseY() {
		return mouseY;
	}
	public EGraphics getEGraphics() {
		return eg;
	}
	
	// KEY INPUT
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if(c >= 0 & c < keys.length)
			keys[c] = true;
		if(c == KeyEvent.VK_F11)
			reqTFullScreen = true;
		methods.get(2).invoke(e);
	}
	public void keyReleased(KeyEvent e) {
		int c = e.getKeyCode();
		if(c >= 0 & c < keys.length)
			keys[c] = false;
		methods.get(3).invoke(e);
	}
	public void keyTyped(KeyEvent e) {
		methods.get(4).invoke(e);
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		methods.get(5).invoke(e);
	}
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		methods.get(6).invoke(e);
	}
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		methods.get(7).invoke(e);
	}
	public void mousePressed(MouseEvent e) {
		int c = e.getButton();
		if(c >= 0 & c < buttons.length)
			buttons[c] = true;
		methods.get(8).invoke(e);
	}
	public void mouseReleased(MouseEvent e) {
		int c = e.getButton();
		if(c >= 0 & c < buttons.length)
			buttons[c] = false;
		methods.get(9).invoke(e);
	}
	public void mouseClicked(MouseEvent e) {
		methods.get(10).invoke(e);
	}
	public void mouseEntered(MouseEvent e) {
		methods.get(11).invoke(e);
	}
	public void mouseExited(MouseEvent e) {
		methods.get(12).invoke(e);
	}
	public void focusGained(FocusEvent e) {
		if(e.getComponent() == canvas)
			inFocus = true;
		methods.get(13).invoke(e);
	}
	public void focusLost(FocusEvent e) {
		if(e.getComponent() == canvas)
			inFocus = false;
		methods.get(14).invoke(e);
	}
	
	public static void showMethodErrors(boolean bool) {
		MethodInvoke.showError = bool;
	}
}

class MethodInvoke {
	protected static boolean showError = true;
	public Object parent;
	public String path;
	public Method method;
	public Class<?>[] paramClass;
	public boolean error;
	
	// CONSTRUCTOR
	protected MethodInvoke(String path, Object parent, Class<?>... params) {
		this.parent = parent;
		this.path = path;
		this.paramClass = params;
	}
	
	// PUBLIC
	public void invoke(Object... params) {
		if(error)
			return;
		try {
			if(method == null) {
				String className = path.substring(0, path.lastIndexOf("."));
				String methodName = path.substring(path.lastIndexOf(".") + 1);
				Class<?> c = Class.forName(className);
				method = c.getMethod(methodName, paramClass);
			}
			method.invoke(parent, params);
		} catch (SecurityException | IllegalAccessException	| IllegalArgumentException |  
				ClassNotFoundException | NoSuchMethodException | StringIndexOutOfBoundsException | NullPointerException e) {
			error = true;
			if(showError)
				e.printStackTrace();
		} catch(InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
package codingmason.easy_window;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;

public class InputListener implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener {
	protected static final int KEY_PRESSED = 0, KEY_RELEASED = 1, KEY_TYPED = 2, MOUSE_WHEEL_MOVED = 3, MOUSE_DRAGGED = 4, MOUSE_MOVED = 5, MOUSE_PRESSED = 6, MOUSE_RELEASED = 7, MOUSE_CLICKED = 8,
			MOUSE_ENTERED = 9, MOUSE_EXITED = 10, FOCUS_GAINED = 11, FOCUS_LOST = 12;
	private boolean[] keys = new boolean[65535], buttons = new boolean[10];
	private int mouseX, mouseY;
	private boolean inFocus;
	private Component component;
	private LinkedList<MethodInvoke[]> outputs = new LinkedList<>();
	
	public InputListener(Component c, Object methodInvokeClass) {
		c.addKeyListener(this);
		c.addMouseListener(this);
		c.addMouseMotionListener(this);
		c.addMouseWheelListener(this);
		c.addFocusListener(this);
		c.requestFocus();
		this.component = c;
		addOutput(methodInvokeClass);
	}

	public int mouseX() {
		return mouseX;
	}
	public int mouseY() {
		return mouseY;
	}
	public boolean inFocus() {
		return inFocus;
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
	public void printErrors(boolean printErrors) {
		MethodInvoke[] invokes = outputs.getLast();
		for(MethodInvoke m : invokes) m.printErrors(printErrors);
	}
	public MethodInvoke[] getOutput(int index) {
		if(index < 0 || index > outputs.size()) return null;
		return outputs.get(index);
	}
	public int getOutputsCount() {
		return outputs.size();
	}

	public void addOutput(Object class_) {
		MethodInvoke[] invokes = new MethodInvoke[13];
		String path = "";
		if(class_ != null)
			path = class_.getClass().getName();
		invokes[0] = new MethodInvoke(path+".focusGained", class_, FocusEvent.class);
		invokes[1] = new MethodInvoke(path+".focusLost", class_, FocusEvent.class);
		invokes[2] = new MethodInvoke(path+".mouseDragged", class_, MouseEvent.class);
		invokes[3] = new MethodInvoke(path+".mouseMoved", class_, MouseEvent.class);
		invokes[4] = new MethodInvoke(path+".mousePressed", class_, MouseEvent.class);
		invokes[5] = new MethodInvoke(path+".mouseReleased", class_, MouseEvent.class);
		invokes[6] = new MethodInvoke(path+".keyPressed", class_, KeyEvent.class);
		invokes[7] = new MethodInvoke(path+".keyReleased", class_, KeyEvent.class);
		invokes[8] = new MethodInvoke(path+".keyTyped", class_, KeyEvent.class);
		invokes[9] = new MethodInvoke(path+".mouseWheelMoved", class_, MouseWheelEvent.class);
		invokes[10] = new MethodInvoke(path+".mouseClicked", class_, MouseEvent.class);
		invokes[11] = new MethodInvoke(path+".mouseEntered", class_, MouseEvent.class);
		invokes[12] = new MethodInvoke(path+".mouseExited", class_, MouseEvent.class);
		outputs.add(invokes);
	}

	public void focusGained(FocusEvent e) {
		if(e.getComponent() == component) inFocus = true;
		for(MethodInvoke[] invokes : outputs) invokes[0].invoke(e);
	}
	public void focusLost(FocusEvent e) {
		if(e.getComponent() == component) inFocus = false;
		for(MethodInvoke[] invokes : outputs) invokes[1].invoke(e);
	}
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		for(MethodInvoke[] invokes : outputs) invokes[2].invoke(e);
	}
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		for(MethodInvoke[] invokes : outputs) invokes[3].invoke(e);
	}
	public void mousePressed(MouseEvent e) {
		int c = e.getButton();
		if(c >= 0 & c < buttons.length) buttons[c] = true;
		for(MethodInvoke[] invokes : outputs) invokes[4].invoke(e);
	}
	public void mouseReleased(MouseEvent e) {
		int c = e.getButton();
		if(c >= 0 & c < buttons.length) buttons[c] = false;
		for(MethodInvoke[] invokes : outputs) invokes[5].invoke(e);
	}
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if(c >= 0 & c < keys.length) keys[c] = true;
		for(MethodInvoke[] invokes : outputs) invokes[6].invoke(e);
	}
	public void keyReleased(KeyEvent e) {
		int c = e.getKeyCode();
		if(c >= 0 & c < keys.length) keys[c] = false;
		for(MethodInvoke[] invokes : outputs) invokes[7].invoke(e);
	}
	public void keyTyped(KeyEvent e) {
		for(MethodInvoke[] invokes : outputs) invokes[8].invoke(e);
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		for(MethodInvoke[] invokes : outputs) invokes[9].invoke(e);
	}
	public void mouseClicked(MouseEvent e) {
		for(MethodInvoke[] invokes : outputs) invokes[10].invoke(e);
	}
	public void mouseEntered(MouseEvent e) {
		for(MethodInvoke[] invokes : outputs) invokes[11].invoke(e);
	}
	public void mouseExited(MouseEvent e) {
		for(MethodInvoke[] invokes : outputs) invokes[12].invoke(e);
	}
}
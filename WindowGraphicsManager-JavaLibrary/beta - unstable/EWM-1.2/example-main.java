package window.code_mason;

import java.awt.Canvas;
import java.awt.Graphics;

public class Display extends Canvas {
	private static final long serialVersionUID = 1L;
	public void tick() {
		
	}
	public void render(Graphics g) {
		
	}
	public static void main(String[] args) {
		Display display = new Display();
		Window w = new Window(display);
		w.start();
	}
}

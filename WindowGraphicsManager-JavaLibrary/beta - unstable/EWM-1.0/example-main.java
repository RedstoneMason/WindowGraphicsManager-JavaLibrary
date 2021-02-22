package testing.code_mason;

import java.awt.Canvas;
import java.awt.Graphics;

import window.code_mason.Window;

public class Display extends Canvas {
	public boolean running=true;
	public Window window;
	
	public void tick() {
		
	}
	
	public void render(Graphics g) {
		
	}
	
	private void run() {
		while(running) {
			window.tick();
			window.render();
		}
	}
	
	private void setupWindow() {
		window = new Window(this, 50, 50, 750, 500, "new custom window");
	}
	
	public static void main(String[] args) {
		Display display = new Display();
		display.setupWindow();
		display.run();
	}
}

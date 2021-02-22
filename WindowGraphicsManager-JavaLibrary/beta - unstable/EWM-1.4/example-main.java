package window;

public class Main {
	public Window w = new Window(this);
	public void render(Graphics g) {

	}
	public void tick() {

	}
	public void start() {
		w.start();
	}
	
	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}
}

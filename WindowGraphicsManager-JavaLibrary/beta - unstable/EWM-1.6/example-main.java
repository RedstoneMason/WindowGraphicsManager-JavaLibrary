import java.awt.Color;
import java.awt.Font;

public class Main {
	public EWindow window = new EWindow(this);
	public void render(EGraphics g) {
		g.clear();
		g.setColor(Color.red);
		g.drawLine(50, 50, window.getMouseX(), window.getMouseY());
		g.setColor(Color.white);
		g.setFont(new Font("", 0, 25));
		g.drawString("ContentPane: "+g.getWidth()+" "+g.getHeight(), 50, 50, false);
	}
	public void tick() {

	}
	public void start() {
		window.setSavePath(System.getProperty("user.home")+"/Desktop/EWindow/window-1.txt");
		window.run();
	}
	
	public static void main(String[] args) {
		EWindow.showMethodErrors(false);
		Main main = new Main();
		main.start();
	}
}

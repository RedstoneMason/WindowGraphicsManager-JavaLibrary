package codingmason.easy_window;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
	public EWindow window = new EWindow(this);
	public ArrayList<Point> dots = new ArrayList<Point>();
	public Main() {
		window.run();
		window.setTPS(60);
		for(int i = 0; i < 100; i++) {
			dots.add(new Point(ThreadLocalRandom.current().nextInt(window.getContentWidth()), ThreadLocalRandom.current().nextInt(window.getContentHeight())));
		}
	}
	public void tick() {
		int speed = 6, randomness = 12;
		for(Point p : dots) {
			p.x += ThreadLocalRandom.current().nextInt(-randomness+1, randomness);
			p.y += ThreadLocalRandom.current().nextInt(-randomness+1, randomness);
			if(p.x < 0) p.x = 0;
			if(p.x > window.getContentWidth()) p.x = window.getContentWidth();
			if(p.y < 0) p.y = 0;
			if(p.y > window.getContentHeight()) p.y = window.getContentHeight();
			Point[] path = getLine(p.x, p.y, window.getInput().mouseX(), window.getInput().mouseY());
			if(path.length < 1) continue;
			int index = speed;
			if(index >= path.length) index = path.length-1;
			p.x = path[index].x;
			p.y = path[index].y;
		}
	}
	public void render(EGraphics e) {
		e.clear();
		e.dontRender(true);
		Graphics g = e.getGraphics();
		g.setColor(Color.black);
		g.setFont(new Font("", 0, 30));
		g.drawString(window.getMSPF()+" mspf", 20, 40);
		g.drawString(window.getFPS()+" fps", 20, 90);
		g.setColor(Color.red);
		for(Point p : dots) g.fillOval(p.x, p.y, 8, 8);
	}
	public void windowClosed() {
		System.out.println("window closed");
	}
	private Point[] getLine(int x0, int y0, int x1, int y1) {
		ArrayList<Point> points = new ArrayList<>();
		int dx = Math.abs(x1-x0);
		int dy = Math.abs(y1-y0);
		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;
		int err = dx-dy, e2;
		while(true) {
			points.add(new Point(x0, y0));
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
		return points.toArray(new Point[] {});
	}
	public static void main(String[] args) {
		new Main();
	}
}

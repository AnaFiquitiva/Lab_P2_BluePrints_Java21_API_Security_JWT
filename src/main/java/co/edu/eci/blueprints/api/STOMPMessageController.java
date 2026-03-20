package co.edu.eci.blueprints.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class STOMPMessageController {

    @Autowired
    SimpMessagingTemplate msgt;

    @MessageMapping("/draw")
    public void handleDrawEvent(DrawMessage message) throws Exception {
        System.out.println("Nuevo punto recibido en el servidor!: " + message);
        msgt.convertAndSend("/topic/blueprints." + message.getAuthor() + "." + message.getName(), message.getPoint());
    }
}

class DrawMessage {
    private String author;
    private String name;
    private Point point;

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Point getPoint() { return point; }
    public void setPoint(Point point) { this.point = point; }

    @Override
    public String toString() {
        return "DrawMessage{" + "author='" + author + '\'' + ", name='" + name + '\'' + ", point=" + point + '}';
    }
}

class Point {
    private int x;
    private int y;

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }
}

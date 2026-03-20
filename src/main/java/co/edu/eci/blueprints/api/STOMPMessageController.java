package co.edu.eci.blueprints.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class STOMPMessageController {

    @Autowired
    SimpMessagingTemplate msgt;

    /**
     * Maneja eventos de dibujo del cliente STOMP.
     * Valida el payload antes de procesar y retransmite a suscriptores.
     * 
     * @param message Mensaje convalidado con restricciones de campo
     * @throws Exception si hay error al enviar mensaje
     */
    @MessageMapping("/draw")
    public void handleDrawEvent(@Valid DrawMessage message) throws Exception {
        // Log del punto recibido (con validación ya aplicada)
        System.out.println("✅ Punto validado y recibido en el servidor: " + message);
        
        // Retransmitir a todos los clientes en la sala del blueprint
        String destination = "/topic/blueprints." + message.getAuthor() + "." + message.getName();
        msgt.convertAndSend(destination, message.getPoint());
        
        System.out.println("📤 Punto retransmitido a: " + destination);
    }
}

/**
 * Mensaje de dibujo del cliente.
 * Contiene validaciones para garantizar datos seguros.
 */
class DrawMessage {
    
    @NotBlank(message = "El author no puede estar vacío")
    @Size(min = 1, max = 50, message = "El author debe tener entre 1 y 50 caracteres")
    private String author;
    
    @NotBlank(message = "El name no puede estar vacío")
    @Size(min = 1, max = 50, message = "El name debe tener entre 1 y 50 caracteres")
    private String name;
    
    @NotNull(message = "El point no puede ser nulo")
    @Valid
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

/**
 * Punto en el canvas.
 * Restriccionado a los límites del lienzo HTML5 (520x360).
 */
class Point {
    
    @NotNull(message = "X no puede ser nulo")
    @Min(value = 0, message = "X debe ser >= 0")
    @Max(value = 520, message = "X debe ser <= 520 (ancho del canvas)")
    private Integer x;
    
    @NotNull(message = "Y no puede ser nulo")
    @Min(value = 0, message = "Y debe ser >= 0")
    @Max(value = 360, message = "Y debe ser <= 360 (alto del canvas)")
    private Integer y;

    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }
    
    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }
}

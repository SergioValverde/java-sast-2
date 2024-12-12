import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.owasp.encoder.Encode;

@WebServlet("/VulnerableWebApp")
public class VulnerableWebApp extends HttpServlet {
    // Vulnerabilidad 1: Reflected XSS
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        
        // XSS Vulnerable: Imprime directamente el parámetro sin sanitización
        response.getWriter().println("Bienvenido: " + username);
        
        // XSS en HTML context
        response.getWriter().println("<div>" + username + "</div>");
    }

    // Vulnerabilidad 2: SQL Injection
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        try {
            // Conexión insegura con concatenación de strings
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/userdb");
            String query = "SELECT * FROM users WHERE email = '" + email + "' AND password = '" + password + "'";
            
            // SQL Injection directa
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Vulnerabilidad 3: Path Traversal
    public void downloadFile(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String fileName = request.getParameter("filename");
        
        // Path Traversal inseguro
        java.io.File file = new java.io.File("/uploads/" + fileName);
        
        // Leer y enviar archivo sin validación de ruta
        java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
    }

    // Vulnerabilidad 4: Command Injection
    public void executeSystemCommand(HttpServletRequest request) 
            throws IOException {
        String userInput = request.getParameter("ping");
        
        // Command Injection directo
        Runtime.getRuntime().exec("ping " + userInput);
    }

    // Vulnerabilidad 5: Stored XSS con almacenamiento en base de datos
    public void storeUserComment(HttpServletRequest request) {
        String comment = request.getParameter("comment");
        
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/commentsdb");
            String insertQuery = "INSERT INTO comments (content) VALUES (?)";
            
            // Almacena comentario sin sanitización
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, comment);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ejemplo de método con intento de mitigación (para comparación)
    protected void secureMethod(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        
        // Ejemplo de sanitización contra XSS
        String safeUsername = Encode.forHtml(username);
        response.getWriter().println("Bienvenido: " + safeUsername);
    }
}

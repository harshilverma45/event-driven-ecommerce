import java.sql.*;

public class VerifyProductDb {
    public static void main(String[] args) throws Exception {
        String adminUrl = "jdbc:postgresql://localhost:5432/postgres";
        try (Connection conn = DriverManager.getConnection(adminUrl, "postgres", "postgres");
             PreparedStatement ps = conn.prepareStatement("SELECT datname FROM pg_database WHERE datname = ?")) {
            ps.setString(1, "product_db");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(rs.getString(1));
                } else {
                    System.out.println("NOT_FOUND");
                }
            }
        }
    }
}

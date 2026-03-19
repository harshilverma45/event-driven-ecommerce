import java.sql.*;

public class CreateProductDb {
    public static void main(String[] args) throws Exception {
        String adminUrl = "jdbc:postgresql://localhost:5432/postgres";
        try (Connection conn = DriverManager.getConnection(adminUrl, "postgres", "postgres")) {
            conn.setAutoCommit(true);
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                ps.setString(1, "product_db");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("EXISTS");
                        return;
                    }
                }
            }
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE DATABASE product_db");
                System.out.println("CREATED");
            }
        }
    }
}

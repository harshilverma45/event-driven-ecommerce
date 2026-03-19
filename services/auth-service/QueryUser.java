import java.sql.*;
public class QueryUser {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:postgresql://localhost:5432/auth_db";
    try (Connection c = DriverManager.getConnection(url, "postgres", "postgres");
         Statement s = c.createStatement();
         ResultSet rs = s.executeQuery("select id, email, password, name from users order by id limit 1")) {
      if (rs.next()) {
        System.out.println(rs.getLong(1)+"|"+rs.getString(2)+"|"+rs.getString(3)+"|"+rs.getString(4));
      } else {
        System.out.println("NO_USERS");
      }
    }
  }
}

import java.sql.*;
public class QueryUser {
  public static void main(String[] args) throws Exception {
    String url = System.getenv("DB_URL");
    String username = System.getenv("DB_USERNAME");
    String password = System.getenv("DB_PASSWORD");
    if (url == null || username == null || password == null) {
      throw new IllegalStateException("Set DB_URL, DB_USERNAME, and DB_PASSWORD environment variables.");
    }
    try (Connection c = DriverManager.getConnection(url, username, password);
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

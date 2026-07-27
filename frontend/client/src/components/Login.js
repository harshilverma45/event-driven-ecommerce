import { useState } from "react";
import { login } from "../api/authService";

function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const data = await login(username, password);
      const token = data.token;

      localStorage.setItem("token", token);
      onLogin(token);
    } catch (err) {
      setError("Invalid credentials");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: "grid", gap: "12px", maxWidth: "320px" }}>
      <div>
        <label htmlFor="username">Username</label>
        <input
          id="username"
          type="text"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          style={{ width: "100%", padding: "8px", marginTop: "4px" }}
          required
        />
      </div>

      <div>
        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          style={{ width: "100%", padding: "8px", marginTop: "4px" }}
          required
        />
      </div>

      <button type="submit" disabled={isSubmitting} style={{ padding: "10px" }}>
        {isSubmitting ? "Logging in..." : "Login"}
      </button>

      {error ? <p style={{ color: "crimson", margin: 0 }}>{error}</p> : null}
    </form>
  );
}

export default Login;

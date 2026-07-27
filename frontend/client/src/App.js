import { useState } from "react";
import Login from "./components/Login";

function App() {
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [isLoggedIn, setIsLoggedIn] = useState(() => Boolean(localStorage.getItem("token")));

  const handleLogin = (jwtToken) => {
    setToken(jwtToken);
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    setToken("");
    setIsLoggedIn(false);
  };

  return (
    <div style={{ padding: "20px" }}>
      {/* Future: connect to API Gateway (http://localhost:8080) */}
      <h1>Event-Driven E-Commerce</h1>

      {isLoggedIn ? (
        <div>
          <p>Login successful.</p>
          <p>Dashboard placeholder</p>
          <p style={{ wordBreak: "break-all" }}>
            <strong>Token:</strong> {token}
          </p>
          <button type="button" onClick={handleLogout} style={{ padding: "10px" }}>
            Logout
          </button>
        </div>
      ) : (
        <Login onLogin={handleLogin} />
      )}
    </div>
  );
}

export default App;

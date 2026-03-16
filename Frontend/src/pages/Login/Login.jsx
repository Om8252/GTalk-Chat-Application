import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import Input from "../../components/common/Input/Input";
import Button from "../../components/common/Button/Button";
import { authApi } from "../../api/auth.api";
import useAuth from "../../hooks/useAuth";
import "./Login.css";

function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!form.username || !form.password) {
      setError("All fields are required");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await authApi.login(form);
      login(response.user, response.token);
      navigate(`/chat/${response.user.userName}`);
    } catch (err) {
      setError(err.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1 className="auth-title">Welcome to G-Talk</h1>

        {error && <div className="auth-error">{error}</div>}

        <Input
          name="username"
          type="text"
          placeholder="Username"
          value={form.username}
          onChange={handleChange}
          ariaLabel="Username"
        />

        <Input
          name="password"
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          ariaLabel="Password"
        />

        <Button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </Button>

        <p className="auth-footer">
          Need an account? <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  );
}

export default Login;

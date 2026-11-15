import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router";
import { errorMessage } from "../api";
import { useAuth } from "../auth";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      await login(email.trim(), password);
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from ?? "/", { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 480, margin: "0 auto" }}>
      <div className="page-header">
        <div>
          <h1>Đăng nhập</h1>
          <p>Nhập email và mật khẩu để tiếp tục.</p>
        </div>
      </div>

      <section className="panel">
        {error && <p className="notice notice-error">{error}</p>}
        <form onSubmit={submit}>
          <div style={{ marginBottom: 12 }}>
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </div>
          <div style={{ marginBottom: 12 }}>
            <label htmlFor="password">Mật khẩu</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="button button-primary" disabled={loading}>
              {loading ? "Đang đăng nhập..." : "Đăng nhập"}
            </button>
            <Link className="button button-plain" to="/register">
              Tạo tài khoản
            </Link>
          </div>
        </form>

        <div className="help">
          <p>Tài khoản dùng thử:</p>
          <p>Sinh viên: student@uniwork.vn / Student@123</p>
          <p>Nhà tuyển dụng: employer@uniwork.vn / Employer@123</p>
          <p>Quản trị viên: admin@uniwork.vn / Admin@123</p>
        </div>
      </section>
    </div>
  );
}

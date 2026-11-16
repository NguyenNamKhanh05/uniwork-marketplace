import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router";
import { api, errorMessage } from "../api";
import { useAuth } from "../auth";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [needVerify, setNeedVerify] = useState(false);
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [mailConfigured, setMailConfigured] = useState<boolean | null>(null);

  useEffect(() => {
    api
      .get("/auth/config")
      .then((response) => setMailConfigured(Boolean(response.data.data.mailConfigured)))
      .catch(() => setMailConfigured(null));
  }, []);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");
    setNeedVerify(false);
    try {
      await login(email.trim(), password);
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from ?? "/", { replace: true });
    } catch (err) {
      const detail = errorMessage(err);
      setError(detail);
      setNeedVerify(detail.includes("xác thực email"));
    } finally {
      setLoading(false);
    }
  };

  const resend = async () => {
    setError("");
    setMessage("");
    setResending(true);
    try {
      await api.post("/auth/resend-verification", { email: email.trim() });
      setMessage(
        mailConfigured === false
          ? "Máy chủ chưa cấu hình gửi email. Vui lòng liên hệ quản trị viên để được gửi lại liên kết xác thực."
          : "Đã gửi lại email xác thực. Vui lòng kiểm tra hộp thư.",
      );
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setResending(false);
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
        {message && <p className="notice notice-success">{message}</p>}
        {needVerify && (
          <div className="form-actions" style={{ marginBottom: 12 }}>
            <button
              type="button"
              className="button button-plain"
              onClick={resend}
              disabled={resending || !email.trim()}
            >
              {resending ? "Đang gửi..." : "Gửi lại email xác thực"}
            </button>
          </div>
        )}
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
      </section>
    </div>
  );
}

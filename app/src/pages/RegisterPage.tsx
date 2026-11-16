import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, errorMessage } from "../api";

export function RegisterPage() {
  const [form, setForm] = useState({
    role: "STUDENT",
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
    employerType: "COMPANY",
  });
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [registered, setRegistered] = useState(false);
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [mailConfigured, setMailConfigured] = useState<boolean | null>(null);

  useEffect(() => {
    api
      .get("/auth/config")
      .then((response) => setMailConfigured(Boolean(response.data.data.mailConfigured)))
      .catch(() => setMailConfigured(null));
  }, []);

  const update = (key: string, value: string) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError("");
    if (form.password !== form.confirmPassword) {
      setError("Mật khẩu xác nhận không khớp.");
      return;
    }
    setLoading(true);
    try {
      await api.post("/auth/register", {
        role: form.role,
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword,
        employerType: form.role === "EMPLOYER" ? form.employerType : undefined,
      });
      setRegistered(true);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const resend = async () => {
    setError("");
    setMessage("");
    setResending(true);
    try {
      await api.post("/auth/resend-verification", { email: form.email.trim() });
      setMessage("Đã gửi lại email xác thực. Vui lòng kiểm tra hộp thư.");
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setResending(false);
    }
  };

  if (registered) {
    return (
      <div style={{ maxWidth: 560, margin: "0 auto" }}>
        <div className="page-header">
          <div>
            <h1>Kiểm tra email để hoàn tất đăng ký</h1>
            <p>Tài khoản chỉ được kích hoạt sau khi bạn xác thực địa chỉ email.</p>
          </div>
        </div>

        <section className="panel">
          {error && <p className="notice notice-error">{error}</p>}
          {message && <p className="notice notice-success">{message}</p>}
          {mailConfigured === false && (
            <p className="notice notice-error">
              Máy chủ chưa cấu hình gửi email nên bạn sẽ không nhận được thư xác thực. Quản trị viên
              cần điền MAIL_USERNAME và MAIL_PASSWORD trong backend/.env rồi khởi động lại backend.
            </p>
          )}
          {mailConfigured === false ? (
            <p>
              Đăng ký đã được ghi nhận cho <strong>{form.email.trim()}</strong>, nhưng chưa có email
              xác thực được gửi đi.
            </p>
          ) : (
            <p>
              Chúng tôi đã gửi liên kết xác thực tới <strong>{form.email.trim()}</strong>.
            </p>
          )}
          <p>
            Mở email, bấm nút "Xác thực tài khoản của tôi" để kích hoạt tài khoản. Liên kết có
            hiệu lực trong 24 giờ.
          </p>
          <p>Nếu không thấy email, hãy kiểm tra mục Spam hoặc gửi lại email xác thực.</p>
          <div className="form-actions">
            <button
              type="button"
              className="button button-primary"
              onClick={resend}
              disabled={resending}
            >
              {resending ? "Đang gửi..." : "Gửi lại email xác thực"}
            </button>
            <Link className="button button-plain" to="/login">
              Đến trang đăng nhập
            </Link>
          </div>
        </section>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 560, margin: "0 auto" }}>
      <div className="page-header">
        <div>
          <h1>Đăng ký tài khoản</h1>
          <p>Chọn vai trò phù hợp với nhu cầu của bạn.</p>
        </div>
      </div>

      <section className="panel">
        {error && <p className="notice notice-error">{error}</p>}
        <form onSubmit={submit}>
          <div className="form-grid">
            <div>
              <label htmlFor="role">Vai trò</label>
              <select
                id="role"
                value={form.role}
                onChange={(event) => update("role", event.target.value)}
              >
                <option value="STUDENT">Sinh viên</option>
                <option value="EMPLOYER">Nhà tuyển dụng</option>
              </select>
            </div>
            <div>
              <label htmlFor="fullName">
                {form.role === "EMPLOYER" ? "Tên đơn vị" : "Họ và tên"}
              </label>
              <input
                id="fullName"
                value={form.fullName}
                onChange={(event) => update("fullName", event.target.value)}
                required
              />
            </div>
            {form.role === "EMPLOYER" && (
              <div>
                <label htmlFor="employerType">Loại nhà tuyển dụng</label>
                <select
                  id="employerType"
                  value={form.employerType}
                  onChange={(event) => update("employerType", event.target.value)}
                >
                  <option value="COMPANY">Doanh nghiệp</option>
                  <option value="INDIVIDUAL">Cá nhân</option>
                </select>
              </div>
            )}
            <div>
              <label htmlFor="email">Email</label>
              <input
                id="email"
                type="email"
                value={form.email}
                onChange={(event) => update("email", event.target.value)}
                required
              />
            </div>
            <div>
              <label htmlFor="password">Mật khẩu</label>
              <input
                id="password"
                type="password"
                value={form.password}
                onChange={(event) => update("password", event.target.value)}
                required
                minLength={8}
              />
            </div>
            <div>
              <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
              <input
                id="confirmPassword"
                type="password"
                value={form.confirmPassword}
                onChange={(event) => update("confirmPassword", event.target.value)}
                required
                minLength={8}
              />
            </div>
          </div>
          <div className="form-actions">
            <button type="submit" className="button button-primary" disabled={loading}>
              {loading ? "Đang tạo tài khoản..." : "Đăng ký"}
            </button>
            <Link className="button button-plain" to="/login">
              Đã có tài khoản
            </Link>
          </div>
        </form>
      </section>
    </div>
  );
}

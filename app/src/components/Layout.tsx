import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router";
import { api, unwrap } from "../api";
import { useAuth } from "../auth";

export function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (!user) {
      setUnread(0);
      return;
    }
    let active = true;
    const load = () => {
      unwrap<{ unread: number }>(api.get("/notifications/unread-count"))
        .then((data) => {
          if (active) {
            setUnread(data.unread);
          }
        })
        .catch(() => undefined);
    };
    load();
    const timer = window.setInterval(load, 30000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, [user]);

  const handleLogout = () => {
    void api.post("/auth/logout").catch(() => undefined);
    logout();
    navigate("/");
  };

  return (
    <div className="app">
      <header className="site-header">
        <div className="container header-inner">
          <Link to="/" className="brand">
            UniWork
          </Link>
          <nav className="main-nav">
            <NavLink to="/" end>
              Trang chủ
            </NavLink>
            <NavLink to="/jobs">Việc làm</NavLink>
            {user?.role === "STUDENT" && (
              <>
                <NavLink to="/student/applications">Đơn ứng tuyển</NavLink>
                <NavLink to="/student/saved-jobs">Việc đã lưu</NavLink>
              </>
            )}
            {user?.role === "EMPLOYER" && (
              <>
                <NavLink to="/employer/jobs">Tin của tôi</NavLink>
                <NavLink to="/employer/jobs/new">Đăng tin</NavLink>
              </>
            )}
            {user?.role === "ADMIN" && <NavLink to="/admin">Quản trị</NavLink>}
            {user && <NavLink to="/reviews">Đánh giá</NavLink>}
          </nav>
          <div className="header-actions">
            {user ? (
              <>
                <Link to="/notifications" className="notification-link">
                  Thông báo{unread > 0 ? ` (${unread})` : ""}
                </Link>
                <Link to="/profile" className="user-link">
                  {user.fullName || user.email}
                </Link>
                <button type="button" className="button button-plain" onClick={handleLogout}>
                  Đăng xuất
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="button button-plain">
                  Đăng nhập
                </Link>
                <Link to="/register" className="button button-primary">
                  Đăng ký
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="main-content">
        <div className="container">
          <Outlet />
        </div>
      </main>

      <footer className="site-footer">
        <div className="container">
          <p>UniWork - Nền tảng việc làm bán thời gian cho sinh viên.</p>
          <p>Email hỗ trợ: hotro@uniwork.vn</p>
        </div>
      </footer>
    </div>
  );
}

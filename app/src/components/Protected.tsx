import { Navigate, Outlet, useLocation } from "react-router";
import { useAuth } from "../auth";
import type { Role } from "../types";

export function Protected({ roles }: { roles?: Role[] }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <p className="notice">Đang tải thông tin tài khoản...</p>;
  }
  if (!user) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }
  if (roles && !roles.includes(user.role)) {
    return <p className="notice notice-error">Bạn không có quyền truy cập trang này.</p>;
  }
  return <Outlet />;
}

import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import type { AuditLog, Dashboard, PageResponse } from "../types";
import { dateTimeText } from "../format";

export function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    unwrap<Dashboard>(api.get("/admin/dashboard"))
      .then(setDashboard)
      .catch((err) => setError(errorMessage(err)));
    unwrap<PageResponse<AuditLog>>(api.get("/admin/audit-logs", { params: { size: 10 } }))
      .then((result) => setLogs(result.content))
      .catch(() => undefined);
  }, []);

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Quản trị hệ thống</h1>
          <p>Tổng quan hoạt động của UniWork.</p>
        </div>
        <div className="row">
          <Link className="button button-plain" to="/admin/users">
            Người dùng
          </Link>
          <Link className="button button-plain" to="/admin/jobs">
            Tin tuyển dụng
          </Link>
          <Link className="button button-plain" to="/admin/categories">
            Danh mục
          </Link>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}

      {dashboard && (
        <section className="summary-grid">
          <div className="summary-item">
            <strong>{dashboard.totalUsers}</strong>
            <span>Tổng người dùng</span>
          </div>
          <div className="summary-item">
            <strong>{dashboard.totalStudents}</strong>
            <span>Sinh viên</span>
          </div>
          <div className="summary-item">
            <strong>{dashboard.totalEmployers}</strong>
            <span>Nhà tuyển dụng</span>
          </div>
          <div className="summary-item">
            <strong>{dashboard.totalJobs}</strong>
            <span>Tổng tin tuyển dụng</span>
          </div>
          <div className="summary-item">
            <strong>{dashboard.openJobs}</strong>
            <span>Tin đang tuyển</span>
          </div>
          <div className="summary-item">
            <strong>{dashboard.totalApplications}</strong>
            <span>Tổng đơn ứng tuyển</span>
          </div>
          <div className="summary-item">
            <strong>{dashboard.pendingApplications}</strong>
            <span>Đơn chờ duyệt</span>
          </div>
        </section>
      )}

      <section className="panel">
        <h2>Nhật ký thao tác gần đây</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Người thực hiện</th>
                <th>Hành động</th>
                <th>Đối tượng</th>
                <th>Địa chỉ IP</th>
                <th>Thời gian</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>{log.actorName}</td>
                  <td>{log.action}</td>
                  <td>
                    {log.entityType ?? "-"}
                    {log.entityId ? ` #${log.entityId}` : ""}
                  </td>
                  <td>{log.ipAddress ?? "-"}</td>
                  <td>{dateTimeText(log.createdAt)}</td>
                </tr>
              ))}
              {logs.length === 0 && (
                <tr>
                  <td colSpan={5}>Chưa có nhật ký nào.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

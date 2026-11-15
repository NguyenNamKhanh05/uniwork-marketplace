import { useEffect, useState } from "react";
import { api, errorMessage, unwrap } from "../api";
import { useAuth } from "../auth";
import { Pagination } from "../components/Pagination";
import type { AdminUser, PageResponse } from "../types";
import { dateText, dateTimeText, USER_STATUS } from "../format";

export function AdminUsersPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [keyword, setKeyword] = useState("");
  const [role, setRole] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const load = () => {
    unwrap<PageResponse<AdminUser>>(
      api.get("/admin/users", {
        params: {
          keyword: keyword || undefined,
          role: role || undefined,
          status: status || undefined,
          page,
          size: 10,
        },
      }),
    )
      .then((result) => {
        setUsers(result.content);
        setTotalPages(result.totalPages);
        setError("");
      })
      .catch((err) => setError(errorMessage(err)));
  };

  useEffect(() => {
    load();
  }, [keyword, role, status, page]);

  const changeStatus = async (id: number, nextStatus: string) => {
    const labels: Record<string, string> = {
      ACTIVE: "mở khóa",
      LOCKED: "khóa",
      DELETED: "xóa",
    };
    if (!window.confirm(`Bạn chắc chắn muốn ${labels[nextStatus]} tài khoản này?`)) {
      return;
    }
    let reason: string | null = null;
    if (nextStatus !== "ACTIVE") {
      const input = window.prompt("Lý do vi phạm (có thể bỏ trống):");
      if (input === null) {
        return;
      }
      reason = input.trim() || null;
    }
    setError("");
    setMessage("");
    try {
      await api.patch(`/admin/users/${id}/status`, {
        status: nextStatus,
        reason,
      });
      setMessage("Cập nhật trạng thái người dùng thành công.");
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const search = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    load();
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Quản lý người dùng</h1>
          <p>Khóa, mở khóa hoặc xóa tài khoản không hợp lệ.</p>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <section className="panel">
        <form className="filters" onSubmit={search}>
          <div>
            <label htmlFor="keyword">Email</label>
            <input
              id="keyword"
              value={keyword}
              onChange={(event) => {
                setKeyword(event.target.value);
                setPage(0);
              }}
            />
          </div>
          <div>
            <label htmlFor="role">Vai trò</label>
            <select
              id="role"
              value={role}
              onChange={(event) => {
                setRole(event.target.value);
                setPage(0);
              }}
            >
              <option value="">Tất cả</option>
              <option value="STUDENT">Sinh viên</option>
              <option value="EMPLOYER">Nhà tuyển dụng</option>
              <option value="ADMIN">Quản trị viên</option>
            </select>
          </div>
          <div>
            <label htmlFor="status">Trạng thái</label>
            <select
              id="status"
              value={status}
              onChange={(event) => {
                setStatus(event.target.value);
                setPage(0);
              }}
            >
              <option value="">Tất cả</option>
              {Object.entries(USER_STATUS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <button type="submit" className="button button-primary">
              Tìm kiếm
            </button>
          </div>
        </form>
      </section>

      <div className="table-wrap panel">
        <table>
          <thead>
            <tr>
              <th>Email</th>
              <th>Họ tên / Đơn vị</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th>Đăng nhập gần nhất</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {users.map((item) => (
              <tr key={item.id}>
                <td>{item.email}</td>
                <td>{item.fullName || "-"}</td>
                <td>{item.role}</td>
                <td>
                  <span className={`status status-${item.status.toLowerCase()}`}>
                    {USER_STATUS[item.status] ?? item.status}
                  </span>
                </td>
                <td>{dateText(item.createdAt)}</td>
                <td>{dateTimeText(item.lastLoginAt) || "-"}</td>
                <td>
                  <div className="row">
                    {item.id === user?.id && <span className="muted">Tài khoản của bạn</span>}
                    {item.id !== user?.id && item.status !== "ACTIVE" && (
                      <button
                        type="button"
                        className="button button-small"
                        onClick={() => changeStatus(item.id, "ACTIVE")}
                      >
                        Mở khóa
                      </button>
                    )}
                    {item.id !== user?.id && item.status === "ACTIVE" && (
                      <button
                        type="button"
                        className="button button-small button-danger"
                        onClick={() => changeStatus(item.id, "LOCKED")}
                      >
                        Khóa
                      </button>
                    )}
                    {item.id !== user?.id && item.status !== "DELETED" && (
                      <button
                        type="button"
                        className="button button-small button-danger"
                        onClick={() => changeStatus(item.id, "DELETED")}
                      >
                        Xóa
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {users.length === 0 && (
              <tr>
                <td colSpan={7}>Không tìm thấy người dùng nào.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  );
}

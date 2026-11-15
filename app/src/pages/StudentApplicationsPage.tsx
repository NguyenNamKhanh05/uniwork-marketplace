import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import { Pagination } from "../components/Pagination";
import type { Application, PageResponse } from "../types";
import { APPLICATION_STATUS, dateText } from "../format";

export function StudentApplicationsPage() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState("");

  useEffect(() => {
    unwrap<PageResponse<Application>>(
      api.get("/student/applications", {
        params: { status: status || undefined, page, size: 10 },
      }),
    )
      .then((result) => {
        setApplications(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(errorMessage(err)));
  }, [status, page]);

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Đơn ứng tuyển của tôi</h1>
          <p>Theo dõi trạng thái các hồ sơ đã nộp.</p>
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
            {Object.entries(APPLICATION_STATUS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}

      <div className="table-wrap panel">
        <table>
          <thead>
            <tr>
              <th>Công việc</th>
              <th>Nhà tuyển dụng</th>
              <th>Ngày nộp</th>
              <th>Trạng thái</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {applications.map((application) => (
              <tr key={application.id}>
                <td>{application.jobTitle}</td>
                <td>{application.employerName}</td>
                <td>{dateText(application.appliedAt)}</td>
                <td>
                  <span className={`status status-${application.status.toLowerCase()}`}>
                    {APPLICATION_STATUS[application.status] ?? application.status}
                  </span>
                </td>
                <td>
                  <Link className="button button-small" to={`/student/applications/${application.id}`}>
                    Chi tiết
                  </Link>
                </td>
              </tr>
            ))}
            {applications.length === 0 && (
              <tr>
                <td colSpan={5}>Chưa có đơn ứng tuyển nào.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  );
}

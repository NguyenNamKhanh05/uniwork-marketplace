import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import { Pagination } from "../components/Pagination";
import type { Job, PageResponse } from "../types";
import { dateText, JOB_STATUS, salaryText } from "../format";

const JOB_STATUS_OPTIONS = [
  "DRAFT",
  "PENDING_REVIEW",
  "REJECTED",
  "OPEN",
  "FILLED",
  "IN_PROGRESS",
  "COMPLETED",
  "CLOSED",
  "CANCELLED",
];

export function EmployerJobsPage() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const load = () => {
    unwrap<PageResponse<Job>>(
      api.get("/employer/jobs", {
        params: { status: status || undefined, page, size: 10 },
      }),
    )
      .then((result) => {
        setJobs(result.content);
        setTotalPages(result.totalPages);
        setError("");
      })
      .catch((err) => setError(errorMessage(err)));
  };

  useEffect(() => {
    load();
  }, [status, page]);

  const closeJob = async (jobId: number) => {
    if (!window.confirm("Đóng tin tuyển dụng này?")) {
      return;
    }
    setError("");
    setMessage("");
    try {
      await api.patch(`/employer/jobs/${jobId}/close`);
      setMessage("Đã đóng tin tuyển dụng.");
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const updateStatus = async (jobId: number, nextStatus: string) => {
    setError("");
    setMessage("");
    try {
      await api.patch(`/employer/jobs/${jobId}/status`, null, {
        params: { status: nextStatus },
      });
      setMessage("Cập nhật trạng thái tin tuyển dụng thành công.");
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Tin tuyển dụng của tôi</h1>
          <p>Quản lý tin đăng và theo dõi ứng viên.</p>
        </div>
        <div className="row">
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
              {JOB_STATUS_OPTIONS.map((value) => (
                <option key={value} value={value}>
                  {JOB_STATUS[value] ?? value}
                </option>
              ))}
            </select>
          </div>
          <Link className="button button-primary" to="/employer/jobs/new">
            Đăng tin mới
          </Link>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <div className="table-wrap panel">
        <table>
          <thead>
            <tr>
              <th>Tiêu đề</th>
              <th>Danh mục</th>
              <th>Thù lao</th>
              <th>Số lượng</th>
              <th>Hạn nộp</th>
              <th>Trạng thái</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {jobs.map((job) => (
              <tr key={job.id}>
                <td>{job.title}</td>
                <td>{job.categoryName}</td>
                <td>{salaryText(job)}</td>
                <td>
                  {job.positionsFilled}/{job.positionsCount}
                </td>
                <td>{dateText(job.applicationDeadline) || "Không giới hạn"}</td>
                <td>
                  <span className={`status status-${job.status.toLowerCase()}`}>
                    {JOB_STATUS[job.status] ?? job.status}
                  </span>
                </td>
                <td>
                  <div className="row">
                    <Link
                      className="button button-small"
                      to={`/employer/jobs/${job.id}`}
                    >
                      Chi tiết
                    </Link>
                    <Link
                      className="button button-small"
                      to={`/employer/jobs/${job.id}/edit`}
                    >
                      Sửa
                    </Link>
                    {job.status === "OPEN" && (
                      <button
                        type="button"
                        className="button button-small button-danger"
                        onClick={() => closeJob(job.id)}
                      >
                        Đóng
                      </button>
                    )}
                    {job.status === "OPEN" && (
                      <button
                        type="button"
                        className="button button-small"
                        onClick={() => updateStatus(job.id, "IN_PROGRESS")}
                      >
                        Bắt đầu
                      </button>
                    )}
                    {job.status === "IN_PROGRESS" && (
                      <button
                        type="button"
                        className="button button-small button-primary"
                        onClick={() => updateStatus(job.id, "COMPLETED")}
                      >
                        Hoàn thành
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {jobs.length === 0 && (
              <tr>
                <td colSpan={7}>Chưa có tin tuyển dụng nào.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  );
}

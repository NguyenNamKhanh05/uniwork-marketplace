import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import { Pagination } from "../components/Pagination";
import type { AdminJob, PageResponse } from "../types";
import { dateText, JOB_STATUS } from "../format";

export function AdminJobsPage() {
  const [jobs, setJobs] = useState<AdminJob[]>([]);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [hidden, setHidden] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const load = () => {
    unwrap<PageResponse<AdminJob>>(
      api.get("/admin/jobs", {
        params: {
          keyword: keyword || undefined,
          status: status || undefined,
          hidden: hidden === "" ? undefined : hidden === "true",
          page,
          size: 10,
        },
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
  }, [keyword, status, hidden, page]);

  const setVisibility = async (id: number, nextHidden: boolean) => {
    const question = nextHidden
      ? "Ẩn tin tuyển dụng này khỏi hệ thống?"
      : "Hiển thị lại tin tuyển dụng này?";
    if (!window.confirm(question)) {
      return;
    }
    setError("");
    setMessage("");
    try {
      await api.patch(`/admin/jobs/${id}/visibility`, {
        hidden: nextHidden,
        reason: null,
      });
      setMessage(nextHidden ? "Đã ẩn tin tuyển dụng." : "Đã hiển thị lại tin tuyển dụng.");
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Quản lý tin tuyển dụng</h1>
          <p>Ẩn hoặc hiển thị lại các tin vi phạm quy định.</p>
        </div>
        <Link className="button button-plain" to="/admin">
          Về trang quản trị
        </Link>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <section className="panel">
        <form className="filters" onSubmit={(event) => event.preventDefault()}>
          <div>
            <label htmlFor="keyword">Tiêu đề</label>
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
              {Object.entries(JOB_STATUS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="hidden">Hiển thị</label>
            <select
              id="hidden"
              value={hidden}
              onChange={(event) => {
                setHidden(event.target.value);
                setPage(0);
              }}
            >
              <option value="">Tất cả</option>
              <option value="false">Đang hiển thị</option>
              <option value="true">Đã ẩn</option>
            </select>
          </div>
        </form>
      </section>

      <div className="table-wrap panel">
        <table>
          <thead>
            <tr>
              <th>Tiêu đề</th>
              <th>Nhà tuyển dụng</th>
              <th>Danh mục</th>
              <th>Đơn ứng tuyển</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {jobs.map((job) => (
              <tr key={job.id}>
                <td>{job.title}</td>
                <td>{job.employerName || "-"}</td>
                <td>{job.categoryName || "-"}</td>
                <td>{job.applicationCount}</td>
                <td>
                  <span className={`status status-${job.status.toLowerCase()}`}>
                    {JOB_STATUS[job.status] ?? job.status}
                  </span>
                  {job.hidden && <span className="status status-rejected"> Đã ẩn</span>}
                </td>
                <td>{dateText(job.createdAt)}</td>
                <td>
                  <div className="row">
                    <Link className="button button-small" to={`/jobs/${job.id}`}>
                      Xem
                    </Link>
                    {job.hidden ? (
                      <button
                        type="button"
                        className="button button-small button-primary"
                        onClick={() => setVisibility(job.id, false)}
                      >
                        Hiện lại
                      </button>
                    ) : (
                      <button
                        type="button"
                        className="button button-small button-danger"
                        onClick={() => setVisibility(job.id, true)}
                      >
                        Ẩn
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {jobs.length === 0 && (
              <tr>
                <td colSpan={7}>Không có tin tuyển dụng nào.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  );
}

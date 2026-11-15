import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import { Pagination } from "../components/Pagination";
import type { Application, Job, PageResponse } from "../types";
import {
  APPLICATION_STATUS,
  DAYS,
  dateText,
  dateTimeText,
  JOB_STATUS,
  salaryText,
  timeText,
  WORK_MODES,
} from "../format";

export function EmployerJobDetailPage() {
  const { id } = useParams();
  const [job, setJob] = useState<Job | null>(null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadJob = () => {
    unwrap<Job>(api.get(`/employer/jobs/${id}`))
      .then(setJob)
      .catch((err) => setError(errorMessage(err)));
  };

  const loadApplications = () => {
    unwrap<PageResponse<Application>>(
      api.get(`/employer/jobs/${id}/applications`, {
        params: { status: status || undefined, page, size: 10 },
      }),
    )
      .then((result) => {
        setApplications(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(errorMessage(err)));
  };

  useEffect(() => {
    loadJob();
  }, [id]);

  useEffect(() => {
    loadApplications();
  }, [id, status, page]);

  const updateStatus = async (applicationId: number, nextStatus: string) => {
    setError("");
    setMessage("");
    try {
      await api.patch(`/employer/applications/${applicationId}/status`, {
        status: nextStatus,
        reason: null,
      });
      setMessage("Cập nhật trạng thái ứng viên thành công.");
      loadApplications();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  if (!job) {
    return error ? (
      <p className="notice notice-error">{error}</p>
    ) : (
      <p className="notice">Đang tải...</p>
    );
  }

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>{job.title}</h1>
          <p>
            {job.categoryName} · {WORK_MODES[job.workMode] ?? job.workMode} ·{" "}
            {salaryText(job)}
          </p>
        </div>
        <div className="row">
          <span className={`status status-${job.status.toLowerCase()}`}>
            {JOB_STATUS[job.status] ?? job.status}
          </span>
          <Link className="button button-plain" to={`/employer/jobs/${job.id}/edit`}>
            Sửa tin
          </Link>
          <Link className="button button-plain" to="/employer/jobs">
            Danh sách tin
          </Link>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <section className="panel">
        <h2>Thông tin tin tuyển dụng</h2>
        <div className="form-grid">
          <p>
            <strong>Địa điểm:</strong>{" "}
            {[job.addressDetail, job.districtName, job.provinceName]
              .filter(Boolean)
              .join(", ") || "Không xác định"}
          </p>
          <p>
            <strong>Số lượng:</strong> {job.positionsFilled}/{job.positionsCount}
          </p>
          <p>
            <strong>Hạn nộp:</strong> {dateText(job.applicationDeadline) || "Không giới hạn"}
          </p>
          <p>
            <strong>Bắt đầu:</strong> {dateText(job.startDate) || "Thỏa thuận"}
          </p>
          <p>
            <strong>Kết thúc:</strong> {dateText(job.endDate) || "Thỏa thuận"}
          </p>
          <p>
            <strong>Lượt xem:</strong> {job.viewCount}
          </p>
        </div>
        {job.skills.length > 0 && (
          <div className="tags">
            {job.skills.map((skill) => (
              <span key={skill.id} className="tag">
                {skill.name}
              </span>
            ))}
          </div>
        )}
        {job.schedules.length > 0 && (
          <>
            <h3>Lịch làm việc</h3>
            <ul>
              {job.schedules.map((schedule, index) => (
                <li key={index}>
                  {DAYS[schedule.dayOfWeek] ?? schedule.dayOfWeek}:{" "}
                  {timeText(schedule.startTime)} - {timeText(schedule.endTime)}
                </li>
              ))}
            </ul>
          </>
        )}
      </section>

      <section className="panel">
        <div className="panel-title">
          <h2>Ứng viên</h2>
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

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Ứng viên</th>
                <th>Trường</th>
                <th>Ngày nộp</th>
                <th>Trạng thái</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {applications.map((application) => (
                <tr key={application.id}>
                  <td>{application.studentName}</td>
                  <td>{application.university || "Chưa cập nhật"}</td>
                  <td>{dateTimeText(application.appliedAt)}</td>
                  <td>
                    <span className={`status status-${application.status.toLowerCase()}`}>
                      {APPLICATION_STATUS[application.status] ?? application.status}
                    </span>
                  </td>
                  <td>
                    <div className="row">
                      <Link
                        className="button button-small"
                        to={`/employer/applications/${application.id}`}
                      >
                        Xem
                      </Link>
                      {["PENDING", "VIEWED", "SHORTLISTED"].includes(application.status) && (
                        <>
                          <button
                            type="button"
                            className="button button-small button-primary"
                            onClick={() => updateStatus(application.id, "ACCEPTED")}
                          >
                            Nhận
                          </button>
                          <button
                            type="button"
                            className="button button-small button-danger"
                            onClick={() => updateStatus(application.id, "REJECTED")}
                          >
                            Từ chối
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {applications.length === 0 && (
                <tr>
                  <td colSpan={5}>Chưa có ứng viên nào.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <Pagination page={page} totalPages={totalPages} onChange={setPage} />
      </section>
    </div>
  );
}

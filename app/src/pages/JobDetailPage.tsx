import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import { useAuth } from "../auth";
import type { Job, Resume } from "../types";
import {
  dateText,
  DAYS,
  JOB_STATUS,
  money,
  salaryText,
  timeText,
  WORK_MODES,
} from "../format";

export function JobDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [job, setJob] = useState<Job | null>(null);
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [coverLetter, setCoverLetter] = useState("");
  const [cvUrl, setCvUrl] = useState("");
  const [resumeId, setResumeId] = useState("");
  const [expectedSalary, setExpectedSalary] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [applying, setApplying] = useState(false);

  const loadJob = () => {
    unwrap<Job>(api.get(`/jobs/${id}`))
      .then(setJob)
      .catch((err) => setError(errorMessage(err)));
  };

  useEffect(() => {
    loadJob();
  }, [id]);

  useEffect(() => {
    if (user?.role === "STUDENT") {
      unwrap<Resume[]>(api.get("/student/resumes"))
        .then(setResumes)
        .catch(() => undefined);
    }
  }, [user]);

  const apply = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!user) {
      navigate("/login", { state: { from: `/jobs/${id}` } });
      return;
    }
    setApplying(true);
    setError("");
    setMessage("");
    try {
      await api.post(`/jobs/${id}/applications`, {
        coverLetter: coverLetter || null,
        cvUrl: cvUrl || null,
        resumeId: resumeId ? Number(resumeId) : null,
        expectedSalary: expectedSalary ? Number(expectedSalary) : null,
      });
      setMessage("Nộp đơn ứng tuyển thành công.");
      setCoverLetter("");
      setCvUrl("");
      setExpectedSalary("");
      setResumeId("");
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setApplying(false);
    }
  };

  const saveJob = async () => {
    if (!user) {
      navigate("/login", { state: { from: `/jobs/${id}` } });
      return;
    }
    try {
      await api.post(`/student/saved-jobs/${id}`);
      setMessage("Đã lưu việc làm vào danh sách quan tâm.");
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  if (!job) {
    return error ? <p className="notice notice-error">{error}</p> : <p className="notice">Đang tải...</p>;
  }

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>{job.title}</h1>
          <p>
            {job.employerName} · {job.categoryName} · {WORK_MODES[job.workMode] ?? job.workMode}
          </p>
        </div>
        <span className={`status status-${job.status.toLowerCase()}`}>
          {JOB_STATUS[job.status] ?? job.status}
        </span>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <div className="two-column">
        <div className="stack">
          <section className="panel">
            <h2>Mô tả công việc</h2>
            <p className="pre-line">{job.description}</p>
            {job.requirements && (
              <>
                <h3>Yêu cầu</h3>
                <p className="pre-line">{job.requirements}</p>
              </>
            )}
            {job.benefits && (
              <>
                <h3>Quyền lợi</h3>
                <p className="pre-line">{job.benefits}</p>
              </>
            )}
          </section>

          {job.skills.length > 0 && (
            <section className="panel">
              <h2>Kỹ năng</h2>
              <div className="tags">
                {job.skills.map((skill) => (
                  <span key={skill.id} className="tag">
                    {skill.name}
                  </span>
                ))}
              </div>
            </section>
          )}

          {job.schedules.length > 0 && (
            <section className="panel">
              <h2>Lịch làm việc</h2>
              <ul>
                {job.schedules.map((schedule, index) => (
                  <li key={index}>
                    {DAYS[schedule.dayOfWeek] ?? schedule.dayOfWeek}:{" "}
                    {timeText(schedule.startTime)} - {timeText(schedule.endTime)}
                  </li>
                ))}
              </ul>
            </section>
          )}

          {user?.role === "STUDENT" && job.status === "OPEN" && (
            <section className="panel">
              <h2>Nộp đơn ứng tuyển</h2>
              <form onSubmit={apply}>
                <div className="form-grid">
                  <div className="full">
                    <label htmlFor="coverLetter">Thư giới thiệu</label>
                    <textarea
                      id="coverLetter"
                      value={coverLetter}
                      onChange={(event) => setCoverLetter(event.target.value)}
                      placeholder="Giới thiệu ngắn gọn về bản thân và lý do phù hợp với công việc"
                    />
                  </div>
                  <div>
                    <label htmlFor="resumeId">Chọn CV đã lưu</label>
                    <select
                      id="resumeId"
                      value={resumeId}
                      onChange={(event) => setResumeId(event.target.value)}
                    >
                      <option value="">Không chọn</option>
                      {resumes.map((resume) => (
                        <option key={resume.id} value={resume.id}>
                          {resume.title}
                          {resume.isDefault ? " (mặc định)" : ""}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label htmlFor="cvUrl">Hoặc dán liên kết CV</label>
                    <input
                      id="cvUrl"
                      value={cvUrl}
                      onChange={(event) => setCvUrl(event.target.value)}
                      placeholder="https://..."
                    />
                  </div>
                  <div>
                    <label htmlFor="expectedSalary">Mức lương mong muốn (VND)</label>
                    <input
                      id="expectedSalary"
                      type="number"
                      min="0"
                      value={expectedSalary}
                      onChange={(event) => setExpectedSalary(event.target.value)}
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="button button-primary" disabled={applying}>
                    {applying ? "Đang gửi..." : "Nộp đơn"}
                  </button>
                </div>
              </form>
            </section>
          )}

          {user?.role === "EMPLOYER" && user.id === job.employerId && (
            <section className="panel">
              <h2>Quản lý tin này</h2>
              <div className="row">
                <Link className="button button-plain" to={`/employer/jobs/${job.id}/edit`}>
                  Sửa tin
                </Link>
                <Link className="button button-plain" to={`/employer/jobs/${job.id}`}>
                  Xem ứng viên
                </Link>
              </div>
            </section>
          )}
        </div>

        <aside className="stack">
          <section className="panel">
            <h2>Thông tin chung</h2>
            <p className="job-salary">{salaryText(job)}</p>
            <p>
              <strong>Địa điểm:</strong>{" "}
              {[job.addressDetail, job.districtName, job.provinceName]
                .filter(Boolean)
                .join(", ") || "Không xác định"}
            </p>
            <p>
              <strong>Số lượng:</strong> {job.positionsCount}
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
            {user?.role === "STUDENT" && (
              <button type="button" className="button button-plain" onClick={saveJob}>
                Lưu việc làm
              </button>
            )}
          </section>

          <section className="panel">
            <h2>Nhà tuyển dụng</h2>
            <p>
              <strong>{job.employerName}</strong>
              {job.employerVerified ? " (đã xác minh)" : ""}
            </p>
            {job.employerRating !== null && (
              <p className="muted">
                Đánh giá: {job.employerRating}/5 ({job.employerRatingCount ?? 0} lượt)
              </p>
            )}
            {job.employerDescription && <p>{job.employerDescription}</p>}
            <p className="muted">
              Lương tối thiểu: {money(job.salaryMin)} · Tối đa: {money(job.salaryMax)}
            </p>
          </section>
        </aside>
      </div>
    </div>
  );
}

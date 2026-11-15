import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import type { Application } from "../types";
import { APPLICATION_STATUS, dateTimeText, money, WORK_MODES } from "../format";

export function ApplicationDetailPage() {
  const { id } = useParams();
  const location = useLocation();
  const isEmployer = location.pathname.startsWith("/employer");
  const [application, setApplication] = useState<Application | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [reason, setReason] = useState("");
  const [reviewRating, setReviewRating] = useState("5");
  const [reviewComment, setReviewComment] = useState("");
  const [showReview, setShowReview] = useState(false);

  const load = () => {
    unwrap<Application>(
      api.get(`${isEmployer ? "/employer" : "/student"}/applications/${id}`),
    )
      .then(setApplication)
      .catch((err) => setError(errorMessage(err)));
  };

  useEffect(() => {
    load();
  }, [id, isEmployer]);

  const withdraw = async () => {
    if (!window.confirm("Rút đơn ứng tuyển này?")) {
      return;
    }
    try {
      await api.patch(`/student/applications/${id}/withdraw`);
      setMessage("Đã rút đơn ứng tuyển.");
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const updateStatus = async (status: string) => {
    try {
      await api.patch(`/employer/applications/${id}/status`, {
        status,
        reason: reason || null,
      });
      setMessage("Cập nhật trạng thái thành công.");
      setReason("");
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const submitReview = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      await api.post("/reviews", {
        applicationId: Number(id),
        rating: Number(reviewRating),
        comment: reviewComment || null,
      });
      setMessage("Gửi đánh giá thành công.");
      setShowReview(false);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  if (!application) {
    return error ? <p className="notice notice-error">{error}</p> : <p className="notice">Đang tải...</p>;
  }

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Đơn ứng tuyển: {application.jobTitle}</h1>
          <p>
            {isEmployer ? application.studentName : application.employerName} ·{" "}
            {dateTimeText(application.appliedAt)}
          </p>
        </div>
        <span className={`status status-${application.status.toLowerCase()}`}>
          {APPLICATION_STATUS[application.status] ?? application.status}
        </span>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <div className="two-column">
        <div className="stack">
          <section className="panel">
            <h2>Thông tin ứng viên</h2>
            <p>
              <strong>Họ tên:</strong> {application.studentName}
            </p>
            <p>
              <strong>Email:</strong> {application.studentEmail}
            </p>
            <p>
              <strong>Điện thoại:</strong> {application.studentPhone || "Chưa cập nhật"}
            </p>
            <p>
              <strong>Trường:</strong> {application.university || "Chưa cập nhật"}
            </p>
            <p>
              <strong>Chuyên ngành:</strong> {application.major || "Chưa cập nhật"} ·{" "}
              <strong>Năm:</strong> {application.yearOfStudy || "Chưa cập nhật"}
            </p>
            {application.studentBio && <p className="pre-line">{application.studentBio}</p>}
          </section>

          <section className="panel">
            <h2>Hồ sơ ứng tuyển</h2>
            <p>
              <strong>CV:</strong>{" "}
              {application.cvUrl ? (
                <a href={application.cvUrl} target="_blank" rel="noreferrer">
                  {application.cvTitle || "Xem CV"}
                </a>
              ) : (
                "Không đính kèm"
              )}
            </p>
            <p>
              <strong>Lương mong muốn:</strong> {money(application.expectedSalary)}
            </p>
            <p className="pre-line">
              <strong>Thư giới thiệu:</strong>{" "}
              {application.coverLetter || "Không có"}
            </p>
          </section>

          <section className="panel">
            <h2>Lịch sử xử lý</h2>
            <ul className="timeline">
              {application.history.map((item, index) => (
                <li key={index}>
                  <p>
                    <strong>{APPLICATION_STATUS[item.toStatus] ?? item.toStatus}</strong>{" "}
                    <span className="muted">({dateTimeText(item.changedAt)})</span>
                  </p>
                  <p className="small muted">
                    {item.changedByName}
                    {item.note ? `: ${item.note}` : ""}
                  </p>
                </li>
              ))}
              {application.history.length === 0 && <li>Chưa có lịch sử.</li>}
            </ul>
          </section>
        </div>

        <aside className="stack">
          <section className="panel">
            <h2>Công việc</h2>
            <p>
              <strong>{application.jobTitle}</strong>
            </p>
            <p>
              <strong>Địa điểm:</strong>{" "}
              {[application.addressDetail, application.provinceName].filter(Boolean).join(", ") ||
                "Không xác định"}
            </p>
            <p>
              <strong>Hình thức:</strong> {WORK_MODES[application.workMode] ?? application.workMode}
            </p>
            <p>
              <strong>Lương:</strong> {money(application.salaryMin)} - {money(application.salaryMax)}
            </p>
            <Link className="button button-plain" to={`/jobs/${application.jobId}`}>
              Xem tin tuyển dụng
            </Link>
          </section>

          {!isEmployer && application.status === "PENDING" && (
            <section className="panel">
              <h2>Thao tác</h2>
              <button type="button" className="button button-danger" onClick={withdraw}>
                Rút đơn ứng tuyển
              </button>
            </section>
          )}

          {isEmployer &&
            ["PENDING", "VIEWED", "SHORTLISTED"].includes(application.status) && (
              <section className="panel">
                <h2>Xử lý ứng viên</h2>
                <div style={{ marginBottom: 10 }}>
                  <label htmlFor="reason">Ghi chú / lý do</label>
                  <textarea
                    id="reason"
                    value={reason}
                    onChange={(event) => setReason(event.target.value)}
                  />
                </div>
                <div className="row">
                  <button type="button" className="button" onClick={() => updateStatus("VIEWED")}>
                    Đã xem
                  </button>
                  <button type="button" className="button" onClick={() => updateStatus("SHORTLISTED")}>
                    Quan tâm
                  </button>
                  <button type="button" className="button button-primary" onClick={() => updateStatus("ACCEPTED")}>
                    Chấp nhận
                  </button>
                  <button type="button" className="button button-danger" onClick={() => updateStatus("REJECTED")}>
                    Từ chối
                  </button>
                </div>
              </section>
            )}

          {application.assignmentStatus === "COMPLETED" && !application.reviewed && (
            <section className="panel">
              <h2>Đánh giá</h2>
              {showReview ? (
                <form onSubmit={submitReview}>
                  <div style={{ marginBottom: 10 }}>
                    <label htmlFor="rating">Điểm (1-5)</label>
                    <select
                      id="rating"
                      value={reviewRating}
                      onChange={(event) => setReviewRating(event.target.value)}
                    >
                      {[5, 4, 3, 2, 1].map((value) => (
                        <option key={value} value={value}>
                          {value}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div style={{ marginBottom: 10 }}>
                    <label htmlFor="comment">Nhận xét</label>
                    <textarea
                      id="comment"
                      value={reviewComment}
                      onChange={(event) => setReviewComment(event.target.value)}
                    />
                  </div>
                  <div className="form-actions">
                    <button type="submit" className="button button-primary">
                      Gửi đánh giá
                    </button>
                    <button type="button" className="button button-plain" onClick={() => setShowReview(false)}>
                      Hủy
                    </button>
                  </div>
                </form>
              ) : (
                <button type="button" className="button button-primary" onClick={() => setShowReview(true)}>
                  Viết đánh giá
                </button>
              )}
            </section>
          )}
        </aside>
      </div>
    </div>
  );
}

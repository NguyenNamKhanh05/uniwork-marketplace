import { useEffect, useState } from "react";
import { api, errorMessage, unwrap } from "../api";
import { useAuth } from "../auth";
import { Pagination } from "../components/Pagination";
import type { PageResponse, PendingReview, Review } from "../types";
import { dateTimeText } from "../format";

export function ReviewsPage() {
  const { user } = useAuth();
  const [pending, setPending] = useState<PendingReview[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [active, setActive] = useState<PendingReview | null>(null);
  const [rating, setRating] = useState("5");
  const [comment, setComment] = useState("");

  const loadPending = () => {
    unwrap<PendingReview[]>(api.get("/reviews/pending"))
      .then(setPending)
      .catch((err) => setError(errorMessage(err)));
  };

  const loadReviews = () => {
    if (!user) {
      return;
    }
    unwrap<PageResponse<Review>>(
      api.get("/reviews", { params: { userId: user.id, page, size: 10 } }),
    )
      .then((result) => {
        setReviews(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(errorMessage(err)));
  };

  useEffect(() => {
    loadPending();
  }, []);

  useEffect(() => {
    loadReviews();
  }, [user, page]);

  const submitReview = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!active) {
      return;
    }
    setError("");
    setMessage("");
    try {
      await api.post("/reviews", {
        applicationId: active.applicationId,
        rating: Number(rating),
        comment: comment || null,
      });
      setMessage("Gửi đánh giá thành công.");
      setActive(null);
      setRating("5");
      setComment("");
      loadPending();
      loadReviews();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Đánh giá</h1>
          <p>Đánh giá đối tác sau khi công việc hoàn thành và xem đánh giá đã nhận.</p>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <section className="panel">
        <h2>Chờ đánh giá</h2>
        {pending.map((item) => (
          <div key={item.assignmentId} className="notification-item">
            <div className="panel-title">
              <strong>{item.jobTitle}</strong>
              <span className="small muted">
                Đối tác: {item.counterpartName}
              </span>
            </div>
            {active?.assignmentId === item.assignmentId ? (
              <form onSubmit={submitReview}>
                <div className="form-grid">
                  <div>
                    <label htmlFor="rating">Điểm đánh giá</label>
                    <select
                      id="rating"
                      value={rating}
                      onChange={(event) => setRating(event.target.value)}
                    >
                      {[5, 4, 3, 2, 1].map((value) => (
                        <option key={value} value={value}>
                          {value}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="full">
                    <label htmlFor="comment">Nhận xét</label>
                    <textarea
                      id="comment"
                      value={comment}
                      onChange={(event) => setComment(event.target.value)}
                      maxLength={500}
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="button button-primary">
                    Gửi đánh giá
                  </button>
                  <button
                    type="button"
                    className="button button-plain"
                    onClick={() => setActive(null)}
                  >
                    Hủy
                  </button>
                </div>
              </form>
            ) : (
              <button
                type="button"
                className="button button-small"
                onClick={() => {
                  setActive(item);
                  setRating("5");
                  setComment("");
                }}
              >
                Viết đánh giá
              </button>
            )}
          </div>
        ))}
        {pending.length === 0 && (
          <p className="muted">Không có đánh giá nào đang chờ.</p>
        )}
      </section>

      <section className="panel">
        <h2>Đánh giá đã nhận</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Công việc</th>
                <th>Người đánh giá</th>
                <th>Điểm</th>
                <th>Nhận xét</th>
                <th>Thời gian</th>
              </tr>
            </thead>
            <tbody>
              {reviews.map((review) => (
                <tr key={review.id}>
                  <td>{review.jobTitle}</td>
                  <td>{review.reviewerName}</td>
                  <td>{review.rating}/5</td>
                  <td>{review.comment || "Không có"}</td>
                  <td>{dateTimeText(review.createdAt)}</td>
                </tr>
              ))}
              {reviews.length === 0 && (
                <tr>
                  <td colSpan={5}>Chưa có đánh giá nào.</td>
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

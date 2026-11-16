import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import { useAuth } from "../auth";
import { Pagination } from "../components/Pagination";
import type { Notification, PageResponse } from "../types";
import { dateTimeText } from "../format";

export function NotificationsPage() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [unread, setUnread] = useState(0);
  const [error, setError] = useState("");

  const load = () => {
    unwrap<PageResponse<Notification>>(
      api.get("/notifications", {
        params: { unreadOnly, page, size: 10 },
      }),
    )
      .then((result) => {
        setNotifications(result.content);
        setTotalPages(result.totalPages);
        setError("");
      })
      .catch((err) => setError(errorMessage(err)));
    unwrap<{ unread: number }>(api.get("/notifications/unread-count"))
      .then((result) => setUnread(result.unread))
      .catch(() => undefined);
  };

  useEffect(() => {
    load();
  }, [unreadOnly, page]);

  const markRead = async (id: number) => {
    try {
      await api.patch(`/notifications/${id}/read`);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const markAllRead = async () => {
    try {
      await api.patch("/notifications/read-all");
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const targetLink = (notification: Notification): string | null => {
    if (!notification.refType || !notification.refId) {
      return null;
    }
    if (notification.refType === "JOB") {
      return `/jobs/${notification.refId}`;
    }
    if (notification.refType === "JOB_POST" && user?.role === "EMPLOYER") {
      return `/employer/jobs/${notification.refId}`;
    }
    if (notification.refType === "APPLICATION") {
      return user?.role === "EMPLOYER"
        ? `/employer/applications/${notification.refId}`
        : `/student/applications/${notification.refId}`;
    }
    return null;
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Thông báo</h1>
          <p>
            {unread > 0 ? `Bạn có ${unread} thông báo chưa đọc.` : "Không có thông báo chưa đọc."}
          </p>
        </div>
        <div className="row">
          <label>
            <input
              type="checkbox"
              checked={unreadOnly}
              onChange={(event) => {
                setUnreadOnly(event.target.checked);
                setPage(0);
              }}
            />{" "}
            Chỉ hiện chưa đọc
          </label>
          <button
            type="button"
            className="button button-plain"
            onClick={markAllRead}
            disabled={unread === 0}
          >
            Đánh dấu tất cả đã đọc
          </button>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}

      <section className="panel">
        {notifications.map((notification) => {
          const link = targetLink(notification);
          return (
            <div
              key={notification.id}
              className={`notification-item${notification.read ? "" : " unread"}`}
            >
              <div className="panel-title">
                <strong>{notification.title}</strong>
                <span className="small muted">{dateTimeText(notification.createdAt)}</span>
              </div>
              {notification.content && <p>{notification.content}</p>}
              <div className="row">
                {link && (
                  <Link className="button button-small" to={link}>
                    Xem chi tiết
                  </Link>
                )}
                {!notification.read && (
                  <button
                    type="button"
                    className="button button-small button-plain"
                    onClick={() => markRead(notification.id)}
                  >
                    Đánh dấu đã đọc
                  </button>
                )}
              </div>
            </div>
          );
        })}
        {notifications.length === 0 && (
          <p className="muted">Không có thông báo nào.</p>
        )}
      </section>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  );
}

import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import { JobCard } from "../components/JobCard";
import type { Category, Job, PageResponse } from "../types";

export function HomePage() {
  const navigate = useNavigate();
  const [jobs, setJobs] = useState<Job[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [keyword, setKeyword] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([
      unwrap<PageResponse<Job>>(api.get("/jobs", { params: { size: 6 } })),
      unwrap<Category[]>(api.get("/categories")),
    ])
      .then(([jobPage, categoryList]) => {
        setJobs(jobPage.content);
        setCategories(categoryList);
      })
      .catch((err) => setError(errorMessage(err)));
  }, []);

  const search = (event: React.FormEvent) => {
    event.preventDefault();
    const params = new URLSearchParams();
    if (keyword.trim()) {
      params.set("keyword", keyword.trim());
    }
    navigate(`/jobs?${params.toString()}`);
  };

  return (
    <div className="stack">
      <section className="panel hero">
        <h1>Việc làm bán thời gian cho sinh viên</h1>
        <p className="muted">
          Tìm việc làm phù hợp với lịch học, ứng tuyển trực tuyến và theo dõi trạng thái
          hồ sơ của bạn.
        </p>
        <form className="row" onSubmit={search}>
          <input
            style={{ maxWidth: 420 }}
            placeholder="Nhập tên công việc hoặc kỹ năng"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button type="submit" className="button button-primary">
            Tìm việc
          </button>
          <Link to="/jobs" className="button button-plain">
            Xem tất cả việc làm
          </Link>
        </form>
      </section>

      <section className="panel">
        <div className="panel-title">
          <h2>Danh mục nghề nghiệp</h2>
        </div>
        <div className="row">
          {categories.map((category) => (
            <Link
              key={category.id}
              className="tag"
              to={`/jobs?categoryId=${category.id}`}
            >
              {category.name}
            </Link>
          ))}
        </div>
      </section>

      {error && <p className="notice notice-error">{error}</p>}

      <section>
        <div className="panel-title">
          <h2>Việc làm mới nhất</h2>
          <Link to="/jobs">Xem tất cả</Link>
        </div>
        <div className="job-list">
          {jobs.map((job) => (
            <JobCard key={job.id} job={job} />
          ))}
          {jobs.length === 0 && !error && (
            <p className="muted">Chưa có tin tuyển dụng nào.</p>
          )}
        </div>
      </section>

      <section className="panel">
        <h2>Dành cho nhà tuyển dụng</h2>
        <p className="muted">
          Đăng tin tuyển dụng, quản lý ứng viên và đánh giá sinh viên sau khi hoàn thành
          công việc.
        </p>
        <Link className="button button-primary" to="/register">
          Tạo tài khoản nhà tuyển dụng
        </Link>
      </section>
    </div>
  );
}

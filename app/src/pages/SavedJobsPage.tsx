import { useEffect, useState } from "react";
import { api, errorMessage, unwrap } from "../api";
import { JobCard } from "../components/JobCard";
import { Pagination } from "../components/Pagination";
import type { Job, PageResponse } from "../types";

export function SavedJobsPage() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [reload, setReload] = useState(0);

  useEffect(() => {
    unwrap<PageResponse<Job>>(
      api.get("/student/saved-jobs", { params: { page, size: 10 } }),
    )
      .then((result) => {
        setJobs(result.content);
        setTotalPages(result.totalPages);
        setError("");
      })
      .catch((err) => setError(errorMessage(err)));
  }, [page, reload]);

  const unsave = async (jobId: number) => {
    setError("");
    setMessage("");
    try {
      await api.delete(`/student/saved-jobs/${jobId}`);
      setMessage("Đã bỏ lưu việc làm.");
      setReload((value) => value + 1);
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Việc làm đã lưu</h1>
          <p>Danh sách các tin tuyển dụng bạn đang quan tâm.</p>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <div className="job-list">
        {jobs.map((job) => (
          <JobCard
            key={job.id}
            job={job}
            action={
              <button
                type="button"
                className="button button-small button-danger"
                onClick={() => unsave(job.id)}
              >
                Bỏ lưu
              </button>
            }
          />
        ))}
        {jobs.length === 0 && (
          <p className="notice">Bạn chưa lưu việc làm nào.</p>
        )}
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  );
}

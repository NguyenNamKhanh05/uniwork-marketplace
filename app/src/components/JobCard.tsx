import type { ReactNode } from "react";
import { Link } from "react-router";
import type { Job } from "../types";
import { dateText, salaryText, WORK_MODES, JOB_STATUS } from "../format";

export function JobCard({ job, action }: { job: Job; action?: ReactNode }) {
  return (
    <article className="job-card">
      <h3>
        <Link to={`/jobs/${job.id}`}>{job.title}</Link>
      </h3>
      <div className="job-meta">
        <span>{job.employerName}</span>
        <span>{job.categoryName}</span>
        <span>{WORK_MODES[job.workMode] ?? job.workMode}</span>
        <span>{job.provinceName ?? "Không xác định"}</span>
      </div>
      <p className="job-salary">{salaryText(job)}</p>
      <div className="job-meta">
        <span>Số lượng: {job.positionsCount}</span>
        <span>Hạn nộp: {dateText(job.applicationDeadline) || "Không giới hạn"}</span>
        <span className={`status status-${job.status.toLowerCase()}`}>
          {JOB_STATUS[job.status] ?? job.status}
        </span>
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
      <div className="job-actions">
        <Link className="button button-plain button-small" to={`/jobs/${job.id}`}>
          Xem chi tiết
        </Link>
        {action}
      </div>
    </article>
  );
}

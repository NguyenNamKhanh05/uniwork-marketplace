import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import { JobCard } from "../components/JobCard";
import { Pagination } from "../components/Pagination";
import type { Category, Job, PageResponse, Province } from "../types";

const WORK_MODE_OPTIONS = [
  { value: "", label: "Tất cả hình thức" },
  { value: "ONSITE", label: "Tại chỗ" },
  { value: "REMOTE", label: "Từ xa" },
  { value: "HYBRID", label: "Kết hợp" },
];

export function JobListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [jobs, setJobs] = useState<Job[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [provinces, setProvinces] = useState<Province[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const page = Number(searchParams.get("page") ?? "0");
  const query = useMemo(() => {
    const params: Record<string, string> = {};
    ["keyword", "categoryId", "provinceId", "workMode"].forEach((key) => {
      const value = searchParams.get(key);
      if (value) {
        params[key] = value;
      }
    });
    params.page = String(page);
    params.size = "10";
    return params;
  }, [searchParams, page]);

  useEffect(() => {
    Promise.all([
      unwrap<Category[]>(api.get("/categories")),
      unwrap<Province[]>(api.get("/public/provinces")),
    ])
      .then(([categoryList, provinceList]) => {
        setCategories(categoryList);
        setProvinces(provinceList);
      })
      .catch(() => undefined);
  }, []);

  useEffect(() => {
    setLoading(true);
    unwrap<PageResponse<Job>>(api.get("/jobs", { params: query }))
      .then((result) => {
        setJobs(result.content);
        setTotalPages(result.totalPages);
        setError("");
      })
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, [query]);

  const update = (key: string, value: string) => {
    const next = new URLSearchParams(searchParams);
    if (value) {
      next.set(key, value);
    } else {
      next.delete(key);
    }
    next.delete("page");
    setSearchParams(next);
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Tìm việc làm</h1>
          <p>Lọc theo danh mục, địa điểm và hình thức làm việc.</p>
        </div>
      </div>

      <section className="panel">
        <div className="filters">
          <div>
            <label htmlFor="keyword">Từ khóa</label>
            <input
              id="keyword"
              value={searchParams.get("keyword") ?? ""}
              onChange={(event) => update("keyword", event.target.value)}
              placeholder="Tên công việc"
            />
          </div>
          <div>
            <label htmlFor="categoryId">Danh mục</label>
            <select
              id="categoryId"
              value={searchParams.get("categoryId") ?? ""}
              onChange={(event) => update("categoryId", event.target.value)}
            >
              <option value="">Tất cả danh mục</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="provinceId">Địa điểm</label>
            <select
              id="provinceId"
              value={searchParams.get("provinceId") ?? ""}
              onChange={(event) => update("provinceId", event.target.value)}
            >
              <option value="">Tất cả địa điểm</option>
              {provinces.map((province) => (
                <option key={province.id} value={province.id}>
                  {province.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="workMode">Hình thức</label>
            <select
              id="workMode"
              value={searchParams.get("workMode") ?? ""}
              onChange={(event) => update("workMode", event.target.value)}
            >
              {WORK_MODE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </section>

      {error && <p className="notice notice-error">{error}</p>}
      {loading && <p className="notice">Đang tải danh sách việc làm...</p>}

      <div className="job-list">
        {jobs.map((job) => (
          <JobCard key={job.id} job={job} />
        ))}
        {!loading && jobs.length === 0 && (
          <p className="notice">Không tìm thấy việc làm phù hợp.</p>
        )}
      </div>

      <Pagination
        page={page}
        totalPages={totalPages}
        onChange={(nextPage) => {
          const next = new URLSearchParams(searchParams);
          next.set("page", String(nextPage));
          setSearchParams(next);
        }}
      />
    </div>
  );
}

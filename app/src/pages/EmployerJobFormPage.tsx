import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import type { Category, District, Job, Province, Skill } from "../types";
import { DAYS } from "../format";

interface ScheduleRow {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

const SALARY_TYPE_OPTIONS = [
  { value: "HOURLY", label: "Theo giờ" },
  { value: "DAILY", label: "Theo ngày" },
  { value: "WEEKLY", label: "Theo tuần" },
  { value: "MONTHLY", label: "Theo tháng" },
  { value: "PER_SHIFT", label: "Theo ca" },
  { value: "PER_TASK", label: "Theo sản phẩm" },
  { value: "PER_JOB", label: "Theo công việc" },
  { value: "NEGOTIABLE", label: "Thỏa thuận" },
];

const EMPTY_FORM = {
  title: "",
  categoryId: "",
  description: "",
  requirements: "",
  benefits: "",
  workMode: "ONSITE",
  provinceId: "",
  districtId: "",
  addressDetail: "",
  salaryType: "HOURLY",
  salaryMin: "",
  salaryMax: "",
  positionsCount: "1",
  startDate: "",
  endDate: "",
  applicationDeadline: "",
};

export function EmployerJobFormPage() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const [form, setForm] = useState<Record<string, string>>({ ...EMPTY_FORM });
  const [skillIds, setSkillIds] = useState<number[]>([]);
  const [schedules, setSchedules] = useState<ScheduleRow[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [provinces, setProvinces] = useState<Province[]>([]);
  const [districts, setDistricts] = useState<District[]>([]);
  const [skills, setSkills] = useState<Skill[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    unwrap<Category[]>(api.get("/categories")).then(setCategories).catch(() => undefined);
    unwrap<Province[]>(api.get("/public/provinces")).then(setProvinces).catch(() => undefined);
    unwrap<Skill[]>(api.get("/public/skills")).then(setSkills).catch(() => undefined);
  }, []);

  useEffect(() => {
    if (!id) {
      return;
    }
    unwrap<Job>(api.get(`/employer/jobs/${id}`))
      .then((job) => {
        setForm({
          title: job.title,
          categoryId: job.categoryId ? String(job.categoryId) : "",
          description: job.description ?? "",
          requirements: job.requirements ?? "",
          benefits: job.benefits ?? "",
          workMode: job.workMode,
          provinceId: job.provinceId ? String(job.provinceId) : "",
          districtId: job.districtId ? String(job.districtId) : "",
          addressDetail: job.addressDetail ?? "",
          salaryType: job.salaryType,
          salaryMin: job.salaryMin !== null ? String(job.salaryMin) : "",
          salaryMax: job.salaryMax !== null ? String(job.salaryMax) : "",
          positionsCount: String(job.positionsCount),
          startDate: job.startDate ?? "",
          endDate: job.endDate ?? "",
          applicationDeadline: job.applicationDeadline
            ? job.applicationDeadline.slice(0, 16)
            : "",
        });
        setSkillIds(job.skills.map((skill) => skill.id));
        setSchedules(
          job.schedules.map((item) => ({
            dayOfWeek: String(item.dayOfWeek),
            startTime: item.startTime.slice(0, 5),
            endTime: item.endTime.slice(0, 5),
          })),
        );
      })
      .catch((err) => setError(errorMessage(err)));
  }, [id]);

  useEffect(() => {
    if (!form.provinceId) {
      setDistricts([]);
      return;
    }
    unwrap<District[]>(api.get(`/public/provinces/${form.provinceId}/districts`))
      .then(setDistricts)
      .catch(() => setDistricts([]));
  }, [form.provinceId]);

  const update = (key: string, value: string) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const toggleSkill = (skillId: number) => {
    setSkillIds((current) =>
      current.includes(skillId)
        ? current.filter((item) => item !== skillId)
        : [...current, skillId],
    );
  };

  const addSchedule = () => {
    setSchedules((current) => [
      ...current,
      { dayOfWeek: "2", startTime: "08:00", endTime: "12:00" },
    ]);
  };

  const updateSchedule = (index: number, key: keyof ScheduleRow, value: string) => {
    setSchedules((current) =>
      current.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [key]: value } : item,
      ),
    );
  };

  const removeSchedule = (index: number) => {
    setSchedules((current) => current.filter((_, itemIndex) => itemIndex !== index));
  };

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError("");
    setLoading(true);
    const payload = {
      title: form.title.trim(),
      categoryId: Number(form.categoryId),
      description: form.description.trim(),
      requirements: form.requirements || null,
      benefits: form.benefits || null,
      workMode: form.workMode,
      provinceId: form.provinceId ? Number(form.provinceId) : null,
      districtId: form.districtId ? Number(form.districtId) : null,
      addressDetail: form.addressDetail || null,
      salaryType: form.salaryType,
      salaryMin: form.salaryMin ? Number(form.salaryMin) : null,
      salaryMax: form.salaryMax ? Number(form.salaryMax) : null,
      positionsCount: Number(form.positionsCount),
      startDate: form.startDate || null,
      endDate: form.endDate || null,
      applicationDeadline: form.applicationDeadline || null,
      skillIds,
      schedules: schedules.map((item) => ({
        dayOfWeek: Number(item.dayOfWeek),
        startTime: item.startTime,
        endTime: item.endTime,
      })),
    };
    try {
      if (editing) {
        await api.put(`/employer/jobs/${id}`, payload);
      } else {
        await api.post("/employer/jobs", payload);
      }
      navigate("/employer/jobs");
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>{editing ? "Sửa tin tuyển dụng" : "Đăng tin tuyển dụng"}</h1>
          <p>Điền đầy đủ thông tin để sinh viên dễ dàng tìm thấy tin của bạn.</p>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}

      <form className="panel" onSubmit={submit}>
        <div className="form-grid">
          <div className="full">
            <label htmlFor="title">Tiêu đề</label>
            <input
              id="title"
              value={form.title}
              onChange={(event) => update("title", event.target.value)}
              required
              minLength={5}
              maxLength={200}
            />
          </div>
          <div>
            <label htmlFor="categoryId">Danh mục</label>
            <select
              id="categoryId"
              value={form.categoryId}
              onChange={(event) => update("categoryId", event.target.value)}
              required
            >
              <option value="">Chọn danh mục</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="workMode">Hình thức làm việc</label>
            <select
              id="workMode"
              value={form.workMode}
              onChange={(event) => update("workMode", event.target.value)}
            >
              <option value="ONSITE">Tại chỗ</option>
              <option value="REMOTE">Từ xa</option>
              <option value="HYBRID">Kết hợp</option>
            </select>
          </div>
          <div className="full">
            <label htmlFor="description">Mô tả công việc</label>
            <textarea
              id="description"
              value={form.description}
              onChange={(event) => update("description", event.target.value)}
              required
              minLength={10}
            />
          </div>
          <div className="full">
            <label htmlFor="requirements">Yêu cầu</label>
            <textarea
              id="requirements"
              value={form.requirements}
              onChange={(event) => update("requirements", event.target.value)}
            />
          </div>
          <div className="full">
            <label htmlFor="benefits">Quyền lợi</label>
            <textarea
              id="benefits"
              value={form.benefits}
              onChange={(event) => update("benefits", event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="provinceId">Tỉnh / thành phố</label>
            <select
              id="provinceId"
              value={form.provinceId}
              onChange={(event) => {
                update("provinceId", event.target.value);
                update("districtId", "");
              }}
            >
              <option value="">Chọn tỉnh thành</option>
              {provinces.map((province) => (
                <option key={province.id} value={province.id}>
                  {province.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="districtId">Quận / huyện</label>
            <select
              id="districtId"
              value={form.districtId}
              onChange={(event) => update("districtId", event.target.value)}
              disabled={districts.length === 0}
            >
              <option value="">Chọn quận huyện</option>
              {districts.map((district) => (
                <option key={district.id} value={district.id}>
                  {district.name}
                </option>
              ))}
            </select>
          </div>
          <div className="full">
            <label htmlFor="addressDetail">Địa chỉ chi tiết</label>
            <input
              id="addressDetail"
              value={form.addressDetail}
              onChange={(event) => update("addressDetail", event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="salaryType">Hình thức trả lương</label>
            <select
              id="salaryType"
              value={form.salaryType}
              onChange={(event) => update("salaryType", event.target.value)}
            >
              {SALARY_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="positionsCount">Số lượng tuyển</label>
            <input
              id="positionsCount"
              type="number"
              min="1"
              max="100"
              value={form.positionsCount}
              onChange={(event) => update("positionsCount", event.target.value)}
              required
            />
          </div>
          {form.salaryType !== "NEGOTIABLE" && (
            <>
              <div>
                <label htmlFor="salaryMin">Thù lao tối thiểu (VND)</label>
                <input
                  id="salaryMin"
                  type="number"
                  min="0"
                  value={form.salaryMin}
                  onChange={(event) => update("salaryMin", event.target.value)}
                  required
                />
              </div>
              <div>
                <label htmlFor="salaryMax">Thù lao tối đa (VND)</label>
                <input
                  id="salaryMax"
                  type="number"
                  min="0"
                  value={form.salaryMax}
                  onChange={(event) => update("salaryMax", event.target.value)}
                />
              </div>
            </>
          )}
          <div>
            <label htmlFor="startDate">Ngày bắt đầu</label>
            <input
              id="startDate"
              type="date"
              value={form.startDate}
              onChange={(event) => update("startDate", event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="endDate">Ngày kết thúc</label>
            <input
              id="endDate"
              type="date"
              value={form.endDate}
              onChange={(event) => update("endDate", event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="applicationDeadline">Hạn nộp</label>
            <input
              id="applicationDeadline"
              type="datetime-local"
              value={form.applicationDeadline}
              onChange={(event) => update("applicationDeadline", event.target.value)}
            />
          </div>
        </div>

        <div style={{ marginTop: 18 }}>
          <label>Kỹ năng yêu cầu</label>
          <div className="checkbox-list">
            {skills.map((skill) => (
              <label key={skill.id}>
                <input
                  type="checkbox"
                  checked={skillIds.includes(skill.id)}
                  onChange={() => toggleSkill(skill.id)}
                />
                {skill.name}
              </label>
            ))}
          </div>
        </div>

        <div style={{ marginTop: 18 }}>
          <div className="panel-title">
            <label>Lịch làm việc theo tuần</label>
            <button type="button" className="button button-small" onClick={addSchedule}>
              Thêm ca
            </button>
          </div>
          {schedules.map((item, index) => (
            <div className="schedule-row" key={index}>
              <select
                value={item.dayOfWeek}
                onChange={(event) => updateSchedule(index, "dayOfWeek", event.target.value)}
              >
                {Object.entries(DAYS).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
              <input
                type="time"
                value={item.startTime}
                onChange={(event) => updateSchedule(index, "startTime", event.target.value)}
              />
              <input
                type="time"
                value={item.endTime}
                onChange={(event) => updateSchedule(index, "endTime", event.target.value)}
              />
              <button
                type="button"
                className="button button-small button-danger"
                onClick={() => removeSchedule(index)}
              >
                Xóa
              </button>
            </div>
          ))}
          {schedules.length === 0 && (
            <p className="help">Chưa có ca làm việc nào.</p>
          )}
        </div>

        <div className="form-actions">
          <button type="submit" className="button button-primary" disabled={loading}>
            {loading ? "Đang lưu..." : editing ? "Lưu thay đổi" : "Đăng tin"}
          </button>
          <button
            type="button"
            className="button button-plain"
            onClick={() => navigate("/employer/jobs")}
          >
            Hủy
          </button>
        </div>
      </form>
    </div>
  );
}

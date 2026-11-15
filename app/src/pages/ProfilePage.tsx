import { useEffect, useState } from "react";
import { api, errorMessage, unwrap } from "../api";
import { useAuth } from "../auth";
import type { Profile, Province, Resume, Skill } from "../types";
import { dateText } from "../format";

export function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [skills, setSkills] = useState<Skill[]>([]);
  const [provinces, setProvinces] = useState<Province[]>([]);
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [selectedSkills, setSelectedSkills] = useState<Record<number, string>>({});
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [resumeForm, setResumeForm] = useState({ title: "", fileUrl: "", isDefault: false });
  const [form, setForm] = useState<Record<string, string>>({});

  const load = async () => {
    try {
      const data = await unwrap<Profile>(api.get("/profiles/me"));
      setProfile(data);
      const map: Record<number, string> = {};
      data.skills.forEach((skill) => {
        map[skill.id] = skill.level ?? "BEGINNER";
      });
      setSelectedSkills(map);
      setForm({
        fullName: data.fullName ?? "",
        displayName: data.displayName ?? "",
        phone: data.phone ?? "",
        contactPhone: data.contactPhone ?? "",
        contactName: data.contactName ?? "",
        taxCode: data.taxCode ?? "",
        employerType: data.employerType ?? "COMPANY",
        studentCode: data.studentCode ?? "",
        university: data.university ?? "",
        major: data.major ?? "",
        yearOfStudy: data.yearOfStudy ? String(data.yearOfStudy) : "",
        provinceId: data.provinceId ? String(data.provinceId) : "",
        address: data.address ?? "",
        bio: data.bio ?? "",
        description: data.description ?? "",
        website: data.website ?? "",
      });
      if (data.role === "STUDENT") {
        const resumeList = await unwrap<Resume[]>(api.get("/student/resumes"));
        setResumes(resumeList);
      }
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  useEffect(() => {
    void load();
    unwrap<Skill[]>(api.get("/public/skills")).then(setSkills).catch(() => undefined);
    unwrap<Province[]>(api.get("/public/provinces")).then(setProvinces).catch(() => undefined);
  }, []);

  const update = (key: string, value: string) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const save = async (event: React.FormEvent) => {
    event.preventDefault();
    setError("");
    setMessage("");
    const payload: Record<string, unknown> = {
      provinceId: form.provinceId ? Number(form.provinceId) : null,
      address: form.address,
      bio: form.bio,
      phone: form.phone,
    };
    if (user?.role === "STUDENT") {
      Object.assign(payload, {
        fullName: form.fullName,
        studentCode: form.studentCode,
        university: form.university,
        major: form.major,
        yearOfStudy: form.yearOfStudy ? Number(form.yearOfStudy) : null,
        skills: Object.entries(selectedSkills).map(([skillId, level]) => ({
          skillId: Number(skillId),
          level,
        })),
      });
    } else if (user?.role === "EMPLOYER") {
      Object.assign(payload, {
        displayName: form.displayName,
        employerType: form.employerType,
        contactName: form.contactName,
        contactPhone: form.contactPhone,
        taxCode: form.taxCode,
        description: form.description,
        website: form.website,
      });
    }
    try {
      await api.put("/profiles/me", payload);
      setMessage("Cập nhật hồ sơ thành công.");
      await load();
      await refreshUser();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const toggleSkill = (skillId: number) => {
    setSelectedSkills((current) => {
      const next = { ...current };
      if (next[skillId]) {
        delete next[skillId];
      } else {
        next[skillId] = "BEGINNER";
      }
      return next;
    });
  };

  const addResume = async (event: React.FormEvent) => {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      await api.post("/student/resumes", {
        title: resumeForm.title,
        fileUrl: resumeForm.fileUrl,
        fileName: resumeForm.fileUrl.split("/").pop() || "cv",
        isDefault: resumeForm.isDefault,
      });
      setResumeForm({ title: "", fileUrl: "", isDefault: false });
      setMessage("Đã thêm CV.");
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const setDefaultResume = async (id: number) => {
    try {
      await api.patch(`/student/resumes/${id}/default`);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const deleteResume = async (id: number) => {
    if (!window.confirm("Xóa CV này?")) {
      return;
    }
    try {
      await api.delete(`/student/resumes/${id}`);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  if (!profile) {
    return error ? <p className="notice notice-error">{error}</p> : <p className="notice">Đang tải hồ sơ...</p>;
  }

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Hồ sơ cá nhân</h1>
          <p>
            {profile.email} ·{" "}
            {profile.role === "STUDENT" ? "Sinh viên" : profile.role === "EMPLOYER" ? "Nhà tuyển dụng" : "Quản trị viên"}
          </p>
        </div>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <section className="panel">
        <h2>Thông tin cơ bản</h2>
        <form onSubmit={save}>
          <div className="form-grid">
            {profile.role === "STUDENT" ? (
              <>
                <div>
                  <label htmlFor="fullName">Họ và tên</label>
                  <input id="fullName" value={form.fullName} onChange={(e) => update("fullName", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="phone">Số điện thoại</label>
                  <input id="phone" value={form.phone} onChange={(e) => update("phone", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="studentCode">Mã sinh viên</label>
                  <input id="studentCode" value={form.studentCode} onChange={(e) => update("studentCode", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="university">Trường</label>
                  <input id="university" value={form.university} onChange={(e) => update("university", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="major">Chuyên ngành</label>
                  <input id="major" value={form.major} onChange={(e) => update("major", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="yearOfStudy">Năm học</label>
                  <input
                    id="yearOfStudy"
                    type="number"
                    min="1"
                    max="8"
                    value={form.yearOfStudy}
                    onChange={(e) => update("yearOfStudy", e.target.value)}
                  />
                </div>
              </>
            ) : (
              <>
                <div>
                  <label htmlFor="displayName">Tên đơn vị</label>
                  <input id="displayName" value={form.displayName} onChange={(e) => update("displayName", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="employerType">Loại nhà tuyển dụng</label>
                  <select id="employerType" value={form.employerType} onChange={(e) => update("employerType", e.target.value)}>
                    <option value="COMPANY">Doanh nghiệp</option>
                    <option value="INDIVIDUAL">Cá nhân</option>
                  </select>
                </div>
                <div>
                  <label htmlFor="contactName">Người liên hệ</label>
                  <input id="contactName" value={form.contactName} onChange={(e) => update("contactName", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="contactPhone">Điện thoại liên hệ</label>
                  <input id="contactPhone" value={form.contactPhone} onChange={(e) => update("contactPhone", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="taxCode">Mã số thuế</label>
                  <input id="taxCode" value={form.taxCode} onChange={(e) => update("taxCode", e.target.value)} />
                </div>
                <div>
                  <label htmlFor="website">Website</label>
                  <input id="website" value={form.website} onChange={(e) => update("website", e.target.value)} />
                </div>
              </>
            )}
            <div>
              <label htmlFor="provinceId">Tỉnh / thành phố</label>
              <select id="provinceId" value={form.provinceId} onChange={(e) => update("provinceId", e.target.value)}>
                <option value="">Chọn tỉnh thành</option>
                {provinces.map((province) => (
                  <option key={province.id} value={province.id}>
                    {province.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="address">Địa chỉ</label>
              <input id="address" value={form.address} onChange={(e) => update("address", e.target.value)} />
            </div>
            {profile.role === "STUDENT" ? (
              <div className="full">
                <label htmlFor="bio">Giới thiệu bản thân</label>
                <textarea id="bio" value={form.bio} onChange={(e) => update("bio", e.target.value)} />
              </div>
            ) : (
              <div className="full">
                <label htmlFor="description">Mô tả đơn vị</label>
                <textarea id="description" value={form.description} onChange={(e) => update("description", e.target.value)} />
              </div>
            )}
          </div>

          {profile.role === "STUDENT" && (
            <div style={{ marginTop: 16 }}>
              <label>Kỹ năng</label>
              <div className="checkbox-list">
                {skills.map((skill) => (
                  <label key={skill.id}>
                    <input
                      type="checkbox"
                      checked={Boolean(selectedSkills[skill.id])}
                      onChange={() => toggleSkill(skill.id)}
                    />
                    {skill.name}
                    {selectedSkills[skill.id] && (
                      <select
                        value={selectedSkills[skill.id]}
                        onChange={(event) =>
                          setSelectedSkills((current) => ({
                            ...current,
                            [skill.id]: event.target.value,
                          }))
                        }
                      >
                        <option value="BEGINNER">Mới bắt đầu</option>
                        <option value="INTERMEDIATE">Trung bình</option>
                        <option value="ADVANCED">Thành thạo</option>
                      </select>
                    )}
                  </label>
                ))}
              </div>
            </div>
          )}

          <div className="form-actions">
            <button type="submit" className="button button-primary">
              Lưu hồ sơ
            </button>
          </div>
        </form>
      </section>

      {profile.role === "STUDENT" && (
        <section className="panel">
          <h2>CV của tôi</h2>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Tiêu đề</th>
                  <th>Liên kết</th>
                  <th>Mặc định</th>
                  <th>Ngày tạo</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {resumes.map((resume) => (
                  <tr key={resume.id}>
                    <td>{resume.title}</td>
                    <td>
                      <a href={resume.fileUrl} target="_blank" rel="noreferrer">
                        {resume.fileName}
                      </a>
                    </td>
                    <td>{resume.isDefault ? "Có" : "Không"}</td>
                    <td>{dateText(resume.createdAt)}</td>
                    <td>
                      <div className="row">
                        {!resume.isDefault && (
                          <button type="button" className="button button-small" onClick={() => setDefaultResume(resume.id)}>
                            Đặt mặc định
                          </button>
                        )}
                        <button type="button" className="button button-small button-danger" onClick={() => deleteResume(resume.id)}>
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {resumes.length === 0 && (
                  <tr>
                    <td colSpan={5}>Chưa có CV nào.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <h3 style={{ marginTop: 16 }}>Thêm CV</h3>
          <form onSubmit={addResume}>
            <div className="form-grid">
              <div>
                <label htmlFor="resumeTitle">Tiêu đề</label>
                <input
                  id="resumeTitle"
                  value={resumeForm.title}
                  onChange={(e) => setResumeForm({ ...resumeForm, title: e.target.value })}
                  required
                />
              </div>
              <div>
                <label htmlFor="resumeUrl">Liên kết CV</label>
                <input
                  id="resumeUrl"
                  value={resumeForm.fileUrl}
                  onChange={(e) => setResumeForm({ ...resumeForm, fileUrl: e.target.value })}
                  placeholder="https://..."
                  required
                />
              </div>
              <div>
                <label>
                  <input
                    type="checkbox"
                    checked={resumeForm.isDefault}
                    onChange={(e) => setResumeForm({ ...resumeForm, isDefault: e.target.checked })}
                  />{" "}
                  Đặt làm CV mặc định
                </label>
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="button button-primary">
                Thêm CV
              </button>
            </div>
          </form>
        </section>
      )}
    </div>
  );
}

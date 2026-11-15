import { useEffect, useState } from "react";
import { Link } from "react-router";
import { api, errorMessage, unwrap } from "../api";
import type { Category } from "../types";

interface CategoryForm {
  name: string;
  slug: string;
  description: string;
  sortOrder: string;
  isActive: boolean;
}

const EMPTY_FORM: CategoryForm = {
  name: "",
  slug: "",
  description: "",
  sortOrder: "",
  isActive: true,
};

export function AdminCategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [form, setForm] = useState<CategoryForm>({ ...EMPTY_FORM });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const load = () => {
    unwrap<Category[]>(api.get("/admin/categories"))
      .then((result) => {
        setCategories(result);
        setError("");
      })
      .catch((err) => setError(errorMessage(err)));
  };

  useEffect(() => {
    load();
  }, []);

  const update = (key: keyof CategoryForm, value: string | boolean) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const resetForm = () => {
    setForm({ ...EMPTY_FORM });
    setEditingId(null);
  };

  const startEdit = (category: Category) => {
    setEditingId(category.id);
    setForm({
      name: category.name,
      slug: category.slug,
      description: category.description ?? "",
      sortOrder: category.sortOrder ? String(category.sortOrder) : "",
      isActive: category.isActive,
    });
  };

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError("");
    setMessage("");
    const payload = {
      name: form.name.trim(),
      slug: form.slug || null,
      description: form.description || null,
      sortOrder: form.sortOrder ? Number(form.sortOrder) : null,
      parentId: null,
      isActive: form.isActive,
    };
    try {
      if (editingId !== null) {
        await api.put(`/admin/categories/${editingId}`, payload);
        setMessage("Cập nhật danh mục thành công.");
      } else {
        await api.post("/admin/categories", payload);
        setMessage("Thêm danh mục thành công.");
      }
      resetForm();
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const toggleVisibility = async (category: Category) => {
    setError("");
    setMessage("");
    try {
      await api.patch(`/admin/categories/${category.id}/visibility`, {
        visible: !category.isActive,
      });
      setMessage("Cập nhật trạng thái hiển thị thành công.");
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h1>Quản lý danh mục</h1>
          <p>Thêm, chỉnh sửa và ẩn/hiện danh mục nghề nghiệp.</p>
        </div>
        <Link className="button button-plain" to="/admin">
          Về trang quản trị
        </Link>
      </div>

      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice notice-success">{message}</p>}

      <section className="panel">
        <h2>{editingId !== null ? "Sửa danh mục" : "Thêm danh mục"}</h2>
        <form onSubmit={submit}>
          <div className="form-grid">
            <div>
              <label htmlFor="name">Tên danh mục</label>
              <input
                id="name"
                value={form.name}
                onChange={(event) => update("name", event.target.value)}
                required
                maxLength={100}
              />
            </div>
            <div>
              <label htmlFor="slug">Slug</label>
              <input
                id="slug"
                value={form.slug}
                onChange={(event) => update("slug", event.target.value)}
                placeholder="Để trống để tự tạo"
                maxLength={120}
              />
            </div>
            <div className="full">
              <label htmlFor="description">Mô tả</label>
              <input
                id="description"
                value={form.description}
                onChange={(event) => update("description", event.target.value)}
                maxLength={255}
              />
            </div>
            <div>
              <label htmlFor="sortOrder">Thứ tự hiển thị</label>
              <input
                id="sortOrder"
                type="number"
                min="0"
                value={form.sortOrder}
                onChange={(event) => update("sortOrder", event.target.value)}
              />
            </div>
            <div>
              <label>
                <input
                  type="checkbox"
                  checked={form.isActive}
                  onChange={(event) => update("isActive", event.target.checked)}
                />{" "}
                Hiển thị danh mục
              </label>
            </div>
          </div>
          <div className="form-actions">
            <button type="submit" className="button button-primary">
              {editingId !== null ? "Lưu thay đổi" : "Thêm danh mục"}
            </button>
            {editingId !== null && (
              <button type="button" className="button button-plain" onClick={resetForm}>
                Hủy
              </button>
            )}
          </div>
        </form>
      </section>

      <div className="table-wrap panel">
        <table>
          <thead>
            <tr>
              <th>Tên danh mục</th>
              <th>Slug</th>
              <th>Thứ tự</th>
              <th>Trạng thái</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {categories.map((category) => (
              <tr key={category.id}>
                <td>{category.name}</td>
                <td>{category.slug}</td>
                <td>{category.sortOrder}</td>
                <td>
                  <span className={`status ${category.isActive ? "status-active" : "status-locked"}`}>
                    {category.isActive ? "Đang hiển thị" : "Đã ẩn"}
                  </span>
                </td>
                <td>
                  <div className="row">
                    <button
                      type="button"
                      className="button button-small"
                      onClick={() => startEdit(category)}
                    >
                      Sửa
                    </button>
                    <button
                      type="button"
                      className="button button-small button-plain"
                      onClick={() => toggleVisibility(category)}
                    >
                      {category.isActive ? "Ẩn" : "Hiện"}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {categories.length === 0 && (
              <tr>
                <td colSpan={5}>Chưa có danh mục nào.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

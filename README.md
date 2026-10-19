# UniWork Marketplace

Nền tảng việc làm part-time dành cho sinh viên.

## 1. Thông tin dự án

- **Tên dự án:** UniWork Marketplace
- **Mô tả:** Website kết nối sinh viên với nhà tuyển dụng cho các công việc part-time, bán thời gian.
- **Đối tượng:** Sinh viên, Nhà tuyển dụng, Quản trị viên.
- **Môn học:** Chuyên đề tốt nghiệp 2

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Frontend | ReactJS |
| Backend | Spring Boot 3, Spring Security, JWT |
| Database | MySQL |
| ORM | Spring Data JPA |
| API Docs | Swagger / OpenAPI |

## 3. Cấu trúc thư mục

```text
.
├── docs/
│   └── 03-api/              # Tài liệu thiết kế API
├── SRS_UniWork_v1.1.docx    # Đặc tả yêu cầu hệ thống
├── product backlog.xlsx     # Product backlog
├── Buoi04_SoDo_KienTruc.drawio
├── Buoi04_SoDo_KienTruc.drawio.png
├── Buoi01_Phac_thao_muc_tieu_va_tinh_nang.docx
├── Nhật ký thực hiện.docx
└── công nghệ đã chọn.md
```

> Khi bổ sung source code, tạo thêm `frontend/` và `backend/` ở thư mục gốc.

## 4. Thành viên nhóm

| STT | Họ và tên | MSSV | GitHub |
|---|---|---|---|
| 1 | Nguyễn Nam Khánh |  | `@NguyenNamKhanh05` |
| 2 |  |  |  |
| 3 |  |  |  |

## 5. Quy trình làm việc với Git

### Nhánh

- `main`: nhánh ổn định, dùng để demo/nộp bài.
- `develop`: nhánh tích hợp tính năng.
- `feature/<ten-tinh-nang>`: phát triển tính năng mới.
- `bugfix/<ten-loi>`: sửa lỗi.
- `docs/<ten-tai-lieu>`: cập nhật tài liệu.

### Quy trình

1. Cập nhật `develop` mới nhất:
   ```bash
   git checkout develop
   git pull origin develop
   ```
2. Tạo nhánh mới:
   ```bash
   git checkout -b feature/dang-nhap
   ```
3. Commit và push:
   ```bash
   git add .
   git commit -m "feat: them chuc nang dang nhap"
   git push -u origin feature/dang-nhap
   ```
4. Tạo Pull Request vào `develop`.
5. Ít nhất 1 thành viên review trước khi merge.
6. Khi hoàn thiện, merge `develop` vào `main`.

### Quy ước commit

| Tiền tố | Ý nghĩa |
|---|---|
| `feat:` | Thêm tính năng |
| `fix:` | Sửa lỗi |
| `docs:` | Cập nhật tài liệu |
| `refactor:` | Tái cấu trúc code |
| `test:` | Thêm/sửa test |
| `chore:` | Cấu hình, công cụ |

Ví dụ: `feat: API dang ky tai khoan sinh vien`

## 6. Tài liệu

- [Thiết kế API](docs/03-api/API_Design_UniWork.md)
- SRS: `SRS_UniWork_v1.1.docx`
- Product backlog: `product backlog.xlsx`
- Sơ đồ kiến trúc: `Buoi04_SoDo_KienTruc.drawio`

## 7. Lưu ý

- Không commit file chứa mật khẩu, API key hoặc thông tin cá nhân nhạy cảm.
- Không commit `node_modules/`, `target/`, `.env`.
- Chạy `git pull` trước khi bắt đầu làm việc.
- Đặt tên nhánh và commit rõ ràng, có ý nghĩa.

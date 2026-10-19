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
| Frontend | ReactJS 18 + Vite 5 |
| Backend | Spring Boot 3.2.12, Java 21, Maven Wrapper |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA |
| API Docs | Swagger / OpenAPI |

## 3. Cấu trúc thư mục

```text
.
├── backend/                 # Spring Boot REST API
│   ├── src/main/java/com/uniwork/backend
│   ├── src/main/resources/application.yml
│   └── pom.xml
├── frontend/                # ReactJS + Vite
│   ├── src/App.jsx
│   └── package.json
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

## 4. Chạy thử dự án Hello World

### Yêu cầu môi trường

- JDK 21 trở lên
- Node.js 20 trở lên
- MySQL 8.0
- Maven Wrapper đã có sẵn trong `backend/`

### Bước 1: Tạo database MySQL

```sql
CREATE DATABASE uniwork CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'uniwork'@'localhost' IDENTIFIED BY 'uniwork123';
GRANT ALL PRIVILEGES ON uniwork.* TO 'uniwork'@'localhost';
FLUSH PRIVILEGES;
```

Mặc định backend kết nối `localhost:3306`, database `uniwork`, user `uniwork`, mật khẩu `uniwork123`.

Nếu MySQL chạy ở cổng khác, đặt biến môi trường `DB_PORT` trước khi chạy backend.

### Bước 2: Chạy backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Nếu dùng macOS/Linux:

```bash
cd backend
./mvnw spring-boot:run
```

Ví dụ khi MySQL ở cổng `3307`:

```powershell
$env:DB_PORT="3307"
.\mvnw.cmd spring-boot:run
```

### Bước 3: Chạy frontend

```bash
cd frontend
npm install
npm run dev
```

Mở trình duyệt tại `http://localhost:5173`.

### Bước 4: Kiểm tra API

- `GET http://localhost:8080/api/hello/ping`
- `GET http://localhost:8080/api/hello`
- `POST http://localhost:8080/api/hello`

Body mẫu:

```json
{
  "content": "Hello UniWork"
}
```

### Bước 5: Chạy test

```bash
cd backend
.\mvnw.cmd test
```

```bash
cd frontend
npm run build
```

Kết quả mong đợi: ReactJS gọi được REST API, Spring Boot ghi dữ liệu vào MySQL và dữ liệu hiển thị lại trên giao diện.

## 5. Thành viên nhóm

| STT | Họ và tên | MSSV | GitHub |
|---|---|---|---|
| 1 | Nguyễn Nam Khánh | 2374802010222 | `@NguyenNamKhanh05` |
| 2 | Phạm Trí Trọng | 2374802010525 | `@tritrongpham0513` |
| 3 | Nguyễn Trần Hoàng Vinh | 2374802010562 | `@NguyenVinh2504` |

## 6. Quy trình làm việc với Git

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

## 7. Tài liệu

- [Thiết kế API](docs/03-api/API_Design_UniWork.md)
- SRS: `SRS_UniWork_v1.1.docx`
- Product backlog: `product backlog.xlsx`
- Sơ đồ kiến trúc: `Buoi04_SoDo_KienTruc.drawio`

## 8. Lưu ý

- Không commit file chứa mật khẩu, API key hoặc thông tin cá nhân nhạy cảm.
- Không commit `node_modules/`, `target/`, `.env`.
- Chạy `git pull` trước khi bắt đầu làm việc.
- Đặt tên nhánh và commit rõ ràng, có ý nghĩa.

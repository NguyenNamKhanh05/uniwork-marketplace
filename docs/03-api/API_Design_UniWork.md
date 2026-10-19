# THIẾT KẾ API – UNIWORK MARKETPLACE

- **Phiên bản:** 1.0
- **Ngày:** 19/10/2026
- **Công nghệ:** Spring Boot 3, Spring Security, JWT, Spring Data JPA, MySQL, ReactJS
- **Đối tượng áp dụng:** Backend Developer, Frontend Developer, Tester

---

## 1. QUY ƯỚC CHUNG

### 1.1. Base URL

| Môi trường | Base URL |
|---|---|
| Local | `http://localhost:8080/api/v1` |
| Demo/Production | `https://api.uniwork.vn/api/v1` |

Tất cả endpoint đều có tiền tố `/api/v1`.

### 1.2. Xác thực và phân quyền

- Sử dụng **JWT Bearer Token**.
- Header:
  ```http
  Authorization: Bearer <access_token>
  ```
- JWT có thời hạn **24 giờ**.
- Claims đề xuất:
  ```json
  {
    "sub": "1",
    "email": "student@example.com",
    "role": "STUDENT",
    "iat": 1760000000,
    "exp": 1760086400
  }
  ```
- Vai trò:
  - `STUDENT` – Sinh viên
  - `EMPLOYER` – Nhà tuyển dụng
  - `ADMIN` – Quản trị viên
- Mật khẩu được băm bằng **BCrypt (cost 10)**; không bao giờ trả về trong response.

### 1.3. Định dạng dữ liệu

| Thành phần | Quy ước |
|---|---|
| Content-Type | `application/json; charset=UTF-8` |
| Ngày | `yyyy-MM-dd` |
| Thời gian | ISO-8601, ví dụ `2026-10-19T10:00:00+07:00` |
| Tiền tệ | VND, kiểu số nguyên (`Long`), không dùng số thập phân |
| Mã định danh | `Long` |
| Tên field JSON | `camelCase` |
| Đường dẫn API | `kebab-case`, danh từ số nhiều |

### 1.4. Phân trang và sắp xếp

Query params dùng chung:

| Param | Kiểu | Mặc định | Ghi chú |
|---|---|---|---|
| `page` | int | `0` | Bắt đầu từ 0 |
| `size` | int | `10` | Tối đa 50 |
| `sort` | string | `createdAt,desc` | Ví dụ: `salaryAmount,asc` |

Response phân trang:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

### 1.5. Response envelope

Mọi response thành công:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Thành công",
  "data": {},
  "timestamp": "2026-10-19T10:00:00+07:00"
}
```

- `data` có thể là object, mảng hoặc `null`.
- Với HTTP 204, không trả body.

### 1.6. Error envelope

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "errors": [
    {
      "field": "salaryAmount",
      "message": "Mức thù lao phải lớn hơn 0"
    }
  ],
  "timestamp": "2026-10-19T10:00:00+07:00"
}
```

### 1.7. HTTP status codes

| Mã | Ý nghĩa |
|---|---|
| `200 OK` | Lấy/cập nhật thành công |
| `201 Created` | Tạo mới thành công |
| `204 No Content` | Xóa/đánh dấu thành công, không có body |
| `400 Bad Request` | Dữ liệu sai định dạng/vi phạm validate |
| `401 Unauthorized` | Chưa đăng nhập hoặc token hết hạn |
| `403 Forbidden` | Không có quyền hoặc tài khoản bị khóa |
| `404 Not Found` | Không tìm thấy tài nguyên |
| `409 Conflict` | Xung đột nghiệp vụ (trùng email, trùng đơn, tin đã đủ người...) |
| `500 Internal Server Error` | Lỗi hệ thống |

### 1.8. Bộ mã lỗi nghiệp vụ

| Code | HTTP | Ý nghĩa |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Dữ liệu đầu vào không hợp lệ |
| `INVALID_CREDENTIALS` | 401 | Sai email hoặc mật khẩu |
| `UNAUTHORIZED` | 401 | Thiếu/sai/hết hạn token |
| `FORBIDDEN` | 403 | Không đủ quyền |
| `ACCOUNT_LOCKED` | 403 | Tài khoản đã bị khóa |
| `EMAIL_ALREADY_EXISTS` | 409 | Email đã được sử dụng |
| `NOT_FOUND` | 404 | Không tìm thấy dữ liệu |
| `JOB_NOT_OPEN` | 409 | Tin không còn mở nhận hồ sơ |
| `JOB_EXPIRED` | 409 | Tin đã hết hạn nộp |
| `DUPLICATE_APPLICATION` | 409 | Đã nộp đơn cho tin này |
| `APPLICATION_NOT_PENDING` | 409 | Đơn không còn ở trạng thái chờ |
| `JOB_FULL` | 409 | Tin đã đủ chỉ tiêu |
| `INVALID_JOB_STATUS` | 409 | Chuyển trạng thái không hợp lệ |
| `REVIEW_NOT_ALLOWED` | 409 | Chưa đủ điều kiện đánh giá |
| `DUPLICATE_REVIEW` | 409 | Đã đánh giá cho đơn này |
| `SELF_LOCK_NOT_ALLOWED` | 400 | Admin không được tự khóa mình |
| `CATEGORY_IN_USE` | 409 | Danh mục đang được sử dụng |
| `INTERNAL_ERROR` | 500 | Lỗi hệ thống |

### 1.9. Quy tắc validate chính

| Field | Quy tắc |
|---|---|
| `fullName` | 2–100 ký tự |
| `email` | Đúng định dạng, tối đa 150 ký tự, duy nhất |
| `password` | 8–72 ký tự, có `confirmPassword` khớp |
| `phone` | 10 chữ số Việt Nam |
| `cvUrl` | URL `http/https` hợp lệ, tối đa 500 ký tự |
| `title` | 5–200 ký tự |
| `description` | 10–5000 ký tự |
| `salaryAmount` | Số nguyên dương |
| `deadline` | Phải sau ngày hiện tại |
| `quantity` | 1–100 |
| `coverLetter` | Tối đa 2000 ký tự |
| `rating` | Số nguyên 1–5 |
| `comment` | Tối đa 500 ký tự |
| `reason` | Tối đa 500 ký tự |

---

## 2. ENUM VÀ TRẠNG THÁI

### 2.1. Role

```text
STUDENT | EMPLOYER | ADMIN
```

### 2.2. UserStatus

```text
ACTIVE | LOCKED
```

### 2.3. JobStatus

```text
OPEN | FILLED | IN_PROGRESS | COMPLETED | CLOSED
```

| Trạng thái | Ý nghĩa |
|---|---|
| `OPEN` | Đang tuyển, nhận hồ sơ |
| `FILLED` | Đã nhận đủ người |
| `IN_PROGRESS` | Đang thực hiện |
| `COMPLETED` | Đã hoàn thành |
| `CLOSED` | Nhà tuyển dụng chủ động đóng |

> Việc Admin ẩn tin dùng field `hidden = true`, **không** dùng thêm trạng thái `HIDDEN`.

### 2.4. WorkType

```text
ONSITE | REMOTE
```

### 2.5. SalaryUnit

```text
HOURLY | PER_SHIFT | PER_JOB
```

### 2.6. ApplicationStatus

```text
PENDING | APPROVED | REJECTED
```

### 2.7. NotificationType

```text
APPLICATION_CREATED
APPLICATION_APPROVED
APPLICATION_REJECTED
JOB_FILLED
JOB_COMPLETED
REVIEW_REQUEST
ACCOUNT_LOCKED
JOB_HIDDEN
```

### 2.8. AuditAction

```text
USER_LOCKED
USER_UNLOCKED
JOB_HIDDEN
JOB_UNHIDDEN
CATEGORY_CREATED
CATEGORY_UPDATED
CATEGORY_HIDDEN
```

### 2.9. Luồng trạng thái Job

```text
OPEN --(đủ chỉ tiêu)--> FILLED
OPEN/FILLED --(NTD đóng tin)--> CLOSED
FILLED --(NTD bắt đầu)--> IN_PROGRESS
IN_PROGRESS --(NTD hoàn thành)--> COMPLETED
```

### 2.10. Luồng trạng thái Application

```text
PENDING --(NTD duyệt)--> APPROVED
PENDING --(NTD từ chối)--> REJECTED
```

---

## 3. MÔ HÌNH DTO CHÍNH

### 3.1. UserSummary

```json
{
  "id": 1,
  "fullName": "Nguyễn Văn A",
  "email": "a@example.com",
  "role": "STUDENT",
  "status": "ACTIVE",
  "avatarUrl": null
}
```

### 3.2. AuthResponse

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "a@example.com",
    "role": "STUDENT",
    "status": "ACTIVE"
  }
}
```

### 3.3. JobSummary

```json
{
  "id": 10,
  "title": "Nhân viên phục vụ part-time",
  "category": {
    "id": 1,
    "name": "Nhà hàng - Khách sạn"
  },
  "salaryAmount": 30000,
  "salaryUnit": "HOURLY",
  "workType": "ONSITE",
  "location": "Quận 1, TP.HCM",
  "shift": "Ca tối 18:00 - 22:00",
  "deadline": "2026-11-01",
  "status": "OPEN",
  "hidden": false,
  "employer": {
    "id": 2,
    "organizationName": "Cà phê ABC",
    "ratingAvg": 4.5,
    "reviewCount": 12
  },
  "createdAt": "2026-10-19T10:00:00+07:00"
}
```

### 3.4. JobDetail

Bao gồm toàn bộ `JobSummary` và thêm:

```json
{
  "description": "Mô tả công việc...",
  "requirements": "Yêu cầu...",
  "benefits": "Quyền lợi...",
  "quantity": 2,
  "approvedCount": 1,
  "employer": {
    "id": 2,
    "organizationName": "Cà phê ABC",
    "field": "F&B",
    "address": "Quận 1, TP.HCM",
    "ratingAvg": 4.5,
    "reviewCount": 12
  }
}
```

### 3.5. ApplicationSummary

```json
{
  "id": 100,
  "jobId": 10,
  "jobTitle": "Nhân viên phục vụ part-time",
  "student": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "university": "Đại học Văn Lang",
    "major": "Công nghệ phần mềm"
  },
  "status": "PENDING",
  "coverLetter": "Em có kinh nghiệm...",
  "cvUrl": "https://drive.google.com/...",
  "appliedAt": "2026-10-19T10:00:00+07:00"
}
```

### 3.6. ReviewSummary

```json
{
  "id": 50,
  "jobId": 10,
  "reviewer": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "role": "STUDENT"
  },
  "reviewee": {
    "id": 2,
    "fullName": "Cà phê ABC",
    "role": "EMPLOYER"
  },
  "rating": 5,
  "comment": "Làm việc đúng giờ, thái độ tốt.",
  "createdAt": "2026-10-19T10:00:00+07:00"
}
```

### 3.7. NotificationSummary

```json
{
  "id": 200,
  "type": "APPLICATION_APPROVED",
  "title": "Đơn ứng tuyển được duyệt",
  "message": "Nhà tuyển dụng Cà phê ABC đã duyệt đơn của bạn.",
  "targetUrl": "/student/applications/100",
  "read": false,
  "createdAt": "2026-10-19T10:00:00+07:00"
}
```

---

## 4. MA TRẬN PHÂN QUYỀN

| Nhóm API | Public | Student | Employer | Admin |
|---|:---:|:---:|:---:|:---:|
| Đăng ký / đăng nhập | ✅ | — | — | — |
| Xem việc làm, danh mục, đánh giá | ✅ | ✅ | ✅ | ✅ |
| Hồ sơ cá nhân | — | ✅ | ✅ | — |
| Ứng tuyển, đơn của tôi | — | ✅ | — | — |
| Quản lý tin đăng | — | — | ✅ | — |
| Quản lý ứng viên | — | — | ✅ | — |
| Đánh giá hai chiều | — | ✅ | ✅ | — |
| Thông báo | — | ✅ | ✅ | ✅ |
| Quản trị hệ thống | — | — | — | ✅ |

---

## 5. DANH SÁCH ENDPOINT

Tổng cộng: **35 endpoint**, chia 8 module.

| Module | Số endpoint |
|---|---:|
| Auth | 4 |
| Profile | 2 |
| Category | 5 |
| Job | 7 |
| Application | 6 |
| Review | 2 |
| Notification | 4 |
| Admin | 5 |

### 5.1. Auth – `/auth`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Đăng ký tài khoản Sinh viên/NTD |
| `POST` | `/auth/login` | Public | Đăng nhập, nhận JWT |
| `POST` | `/auth/logout` | Đã đăng nhập | Đăng xuất (client xóa token) |
| `GET` | `/auth/me` | Đã đăng nhập | Lấy thông tin người dùng hiện tại |

### 5.2. Profile – `/profiles`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/profiles/me` | Student, Employer | Xem hồ sơ của mình |
| `PUT` | `/profiles/me` | Student, Employer | Cập nhật hồ sơ của mình |

### 5.3. Category – `/categories`, `/admin/categories`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/categories` | Public | Danh mục đang hiển thị |
| `GET` | `/admin/categories` | Admin | Tất cả danh mục, gồm cả ẩn |
| `POST` | `/admin/categories` | Admin | Thêm danh mục |
| `PUT` | `/admin/categories/{id}` | Admin | Đổi tên/mô tả danh mục |
| `PATCH` | `/admin/categories/{id}/visibility` | Admin | Ẩn/hiện danh mục |

### 5.4. Job – `/jobs`, `/employer/jobs`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/jobs` | Public | Danh sách việc làm + tìm kiếm/lọc |
| `GET` | `/jobs/{id}` | Public | Chi tiết việc làm |
| `GET` | `/employer/jobs` | Employer | Danh sách tin của tôi |
| `POST` | `/employer/jobs` | Employer | Đăng tin mới |
| `PUT` | `/employer/jobs/{id}` | Employer | Cập nhật tin |
| `PATCH` | `/employer/jobs/{id}/close` | Employer | Đóng tin |
| `PATCH` | `/employer/jobs/{id}/status` | Employer | Chuyển `IN_PROGRESS`/`COMPLETED` |

### 5.5. Application – `/jobs/{jobId}/applications`, `/student/applications`, `/employer/applications`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/jobs/{jobId}/applications` | Student | Nộp đơn ứng tuyển |
| `GET` | `/student/applications` | Student | Đơn ứng tuyển của tôi |
| `GET` | `/student/applications/{id}` | Student | Chi tiết đơn của tôi |
| `GET` | `/employer/jobs/{jobId}/applications` | Employer | Ứng viên của một tin |
| `GET` | `/employer/applications/{id}` | Employer | Chi tiết ứng viên |
| `PATCH` | `/employer/applications/{id}/status` | Employer | Duyệt/từ chối ứng viên |

### 5.6. Review – `/reviews`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/reviews` | Student, Employer | Gửi đánh giá sau khi hoàn thành |
| `GET` | `/reviews?userId=&jobId=` | Public | Danh sách đánh giá theo người hoặc tin |

### 5.7. Notification – `/notifications`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/notifications` | Đã đăng nhập | Danh sách thông báo |
| `GET` | `/notifications/unread-count` | Đã đăng nhập | Số thông báo chưa đọc |
| `PATCH` | `/notifications/{id}/read` | Đã đăng nhập | Đánh dấu đã đọc |
| `PATCH` | `/notifications/read-all` | Đã đăng nhập | Đánh dấu tất cả đã đọc |

### 5.8. Admin – `/admin`

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/admin/users` | Admin | Danh sách/tìm kiếm người dùng |
| `PATCH` | `/admin/users/{id}/status` | Admin | Khóa/mở khóa tài khoản |
| `GET` | `/admin/jobs` | Admin | Danh sách tin để hậu kiểm |
| `PATCH` | `/admin/jobs/{id}/visibility` | Admin | Ẩn/hiện tin vi phạm |
| `GET` | `/admin/audit-logs` | Admin | Nhật ký thao tác quản trị |

---

## 6. CHI TIẾT ENDPOINT

### 6.1. Đăng ký

**`POST /auth/register`**

Request:

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "a@example.com",
  "password": "12345678",
  "confirmPassword": "12345678",
  "role": "STUDENT"
}
```

Response `201`:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "a@example.com",
    "role": "STUDENT",
    "status": "ACTIVE",
    "createdAt": "2026-10-19T10:00:00+07:00"
  },
  "timestamp": "2026-10-19T10:00:00+07:00"
}
```

Lỗi:
- `400 VALIDATION_ERROR`
- `409 EMAIL_ALREADY_EXISTS`

### 6.2. Đăng nhập

**`POST /auth/login`**

Request:

```json
{
  "email": "a@example.com",
  "password": "12345678"
}
```

Response `200`:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "fullName": "Nguyễn Văn A",
      "email": "a@example.com",
      "role": "STUDENT",
      "status": "ACTIVE"
    }
  },
  "timestamp": "2026-10-19T10:00:00+07:00"
}
```

Lỗi:
- `401 INVALID_CREDENTIALS`
- `403 ACCOUNT_LOCKED`

### 6.3. Danh sách việc làm

**`GET /jobs`**

Query params:

| Param | Kiểu | Ghi chú |
|---|---|---|
| `keyword` | string | Tìm trong tiêu đề/mô tả |
| `categoryId` | long | Danh mục |
| `salaryMin` | long | Lương tối thiểu |
| `salaryMax` | long | Lương tối đa |
| `workType` | enum | `ONSITE`, `REMOTE` |
| `location` | string | Địa điểm |
| `shift` | string | Ca/khung giờ |
| `page` | int | Mặc định 0 |
| `size` | int | Mặc định 10 |
| `sort` | string | Mặc định `createdAt,desc` |

Điều kiện trả về:
- `status = OPEN`
- `hidden = false`
- `deadline >= hôm nay`
- Tài khoản Nhà tuyển dụng đang `ACTIVE`

Response `200`:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Thành công",
  "data": {
    "content": [
      {
        "id": 10,
        "title": "Nhân viên phục vụ part-time",
        "category": {
          "id": 1,
          "name": "Nhà hàng - Khách sạn"
        },
        "salaryAmount": 30000,
        "salaryUnit": "HOURLY",
        "workType": "ONSITE",
        "location": "Quận 1, TP.HCM",
        "shift": "Ca tối 18:00 - 22:00",
        "deadline": "2026-11-01",
        "status": "OPEN",
        "hidden": false,
        "employer": {
          "id": 2,
          "organizationName": "Cà phê ABC",
          "ratingAvg": 4.5,
          "reviewCount": 12
        },
        "createdAt": "2026-10-19T10:00:00+07:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-10-19T10:00:00+07:00"
}
```

### 6.4. Đăng tin

**`POST /employer/jobs`**

Request:

```json
{
  "title": "Nhân viên phục vụ part-time",
  "categoryId": 1,
  "description": "Phục vụ khách, chuẩn bị bàn ghế...",
  "requirements": "Nhanh nhẹn, có thể làm ca tối...",
  "benefits": "Lương theo giờ, hỗ trợ ăn ca...",
  "salaryAmount": 30000,
  "salaryUnit": "HOURLY",
  "workType": "ONSITE",
  "location": "Quận 1, TP.HCM",
  "shift": "Ca tối 18:00 - 22:00",
  "deadline": "2026-11-01",
  "quantity": 2
}
```

Response `201`: trả về `JobDetail`.

Lỗi:
- `400 VALIDATION_ERROR`
- `403 FORBIDDEN`
- `404 NOT_FOUND` (category không tồn tại)

### 6.5. Nộp đơn ứng tuyển

**`POST /jobs/{jobId}/applications`**

Request:

```json
{
  "coverLetter": "Em có kinh nghiệm làm phục vụ 6 tháng...",
  "cvUrl": "https://drive.google.com/file/d/..."
}
```

Response `201`:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Ứng tuyển thành công",
  "data": {
    "id": 100,
    "jobId": 10,
    "status": "PENDING",
    "appliedAt": "2026-10-19T10:00:00+07:00"
  },
  "timestamp": "2026-10-19T10:00:00+07:00"
}
```

Lỗi:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN` (không phải Sinh viên)
- `404 NOT_FOUND`
- `409 JOB_NOT_OPEN`
- `409 JOB_EXPIRED`
- `409 DUPLICATE_APPLICATION`

### 6.6. Duyệt/từ chối ứng viên

**`PATCH /employer/applications/{id}/status`**

Request duyệt:

```json
{
  "status": "APPROVED",
  "reason": null
}
```

Request từ chối:

```json
{
  "status": "REJECTED",
  "reason": "Hồ sơ chưa phù hợp với ca làm việc."
}
```

Response `200`: trả về `ApplicationDetail`.

Lỗi:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN` (không phải chủ tin)
- `404 NOT_FOUND`
- `409 APPLICATION_NOT_PENDING`
- `409 JOB_FULL`

### 6.7. Đánh giá

**`POST /reviews`**

Request:

```json
{
  "applicationId": 100,
  "rating": 5,
  "comment": "Làm việc đúng giờ, thái độ tốt."
}
```

Response `201`:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Gửi đánh giá thành công",
  "data": {
    "id": 50,
    "applicationId": 100,
    "rating": 5,
    "comment": "Làm việc đúng giờ, thái độ tốt.",
    "createdAt": "2026-10-19T10:00:00+07:00"
  },
  "timestamp": "2026-10-19T10:00:00+07:00"
}
```

Lỗi:
- `400 VALIDATION_ERROR`
- `403 FORBIDDEN`
- `404 NOT_FOUND`
- `409 REVIEW_NOT_ALLOWED`
- `409 DUPLICATE_REVIEW`

### 6.8. Admin khóa tài khoản

**`PATCH /admin/users/{id}/status`**

Request:

```json
{
  "status": "LOCKED",
  "reason": "Tin tuyển dụng có dấu hiệu lừa đảo."
}
```

Response `200`: trả về `UserSummary`.

Lỗi:
- `400 SELF_LOCK_NOT_ALLOWED`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 NOT_FOUND`

### 6.9. Admin ẩn tin

**`PATCH /admin/jobs/{id}/visibility`**

Request:

```json
{
  "hidden": true,
  "reason": "Tin có dấu hiệu nạp cọc."
}
```

Response `200`: trả về `JobDetail`.

Lỗi:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 NOT_FOUND`

### 6.10. Lỗi chuẩn

```json
{
  "success": false,
  "code": "DUPLICATE_APPLICATION",
  "message": "Bạn đã nộp đơn ứng tuyển cho công việc này rồi.",
  "errors": [],
  "timestamp": "2026-10-19T10:00:00+07:00"
}
```

---

## 7. QUY TẮC NGHIỆP VỤ QUAN TRỌNG

1. **Đăng ký**
   - Email duy nhất, không phân biệt hoa/thường.
   - Mật khẩu ≥ 8 ký tự và khớp `confirmPassword`.
   - Chỉ cho đăng ký `STUDENT` hoặc `EMPLOYER`; `ADMIN` do hệ thống khởi tạo.

2. **Đăng nhập**
   - Sai email/mật khẩu trả cùng một thông báo, không tiết lộ email tồn tại hay không.
   - Tài khoản `LOCKED` bị chặn đăng nhập.
   - Token hết hạn sau 24 giờ.

3. **Việc làm công khai**
   - Chỉ hiển thị tin `OPEN`, `hidden = false`, còn hạn nộp.
   - Tin bị Admin ẩn phải biến mất khỏi danh sách và chi tiết công khai.

4. **Ứng tuyển**
   - Chỉ `STUDENT` được ứng tuyển.
   - Tin phải `OPEN`, chưa hết hạn, chưa bị ẩn.
   - Mỗi Sinh viên chỉ ứng tuyển một lần cho một tin.
   - `coverLetter` và `cvUrl` lấy từ form; nếu không nhập `cvUrl` thì lấy từ hồ sơ.

5. **Duyệt ứng viên**
   - Chỉ thao tác trên đơn `PENDING`.
   - Không duyệt vượt `quantity`.
   - Khi `approvedCount == quantity`, tự động chuyển Job sang `FILLED`.
   - Gửi thông báo cho Sinh viên.

6. **Vòng đời công việc**
   - `FILLED → IN_PROGRESS` khi NTD bắt đầu.
   - `IN_PROGRESS → COMPLETED` khi NTD xác nhận hoàn thành.
   - Chỉ khi `COMPLETED` mới mở đánh giá hai chiều.

7. **Đánh giá**
   - Chỉ hai bên trong đơn được duyệt mới được đánh giá.
   - Mỗi bên chỉ đánh giá một lần cho một đơn.
   - Không cho sửa/xóa đánh giá.
   - Rating trung bình được tính lại sau mỗi đánh giá.

8. **Admin**
   - Không được tự khóa tài khoản của chính mình.
   - Khóa tài khoản phải kèm lý do.
   - Ẩn tin phải kèm lý do.
   - Mọi thao tác quản trị ghi vào `audit_log`.

9. **Thông báo**
   - Tạo thông báo in-app khi:
     - Có đơn ứng tuyển mới.
     - Đơn được duyệt/từ chối.
     - Tin đã đủ người.
     - Công việc hoàn thành.
     - Có yêu cầu đánh giá.
     - Tài khoản bị khóa.
     - Tin bị ẩn.

---

## 8. LUỒNG API TIÊU BIỂU

### 8.1. Đăng nhập

```text
ReactJS → POST /auth/login
        → Spring Security xác thực
        → BCrypt so khớp mật khẩu
        → Sinh JWT 24h
        → Trả accessToken + user
        → React lưu token, gọi GET /auth/me
```

### 8.2. Ứng tuyển

```text
Sinh viên → GET /jobs/{id}
          → POST /jobs/{id}/applications
          → Kiểm tra role, trạng thái tin, hạn nộp, trùng đơn
          → Lưu Application(PENDING)
          → Tạo Notification cho NTD
          → Trả 201
```

### 8.3. Duyệt ứng viên

```text
NTD → GET /employer/jobs/{jobId}/applications
    → PATCH /employer/applications/{id}/status {APPROVED}
    → Kiểm tra chủ tin, đơn PENDING, chỉ tiêu
    → Cập nhật Application(APPROVED)
    → Nếu đủ chỉ tiêu: Job(FILLED)
    → Tạo Notification cho Sinh viên
```

### 8.4. Đánh giá hai chiều

```text
NTD → PATCH /employer/jobs/{id}/status {COMPLETED}
    → Hệ thống mở đánh giá
Sinh viên → POST /reviews {applicationId, rating, comment}
NTD       → POST /reviews {applicationId, rating, comment}
    → Kiểm tra quyền, trạng thái COMPLETED, trùng đánh giá
    → Lưu Review
    → Tính lại ratingAvg
```

---

## 9. GỢI Ý TRIỂN KHAI SPRING BOOT

### 9.1. Cấu trúc package

```text
com.uniwork
├── config
│   ├── SecurityConfig
│   ├── CorsConfig
│   └── OpenApiConfig
├── security
│   ├── JwtAuthenticationFilter
│   ├── JwtService
│   └── CustomUserDetailsService
├── common
│   ├── ApiResponse
│   ├── PageResponse
│   ├── ErrorCode
│   └── GlobalExceptionHandler
├── auth
├── user
├── profile
├── category
├── job
├── application
├── review
├── notification
└── admin
```

### 9.2. Annotation gợi ý

- `@RestController`, `@RequestMapping("/api/v1/...")`
- `@Valid`, `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max`, `@Future`
- `@PreAuthorize("hasRole('STUDENT')")`
- `@Transactional` cho các nghiệp vụ duyệt/từ chối/chuyển trạng thái
- `@ControllerAdvice` + `@ExceptionHandler` cho lỗi toàn cục
- `Pageable` + `Page<T>` → `PageResponse<T>`
- `@Operation`, `@ApiResponse` để sinh Swagger

### 9.3. Cấu hình bảo mật

- `SecurityFilterChain`:
  - Permit: `/auth/**`, `GET /jobs/**`, `GET /categories`, `GET /reviews`.
  - Authenticated: `/profiles/**`, `/student/**`, `/employer/**`, `/notifications/**`.
  - Admin: `/admin/**` với `hasRole('ADMIN')`.
- `JwtAuthenticationFilter` chạy trước `UsernamePasswordAuthenticationFilter`.
- Kiểm tra `UserStatus` mỗi request để chặn tài khoản vừa bị khóa.
- CORS cho `http://localhost:5173` (React Vite) khi dev.

### 9.4. Chỉ mục CSDL gợi ý

```sql
INDEX idx_jobs_public (status, hidden, deadline);
INDEX idx_jobs_category (category_id);
INDEX idx_applications_job_status (job_id, status);
UNIQUE INDEX uk_application_student_job (job_id, student_id);
UNIQUE INDEX uk_review_application_reviewer (application_id, reviewer_id);
INDEX idx_notifications_user_read (user_id, is_read);
INDEX idx_reviews_reviewee (reviewee_id);
```

### 9.5. Sinh tài liệu API

- Dùng `springdoc-openapi-starter-webmvc-ui`.
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Có thể export `openapi.yaml` để nộp kèm báo cáo.

---

## 10. CHECKLIST HOÀN THIỆN API

- [ ] Chốt 35 endpoint và đặt tên thống nhất.
- [ ] Viết DTO request/response, không trả trực tiếp entity.
- [ ] Cấu hình JWT + RBAC + xử lý tài khoản bị khóa.
- [ ] Viết `GlobalExceptionHandler` và bộ mã lỗi.
- [ ] Validate đầu vào bằng Jakarta Validation.
- [ ] Phân trang, lọc, sắp xếp cho các API danh sách.
- [ ] Viết Swagger/OpenAPI.
- [ ] Viết Postman collection cho luồng chính.
- [ ] Kiểm thử các lỗi 401/403/404/409.
- [ ] Kiểm thử nghiệp vụ duyệt đủ chỉ tiêu, chặn nộp trùng, đánh giá một lần.
- [ ] Ghi log/audit cho thao tác Admin.
- [ ] Đảm bảo tất cả response tiếng Việt UTF-8.

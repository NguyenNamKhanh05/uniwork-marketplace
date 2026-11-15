# UniWork Marketplace - Frontend

Giao diện React cho nền tảng việc làm bán thời gian UniWork. Ứng dụng gọi REST API của backend Spring Boot và hiển thị giao diện theo ba vai trò: sinh viên, nhà tuyển dụng, quản trị viên.

## Công nghệ

- React 19, TypeScript
- Vite 6
- react-router 7
- axios
- CSS thuần trong `src/index.css`

## Chạy local

```bash
npm install
npm run dev
```

Mặc định chạy tại `http://localhost:5173` và gọi API `http://localhost:8080/api/v1`. Đổi địa chỉ API bằng biến `VITE_API_URL`:

```bash
# app/.env
VITE_API_URL=http://localhost:8080/api/v1
```

Cần chạy backend trước khi mở giao diện. Hướng dẫn đầy đủ ở `../README.md`.

## Script

| Lệnh | Mô tả |
|---|---|
| `npm run dev` | Chạy dev server có hot reload |
| `npm run build` | Kiểm tra TypeScript và build vào `dist/` |
| `npm run preview` | Xem thử bản build |

## Cấu trúc

```text
src/
├── pages/        # 18 trang theo vai trò và đường dẫn
├── components/   # Layout, thẻ tin, phân trang
├── api.ts        # Cấu hình axios, gắn token, xử lý lỗi
├── auth.tsx      # Phiên đăng nhập và phân quyền giao diện
├── format.ts     # Định dạng tiền, ngày, trạng thái
├── types.ts      # Kiểu dữ liệu dùng chung
└── index.css     # Bảng màu cam - xanh và bố cục
```

Token đăng nhập lưu ở `localStorage` với khóa `uniwork_token`. Khi API trả về 401, token bị xóa và ứng dụng chuyển về trạng thái chưa đăng nhập.

## Tài khoản demo

| Vai trò | Email | Mật khẩu |
|---|---|---|
| Sinh viên | `student@uniwork.vn` | `Student@123` |
| Nhà tuyển dụng | `employer@uniwork.vn` | `Employer@123` |
| Quản trị viên | `admin@uniwork.vn` | `Admin@123` |

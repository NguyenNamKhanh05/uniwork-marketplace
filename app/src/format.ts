export const SALARY_UNITS: Record<string, string> = {
  HOURLY: "giờ",
  DAILY: "ngày",
  WEEKLY: "tuần",
  MONTHLY: "tháng",
  PER_SHIFT: "ca",
  PER_TASK: "sản phẩm",
  PER_JOB: "công việc",
  NEGOTIABLE: "thỏa thuận",
};

export const WORK_MODES: Record<string, string> = {
  ONSITE: "Tại chỗ",
  REMOTE: "Từ xa",
  HYBRID: "Kết hợp",
};

export const APPLICATION_STATUS: Record<string, string> = {
  PENDING: "Chờ duyệt",
  VIEWED: "Đã xem",
  SHORTLISTED: "Quan tâm",
  ACCEPTED: "Đã nhận",
  REJECTED: "Từ chối",
  WITHDRAWN: "Đã rút",
};

export const JOB_STATUS: Record<string, string> = {
  DRAFT: "Nháp",
  PENDING_REVIEW: "Chờ duyệt",
  REJECTED: "Bị từ chối",
  OPEN: "Đang tuyển",
  FILLED: "Đủ người",
  IN_PROGRESS: "Đang thực hiện",
  COMPLETED: "Đã hoàn thành",
  CLOSED: "Đã đóng",
  CANCELLED: "Đã hủy",
};

export const USER_STATUS: Record<string, string> = {
  PENDING_VERIFY: "Chờ xác thực",
  ACTIVE: "Đang hoạt động",
  LOCKED: "Đã khóa",
  DELETED: "Đã xóa",
};

export const DAYS: Record<number, string> = {
  1: "Thứ 2",
  2: "Thứ 3",
  3: "Thứ 4",
  4: "Thứ 5",
  5: "Thứ 6",
  6: "Thứ 7",
  7: "Chủ nhật",
};

export function money(value: number | null | undefined): string {
  if (value === null || value === undefined) {
    return "Thỏa thuận";
  }
  return new Intl.NumberFormat("vi-VN").format(value) + " đ";
}

export function salaryText(job: {
  salaryType: string;
  salaryMin: number | null;
  salaryMax: number | null;
}): string {
  if (job.salaryType === "NEGOTIABLE" || job.salaryMin === null) {
    return "Thỏa thuận";
  }
  const unit = SALARY_UNITS[job.salaryType] ?? "";
  if (job.salaryMax === null || job.salaryMax === job.salaryMin) {
    return `${money(job.salaryMin)}/${unit}`;
  }
  return `${money(job.salaryMin)} - ${money(job.salaryMax)}/${unit}`;
}

export function dateText(value: string | null | undefined): string {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleDateString("vi-VN");
}

export function dateTimeText(value: string | null | undefined): string {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString("vi-VN");
}

export function timeText(value: string | null | undefined): string {
  if (!value) {
    return "";
  }
  return value.slice(0, 5);
}

export function statusText(map: Record<string, string>, value: string): string {
  return map[value] ?? value;
}

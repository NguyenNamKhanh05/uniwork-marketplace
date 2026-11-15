export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export type Role = "STUDENT" | "EMPLOYER" | "ADMIN";

export interface UserSummary {
  id: number;
  email: string;
  role: Role;
  fullName: string | null;
  status: string;
  emailVerifiedAt: string | null;
  authProvider: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  sortOrder: number;
  isActive: boolean;
  parentId: number | null;
}

export interface Province {
  id: number;
  code: string;
  name: string;
}

export interface District {
  id: number;
  provinceId: number;
  code: string;
  name: string;
}

export interface Skill {
  id: number;
  name: string;
  slug: string;
}

export interface SkillItem {
  id: number;
  name: string;
  level: string | null;
}

export interface ScheduleItem {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
}

export interface Job {
  id: number;
  title: string;
  categoryId: number | null;
  categoryName: string | null;
  description: string | null;
  requirements: string | null;
  benefits: string | null;
  workMode: string;
  provinceId: number | null;
  provinceName: string | null;
  districtId: number | null;
  districtName: string | null;
  addressDetail: string | null;
  salaryType: string;
  salaryMin: number | null;
  salaryMax: number | null;
  salaryCurrency: string;
  positionsCount: number;
  positionsFilled: number;
  startDate: string | null;
  endDate: string | null;
  applicationDeadline: string | null;
  status: string;
  viewCount: number;
  publishedAt: string | null;
  createdAt: string | null;
  employerId: number | null;
  employerName: string | null;
  employerVerified: boolean | null;
  employerRating: number | null;
  employerRatingCount: number | null;
  employerLogoUrl: string | null;
  employerDescription: string | null;
  applicationCount: number;
  saved: boolean | null;
  skills: SkillItem[];
  schedules: ScheduleItem[];
}

export interface Resume {
  id: number;
  title: string;
  fileUrl: string;
  fileName: string;
  isDefault: boolean;
  createdAt: string;
}

export interface Profile {
  userId: number;
  email: string;
  role: Role;
  fullName: string | null;
  phone: string | null;
  dateOfBirth: string | null;
  gender: string | null;
  studentCode: string | null;
  university: string | null;
  major: string | null;
  yearOfStudy: number | null;
  provinceId: number | null;
  provinceName: string | null;
  address: string | null;
  bio: string | null;
  avatarUrl: string | null;
  avgRating: number | null;
  ratingCount: number | null;
  employerType: string | null;
  displayName: string | null;
  contactName: string | null;
  contactPhone: string | null;
  taxCode: string | null;
  description: string | null;
  logoUrl: string | null;
  website: string | null;
  isVerified: boolean | null;
  verifiedAt: string | null;
  skills: SkillItem[];
  resumes: Resume[];
}

export interface HistoryItem {
  fromStatus: string | null;
  toStatus: string;
  note: string | null;
  changedByName: string;
  changedAt: string;
}

export interface Application {
  id: number;
  jobId: number;
  jobTitle: string;
  jobStatus: string;
  workMode: string;
  provinceId: number | null;
  provinceName: string | null;
  addressDetail: string | null;
  salaryType: string;
  salaryMin: number | null;
  salaryMax: number | null;
  startDate: string | null;
  applicationDeadline: string | null;
  employerId: number | null;
  employerName: string | null;
  employerLogoUrl: string | null;
  employerRating: number | null;
  employerRatingCount: number | null;
  studentId: number | null;
  studentName: string | null;
  studentEmail: string | null;
  studentPhone: string | null;
  university: string | null;
  major: string | null;
  yearOfStudy: number | null;
  studentBio: string | null;
  avatarUrl: string | null;
  coverLetter: string | null;
  cvUrl: string | null;
  cvTitle: string | null;
  expectedSalary: number | null;
  status: string;
  employerNote: string | null;
  rejectReason: string | null;
  appliedAt: string;
  reviewedAt: string | null;
  decidedAt: string | null;
  assignmentId: number | null;
  assignmentStatus: string | null;
  reviewed: boolean;
  history: HistoryItem[];
}

export interface Review {
  id: number;
  applicationId: number | null;
  jobId: number | null;
  jobTitle: string | null;
  reviewerId: number;
  reviewerName: string;
  reviewerRole: string;
  revieweeId: number;
  revieweeName: string;
  rating: number;
  comment: string | null;
  createdAt: string;
}

export interface PendingReview {
  assignmentId: number;
  applicationId: number;
  jobId: number;
  jobTitle: string;
  counterpartName: string;
  counterpartRole: string;
  reviewerRole: string;
}

export interface Notification {
  id: number;
  type: string;
  title: string;
  content: string | null;
  refType: string | null;
  refId: number | null;
  read: boolean;
  createdAt: string;
}

export interface AdminUser {
  id: number;
  email: string;
  fullName: string | null;
  role: string;
  status: string;
  authProvider: string;
  createdAt: string | null;
  lastLoginAt: string | null;
}

export interface AdminJob {
  id: number;
  title: string;
  employerName: string | null;
  categoryName: string | null;
  status: string;
  hidden: boolean;
  applicationCount: number;
  positionsCount: number;
  positionsFilled: number;
  createdAt: string | null;
}

export interface AuditLog {
  id: number;
  actorName: string;
  action: string;
  entityType: string | null;
  entityId: number | null;
  ipAddress: string | null;
  createdAt: string;
}

export interface Dashboard {
  totalUsers: number;
  totalStudents: number;
  totalEmployers: number;
  totalJobs: number;
  openJobs: number;
  totalApplications: number;
  pendingApplications: number;
}

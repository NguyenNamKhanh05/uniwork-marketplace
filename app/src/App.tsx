import { Route, Routes } from "react-router";
import { Layout } from "./components/Layout";
import { Protected } from "./components/Protected";
import { HomePage } from "./pages/HomePage";
import { JobListPage } from "./pages/JobListPage";
import { JobDetailPage } from "./pages/JobDetailPage";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ProfilePage } from "./pages/ProfilePage";
import { NotificationsPage } from "./pages/NotificationsPage";
import { ReviewsPage } from "./pages/ReviewsPage";
import { StudentApplicationsPage } from "./pages/StudentApplicationsPage";
import { ApplicationDetailPage } from "./pages/ApplicationDetailPage";
import { SavedJobsPage } from "./pages/SavedJobsPage";
import { EmployerJobsPage } from "./pages/EmployerJobsPage";
import { EmployerJobFormPage } from "./pages/EmployerJobFormPage";
import { EmployerJobDetailPage } from "./pages/EmployerJobDetailPage";
import { AdminDashboardPage } from "./pages/AdminDashboardPage";
import { AdminUsersPage } from "./pages/AdminUsersPage";
import { AdminJobsPage } from "./pages/AdminJobsPage";
import { AdminCategoriesPage } from "./pages/AdminCategoriesPage";

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="jobs" element={<JobListPage />} />
        <Route path="jobs/:id" element={<JobDetailPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        <Route element={<Protected />}>
          <Route path="profile" element={<ProfilePage />} />
          <Route path="notifications" element={<NotificationsPage />} />
          <Route path="reviews" element={<ReviewsPage />} />
        </Route>

        <Route element={<Protected roles={["STUDENT"]} />}>
          <Route path="student/applications" element={<StudentApplicationsPage />} />
          <Route path="student/applications/:id" element={<ApplicationDetailPage />} />
          <Route path="student/saved-jobs" element={<SavedJobsPage />} />
        </Route>

        <Route element={<Protected roles={["EMPLOYER"]} />}>
          <Route path="employer/jobs" element={<EmployerJobsPage />} />
          <Route path="employer/jobs/new" element={<EmployerJobFormPage />} />
          <Route path="employer/jobs/:id" element={<EmployerJobDetailPage />} />
          <Route path="employer/jobs/:id/edit" element={<EmployerJobFormPage />} />
          <Route path="employer/applications/:id" element={<ApplicationDetailPage />} />
        </Route>

        <Route element={<Protected roles={["ADMIN"]} />}>
          <Route path="admin" element={<AdminDashboardPage />} />
          <Route path="admin/users" element={<AdminUsersPage />} />
          <Route path="admin/jobs" element={<AdminJobsPage />} />
          <Route path="admin/categories" element={<AdminCategoriesPage />} />
        </Route>

        <Route
          path="*"
          element={<p className="notice notice-error">Trang không tồn tại.</p>}
        />
      </Route>
    </Routes>
  );
}

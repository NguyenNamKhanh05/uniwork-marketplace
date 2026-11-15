import axios, { AxiosError } from "axios";
import type { ApiResponse } from "./types";

export const TOKEN_KEY = "uniwork_token";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && localStorage.getItem(TOKEN_KEY)) {
      localStorage.removeItem(TOKEN_KEY);
      window.dispatchEvent(new Event("uniwork:unauthorized"));
    }
    return Promise.reject(error);
  },
);

export function errorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as { message?: string } | undefined;
    if (data?.message) {
      return data.message;
    }
    if (!error.response) {
      return "Không kết nối được máy chủ. Vui lòng kiểm tra backend.";
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return "Đã có lỗi xảy ra. Vui lòng thử lại.";
}

export async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise;
  return response.data.data;
}

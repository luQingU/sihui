import { redirect } from "next/navigation"

export default function ContentPage() {
  // 重定向到视频管理页面
  redirect("/dashboard/content/videos")
}

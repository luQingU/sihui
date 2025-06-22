import { redirect } from "next/navigation"

export default function MonitoringPage() {
  // 重定向到性能监控页面
  redirect("/dashboard/monitoring/performance")
}

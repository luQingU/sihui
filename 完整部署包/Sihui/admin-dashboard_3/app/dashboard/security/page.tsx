import { redirect } from "next/navigation"

export default function SecurityPage() {
  // 重定向到认证管理页面
  redirect("/dashboard/security/auth")
}

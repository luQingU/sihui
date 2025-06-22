import { redirect } from "next/navigation"

export default function AIPage() {
  // 重定向到AI对话管理页面
  redirect("/dashboard/ai/chat")
}

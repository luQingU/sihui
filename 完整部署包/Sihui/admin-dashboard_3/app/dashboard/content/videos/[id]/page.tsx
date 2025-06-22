import { redirect } from "next/navigation"

export default function VideoDetailPage({ params }: { params: { id: string } }) {
  // 重定向到通用内容详情页
  redirect(`/dashboard/content/${params.id}`)
}

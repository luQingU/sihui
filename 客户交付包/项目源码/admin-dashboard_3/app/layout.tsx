import type { Metadata } from 'next'
import './globals.css'
import { AuthGuard } from '@/components/auth-guard'
import { ThemeProvider } from '@/components/theme-provider'

export const metadata: Metadata = {
  title: '四会培训管理平台',
  description: '基于Next.js的管理系统',
  keywords: ['培训管理', '问卷系统', 'AI问答', '内容管理'],
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="zh-CN" suppressHydrationWarning>
      <body>
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange
        >
          <AuthGuard>
            {children}
          </AuthGuard>
        </ThemeProvider>
      </body>
    </html>
  )
}

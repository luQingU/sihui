"use client"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Switch } from "@/components/ui/switch"
import { Plus, Trash2, MoveDown, MoveUp, Save, ArrowLeft } from "lucide-react"
import Link from "next/link"

// 日期选择器组件（简化版）
function CustomDatePicker({ value, onChange }: { value: string; onChange: (value: string) => void }) {
  return <Input type="date" value={value} onChange={(e) => onChange(e.target.value)} />
}

// 问题类型组件
const QuestionTypeSelect = ({ value, onChange }: { value: string; onChange: (value: string) => void }) => (
  <Select value={value} onValueChange={onChange}>
    <SelectTrigger>
      <SelectValue placeholder="选择问题类型" />
    </SelectTrigger>
    <SelectContent>
      <SelectItem value="single">单选题</SelectItem>
      <SelectItem value="multiple">多选题</SelectItem>
      <SelectItem value="text">文本题</SelectItem>
      <SelectItem value="rating">评分题</SelectItem>
    </SelectContent>
  </Select>
)

// 模拟问卷数据
const mockQuestionnaire = {
  id: 1,
  title: "客户满意度调查",
  description: "了解客户对我们产品和服务的满意度",
  endDate: "2025-05-10",
  isAnonymous: true,
  questions: [
    {
      id: 1,
      type: "single",
      title: "您对我们的产品总体满意度如何？",
      required: true,
      options: ["非常满意", "满意", "一般", "不满意", "非常不满意"],
    },
    {
      id: 2,
      type: "multiple",
      title: "您最常使用我们产品的哪些功能？（多选）",
      required: true,
      options: ["功能A", "功能B", "功能C", "功能D", "功能E"],
    },
    {
      id: 3,
      type: "text",
      title: "您对产品有什么建议？",
      required: false,
      options: [],
    },
  ],
}

export default function EditQuestionnairePage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const questionnaireId = searchParams.get("id") || "1"

  const [activeTab, setActiveTab] = useState("basic")
  const [title, setTitle] = useState("")
  const [description, setDescription] = useState("")
  const [endDate, setEndDate] = useState("")
  const [isAnonymous, setIsAnonymous] = useState(true)
  const [questions, setQuestions] = useState<any[]>([])

  // 加载问卷数据
  useEffect(() => {
    // 实际项目中，这里应该调用获取问卷的 API
    setTitle(mockQuestionnaire.title)
    setDescription(mockQuestionnaire.description)
    setEndDate(mockQuestionnaire.endDate)
    setIsAnonymous(mockQuestionnaire.isAnonymous)
    setQuestions(mockQuestionnaire.questions)
  }, [questionnaireId])

  const addQuestion = () => {
    const newId = questions.length > 0 ? Math.max(...questions.map((q) => q.id)) + 1 : 1
    setQuestions([
      ...questions,
      {
        id: newId,
        type: "single",
        title: "",
        required: true,
        options: ["", ""],
      },
    ])
  }

  const removeQuestion = (id: number) => {
    setQuestions(questions.filter((q) => q.id !== id))
  }

  const updateQuestion = (id: number, field: string, value: any) => {
    setQuestions(
      questions.map((q) => {
        if (q.id === id) {
          return { ...q, [field]: value }
        }
        return q
      }),
    )
  }

  const addOption = (questionId: number) => {
    setQuestions(
      questions.map((q) => {
        if (q.id === questionId) {
          return { ...q, options: [...q.options, ""] }
        }
        return q
      }),
    )
  }

  const updateOption = (questionId: number, optionIndex: number, value: string) => {
    setQuestions(
      questions.map((q) => {
        if (q.id === questionId) {
          const newOptions = [...q.options]
          newOptions[optionIndex] = value
          return { ...q, options: newOptions }
        }
        return q
      }),
    )
  }

  const removeOption = (questionId: number, optionIndex: number) => {
    setQuestions(
      questions.map((q) => {
        if (q.id === questionId && q.options.length > 2) {
          const newOptions = [...q.options]
          newOptions.splice(optionIndex, 1)
          return { ...q, options: newOptions }
        }
        return q
      }),
    )
  }

  const moveQuestion = (id: number, direction: "up" | "down") => {
    const index = questions.findIndex((q) => q.id === id)
    if ((direction === "up" && index === 0) || (direction === "down" && index === questions.length - 1)) {
      return
    }

    const newQuestions = [...questions]
    const targetIndex = direction === "up" ? index - 1 : index + 1
    const temp = newQuestions[index]
    newQuestions[index] = newQuestions[targetIndex]
    newQuestions[targetIndex] = temp
    setQuestions(newQuestions)
  }

  const handleSave = () => {
    // 实际项目中，这里应该调用保存问卷的 API
    console.log("保存问卷:", { id: questionnaireId, title, description, endDate, isAnonymous, questions })
    router.push("/dashboard/questionnaires")
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <Button variant="ghost" size="icon" asChild className="mr-2">
            <Link href="/dashboard/questionnaires">
              <ArrowLeft className="h-4 w-4" />
            </Link>
          </Button>
          <h1 className="text-3xl font-bold tracking-tight">编辑问卷</h1>
        </div>
        <Button onClick={handleSave}>
          <Save className="h-4 w-4 mr-2" />
          保存问卷
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList>
          <TabsTrigger value="basic">基本信息</TabsTrigger>
          <TabsTrigger value="questions">问题设计</TabsTrigger>
          <TabsTrigger value="settings">问卷设置</TabsTrigger>
        </TabsList>

        <TabsContent value="basic" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>基本信息</CardTitle>
              <CardDescription>设置问卷的基本信息</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="title">问卷标题</Label>
                <Input
                  id="title"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="请输入问卷标题"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="description">问卷描述</Label>
                <Textarea
                  id="description"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="请输入问卷描述"
                  rows={4}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="end-date">截止日期</Label>
                <CustomDatePicker value={endDate} onChange={setEndDate} />
              </div>
            </CardContent>
            <CardFooter>
              <Button onClick={() => setActiveTab("questions")}>下一步：问题设计</Button>
            </CardFooter>
          </Card>
        </TabsContent>

        <TabsContent value="questions" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>问题设计</CardTitle>
              <CardDescription>添加和编辑问卷问题</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {questions.map((question, index) => (
                <Card key={question.id} className="border-dashed">
                  <CardHeader className="pb-2">
                    <div className="flex justify-between items-center">
                      <CardTitle className="text-sm font-medium">问题 {index + 1}</CardTitle>
                      <div className="flex space-x-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => moveQuestion(question.id, "up")}
                          disabled={index === 0}
                        >
                          <MoveUp className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => moveQuestion(question.id, "down")}
                          disabled={index === questions.length - 1}
                        >
                          <MoveDown className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" onClick={() => removeQuestion(question.id)}>
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-4 gap-4">
                      <div className="col-span-3">
                        <Label htmlFor={`question-${question.id}-title`}>问题标题</Label>
                        <Input
                          id={`question-${question.id}-title`}
                          value={question.title}
                          onChange={(e) => updateQuestion(question.id, "title", e.target.value)}
                          placeholder="请输入问题标题"
                        />
                      </div>
                      <div>
                        <Label htmlFor={`question-${question.id}-type`}>问题类型</Label>
                        <QuestionTypeSelect
                          value={question.type}
                          onChange={(value) => updateQuestion(question.id, "type", value)}
                        />
                      </div>
                    </div>

                    {(question.type === "single" || question.type === "multiple") && (
                      <div className="space-y-2">
                        <Label>选项</Label>
                        {question.options.map((option: string, optionIndex: number) => (
                          <div key={optionIndex} className="flex items-center space-x-2">
                            <Input
                              value={option}
                              onChange={(e) => updateOption(question.id, optionIndex, e.target.value)}
                              placeholder={`选项 ${optionIndex + 1}`}
                            />
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => removeOption(question.id, optionIndex)}
                              disabled={question.options.length <= 2}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        ))}
                        <Button variant="outline" size="sm" onClick={() => addOption(question.id)}>
                          <Plus className="h-4 w-4 mr-2" />
                          添加选项
                        </Button>
                      </div>
                    )}

                    <div className="flex items-center space-x-2">
                      <Switch
                        id={`question-${question.id}-required`}
                        checked={question.required}
                        onCheckedChange={(checked) => updateQuestion(question.id, "required", checked)}
                      />
                      <Label htmlFor={`question-${question.id}-required`}>必填</Label>
                    </div>
                  </CardContent>
                </Card>
              ))}

              <Button variant="outline" onClick={addQuestion}>
                <Plus className="h-4 w-4 mr-2" />
                添加问题
              </Button>
            </CardContent>
            <CardFooter className="flex justify-between">
              <Button variant="outline" onClick={() => setActiveTab("basic")}>
                上一步：基本信息
              </Button>
              <Button onClick={() => setActiveTab("settings")}>下一步：问卷设置</Button>
            </CardFooter>
          </Card>
        </TabsContent>

        <TabsContent value="settings" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>问卷设置</CardTitle>
              <CardDescription>配置问卷的高级设置</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center space-x-2">
                <Switch id="anonymous" checked={isAnonymous} onCheckedChange={setIsAnonymous} />
                <Label htmlFor="anonymous">匿名问卷</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Switch id="one-time" defaultChecked />
                <Label htmlFor="one-time">每人只能提交一次</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Switch id="show-progress" defaultChecked />
                <Label htmlFor="show-progress">显示填写进度</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Switch id="shuffle-questions" />
                <Label htmlFor="shuffle-questions">随机问题顺序</Label>
              </div>
            </CardContent>
            <CardFooter className="flex justify-between">
              <Button variant="outline" onClick={() => setActiveTab("questions")}>
                上一步：问题设计
              </Button>
              <Button onClick={handleSave}>保存问卷</Button>
            </CardFooter>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}

"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { DragDropContext, Droppable, Draggable } from "@hello-pangea/dnd"
import { Layout, Type, ImageIcon, BarChart, Save, Eye, Smartphone, Monitor, Tablet } from "lucide-react"

// 模拟问题组件
const questionComponents = [
  { id: "single-choice", name: "单选题", icon: <BarChart className="h-4 w-4" />, type: "choice" },
  { id: "multiple-choice", name: "多选题", icon: <BarChart className="h-4 w-4" />, type: "choice" },
  { id: "text-input", name: "文本输入", icon: <Type className="h-4 w-4" />, type: "input" },
  { id: "rating", name: "评分题", icon: <BarChart className="h-4 w-4" />, type: "rating" },
  { id: "image-choice", name: "图片选择", icon: <ImageIcon className="h-4 w-4" />, type: "media" },
]

export default function QuestionnaireDesignerPage() {
  const [selectedDevice, setSelectedDevice] = useState("desktop")
  const [questions, setQuestions] = useState([
    { id: "q1", type: "single-choice", title: "您对我们的服务满意度如何？", required: true },
    { id: "q2", type: "text-input", title: "请留下您的宝贵建议", required: false },
  ])

  const handleDragEnd = (result: any) => {
    if (!result.destination) return

    const items = Array.from(questions)
    const [reorderedItem] = items.splice(result.source.index, 1)
    items.splice(result.destination.index, 0, reorderedItem)

    setQuestions(items)
  }

  const getDeviceIcon = (device: string) => {
    switch (device) {
      case "desktop":
        return <Monitor className="h-4 w-4" />
      case "tablet":
        return <Tablet className="h-4 w-4" />
      case "mobile":
        return <Smartphone className="h-4 w-4" />
      default:
        return <Monitor className="h-4 w-4" />
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">可视化问卷设计器</h1>
          <p className="text-muted-foreground">拖拽式问卷设计，支持多设备预览</p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline">
            <Eye className="h-4 w-4 mr-2" />
            预览
          </Button>
          <Button>
            <Save className="h-4 w-4 mr-2" />
            保存问卷
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-4">
        {/* 组件库 */}
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="text-lg">组件库</CardTitle>
            <CardDescription>拖拽组件到设计区域</CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="questions" className="space-y-4">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="questions">问题</TabsTrigger>
                <TabsTrigger value="layout">布局</TabsTrigger>
              </TabsList>

              <TabsContent value="questions" className="space-y-2">
                {questionComponents.map((component) => (
                  <div
                    key={component.id}
                    className="p-3 border rounded-lg cursor-move hover:bg-muted/50 transition-colors"
                    draggable
                  >
                    <div className="flex items-center space-x-2">
                      {component.icon}
                      <span className="text-sm font-medium">{component.name}</span>
                    </div>
                  </div>
                ))}
              </TabsContent>

              <TabsContent value="layout" className="space-y-2">
                <div className="p-3 border rounded-lg cursor-move hover:bg-muted/50 transition-colors">
                  <div className="flex items-center space-x-2">
                    <Layout className="h-4 w-4" />
                    <span className="text-sm font-medium">分页符</span>
                  </div>
                </div>
                <div className="p-3 border rounded-lg cursor-move hover:bg-muted/50 transition-colors">
                  <div className="flex items-center space-x-2">
                    <Type className="h-4 w-4" />
                    <span className="text-sm font-medium">说明文字</span>
                  </div>
                </div>
                <div className="p-3 border rounded-lg cursor-move hover:bg-muted/50 transition-colors">
                  <div className="flex items-center space-x-2">
                    <ImageIcon className="h-4 w-4" />
                    <span className="text-sm font-medium">图片</span>
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        {/* 设计区域 */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-lg">设计区域</CardTitle>
                <CardDescription>拖拽排列问题顺序</CardDescription>
              </div>
              <div className="flex space-x-1">
                {["desktop", "tablet", "mobile"].map((device) => (
                  <Button
                    key={device}
                    variant={selectedDevice === device ? "default" : "outline"}
                    size="sm"
                    onClick={() => setSelectedDevice(device)}
                  >
                    {getDeviceIcon(device)}
                  </Button>
                ))}
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div
              className={`border-2 border-dashed border-muted-foreground/25 rounded-lg p-4 min-h-[500px] ${
                selectedDevice === "mobile"
                  ? "max-w-sm mx-auto"
                  : selectedDevice === "tablet"
                    ? "max-w-2xl mx-auto"
                    : "w-full"
              }`}
            >
              <div className="mb-4 p-4 bg-muted/20 rounded-lg">
                <h3 className="font-medium mb-2">问卷标题</h3>
                <Input placeholder="请输入问卷标题" className="mb-2" />
                <Input placeholder="问卷描述（可选）" />
              </div>

              <DragDropContext onDragEnd={handleDragEnd}>
                <Droppable droppableId="questions">
                  {(provided) => (
                    <div {...provided.droppableProps} ref={provided.innerRef} className="space-y-4">
                      {questions.map((question, index) => (
                        <Draggable key={question.id} draggableId={question.id} index={index}>
                          {(provided) => (
                            <div
                              ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps}
                              className="p-4 border rounded-lg bg-white hover:shadow-md transition-shadow"
                            >
                              <div className="flex items-center justify-between mb-2">
                                <span className="text-sm text-muted-foreground">问题 {index + 1}</span>
                                <div className="flex items-center space-x-2">
                                  {question.required && <Badge variant="secondary">必填</Badge>}
                                  <Badge variant="outline">{question.type}</Badge>
                                </div>
                              </div>
                              <div className="font-medium mb-3">{question.title}</div>

                              {question.type === "single-choice" && (
                                <div className="space-y-2">
                                  <div className="flex items-center space-x-2">
                                    <input type="radio" disabled />
                                    <span className="text-sm">选项 1</span>
                                  </div>
                                  <div className="flex items-center space-x-2">
                                    <input type="radio" disabled />
                                    <span className="text-sm">选项 2</span>
                                  </div>
                                </div>
                              )}

                              {question.type === "text-input" && <Input placeholder="用户输入区域" disabled />}
                            </div>
                          )}
                        </Draggable>
                      ))}
                      {provided.placeholder}
                    </div>
                  )}
                </Droppable>
              </DragDropContext>

              <div className="mt-6 p-4 border-2 border-dashed border-muted-foreground/25 rounded-lg text-center text-muted-foreground">
                拖拽组件到此处添加新问题
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 属性面板 */}
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="text-lg">属性设置</CardTitle>
            <CardDescription>配置选中组件的属性</CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="question" className="space-y-4">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="question">问题</TabsTrigger>
                <TabsTrigger value="style">样式</TabsTrigger>
              </TabsList>

              <TabsContent value="question" className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="question-title">问题标题</Label>
                  <Input id="question-title" placeholder="输入问题标题" />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="question-desc">问题描述</Label>
                  <Input id="question-desc" placeholder="问题描述（可选）" />
                </div>

                <div className="flex items-center space-x-2">
                  <input type="checkbox" id="required" />
                  <Label htmlFor="required">必填项</Label>
                </div>

                <div className="space-y-2">
                  <Label>选项设置</Label>
                  <div className="space-y-2">
                    <Input placeholder="选项 1" />
                    <Input placeholder="选项 2" />
                    <Button variant="outline" size="sm" className="w-full">
                      添加选项
                    </Button>
                  </div>
                </div>
              </TabsContent>

              <TabsContent value="style" className="space-y-4">
                <div className="space-y-2">
                  <Label>主题颜色</Label>
                  <div className="flex space-x-2">
                    <div className="w-8 h-8 bg-blue-500 rounded cursor-pointer border-2 border-blue-600"></div>
                    <div className="w-8 h-8 bg-green-500 rounded cursor-pointer"></div>
                    <div className="w-8 h-8 bg-purple-500 rounded cursor-pointer"></div>
                    <div className="w-8 h-8 bg-orange-500 rounded cursor-pointer"></div>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="font-size">字体大小</Label>
                  <Input id="font-size" type="number" placeholder="14" />
                </div>

                <div className="space-y-2">
                  <Label>布局方向</Label>
                  <div className="flex space-x-2">
                    <Button variant="outline" size="sm">
                      垂直
                    </Button>
                    <Button variant="outline" size="sm">
                      水平
                    </Button>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <input type="checkbox" id="show-progress" />
                  <Label htmlFor="show-progress">显示进度条</Label>
                </div>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

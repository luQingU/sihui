import { api } from '../api'
import type { 
  FileInfo,
  UploadFileRequest,
  PaginationParams,
  PageResponse
} from '../../types/api'

// 内容管理服务
export const contentService = {
  // 上传单个文件
  async uploadFile(file: File, metadata?: UploadFileRequest): Promise<FileInfo> {
    const formData = new FormData()
    formData.append('file', file)
    
    if (metadata?.category) formData.append('category', metadata.category)
    if (metadata?.description) formData.append('description', metadata.description)
    if (metadata?.folder) formData.append('folder', metadata.folder)
    if (metadata?.isPublic !== undefined) formData.append('isPublic', String(metadata.isPublic))
    if (metadata?.tags) formData.append('tags', metadata.tags)

    return api.upload<FileInfo>('/api/contents/upload', formData)
  },

  // 批量上传文件
  async uploadFiles(files: File[], metadata?: UploadFileRequest): Promise<FileInfo[]> {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    
    if (metadata?.category) formData.append('category', metadata.category)
    if (metadata?.description) formData.append('description', metadata.description)
    if (metadata?.folder) formData.append('folder', metadata.folder)
    if (metadata?.isPublic !== undefined) formData.append('isPublic', String(metadata.isPublic))
    if (metadata?.tags) formData.append('tags', metadata.tags)

    return api.upload<FileInfo[]>('/api/contents/upload/batch', formData)
  },

  // 获取文件列表
  async getFiles(params?: PaginationParams & {
    category?: string
    folder?: string
    tags?: string[]
    isPublic?: boolean
    keyword?: string
  }): Promise<PageResponse<FileInfo>> {
    return api.get<PageResponse<FileInfo>>('/api/contents', params)
  },

  // 获取文件详情
  async getFile(fileName: string): Promise<FileInfo> {
    return api.get<FileInfo>(`/api/contents/${encodeURIComponent(fileName)}`)
  },

  // 删除文件
  async deleteFile(fileName: string): Promise<void> {
    return api.delete(`/api/contents/${encodeURIComponent(fileName)}`)
  },

  // 批量删除文件
  async batchDeleteFiles(fileNames: string[]): Promise<void> {
    return api.delete('/api/contents/batch', { fileNames })
  },

  // 检查文件是否存在
  async checkFileExists(fileName: string): Promise<{ exists: boolean }> {
    return api.get<{ exists: boolean }>(`/api/contents/exists/${encodeURIComponent(fileName)}`)
  },

  // 获取文件签名URL
  async getSignedUrl(fileName: string, expiredInSeconds?: number): Promise<{ url: string; expiresAt: string }> {
    return api.get<{ url: string; expiresAt: string }>(`/api/contents/signed-url/${encodeURIComponent(fileName)}`, {
      expiredInSeconds: expiredInSeconds || 3600
    })
  },

  // 更新文件信息
  async updateFileInfo(fileName: string, data: Partial<{
    category: string
    description: string
    isPublic: boolean
    tags: string[]
  }>): Promise<FileInfo> {
    return api.put<FileInfo>(`/api/contents/${encodeURIComponent(fileName)}/info`, data)
  },

  // 移动文件到新文件夹
  async moveFile(fileName: string, newFolder: string): Promise<FileInfo> {
    return api.patch<FileInfo>(`/api/contents/${encodeURIComponent(fileName)}/move`, {
      newFolder
    })
  },

  // 复制文件
  async copyFile(fileName: string, newFileName: string, folder?: string): Promise<FileInfo> {
    return api.post<FileInfo>(`/api/contents/${encodeURIComponent(fileName)}/copy`, {
      newFileName,
      folder
    })
  },

  // 获取文件分类列表
  async getCategories(): Promise<string[]> {
    return api.get<string[]>('/api/contents/categories')
  },

  // 获取文件夹列表
  async getFolders(): Promise<string[]> {
    return api.get<string[]>('/api/contents/folders')
  },

  // 获取热门标签
  async getPopularTags(limit?: number): Promise<Array<{
    tag: string
    count: number
  }>> {
    return api.get('/api/contents/tags/popular', {
      limit: limit || 20
    })
  },

  // 搜索文件
  async searchFiles(params: {
    keyword: string
    category?: string
    tags?: string[]
    fileType?: 'image' | 'video' | 'document' | 'audio'
    page?: number
    size?: number
  }): Promise<PageResponse<FileInfo>> {
    return api.get<PageResponse<FileInfo>>('/api/contents/search', params)
  },

  // 获取文件统计信息
  async getFileStats(): Promise<{
    totalFiles: number
    totalSize: number
    byCategory: Array<{
      category: string
      count: number
      size: number
    }>
    byType: Array<{
      type: string
      count: number
      size: number
    }>
    recentUploads: number
    popularFiles: Array<{
      fileName: string
      downloadCount: number
    }>
  }> {
    return api.get('/api/contents/stats')
  }
}

// 视频内容管理服务
export const videoService = {
  // 获取视频列表
  async getVideos(params?: PaginationParams & {
    category?: string
    duration?: 'short' | 'medium' | 'long'
    quality?: '720p' | '1080p' | '4k'
    keyword?: string
  }): Promise<PageResponse<FileInfo & {
    duration: number
    resolution: string
    thumbnail: string
    metadata: any
  }>> {
    return api.get('/api/contents/videos', params)
  },

  // 获取视频详情
  async getVideo(id: number): Promise<FileInfo & {
    duration: number
    resolution: string
    thumbnail: string
    metadata: any
    playUrl: string
  }> {
    return api.get(`/api/contents/videos/${id}`)
  },

  // 生成视频缩略图
  async generateThumbnail(fileName: string, timeOffset?: number): Promise<{
    thumbnailUrl: string
  }> {
    return api.post(`/api/contents/videos/${encodeURIComponent(fileName)}/thumbnail`, {
      timeOffset: timeOffset || 0
    })
  },

  // 获取视频播放统计
  async getVideoStats(id: number): Promise<{
    totalViews: number
    totalDuration: number
    averageWatchTime: number
    completionRate: number
    viewsByDay: Array<{
      date: string
      views: number
    }>
  }> {
    return api.get(`/api/contents/videos/${id}/stats`)
  }
}

// 文档内容管理服务
export const documentService = {
  // 获取文档列表
  async getDocuments(params?: PaginationParams & {
    category?: string
    format?: 'PDF' | 'WORD' | 'EXCEL' | 'PPT'
    keyword?: string
  }): Promise<PageResponse<FileInfo & {
    pageCount: number
    wordCount: number
    preview: string
  }>> {
    return api.get('/api/contents/documents', params)
  },

  // 获取文档详情
  async getDocument(id: number): Promise<FileInfo & {
    pageCount: number
    wordCount: number
    preview: string
    downloadUrl: string
  }> {
    return api.get(`/api/contents/documents/${id}`)
  },

  // 文档格式转换
  async convertDocument(fileName: string, targetFormat: 'PDF' | 'WORD' | 'HTML'): Promise<{
    convertedFileName: string
    downloadUrl: string
  }> {
    return api.post(`/api/contents/documents/${encodeURIComponent(fileName)}/convert`, {
      targetFormat
    })
  },

  // 生成文档预览
  async generatePreview(fileName: string, pageNumber?: number): Promise<{
    previewUrl: string
    totalPages: number
  }> {
    return api.post(`/api/contents/documents/${encodeURIComponent(fileName)}/preview`, {
      pageNumber: pageNumber || 1
    })
  }
} 
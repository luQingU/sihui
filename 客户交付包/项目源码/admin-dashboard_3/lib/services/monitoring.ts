import { api } from '../api'
import type { 
  SystemHealth,
  SystemMetrics,
  PerformanceStats,
  SlowQuery
} from '../../types/api'

// 系统监控服务
export const monitoringService = {
  // 系统健康检查
  async getSystemHealth(): Promise<SystemHealth> {
    return api.get<SystemHealth>('/api/monitoring/health')
  },

  // 获取系统指标
  async getSystemMetrics(): Promise<SystemMetrics> {
    return api.get<SystemMetrics>('/api/monitoring/metrics')
  },

  // 获取JWT性能统计
  async getJwtPerformanceStats(): Promise<{
    totalTokensGenerated: number
    totalTokensValidated: number
    averageGenerationTime: number
    averageValidationTime: number
    errorRate: number
    peakUsage: number
  }> {
    return api.get('/api/monitoring/jwt/performance')
  },

  // 重置JWT性能统计
  async resetJwtPerformanceStats(): Promise<void> {
    return api.post('/api/monitoring/jwt/performance/reset')
  },

  // 生成JWT性能报告
  async generateJwtPerformanceReport(): Promise<{
    reportId: string
    reportContent: string
    generatedAt: string
  }> {
    return api.post('/api/monitoring/jwt/performance/report')
  }
}

// 性能监控服务
export const performanceService = {
  // 获取性能概览
  async getPerformanceOverview(): Promise<{
    systemStatus: 'HEALTHY' | 'WARNING' | 'CRITICAL'
    uptime: number
    cpuUsage: number
    memoryUsage: number
    diskUsage: number
    activeConnections: number
    requestsPerMinute: number
    averageResponseTime: number
    errorRate: number
  }> {
    return api.get('/api/performance/overview')
  },

  // 获取性能统计
  async getPerformanceStats(): Promise<PerformanceStats> {
    return api.get<PerformanceStats>('/api/performance/stats')
  },

  // 清除性能统计
  async clearPerformanceStats(): Promise<void> {
    return api.delete('/api/performance/stats')
  },

  // 获取系统资源
  async getSystemResources(): Promise<{
    cpu: {
      usage: number
      cores: number
      loadAverage: number[]
    }
    memory: {
      total: number
      used: number
      free: number
      cached: number
    }
    disk: {
      total: number
      used: number
      free: number
    }
    network: {
      bytesIn: number
      bytesOut: number
      packetsIn: number
      packetsOut: number
    }
  }> {
    return api.get('/api/performance/resources')
  },

  // 获取性能健康状态
  async getPerformanceHealth(): Promise<{
    status: 'HEALTHY' | 'WARNING' | 'CRITICAL'
    issues: Array<{
      type: string
      severity: 'LOW' | 'MEDIUM' | 'HIGH'
      message: string
      suggestions: string[]
    }>
    lastChecked: string
  }> {
    return api.get('/api/performance/health')
  },

  // 获取慢查询日志
  async getSlowQueries(limit?: number): Promise<SlowQuery[]> {
    return api.get<SlowQuery[]>('/api/performance/slow-queries', {
      limit: limit || 100
    })
  }
}

// 性能测试服务
export const performanceTestService = {
  // 系统健康检查
  async testSystemHealth(): Promise<{
    status: 'PASS' | 'FAIL'
    checks: Array<{
      name: string
      status: 'PASS' | 'FAIL'
      duration: number
      details?: string
    }>
  }> {
    return api.get('/api/performance/test-health')
  },

  // 获取性能指标
  async getTestMetrics(): Promise<{
    timestamp: string
    metrics: {
      responseTime: number
      throughput: number
      errorRate: number
      concurrency: number
    }
  }> {
    return api.get('/api/performance/metrics')
  },

  // CPU负载测试
  async runCpuLoadTest(params?: {
    duration?: number
    intensity?: 'low' | 'medium' | 'high'
  }): Promise<{
    testId: string
    status: 'RUNNING' | 'COMPLETED' | 'FAILED'
    results?: {
      averageCpuUsage: number
      peakCpuUsage: number
      duration: number
    }
  }> {
    return api.post('/api/performance/load-test/cpu', params)
  },

  // 内存负载测试
  async runMemoryLoadTest(params?: {
    duration?: number
    memorySize?: number
  }): Promise<{
    testId: string
    status: 'RUNNING' | 'COMPLETED' | 'FAILED'
    results?: {
      averageMemoryUsage: number
      peakMemoryUsage: number
      gcCount: number
    }
  }> {
    return api.post('/api/performance/load-test/memory', params)
  },

  // 数据库连接池测试
  async runDatabaseLoadTest(params?: {
    concurrency?: number
    duration?: number
  }): Promise<{
    testId: string
    status: 'RUNNING' | 'COMPLETED' | 'FAILED'
    results?: {
      totalConnections: number
      activeConnections: number
      averageConnectionTime: number
      errors: number
    }
  }> {
    return api.post('/api/performance/load-test/database', params)
  },

  // 缓存性能测试
  async runCacheLoadTest(params?: {
    operations?: number
    keySize?: number
    valueSize?: number
  }): Promise<{
    testId: string
    status: 'RUNNING' | 'COMPLETED' | 'FAILED'
    results?: {
      readThroughput: number
      writeThroughput: number
      hitRate: number
      averageLatency: number
    }
  }> {
    return api.post('/api/performance/load-test/cache', params)
  },

  // 缓存预热
  async warmupCache(): Promise<{
    status: 'SUCCESS' | 'FAILED'
    preloadedKeys: number
    duration: number
  }> {
    return api.post('/api/performance/cache/warmup')
  },

  // 清理所有缓存
  async clearAllCache(): Promise<{
    status: 'SUCCESS' | 'FAILED'
    clearedKeys: number
  }> {
    return api.delete('/api/performance/cache/clear')
  }
}

// 性能优化服务
export const performanceOptimizationService = {
  // API性能分析
  async analyzeApiPerformance(): Promise<{
    topSlowEndpoints: Array<{
      endpoint: string
      averageTime: number
      callCount: number
      errorRate: number
    }>
    suggestions: string[]
  }> {
    return api.get('/api/performance/analysis/api')
  },

  // 查询性能分析
  async analyzeQueryPerformance(): Promise<{
    slowQueries: SlowQuery[]
    indexSuggestions: Array<{
      table: string
      columns: string[]
      reason: string
    }>
    optimizationTips: string[]
  }> {
    return api.get('/api/performance/analysis/query')
  },

  // 内存使用分析
  async analyzeMemoryUsage(): Promise<{
    heapAnalysis: {
      used: number
      max: number
      usage: number
    }
    gcAnalysis: {
      frequency: number
      averageDuration: number
      recommendations: string[]
    }
    memoryLeaks: Array<{
      source: string
      severity: 'LOW' | 'MEDIUM' | 'HIGH'
      description: string
    }>
  }> {
    return api.get('/api/performance/analysis/memory')
  },

  // 缓存分析
  async analyzeCaching(): Promise<{
    cacheStats: {
      hitRate: number
      missRate: number
      evictionRate: number
    }
    recommendations: string[]
    inefficientCaches: Array<{
      cacheName: string
      issue: string
      suggestion: string
    }>
  }> {
    return api.get('/api/performance/analysis/caching')
  },

  // 最佳实践审查
  async reviewBestPractices(): Promise<{
    score: number
    checks: Array<{
      category: string
      status: 'PASS' | 'FAIL' | 'WARNING'
      message: string
      impact: 'LOW' | 'MEDIUM' | 'HIGH'
    }>
    recommendations: string[]
  }> {
    return api.get('/api/performance/review/best-practices')
  },

  // 数据库索引审查
  async reviewDatabaseIndexes(): Promise<{
    existingIndexes: Array<{
      table: string
      index: string
      usage: number
      effectiveness: number
    }>
    missingIndexes: Array<{
      table: string
      columns: string[]
      estimatedImpact: number
    }>
    unusedIndexes: Array<{
      table: string
      index: string
      lastUsed: string
    }>
  }> {
    return api.get('/api/performance/review/database-indexes')
  },

  // 综合性能审查
  async comprehensiveReview(): Promise<{
    overallScore: number
    categories: Array<{
      name: string
      score: number
      issues: number
      recommendations: string[]
    }>
    priorityActions: Array<{
      action: string
      impact: 'LOW' | 'MEDIUM' | 'HIGH'
      effort: 'LOW' | 'MEDIUM' | 'HIGH'
    }>
  }> {
    return api.post('/api/performance/review/comprehensive')
  },

  // 优化用户查询
  async optimizeUserQueries(): Promise<{
    optimizedQueries: number
    estimatedImprovement: number
    details: Array<{
      originalQuery: string
      optimizedQuery: string
      improvement: number
    }>
  }> {
    return api.post('/api/performance/optimization/user-queries')
  },

  // 执行基准测试
  async runBenchmark(params?: {
    testSuite?: string
    duration?: number
    concurrency?: number
  }): Promise<{
    benchmarkId: string
    status: 'RUNNING' | 'COMPLETED' | 'FAILED'
    results?: {
      throughput: number
      latency: {
        p50: number
        p95: number
        p99: number
      }
      errorRate: number
      resourceUsage: {
        cpu: number
        memory: number
      }
    }
  }> {
    return api.post('/api/performance/benchmark', params)
  },

  // 生成优化报告
  async generateOptimizationReport(): Promise<{
    reportId: string
    reportUrl: string
    generatedAt: string
    summary: {
      currentPerformance: number
      potentialImprovement: number
      priorityRecommendations: string[]
    }
  }> {
    return api.post('/api/performance/report/optimization')
  }
} 
export type AiAnalysisStatus
  = | 'READY'
    | 'NO_HISTORY'
    | 'SERVICE_UNAVAILABLE'
    | 'MODEL_ERROR'
    | 'DISABLED'
    | 'RATE_LIMITED'

export interface AiCitation {
  index: number
  postId: number
  commentId: number
  title: string
  url: string
}

export interface AiRelatedPost {
  postId: number
  title: string
  category: string
  summary: string
  createdAt: string
  matchScore: number
  evidenceCount: number
  url: string
}

export interface AiRelatedReply {
  postId: number
  commentId: number
  citationIndex: number
  title: string
  sourceType: string
  content: string
  createdAt: string
  score: number
  url: string
}

export interface PostAiAnalysis {
  status: AiAnalysisStatus
  conclusion: string
  citations: AiCitation[]
  relatedPosts: AiRelatedPost[]
  relatedReplies: AiRelatedReply[]
  notice: string
  generatedAt?: string
  model?: string
  latencyMs?: number
}

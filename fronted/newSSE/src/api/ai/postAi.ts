import type { PostAiAnalysis } from '@/types/ai'
import { requestFunc } from '../req'

async function getPostAiAnalysis(postId: number): Promise<PostAiAnalysis | null> {
  try {
    const res = await requestFunc(
      `/auth/post/${postId}/ai-analysis`,
      {
        method: 'GET',
        headers: {},
      },
      true,
    )
    if (!res || !res.ok) {
      return null
    }
    const json = await res.json()
    if (json?.code !== 200 || !json.data) {
      return null
    }
    return json.data as PostAiAnalysis
  }
  catch (e) {
    console.error('获取 AI 分析失败', e)
    return null
  }
}

export { getPostAiAnalysis }

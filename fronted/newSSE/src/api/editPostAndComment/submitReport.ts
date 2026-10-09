import { requestFunc } from '../req'

export async function submitReport(
  targetID: number,
  targetType: 'post' | 'pcomment' | 'ccomment',
  reason: string,
) {
  const res = await requestFunc('/auth/submitReport', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: { targetID, targetType, reason },
  }, true)
  if (!res || !res.ok) {
    throw new Error('举报失败')
  }
  return res.json()
}

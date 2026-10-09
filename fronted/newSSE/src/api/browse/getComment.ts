import type { Comment, SubComment } from '@/types/comment'
import { requestFunc } from '../req'

/**
 * 后端 RCommentResponseVO 使用小驼峰，前端 SubComment 接口使用自己的风格，这里做转换
 */
function mapSubComment(data: any): SubComment {
  return {
    ccommentID: data.commentId ?? 0,
    author: data.username ?? '',
    authorID: data.userId ?? 0,
    authorScore: data.authorScore ?? 0,
    authorAvatar: data.authorAvatar ?? '',
    authorIdentity: data.authorIdentity ?? '',
    commentTime: data.createTime ?? '',
    content: data.content ?? '',
    likeNum: data.likeNum ?? 0,
    denyNum: data.denyNum ?? 0,
    isLiked: data.isLiked ?? false,
    isDenied: data.isDenied ?? false,
    userTargetName: data.replyUsername ?? '',
    showMenu: false,
  }
}

/**
 * 后端 CommentResponseVO 使用小驼峰，前端 Comment 接口使用属于自己的风格，这里做转换
 */
function mapComment(data: any): Comment {
  return {
    PcommentID: data.commentId ?? 0,
    AuthorID: data.userId ?? 0,
    Author: data.username ?? '',
    AuthorScore: data.authorScore ?? 0,
    AuthorAvatar: data.authorAvatar ?? '',
    AuthorIdentity: data.authorIdentity ?? '',
    CommentTime: data.createTime ?? '',
    Content: data.content ?? '',
    LikeNum: data.likeNum ?? 0,
    DenyNum: data.denyNum ?? 0,
    SubComments: Array.isArray(data.children)
      ? data.children.map(mapSubComment)
      : [],
    IsLiked: data.isLiked ?? false,
    IsDenied: data.isDenied ?? false,
  }
}

/**
 * @param {number} PostID 帖子ID
 */
async function getCommentsByPostID(PostID: number): Promise<Comment[]> {
  try {
    const res = await requestFunc(
      `/auth/showPcomments`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          postID: PostID,
        },
      },
      true,
    )
    if (!res!.ok) {
      console.error('获取评论失败:', res!.status, res!.statusText)
      return []
    }

    const data = await res!.json()
    return (data as any[]).map(mapComment)
  }
  catch (e) {
    console.error(e)
    return []
  }
}

export { getCommentsByPostID }

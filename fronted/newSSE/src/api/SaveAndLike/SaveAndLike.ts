import { requestFunc } from '../req'

/**
 * @description 后端未返回数据，只能通过状态码判断是否成功
 * @param {number} postID
 * @returns 如果状态码2xx，返回true
 */
async function likePost(postID: number) {
  try {
    // 没有返回数据，只能通过状态码判断
    const res = await requestFunc(`/auth/updateLike`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        postID,
      },
    }, true)
    return res?.ok || false
  }
  catch (e) {
    console.error(e)
    return false
  }
}

/***
 * @param {number} pcommentID 评论id
 * @returns 如果状态码2xx，返回true
 */
async function likePostComment(pcommentID: number) {
  try {
    const res = await requestFunc(`/auth/updatePcommentLike`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        pcommentID,
      },
    }, true)
    return res?.ok || false
  }
  catch (e) {
    console.error(e)
    return false
  }
}

/***
 * @param {number} ccommentID 评论的评论id
 * @returns 如果状态码2xx，返回true
 */
async function likeCommentComment(ccommentID: number) {
  try {
    const res = await requestFunc(`/auth/updateCcommentLike`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        ccommentID,
      },
    }, true)
    return res?.ok || false
  }
  catch (e) {
    console.error(e)
    return false
  }
}

/***
 * @param {number} pcommentID 评论id
 * @returns 如果状态码2xx，返回true
 */
async function denyPostComment(pcommentID: number) {
  try {
    const res = await requestFunc(`/auth/updatePcommentDeny`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        pcommentID,
      },
    }, true)
    return res?.ok || false
  }
  catch (e) {
    console.error(e)
    return false
  }
}

/***
 * @param {number} ccommentID 评论的评论id
 * @returns 如果状态码2xx，返回true
 */
async function denyCommentComment(ccommentID: number) {
  try {
    const res = await requestFunc(`/auth/updateCcommentDeny`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        ccommentID,
      },
    }, true)
    return res?.ok || false
  }
  catch (e) {
    console.error(e)
    return false
  }
}

/***
 * @param {number} postID 要收藏的帖子id
 * @returns 如果状态码2xx，返回true
 */
async function savePost(postID: number) {
  try {
    const res = await requestFunc(`/auth/updateSave`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        postID,
      },
    }, true)
    return res?.ok || false
  }
  catch (e) {
    console.error(e)
    return false
  }
}

export { denyCommentComment, denyPostComment, likeCommentComment, likePost, likePostComment, savePost }

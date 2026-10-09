import { requestFunc } from '../req'

/**
 * @description 给帖子发一级评论，评分帖可附带 rating
 * @param {string} content
 * @param {number} postID
 * @param {number} rating 可选，评分帖传入
 */
async function sendComment(
  content: string,
  postID: number,
  rating?: number,
) {
  try {
    const res = await requestFunc(
      `/auth/postPcomment`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          content,
          postID,
          ...(rating !== undefined && rating > 0 ? { rating } : {}),
        },
      },
      true,
    )
    return res!.ok
  }
  catch (e) {
    console.error(e)
    return false
  }
}

export interface sendPCommentObject {
  content: string
  postID: number
  pcommentID: number
  ccommentID?: number
}

/**
 * @description 给评论发评论
 * @param {{content:string,postID:number,pcommentID:number}} object 如果是给评论的评论发评论，需要多一个ccommentID:number
 */
async function sendPComment(object: sendPCommentObject) {
  try {
    const res = await requestFunc(
      `/auth/postCcomment`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: object,
      },
      true,
    )
    return res!.ok
  }
  catch (e) {
    console.error(e)
    return false
  }
}

/**
 *
 * @param {number} pcommentID
 */
async function delComment(pcommentID: number) {
  try {
    const res = await requestFunc(
      `/auth/deletePcomment`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          pcommentID,
        },
      },
      true,
    )
    return res!.ok
  }
  catch (e) {
    console.error(e)
    return false
  }
}

/**
 *
 * @param {number} ccommentID
 */
async function delCcomment(ccommentID: number) {
  try {
    const res = await requestFunc(
      `/auth/deleteCcomment`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          ccommentID,
        },
      },
      true,
    )
    return res!.ok
  }
  catch (e) {
    console.error(e)
    return false
  }
}


/**
 * @description 单独提交评分
 * @param {number} postID 帖子ID
 * @param {number} rating 评分
 */
async function submitRating(
  postID: number,
  rating: number,
) {
  try {
    const res = await requestFunc(
      `/auth/submitRating`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          postID: postID,
          rating: rating,
        },
      },
      true,
    )
    return res!.ok
  }
  catch (e) {
    console.error('submitRating 错误:', e)
    return false
  }
}

/**
 * @description 获取用户对帖子的评分
 * @param {number} postID 帖子ID
 */
async function getUserPostRating(postID: number) {
  try {
    const res = await requestFunc(
      `/auth/userPostRating`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          postID: postID,
        },
      },
      true,
    )

    if (res!.ok) {
      const data = await res!.json()
      return data.data || 0
    }
    else {
      return 0
    }
  }
  catch (e) {
    console.error('获取用户评分失败:', e)
    return 0
  }
}

/**
 * @description 获取帖子的评分分布
 * @param {number} postID 帖子ID
 */
async function getStarsDistribution(postID: number) {
  try {
    const res = await requestFunc(
      `/auth/getStars`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          postID: postID,
        },
      },
      true,
    )

    if (res!.ok) {
      const data = await res!.json()
      const stars = [data.data.star1 || 0, data.data.star2 || 0, data.data.star3 || 0, data.data.star4 || 0, data.data.star5 || 0]
      return stars as [number, number, number, number, number]
    }
    else {
      return [0, 0, 0, 0, 0] as [number, number, number, number, number]
    }
  }
  catch (e) {
    console.error('获取评分分布失败:', e)
    return [0, 0, 0, 0, 0] as [number, number, number, number, number]
  }
}

/**
 * @description 获取帖子的平均评分
 * @param {number} postID 帖子ID
 */
async function getAverageRating(postID: number) {
  try {
    const res = await requestFunc(
      `/auth/getAverageRating`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          postID: postID,
        },
      },
      true,
    )

    if (res!.ok) {
      const data = await res!.json()
      return data.data.averageRating || 0
    }
    else {
      return 0
    }
  }
  catch (e) {
    console.error('获取平均评分失败:', e)
    return 0
  }
}

export { delCcomment, delComment, getAverageRating, getStarsDistribution, getUserPostRating, sendComment, sendPComment, submitRating }

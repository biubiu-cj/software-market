import { requestFunc } from '../req'

/**
 *
 * @param {string} content 内容
 * @param {string} category 分区
 * @param {string} photos 只有旧前端需要，新前端已弃用。应该为空字符串
 * @param {string} tagList 如果专区是课程专区，是一个老师；不是，为空
 * @param {string} title 标题
 */
async function sendPost(
  content: string,
  category: string,
  photos: string,
  tagList: string,
  title: string,
) {
  try {
    const res = await requestFunc(`/auth/post`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        content,
        category,
        photos,
        tagList,
        title,
      },
    }, true)
    const data = await res!.json()
    return data
  }
  catch (e) {
    console.error(e)
    return null
  }
}

/**
 *
 * @param {number} postID
 */
async function delPost(postID: number) {
  try {
    const res = await requestFunc(`/auth/deletePost`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        postID,
      },
    }, true)
    const data = await res!.json()
    return data
  }
  catch (e) {
    console.error(e)
    return null
  }
}

export { delPost, sendPost }

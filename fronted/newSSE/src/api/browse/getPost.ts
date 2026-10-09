import type { Post, PostType, Rating } from '@/types/post'
import { requestFunc } from '../req'

export interface getPostsObject {
  limit: number
  offset: number
  category: string
  searchSort: string
  searchInfo: string
  tag: string
}

const defaultPost: Post = {
  PostID: 0,
  UserID: 0,
  UserName: '',
  UserScore: 0,
  UserAvatar: '',
  UserIdentity: '',
  Title: '',
  Content: '',
  Like: 0,
  Comment: 0,
  Browse: 0,
  Heat: 0,
  PostTime: '',
  IsSaved: false,
  IsLiked: false,
  Photos: '',
  Tag: '',
}

const defaultRating: Rating = {
  PostID: 0,
  UserID: 0,
  UserName: '',
  UserScore: 0,
  UserAvatar: '',
  UserIdentity: '',
  Title: '',
  Content: '',
  Like: 0,
  Comment: 0,
  Browse: 0,
  Heat: 0,
  PostTime: '',
  IsSaved: false,
  IsLiked: false,
  Photos: '',
  Tag: '',
  Rating: 0,
  Stars: [0, 0, 0, 0, 0],
  UserRating: 0,
}

/**
 * 后端 PostVO 使用小驼峰，前端 Post 接口使用大驼峰，这里做转换
 */
function mapPost(data: any): Post {
  return {
    PostID: data.postId ?? 0,
    UserID: data.userId,
    UserName: data.userName ?? '',
    UserScore: data.userScore ?? 0,

    UserAvatar: data.userAvatar ?? '',
    UserIdentity: data.userIdentity ?? '',
    Title: data.title ?? '',
    Content: data.content ?? '',
    Like: data.like ?? 0,
    Comment: data.comment ?? 0,
    Browse: data.browse ?? 0,
    Heat: data.heat ?? 0,
    PostTime: data.postTime ?? '',
    IsSaved: data.saved ?? false,
    IsLiked: data.liked ?? false,
    Photos: data.photos ?? '',
    Tag: data.tag ?? '',
  }
}

/**
 * 帖子详情映射（支持评分帖）
 */
function mapPostDetail(data: any, postType: PostType): Post | Rating {
  const base = mapPost(data)
  if (postType === 'rating') {
    return {
      ...base,
      Rating: data.rating ?? 0,
      Stars: Array.isArray(data.stars) && data.stars.length === 5
        ? data.stars as [number, number, number, number, number]
        : [0, 0, 0, 0, 0],
      UserRating: data.userRating ?? 0,
    }
  }
  return base
}

async function getPosts(object: getPostsObject): Promise<Post[] | Rating[]> {
  try {
    const res = await requestFunc(
      '/auth/browse',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          ...object,
        },
      },
      true,
    )
    if (!res) {
      console.error('getPosts: 请求失败，未获得响应')
      return []
    }

    try {
      const json = await res.json()
      return (json as any[]).map((item) => {
        if (object.searchSort === 'rating') {
          return mapPostDetail(item, 'rating')
        }
        return mapPost(item)
      })
    }
    catch (e) {
      console.error('getPosts: 解析响应失败', e)
      return []
    }
  }
  catch (e) {
    console.error(e)
    return []
  }
}

export interface getPostsNumObject {
  category: string
  searchSort: string
  searchInfo: string
}

/***
 * @description 获取当前分区和搜索条件下的帖子数量，课程专区无论是否有tag都只返回所有数量
 * @returns {number} 返回帖子数量
 */
async function getPostsNum(object: getPostsNumObject): Promise<number> {
  try {
    const res = await requestFunc(
      `/auth/getPostNum`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          category: object.category,
          searchsort: object.searchSort,
          searchinfo: object.searchInfo,
        },
      },
      true,
    )
    if (!res) {
      console.error('getPostsNum: 请求失败，未获得响应')
      return -1
    }

    try {
      const data = await res.json()
      return typeof data.data?.postCount === 'number' ? data.data.postCount : -1
    }
    catch (e) {
      console.error('getPostsNum: 解析响应失败', e)
      return -1
    }
  }
  catch (e) {
    console.error(e)
    return -1
  }
}

export interface HeatPost {
  PostID: number
  Title: string
  Heat: number
}

/**
 * @description 获取热门帖子
 * @returns 帖子数组
 */
async function getHeatPosts(): Promise<HeatPost[]> {
  try {
    const res = await requestFunc(
      `/auth/calculateHeat`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      },
      true,
    )
    const json = await res!.json()
    const list = json.data ?? []
    return list.map((item: any) => ({
      PostID: item.postID ?? 0,
      Title: item.title ?? '',
      Heat: item.heat ?? 0,
    }))
  }
  catch (e) {
    console.error(e)
    return []
  }
}

/***
 * @description 获取帖子详情，可以增加用户浏览量
 * @param {number} PostID 帖子ID
 * @returns {object} 返回帖子详情
 */
async function getPostByID(
  PostID: number,
  postType: PostType = 'post',
): Promise<Post | Rating> {
  const result = await updateBrowseNum(PostID)
  if (!result) {
    console.error('增加浏览量失败')
  }
  try {
    const res = await requestFunc(
      `/auth/showDetails`,
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
    try {
      const json = await res!.json()
      if (!json.data) {
        throw new Error('帖子数据为空')
      }
      return mapPostDetail(json.data, postType)
    }
    catch (e) {
      console.error(e)
      return postType === 'post'
        ? {
            ...defaultPost,
            Title: '帖子不存在',
            Content: '帖子不存在',
          }
        : {
            ...defaultRating,
            Title: '帖子不存在',
            Content: '帖子不存在',
          }
    }
  }
  catch (e) {
    console.error(e)
    return postType === 'post'
      ? {
          ...defaultPost,
          Title: '网络错误',
          Content: '网络错误',
        }
      : {
          ...defaultRating,
          Title: '网络错误',
          Content: '网络错误',
        }
  }
}

async function updateBrowseNum(PostID: number) {
  try {
    const res = await requestFunc(
      `/auth/updateBrowseNum`,
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
    return res!.ok
  }
  catch (e) {
    console.error(e)
    return false
  }
}

async function getPostTypeByID(PostID: number): Promise<PostType> {
  try {
    const res = await requestFunc(
      `/auth/getPostType`,
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
      console.error('获取帖子类型失败:', res!.status, res!.statusText)
      return 'post'
    }

    const data = await res!.json()
    return data.data.postType as PostType
  }
  catch (e) {
    console.error(e)
    return 'post'
  }
}

export { getHeatPosts, getPostByID, getPosts, getPostsNum, getPostTypeByID }

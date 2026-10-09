import request from '@/utils/request'

export interface AdminLoginRequest {
  account: string
  password: string
}

export interface AdminLoginResult {
  code: number
  msg: string
  data: {
    token: string
  }
}

export interface AdminInfo {
  account: string
  name: string
  avatarUrl: string
  identity: string
}

export interface AdminInfoResult {
  code: number
  msg: string
  data: {
    admin: AdminInfo
  }
}

export interface AddAdminRequest {
  account: string
  password1: string
  password2: string
}

export interface AddAdminResult {
  code: number
  msg: string
  data: {
    adminId: number
    account: string
  }
}

export interface ChangePasswordRequest {
  account: string
  password1: string
  password2: string
}

export interface DeleteUserRequest {
  name: string
}

export interface DeleteAdminRequest {
  account: string
}

export interface ShowUsersRequest {
  name?: string
  idPass?: number
}

export interface UserVO {
  userId: number
  phone: string
  email: string
  name: string
  intro: string
  avatarUrl: string
  identity: string
  score: number
  punishNum: number
  banend: string | null
  createdAt: string
}

export interface ShowUsersResult {
  code: number
  msg: string
  data: UserVO[]
}

export interface SueVO {
  sueId: number
  targettype: string
  targetId: number
  userId: number
  reason: string
  time: string
  status: string
  finish: boolean
  reporterName: string
  targetTitle: string
}

export interface SueActionRequest {
  sueID: number
}

export interface AdminPostRequest {
  title: string
  content: string
  partition: string
  photos?: string
  tagList?: string
}

export async function adminLogin(data: AdminLoginRequest): Promise<AdminLoginResult> {
  const response = await request.post<AdminLoginResult>('/auth/adminLogin', data)
  return response.data
}

export async function getAdminInfo(): Promise<AdminInfoResult> {
  const response = await request.get<AdminInfoResult>('/auth/admininfo')
  return response.data
}

export async function addAdmin(data: AddAdminRequest): Promise<AddAdminResult> {
  const response = await request.post<AddAdminResult>('/auth/addAdmin', data)
  return response.data
}

export interface UpdateAdminInfoRequest {
  account: string
  name?: string
  password1?: string
  password2?: string
}

export async function changePassword(data: ChangePasswordRequest): Promise<{ code: number; msg: string }> {
  const response = await request.post<{ code: number; msg: string }>('/auth/changePassword', data)
  return response.data
}

export async function updateAdminInfo(data: UpdateAdminInfoRequest): Promise<{ code: number; msg: string }> {
  const response = await request.post<{ code: number; msg: string }>('/auth/updateAdminInfo', data)
  return response.data
}

export async function deleteUser(data: DeleteUserRequest): Promise<{ code: number; msg: string }> {
  const response = await request.post<{ code: number; msg: string }>('/auth/deleteUser', data)
  return response.data
}

export async function deleteAdmin(data: DeleteAdminRequest): Promise<{ code: number; msg: string }> {
  const response = await request.post<{ code: number; msg: string }>('/auth/deleteAdmin', data)
  return response.data
}

export async function showUsers(data: ShowUsersRequest): Promise<ShowUsersResult> {
  const response = await request.post<ShowUsersResult>('/auth/showUsers', data)
  return response.data
}

export async function getSues(): Promise<SueVO[]> {
  const response = await request.get<SueVO[]>('/auth/getSues')
  return response.data
}

export async function noViolation(data: SueActionRequest): Promise<{ code: number; msg: string }> {
  const response = await request.post<{ code: number; msg: string }>('/auth/noViolation', data)
  return response.data
}

export async function violation(data: SueActionRequest): Promise<{ code: number; msg: string }> {
  const response = await request.post<{ code: number; msg: string }>('/auth/violation', data)
  return response.data
}

export async function adminPost(data: AdminPostRequest): Promise<{ code: number; msg: string }> {
  const response = await request.post<{ code: number; msg: string }>('/auth/adminPost', data)
  return response.data
}

// ====== 反馈 ======

export interface FeedbackVO {
  feedbackId: number
  ftext: string
  attachment: string
  status: string
  time: string
}

export interface GetAllFeedbackResult {
  code: number
  msg: string
  data: {
    feedbacks: FeedbackVO[]
  }
}

export async function getAllFeedback(): Promise<GetAllFeedbackResult> {
  const response = await request.get<GetAllFeedbackResult>('/auth/getAllFeedback')
  return response.data
}

export async function resolveFeedback(feedbackId: number): Promise<{ code: number; msg: string }> {
  const response = await request.post<{ code: number; msg: string }>('/auth/resolveFeedback', { feedbackId })
  return response.data
}

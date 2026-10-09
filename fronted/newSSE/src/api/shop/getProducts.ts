import { requestFunc } from '../req'

export interface Product {
  sellerId: number
  productId: number
  seller: string
  price: number
  name: string
  description: string
  photos: string[]
  isSold: boolean
  isAnonymous: boolean
}

export interface ProductDetail {
  sellerId: number
  productId: number
  seller: string
  price: number
  name: string
  description: string
  photos: string[]
  isSold: boolean
  isAnonymous: boolean
}

async function getProducts(Searchsort: string): Promise<Product[]> {
  try {
    const res = await requestFunc(`/auth/getProducts`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        searchInfo: '',
        searchSort: Searchsort, // 用于分表查询 分为home,history两种
        limit: 1000,
        offset: 0,
      },
    }, true)

    if (!res) {
      throw new Error('No response received')
    }

    const data = await res.json() as Product[]
    return data || []
  }
  catch (e) {
    console.error(e)
    return []
  }
}

async function getProductByID(id: number): Promise<ProductDetail> {
  try {
    const res = await requestFunc(`/auth/getProductDetail`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        productId: id,
      },
    }, true)

    if (!res) {
      throw new Error('No response received')
    }

    const data = await res.json() as ProductDetail
    return data
  }
  catch (e) {
    console.error(e)
    return {
      sellerId: 0,
      productId: 0,
      seller: '',
      price: 0,
      name: '',
      description: '',
      photos: [],
      isSold: false,
      isAnonymous: false,
    }
  }
}

// 获取商品数量
async function getProductNum(searchSort: string): Promise<number> {
  try {
    const res = await requestFunc(`/auth/getProductNum`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        searchInfo: '',
        searchSort,
        limit: 1000,
        offset: 0,
      },
    }, true)

    if (!res) {
      throw new Error('No response received')
    }

    const json = await res.json()
    return json.data?.productCount || 0
  }
  catch (e) {
    console.error(e)
    return 0
  }
}

export { getProductByID, getProducts, getProductNum }

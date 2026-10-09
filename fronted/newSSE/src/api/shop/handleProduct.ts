import { requestFunc } from '../req'

export interface ProductCreation {
  price: number
  name: string
  description: string
  images: string[]
}

async function handleProduct(product: ProductCreation, id: number): Promise<any> {
  try {
    const res = await requestFunc(`/auth/postProduct`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        userID: id,
        price: product.price,
        title: product.name,
        content: product.description,
        photos: product.images,
        isAnonymous: false,
      },
    }, true)

    // 添加类型检查
    if (!res) {
      throw new Error('No response received')
    }

    const data = await res.json()
    return data
  }
  catch (e) {
    console.error(e)
  }
}

export { handleProduct }

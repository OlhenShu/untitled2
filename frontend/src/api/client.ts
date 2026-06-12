import type { CreateReposRequest, CreateReposResponse, InviteRequest, InviteResponse } from '../types'

const BASE_URL = import.meta.env.VITE_API_URL || ''

async function request<T>(url: string, options: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  if (!res.ok) {
    let message = `HTTP ${res.status}`
    try {
      const err = await res.json()
      message = err.message || err.error || message
    } catch {
      // ignore parse error
    }
    throw new Error(message)
  }

  return res.json() as Promise<T>
}

export async function createRepos(token: string, req: CreateReposRequest): Promise<CreateReposResponse> {
  return request('/api/repos/generate', {
    method: 'POST',
    headers: { 'X-GitHub-Token': token },
    body: JSON.stringify(req),
  })
}

export async function sendInvites(token: string, req: InviteRequest): Promise<InviteResponse> {
  return request('/api/invites/send', {
    method: 'POST',
    headers: { 'X-GitHub-Token': token },
    body: JSON.stringify(req),
  })
}

export async function checkHealth(): Promise<{ status: string }> {
  return request('/api/health', { method: 'GET' })
}

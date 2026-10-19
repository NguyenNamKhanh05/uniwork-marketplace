import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const formatDate = (value) => {
  if (!value) return ''
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function App() {
  const [messages, setMessages] = useState([])
  const [content, setContent] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [lastSaved, setLastSaved] = useState('')

  const loadMessages = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await fetch(`${API_URL}/hello`)
      const body = await response.json().catch(() => null)

      if (!response.ok || !body?.success) {
        throw new Error(body?.message || `Không thể tải dữ liệu (HTTP ${response.status})`)
      }

      setMessages(body.data ?? [])
    } catch (requestError) {
      setError(
        requestError.message ||
          'Không kết nối được backend. Kiểm tra Spring Boot đã chạy ở cổng 8080 chưa.',
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadMessages()
  }, [])

  const handleSubmit = async (event) => {
    event.preventDefault()
    const value = content.trim()
    if (!value || saving) return

    setSaving(true)
    setError('')
    setLastSaved('')

    try {
      const response = await fetch(`${API_URL}/hello`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: value }),
      })
      const body = await response.json().catch(() => null)

      if (!response.ok || !body?.success) {
        const detail = body?.errors?.[0]?.message
        throw new Error(detail || body?.message || `Không thể lưu dữ liệu (HTTP ${response.status})`)
      }

      setContent('')
      setLastSaved(body.data?.content || value)
      await loadMessages()
    } catch (requestError) {
      setError(requestError.message || 'Không thể lưu dữ liệu vào MySQL.')
    } finally {
      setSaving(false)
    }
  }

  const isConnected = !loading && !error

  return (
    <main className="app">
      <section className="hero">
        <span className="eyebrow">Chuyên đề tốt nghiệp 2 · Sprint 1</span>
        <h1>
          UniWork <span>Marketplace</span>
        </h1>
        <p>
          Kiểm tra luồng <strong>ReactJS → Spring Boot REST API → MySQL</strong> bằng một
          chương trình Hello World có ghi dữ liệu thật xuống cơ sở dữ liệu.
        </p>
        <div className={`status ${isConnected ? 'status-ok' : 'status-wait'}`}>
          <span className="status-dot" />
          {loading
            ? 'Đang kết nối backend...'
            : error
              ? 'Chưa kết nối được backend'
              : 'Đã kết nối Spring Boot + MySQL'}
        </div>
      </section>

      <section className="card">
        <div className="card-header">
          <div>
            <h2>Gửi lời chào vào MySQL</h2>
            <p className="muted">
              Dữ liệu gửi từ ReactJS sẽ được Spring Boot lưu vào bảng{' '}
              <code>hello_messages</code>.
            </p>
          </div>
          <button className="button ghost" type="button" onClick={loadMessages} disabled={loading}>
            Tải lại
          </button>
        </div>

        <form className="form" onSubmit={handleSubmit}>
          <label htmlFor="message">Nội dung</label>
          <div className="form-row">
            <input
              id="message"
              type="text"
              maxLength={255}
              value={content}
              onChange={(event) => setContent(event.target.value)}
              placeholder="Ví dụ: Hello UniWork từ ReactJS!"
              autoComplete="off"
            />
            <button className="button primary" type="submit" disabled={saving || !content.trim()}>
              {saving ? 'Đang gửi...' : 'Gửi vào MySQL'}
            </button>
          </div>
        </form>

        {lastSaved && (
          <p className="notice success">Đã lưu “{lastSaved}” vào MySQL thành công.</p>
        )}
        {error && <p className="notice error">{error}</p>}

        <div className="list-header">
          <h3>Dữ liệu đã lưu</h3>
          <span className="counter">{messages.length} bản ghi</span>
        </div>

        {loading ? (
          <p className="empty">Đang tải dữ liệu...</p>
        ) : messages.length === 0 ? (
          <p className="empty">Chưa có dữ liệu. Hãy gửi lời chào đầu tiên.</p>
        ) : (
          <ul className="message-list">
            {messages.map((message) => (
              <li key={message.id} className="message-item">
                <div>
                  <span className="message-id">#{message.id}</span>
                  <p>{message.content}</p>
                </div>
                <time dateTime={message.createdAt}>{formatDate(message.createdAt)}</time>
              </li>
            ))}
          </ul>
        )}
      </section>

      <footer className="footer">
        <span>API: {API_URL}</span>
        <span>ReactJS 18 · Spring Boot 3.2 · MySQL 8.0</span>
      </footer>
    </main>
  )
}

export default App

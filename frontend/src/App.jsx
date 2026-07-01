import { useEffect, useState } from 'react'
import { Routes, Route } from 'react-router-dom'
import axios from 'axios'

function Home() {
  const [status, setStatus] = useState('checking')

  useEffect(() => {
    axios
      .get('/api/v1/auth/oauth/providers')
      .then(() => setStatus('online'))
      .catch(() => setStatus('offline'))
  }, [])

  return (
    <main className="page">
      <h1>Unified API Manager</h1>
      <p className="tagline">One API to publish across every social platform.</p>
      <p className={`status status--${status}`}>
        Backend: {status}
      </p>
    </main>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
    </Routes>
  )
}

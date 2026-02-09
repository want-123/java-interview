import React, { useState, useEffect } from 'react';
import axios from 'axios';

function App() {
  const [activeTab, setActiveTab] = useState('chat');
  const [message, setMessage] = useState('');
  const [response, setResponse] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [token, setToken] = useState('');
  const [tokenLoading, setTokenLoading] = useState(true);

  // 获取测试token
  useEffect(() => {
    const fetchToken = async () => {
      try {
        const res = await axios.get('/api/test/gtoken', {
          params: {
            username: 'root' // 使用root用户获取token
          }
        });
        setToken(res.data.accessToken);
      } catch (err) {
        console.error('获取token失败:', err);
        setError('获取token失败，请检查后端服务是否启动');
      } finally {
        setTokenLoading(false);
      }
    };

    fetchToken();
  }, []);

  // 处理API调用
  const handleApiCall = async (endpoint, data, method = 'post') => {
    setLoading(true);
    setError('');
    try {
      const config = {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      };

      let res;
      if (method === 'post') {
        res = await axios.post(endpoint, data, config);
      } else {
        res = await axios.get(endpoint, config);
      }
      setResponse(JSON.stringify(res.data, null, 2));
    } catch (err) {
      setError(err.response?.data?.msg || '请求失败，请稍后重试');
      setResponse('');
    } finally {
      setLoading(false);
    }
  };

  // 处理聊天请求
  const handleChat = () => {
    const data = {
      content: message,
      type: 'TEXT'
    };
    handleApiCall('/api/java/chat', data);
  };

  // 处理代码评估请求
  const handleCodeEvaluation = () => {
    const data = {
      content: message,
      type: 'TEXT'
    };
    handleApiCall('/api/java/codeEvaluation', data);
  };

  // 处理面试模拟请求
  const handleInterviewSimulation = () => {
    const data = {
      content: message,
      type: 'TEXT'
    };
    handleApiCall('/api/java/interviewSimulation', data);
  };

  // 处理简历解析请求
  const handleResumeParser = () => {
    const data = {
      content: message,
      type: 'TEXT'
    };
    handleApiCall('/api/java/resumeParser', data);
  };

  // 处理健康检查请求
  const handleHealthCheck = async () => {
    handleApiCall('/api/health/status', {}, 'get');
  };

  return (
    <div className="min-h-screen bg-gray-100 dark:bg-gray-900">
      {/* 导航栏 */}
      <nav className="bg-blue-600 text-white p-4 shadow-md">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold">Java面试智能平台</h1>
          <div className="flex space-x-4">
            <button 
              className={`px-3 py-1 rounded ${activeTab === 'chat' ? 'bg-blue-700' : 'hover:bg-blue-700'}`}
              onClick={() => setActiveTab('chat')}
            >
              智能聊天
            </button>
            <button 
              className={`px-3 py-1 rounded ${activeTab === 'code' ? 'bg-blue-700' : 'hover:bg-blue-700'}`}
              onClick={() => setActiveTab('code')}
            >
              代码评估
            </button>
            <button 
              className={`px-3 py-1 rounded ${activeTab === 'interview' ? 'bg-blue-700' : 'hover:bg-blue-700'}`}
              onClick={() => setActiveTab('interview')}
            >
              面试模拟
            </button>
            <button 
              className={`px-3 py-1 rounded ${activeTab === 'resume' ? 'bg-blue-700' : 'hover:bg-blue-700'}`}
              onClick={() => setActiveTab('resume')}
            >
              简历解析
            </button>
            <button 
              className={`px-3 py-1 rounded ${activeTab === 'health' ? 'bg-blue-700' : 'hover:bg-blue-700'}`}
              onClick={() => setActiveTab('health')}
            >
              健康检查
            </button>
          </div>
        </div>
      </nav>

      {/* 主要内容区域 */}
      <div className="container mx-auto p-4">
        {/* Token加载状态 */}
        {tokenLoading && (
          <div className="bg-blue-100 dark:bg-blue-900 text-blue-700 dark:text-blue-300 p-4 rounded-md mb-4">
            <div className="flex items-center">
              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-500 mr-3"></div>
              <span>正在获取认证Token，请稍候...</span>
            </div>
          </div>
        )}

        {/* 聊天页面 */}
        {activeTab === 'chat' && (
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold mb-4">智能聊天</h2>
            <div className="mb-4">
              <label className="block text-gray-700 dark:text-gray-300 mb-2">输入消息</label>
              <textarea
                className="w-full border border-gray-300 dark:border-gray-600 rounded-md p-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                rows={4}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="请输入您的问题..."
              />
            </div>
            <button
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
              onClick={handleChat}
              disabled={loading || tokenLoading}
            >
              {loading ? '发送中...' : '发送'}
            </button>
            {error && (
              <div className="mt-4 p-3 bg-red-100 dark:bg-red-900 text-red-700 dark:text-red-300 rounded-md">
                {error}
              </div>
            )}
            {response && (
              <div className="mt-4 p-3 bg-gray-100 dark:bg-gray-700 rounded-md">
                <h3 className="font-bold mb-2">响应结果：</h3>
                <pre className="whitespace-pre-wrap">{response}</pre>
              </div>
            )}
          </div>
        )}

        {/* 代码评估页面 */}
        {activeTab === 'code' && (
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold mb-4">代码评估</h2>
            <div className="mb-4">
              <label className="block text-gray-700 dark:text-gray-300 mb-2">输入代码</label>
              <textarea
                className="w-full border border-gray-300 dark:border-gray-600 rounded-md p-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white font-mono"
                rows={10}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="请输入Java代码..."
              />
            </div>
            <button
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
              onClick={handleCodeEvaluation}
              disabled={loading || tokenLoading}
            >
              {loading ? '评估中...' : '评估代码'}
            </button>
            {error && (
              <div className="mt-4 p-3 bg-red-100 dark:bg-red-900 text-red-700 dark:text-red-300 rounded-md">
                {error}
              </div>
            )}
            {response && (
              <div className="mt-4 p-3 bg-gray-100 dark:bg-gray-700 rounded-md">
                <h3 className="font-bold mb-2">评估结果：</h3>
                <pre className="whitespace-pre-wrap">{response}</pre>
              </div>
            )}
          </div>
        )}

        {/* 面试模拟页面 */}
        {activeTab === 'interview' && (
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold mb-4">面试模拟</h2>
            <div className="mb-4">
              <label className="block text-gray-700 dark:text-gray-300 mb-2">输入请求</label>
              <textarea
                className="w-full border border-gray-300 dark:border-gray-600 rounded-md p-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                rows={4}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="例如：生成5个Java中级面试问题"
              />
            </div>
            <button
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
              onClick={handleInterviewSimulation}
              disabled={loading || tokenLoading}
            >
              {loading ? '处理中...' : '提交请求'}
            </button>
            {error && (
              <div className="mt-4 p-3 bg-red-100 dark:bg-red-900 text-red-700 dark:text-red-300 rounded-md">
                {error}
              </div>
            )}
            {response && (
              <div className="mt-4 p-3 bg-gray-100 dark:bg-gray-700 rounded-md">
                <h3 className="font-bold mb-2">模拟结果：</h3>
                <pre className="whitespace-pre-wrap">{response}</pre>
              </div>
            )}
          </div>
        )}

        {/* 简历解析页面 */}
        {activeTab === 'resume' && (
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold mb-4">简历解析</h2>
            <div className="mb-4">
              <label className="block text-gray-700 dark:text-gray-300 mb-2">输入简历内容</label>
              <textarea
                className="w-full border border-gray-300 dark:border-gray-600 rounded-md p-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                rows={10}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="请输入简历内容..."
              />
            </div>
            <button
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
              onClick={handleResumeParser}
              disabled={loading || tokenLoading}
            >
              {loading ? '解析中...' : '解析简历'}
            </button>
            {error && (
              <div className="mt-4 p-3 bg-red-100 dark:bg-red-900 text-red-700 dark:text-red-300 rounded-md">
                {error}
              </div>
            )}
            {response && (
              <div className="mt-4 p-3 bg-gray-100 dark:bg-gray-700 rounded-md">
                <h3 className="font-bold mb-2">解析结果：</h3>
                <pre className="whitespace-pre-wrap">{response}</pre>
              </div>
            )}
          </div>
        )}

        {/* 健康检查页面 */}
        {activeTab === 'health' && (
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold mb-4">健康检查</h2>
            <button
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
              onClick={handleHealthCheck}
              disabled={loading || tokenLoading}
            >
              {loading ? '检查中...' : '检查系统健康状态'}
            </button>
            {error && (
              <div className="mt-4 p-3 bg-red-100 dark:bg-red-900 text-red-700 dark:text-red-300 rounded-md">
                {error}
              </div>
            )}
            {response && (
              <div className="mt-4 p-3 bg-gray-100 dark:bg-gray-700 rounded-md">
                <h3 className="font-bold mb-2">健康状态：</h3>
                <pre className="whitespace-pre-wrap">{response}</pre>
              </div>
            )}
          </div>
        )}
      </div>

      {/* 页脚 */}
      <footer className="bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 p-4 mt-8">
        <div className="container mx-auto text-center">
          <p>© 2025 Java面试智能平台</p>
        </div>
      </footer>
    </div>
  );
}

export default App;

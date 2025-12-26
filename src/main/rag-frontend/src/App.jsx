import React, { useState, useEffect, useRef } from 'react';
import { Upload, FileText, MessageSquare, Send, Loader, ChevronDown, ChevronUp } from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080/api';

function App() {
  const [documents, setDocuments] = useState([]);
  const [selectedDoc, setSelectedDoc] = useState(null);
  const [chatMessages, setChatMessages] = useState([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(false);
  const [chunks, setChunks] = useState([]);
  const [showChunks, setShowChunks] = useState(false);
  const chatEndRef = useRef(null);

  useEffect(() => {
    fetchDocuments();
  }, []);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages]);

  const fetchDocuments = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/documents`);
      const data = await response.json();
      setDocuments(data);
    } catch (error) {
      console.error('Error fetching documents:', error);
    }
  };

  const fetchChunks = async (docId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/documents/${docId}/chunks`);
      const data = await response.json();
      setChunks(data);
    } catch (error) {
      console.error('Error fetching chunks:', error);
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.name.endsWith('.pdf')) {
      alert('Please upload a PDF file');
      return;
    }

    setUploadProgress(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch(`${API_BASE_URL}/documents/upload`, {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        await fetchDocuments();
        alert('Document uploaded and processed successfully!');
      } else {
        alert('Error uploading document');
      }
    } catch (error) {
      console.error('Upload error:', error);
      alert('Error uploading document');
    } finally {
      setUploadProgress(false);
      event.target.value = '';
    }
  };

  const handleQuery = async () => {
    if (!query.trim()) return;

    setChatMessages(prev => [...prev, { type: 'user', content: query }]);
    const currentQuery = query;
    setQuery('');
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/chat/query`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: currentQuery,
          documentId: selectedDoc?.id || null,
        }),
      });

      const data = await response.json();
      setChatMessages(prev => [
        ...prev,
        {
          type: 'assistant',
          content: data.response,
          sourceChunks: data.sourceChunks || [],
        },
      ]);
    } catch (error) {
      console.error('Query error:', error);
      setChatMessages(prev => [
        ...prev,
        {
          type: 'error',
          content: 'Error processing your query. Please try again.',
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleQuery();
    }
  };

  const selectDocument = (doc) => {
    setSelectedDoc(doc);
    fetchChunks(doc.id);
    setChatMessages([]);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="container mx-auto px-4 py-8">
        <header className="text-center mb-8">
          <h1 className="text-4xl font-bold text-indigo-900 mb-2">
            PDF RAG Application
          </h1>
          <p className="text-gray-600">
            Upload PDFs, process with Ollama Mistral 7B, and query with AI
          </p>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-1 bg-white rounded-lg shadow-lg p-6">
            <div className="mb-4">
              <label className="flex items-center justify-center w-full px-4 py-3 bg-indigo-600 text-white rounded-lg cursor-pointer hover:bg-indigo-700 transition-colors">
                <Upload className="w-5 h-5 mr-2" />
                Upload PDF
                <input
                  type="file"
                  className="hidden"
                  accept=".pdf"
                  onChange={handleFileUpload}
                  disabled={uploadProgress}
                />
              </label>
              {uploadProgress && (
                <div className="mt-2 flex items-center justify-center text-indigo-600">
                  <Loader className="w-5 h-5 animate-spin mr-2" />
                  Processing...
                </div>
              )}
            </div>

            <div className="border-t pt-4">
              <h2 className="text-lg font-semibold mb-3 flex items-center">
                <FileText className="w-5 h-5 mr-2 text-indigo-600" />
                Documents ({documents.length})
              </h2>
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {documents.length === 0 ? (
                  <p className="text-gray-500 text-sm text-center py-4">
                    No documents uploaded yet
                  </p>
                ) : (
                  documents.map((doc) => (
                    <div
                      key={doc.id}
                      onClick={() => selectDocument(doc)}
                      className={`p-3 rounded-lg border-2 cursor-pointer transition-all ${
                        selectedDoc?.id === doc.id
                          ? 'border-indigo-600 bg-indigo-50'
                          : 'border-gray-200 hover:border-indigo-300'
                      }`}
                    >
                      <div className="font-medium text-sm truncate">
                        {doc.originalFilename}
                      </div>
                      <div className="flex justify-between items-center mt-1">
                        <span className="text-xs text-gray-500">
                          {doc.totalChunks} chunks
                        </span>
                        <span
                          className={`text-xs px-2 py-1 rounded ${
                            doc.status === 'COMPLETED'
                              ? 'bg-green-100 text-green-700'
                              : doc.status === 'PROCESSING'
                              ? 'bg-yellow-100 text-yellow-700'
                              : 'bg-red-100 text-red-700'
                          }`}
                        >
                          {doc.status}
                        </span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>

            {selectedDoc && chunks.length > 0 && (
              <div className="mt-4 border-t pt-4">
                <button
                  onClick={() => setShowChunks(!showChunks)}
                  className="w-full flex items-center justify-between px-4 py-2 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                >
                  <span className="font-medium text-sm">
                    View Chunks ({chunks.length})
                  </span>
                  {showChunks ? (
                    <ChevronUp className="w-4 h-4" />
                  ) : (
                    <ChevronDown className="w-4 h-4" />
                  )}
                </button>
                {showChunks && (
                  <div className="mt-2 max-h-64 overflow-y-auto space-y-2">
                    {chunks.map((chunk) => (
                      <div
                        key={chunk.id}
                        className="p-2 bg-gray-50 rounded text-xs"
                      >
                        <div className="font-semibold text-indigo-600 mb-1">
                          Chunk {chunk.chunkIndex}
                        </div>
                        <div className="text-gray-600 line-clamp-3">
                          {chunk.content}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="lg:col-span-2 bg-white rounded-lg shadow-lg flex flex-col h-[600px]">
            <div className="p-4 border-b bg-indigo-600 rounded-t-lg">
              <h2 className="text-lg font-semibold flex items-center text-white">
                <MessageSquare className="w-5 h-5 mr-2" />
                Chat with {selectedDoc ? selectedDoc.originalFilename : 'All Documents'}
              </h2>
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {chatMessages.length === 0 ? (
                <div className="text-center text-gray-500 mt-20">
                  <MessageSquare className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                  <p className="text-lg font-medium">Start asking questions</p>
                  <p className="text-sm mt-2">
                    {selectedDoc
                      ? 'Ask questions about the selected document'
                      : 'Select a document or ask questions about all documents'}
                  </p>
                </div>
              ) : (
                chatMessages.map((msg, idx) => (
                  <div
                    key={idx}
                    className={`flex ${
                      msg.type === 'user' ? 'justify-end' : 'justify-start'
                    }`}
                  >
                    <div
                      className={`max-w-[80%] p-4 rounded-lg ${
                        msg.type === 'user'
                          ? 'bg-indigo-600 text-white'
                          : msg.type === 'error'
                          ? 'bg-red-100 text-red-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}
                    >
                      <div className="whitespace-pre-wrap">{msg.content}</div>
                      {msg.sourceChunks && msg.sourceChunks.length > 0 && (
                        <div className="mt-3 pt-3 border-t border-gray-300">
                          <p className="text-xs font-semibold mb-2">Sources:</p>
                          {msg.sourceChunks.map((chunk, i) => (
                            <p key={i} className="text-xs opacity-75 mb-1">
                              • {chunk}
                            </p>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ))
              )}
              {loading && (
                <div className="flex justify-start">
                  <div className="bg-gray-100 p-4 rounded-lg">
                    <Loader className="w-5 h-5 animate-spin text-indigo-600" />
                  </div>
                </div>
              )}
              <div ref={chatEndRef} />
            </div>

            <div className="p-4 border-t">
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="Ask a question about your documents..."
                  className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-600"
                  disabled={loading}
                />
                <button
                  onClick={handleQuery}
                  disabled={loading || !query.trim()}
                  className="px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors flex items-center"
                >
                  <Send className="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
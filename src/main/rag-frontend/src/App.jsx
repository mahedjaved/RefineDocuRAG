import React, { useState, useEffect, useRef } from 'react';
import { Upload, FileText, MessageSquare, Send, Loader, ChevronDown, ChevronUp, Sparkles, TrendingUp, X } from 'lucide-react';

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
  
  // Refinement states
  const [showRefinement, setShowRefinement] = useState(true);
  const [refining, setRefining] = useState(false);
  const [refinementResult, setRefinementResult] = useState(null);
  const [refinementSettings, setRefinementSettings] = useState({
    maxIterations: 5,
    convergenceThreshold: 0.95,
    regressionMethod: 'ENSEMBLE',
    optimizationGoals: ['CLARITY', 'RELEVANCE']
  });
  
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

  const handleRefinePrompt = async () => {
    if (!query.trim()) return;

    setRefining(true);
    setRefinementResult(null);

    try {
      const response = await fetch(`${API_BASE_URL}/refinement/refine`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          prompt: query,
          ...refinementSettings,
        }),
      });

      const data = await response.json();
      
      if (response.ok) {
        setRefinementResult(data);
        
        // Add refinement history to chat
        setChatMessages(prev => [...prev, {
          type: 'refinement',
          content: {
            original: query,
            refined: data.refinedPrompt,
            improvement: data.improvementPercentage,
            iterations: data.totalIterations
          }
        }]);

        setQuery(data.refinedPrompt); // Update query with refined prompt
      } else {
        alert('Error refining prompt: ' + (data.error || 'Unknown error'));
      }
    } catch (error) {
      console.error('Refinement error:', error);
      alert('Error refining prompt');
    } finally {
      setRefining(false);
    }
  };

  const handleQuery = async () => {
    if (!query.trim()) return;

    setChatMessages(prev => [...prev, { type: 'user', content: query }]);
    const currentQuery = query;
    setQuery('');
    setLoading(true);
    setRefinementResult(null); // Clear refinement result when sending

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

  const toggleOptimizationGoal = (goal) => {
    setRefinementSettings(prev => ({
      ...prev,
      optimizationGoals: prev.optimizationGoals.includes(goal)
        ? prev.optimizationGoals.filter(g => g !== goal)
        : [...prev.optimizationGoals, goal]
    }));
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="container mx-auto px-4 py-8">
        <header className="text-center mb-8">
          <h1 className="text-4xl font-bold text-indigo-900 mb-2">
            PDF RAG with Prompt Refinement
          </h1>
          <p className="text-gray-600">
            Upload PDFs, refine prompts with ML, and query with Ollama Mistral 7B
          </p>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Sidebar - Documents */}
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

          {/* Main Chat Area */}
          <div className="lg:col-span-2 bg-white rounded-lg shadow-lg flex flex-col h-[700px]">
            <div className="p-4 border-b bg-indigo-600 rounded-t-lg">
              <h2 className="text-lg font-semibold flex items-center justify-between text-white">
                <div className="flex items-center">
                  <MessageSquare className="w-5 h-5 mr-2" />
                  Chat with {selectedDoc ? selectedDoc.originalFilename : 'All Documents'}
                </div>
                <button
                  onClick={() => setShowRefinement(!showRefinement)}
                  className="flex items-center px-3 py-1 bg-white text-indigo-600 rounded-lg text-sm hover:bg-indigo-50 transition-colors"
                >
                  <Sparkles className="w-4 h-4 mr-1" />
                  {showRefinement ? 'Hide' : 'Show'} Refinement
                </button>
              </h2>
            </div>

            {/* Refinement Panel */}
            {showRefinement && (
              <div className="p-4 bg-gradient-to-r from-purple-50 to-pink-50 border-b">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="font-semibold text-sm flex items-center text-purple-900">
                    <TrendingUp className="w-4 h-4 mr-2" />
                    Prompt Refinement Settings
                  </h3>
                  <button
                    onClick={() => setShowRefinement(false)}
                    className="text-gray-500 hover:text-gray-700"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
                
                <div className="grid grid-cols-3 gap-3 mb-3">
                  <div>
                    <label className="text-xs text-gray-600 block mb-1">Max Iterations</label>
                    <input
                      type="number"
                      min="1"
                      max="10"
                      value={refinementSettings.maxIterations}
                      onChange={(e) => setRefinementSettings(prev => ({ 
                        ...prev, 
                        maxIterations: parseInt(e.target.value) 
                      }))}
                      className="w-full px-2 py-1 border rounded text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-gray-600 block mb-1">Convergence</label>
                    <input
                      type="number"
                      min="0.7"
                      max="0.99"
                      step="0.01"
                      value={refinementSettings.convergenceThreshold}
                      onChange={(e) => setRefinementSettings(prev => ({ 
                        ...prev, 
                        convergenceThreshold: parseFloat(e.target.value) 
                      }))}
                      className="w-full px-2 py-1 border rounded text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-gray-600 block mb-1">Method</label>
                    <select
                      value={refinementSettings.regressionMethod}
                      onChange={(e) => setRefinementSettings(prev => ({ 
                        ...prev, 
                        regressionMethod: e.target.value 
                      }))}
                      className="w-full px-2 py-1 border rounded text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                    >
                      <option value="ENSEMBLE">Ensemble</option>
                      <option value="LINEAR">Linear</option>
                      <option value="POLYNOMIAL">Polynomial</option>
                      <option value="NEURAL">Neural</option>
                    </select>
                  </div>
                </div>

                <div className="mb-3">
                  <label className="text-xs text-gray-600 block mb-2">Optimization Goals</label>
                  <div className="flex flex-wrap gap-2">
                    {['CLARITY', 'RELEVANCE', 'COMPLETENESS', 'SPECIFICITY'].map(goal => (
                      <button
                        key={goal}
                        onClick={() => toggleOptimizationGoal(goal)}
                        className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
                          refinementSettings.optimizationGoals.includes(goal)
                            ? 'bg-purple-600 text-white'
                            : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                      >
                        {goal}
                      </button>
                    ))}
                  </div>
                </div>

                {refinementResult && (
                  <div className="p-3 bg-white rounded-lg border border-purple-200 shadow-sm">
                    <div className="grid grid-cols-3 gap-2 mb-2">
                      <div className="text-center">
                        <div className="text-xs text-gray-600">Final Score</div>
                        <div className="text-lg font-bold text-green-600">
                          {(refinementResult.finalScore * 100).toFixed(1)}%
                        </div>
                      </div>
                      <div className="text-center">
                        <div className="text-xs text-gray-600">Improvement</div>
                        <div className="text-lg font-bold text-blue-600">
                          +{refinementResult.improvementPercentage.toFixed(1)}%
                        </div>
                      </div>
                      <div className="text-center">
                        <div className="text-xs text-gray-600">Iterations</div>
                        <div className="text-lg font-bold text-purple-600">
                          {refinementResult.totalIterations}
                        </div>
                      </div>
                    </div>
                    <div className="text-xs text-gray-600 mt-2 p-2 bg-purple-50 rounded">
                      <strong className="text-purple-900">Refined Prompt:</strong>
                      <div className="mt-1 text-gray-800">{refinementResult.refinedPrompt}</div>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Chat Messages */}
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
                  {showRefinement && (
                    <p className="text-sm mt-2 text-purple-600">
                      ✨ Try the Sparkles button to refine your prompt first!
                    </p>
                  )}
                </div>
              ) : (
                chatMessages.map((msg, idx) => (
                  <div
                    key={idx}
                    className={`flex ${
                      msg.type === 'user' ? 'justify-end' : 
                      msg.type === 'refinement' ? 'justify-center' : 'justify-start'
                    }`}
                  >
                    {msg.type === 'refinement' ? (
                      <div className="w-full max-w-2xl bg-purple-50 border border-purple-200 rounded-lg p-4 mx-4 my-2">
                        <div className="flex items-center text-purple-900 font-semibold mb-2 text-sm">
                          <Sparkles className="w-4 h-4 mr-2" />
                          Prompt Refined
                          <span className="ml-auto text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full">
                            +{msg.content.improvement.toFixed(1)}% Improved
                          </span>
                        </div>
                        <div className="space-y-2 text-sm">
                          <div className="text-gray-500 line-through decoration-red-400">
                            {msg.content.original}
                          </div>
                          <div className="text-indigo-900 font-medium bg-white p-2 rounded border border-purple-100">
                            {msg.content.refined}
                          </div>
                        </div>
                      </div>
                    ) : (
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
                    )}
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

            {/* Input Area */}
            <div className="p-4 border-t">
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="Ask a question about your documents..."
                  className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-600"
                  disabled={loading || refining}
                />
                {showRefinement && (
                  <button
                    onClick={handleRefinePrompt}
                    disabled={refining || !query.trim() || loading}
                    className="px-4 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors flex items-center font-medium"
                    title="Refine prompt with ML"
                  >
                    {refining ? (
                      <>
                        <Loader className="w-5 h-5 animate-spin mr-2" />
                        Refining...
                      </>
                    ) : (
                      <>
                        <Sparkles className="w-5 h-5 mr-2" />
                        Refine
                      </>
                    )}
                  </button>
                )}
                <button
                  onClick={handleQuery}
                  disabled={loading || !query.trim() || refining}
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
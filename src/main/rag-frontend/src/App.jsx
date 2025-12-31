import React, { useState, useEffect, useRef } from 'react';
import { Upload, FileText, MessageSquare, Send, Loader, ChevronDown, ChevronUp, Sparkles, TrendingUp, X, Menu, Sun, ArrowDown } from 'lucide-react';

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
    <div className="min-h-screen bg-[#FFFDF5] text-[#1A1A1A] font-sans selection:bg-pink-200">
      
      {/* Top Navigation Bar */}
      <header className="h-20 border-b border-[#1A1A1A]/10 flex items-center justify-between px-8 sticky top-0 bg-[#FFFDF5]/90 backdrop-blur-sm z-50">
        <div className="flex items-center">
          <div className="text-3xl font-black tracking-tighter mr-2">R.</div>
        </div>
        
        <div className="hidden md:flex items-center space-x-4">
           <div className="bg-white border border-[#1A1A1A]/10 rounded-full px-1 p-1 flex items-center">
             <Sun className="w-5 h-5 mx-2 text-yellow-500 fill-current" />
             <div className="w-8 h-4 bg-gray-100 rounded-full mr-1"></div>
           </div>
           
           <div className="bg-[#FCD34D] px-4 py-2 rounded-full text-sm font-bold border border-[#1A1A1A]/10 flex items-center shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:translate-y-[1px] hover:shadow-[1px_1px_0px_0px_rgba(0,0,0,1)] transition-all cursor-pointer">
             user@example.com <X className="w-3 h-3 ml-2" />
           </div>
           
           <button className="bg-[#1A1A1A] text-white px-5 py-2 rounded-full text-sm font-bold hover:bg-[#333] transition-colors flex items-center">
             Menu <Menu className="w-4 h-4 ml-2" />
           </button>
        </div>
      </header>

      {/* Main Layout Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[calc(100vh-80px)]">
        
        {/* Sidebar - Documents */}
        <div className="lg:col-span-3 border-r border-[#1A1A1A]/10 p-6 flex flex-col bg-[#FAFAF8]">
          <div className="mb-8">
            <h3 className="text-xs font-bold uppercase tracking-widest text-gray-500 mb-4">Documents</h3>
            <label className="flex items-center justify-center w-full px-4 py-4 bg-white border-2 border-[#1A1A1A]/10 rounded-xl cursor-pointer hover:border-pink-500 hover:text-pink-600 transition-all group shadow-sm">
              <Upload className="w-5 h-5 mr-3 text-gray-400 group-hover:text-pink-500" />
              <span className="font-bold">Upload PDF</span>
              <input
                type="file"
                className="hidden"
                accept=".pdf"
                onChange={handleFileUpload}
                disabled={uploadProgress}
              />
            </label>
            {uploadProgress && (
              <div className="mt-2 flex items-center justify-center text-pink-600 text-sm font-medium">
                <Loader className="w-4 h-4 animate-spin mr-2" />
                Processing...
              </div>
            )}
          </div>

          <div className="flex-1 overflow-y-auto space-y-3 pr-2">
            {documents.length === 0 ? (
              <p className="text-gray-400 text-sm italic">No documents yet.</p>
            ) : (
              documents.map((doc) => (
                <div
                  key={doc.id}
                  onClick={() => selectDocument(doc)}
                  className={`p-4 rounded-xl border transition-all cursor-pointer ${
                    selectedDoc?.id === doc.id
                      ? 'bg-white border-pink-500 shadow-[4px_4px_0px_0px_rgba(233,30,99,0.2)]'
                      : 'bg-white border-[#1A1A1A]/5 hover:border-[#1A1A1A]/20'
                  }`}
                >
                  <div className="font-bold text-sm truncate mb-1">{doc.originalFilename}</div>
                  <div className="flex justify-between items-center">
                    <span className="text-xs text-gray-400 font-medium">{doc.totalChunks} chunks</span>
                    <div className={`w-2 h-2 rounded-full ${
                      doc.status === 'COMPLETED' ? 'bg-green-500' : 'bg-yellow-500'
                    }`} />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Main Content Area */}
        <div className="lg:col-span-9 flex flex-col relative">
          
          {/* Hero / Header Area */}
          <div className="p-8 pb-4 border-b border-[#1A1A1A]/5">
            <h1 className="text-5xl md:text-6xl font-black leading-[0.9] tracking-tight mb-4">
              Refining for <span className="text-pink-600">Impact</span>, <br />
              Creating prompts that drive <span className="text-pink-600">Growth</span>.
            </h1>
            <div className="flex items-center text-gray-500 font-medium">
               <span className="mr-2">Powered by Ollama RAG</span>
               <ArrowDown className="w-4 h-4" />
            </div>
          </div>

          {/* Chat & Refinement Container */}
          <div className="flex-1 flex flex-col overflow-hidden relative">
            
            {/* Refinement Settings Bar (Collapsible/Top) */}
            {showRefinement && (
              <div className="bg-white border-b border-[#1A1A1A]/5 p-4 flex flex-wrap gap-6 items-center shadow-sm z-10">
                 <div className="flex items-center text-xs font-bold uppercase tracking-wider text-gray-400">
                    <TrendingUp className="w-4 h-4 mr-2 text-pink-500" /> Settings
                 </div>
                 
                 <div className="flex items-center space-x-2">
                    <span className="text-xs font-bold">Iterations:</span>
                    <input 
                      type="number" min="1" max="10" 
                      value={refinementSettings.maxIterations}
                      onChange={(e) => setRefinementSettings(prev => ({ ...prev, maxIterations: parseInt(e.target.value) }))}
                      className="w-12 bg-gray-50 border border-gray-200 rounded px-1 text-sm focus:border-pink-500 outline-none"
                    />
                 </div>

                 <div className="flex items-center space-x-2">
                    <span className="text-xs font-bold">Goals:</span>
                    <div className="flex gap-1">
                      {['CLARITY', 'RELEVANCE', 'SPECIFICITY'].map(goal => (
                        <button
                          key={goal}
                          onClick={() => toggleOptimizationGoal(goal)}
                          className={`px-2 py-0.5 text-[10px] font-bold rounded-full border transition-all ${
                            refinementSettings.optimizationGoals.includes(goal)
                              ? 'bg-pink-100 text-pink-700 border-pink-200'
                              : 'bg-gray-50 text-gray-500 border-gray-200 hover:bg-gray-100'
                          }`}
                        >
                          {goal}
                        </button>
                      ))}
                    </div>
                 </div>
                 
                 <button onClick={() => setShowRefinement(false)} className="ml-auto text-gray-400 hover:text-gray-600">
                   <X className="w-4 h-4" />
                 </button>
              </div>
            )}

            {/* Chat Messages Area */}
            <div className="flex-1 overflow-y-auto p-8 space-y-6">
               {chatMessages.length === 0 ? (
                 <div className="h-full flex flex-col items-center justify-center text-center opacity-40">
                    <Sparkles className="w-16 h-16 text-pink-300 mb-4" />
                    <p className="text-xl font-bold">Start your refinement journey</p>
                 </div>
               ) : (
                 chatMessages.map((msg, idx) => (
                   <div key={idx} className={`flex ${msg.type === 'user' ? 'justify-end' : 'justify-start'}`}>
                      
                      {/* Refinement Card */}
                      {msg.type === 'refinement' ? (
                        <div className="w-full max-w-2xl bg-[#FFFDF5] border-2 border-pink-100 rounded-2xl p-6 mx-auto my-4 shadow-[8px_8px_0px_0px_rgba(253,242,248,1)]">
                           <div className="flex items-center justify-between mb-4 border-b border-pink-50 pb-2">
                              <div className="flex items-center text-pink-600 font-black text-sm uppercase tracking-wider">
                                <Sparkles className="w-4 h-4 mr-2" /> Refined Prompt
                              </div>
                              <span className="bg-green-100 text-green-700 text-xs font-bold px-2 py-1 rounded-full">
                                +{msg.content.improvement.toFixed(1)}% Better
                              </span>
                           </div>
                           <div className="space-y-4">
                              <div className="opacity-50 text-sm line-through decoration-pink-400 decoration-2">
                                {msg.content.original}
                              </div>
                              <div className="text-lg font-bold text-[#1A1A1A] bg-white p-4 rounded-xl border border-gray-100 shadow-sm">
                                {msg.content.refined}
                              </div>
                           </div>
                        </div>
                      ) : (
                        /* Standard Chat Bubbles */
                        <div className={`max-w-[80%] p-6 rounded-2xl shadow-sm border ${
                          msg.type === 'user' 
                            ? 'bg-[#1A1A1A] text-white rounded-br-none border-transparent' 
                            : msg.type === 'error'
                            ? 'bg-red-50 text-red-900 border-red-100'
                            : 'bg-white text-[#1A1A1A] rounded-bl-none border-[#1A1A1A]/5'
                        }`}>
                          <div className="whitespace-pre-wrap font-medium leading-relaxed">{msg.content}</div>
                          {msg.sourceChunks && msg.sourceChunks.length > 0 && (
                             <div className="mt-4 pt-4 border-t border-dashed border-gray-200">
                               <p className="text-xs font-bold uppercase text-gray-400 mb-2">Sources</p>
                               {msg.sourceChunks.map((chunk, i) => (
                                 <div key={i} className="text-xs text-gray-500 bg-gray-50 p-2 rounded mb-1 border border-gray-100">
                                   {chunk}
                                 </div>
                               ))}
                             </div>
                          )}
                        </div>
                      )}
                   </div>
                 ))
               )}
               <div ref={chatEndRef} />
            </div>

            {/* Input Area - Fixed Bottom or Sticky */}
            <div className="p-6 bg-[#FFFDF5] border-t border-[#1A1A1A]/10">
              <div className="flex gap-3 max-w-4xl mx-auto">
                <input
                  type="text"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="Ask a question..."
                  className="flex-1 bg-white border-2 border-[#1A1A1A]/10 rounded-xl px-6 py-4 font-medium focus:outline-none focus:border-pink-500 focus:ring-1 focus:ring-pink-500 transition-all placeholder:text-gray-300"
                  disabled={loading || refining}
                />
                
                <button
                  onClick={handleRefinePrompt}
                  disabled={refining || !query.trim() || loading}
                  className="bg-pink-600 text-white px-6 rounded-xl font-bold hover:bg-pink-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-[4px_4px_0px_0px_rgba(0,0,0,0.1)] hover:translate-y-[1px] hover:shadow-none active:translate-y-[2px] flex items-center"
                  title="Refine with ML"
                >
                  {refining ? <Loader className="w-5 h-5 animate-spin" /> : <Sparkles className="w-5 h-5" />}
                </button>

                <button
                  onClick={handleQuery}
                  disabled={loading || !query.trim() || refining}
                  className="bg-[#1A1A1A] text-white px-6 rounded-xl font-bold hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-[4px_4px_0px_0px_rgba(233,30,99,0.3)] hover:translate-y-[1px] hover:shadow-none active:translate-y-[2px] flex items-center"
                >
                  <Send className="w-5 h-5" />
                </button>
              </div>
              
              {!showRefinement && (
                <div className="text-center mt-2">
                  <button onClick={() => setShowRefinement(true)} className="text-xs font-bold text-gray-400 hover:text-pink-600 uppercase tracking-widest flex items-center justify-center mx-auto">
                    Show Refinement Settings <ChevronUp className="w-3 h-3 ml-1" />
                  </button>
                </div>
              )}
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}

export default App;

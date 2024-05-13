import MainLayout from 'Frontend/views/MainLayout.js';
import { createBrowserRouter, RouteObject } from 'react-router-dom';
import ChatView from "Frontend/views/chat/ChatView";
import ChatViewStreaming from "Frontend/views/chat-streaming/ChatViewStreaming";


export const routes: RouteObject[] = [
  {
    element: <MainLayout />,
    handle: { title: 'Main' },
    children: [
      { path: '/', element: <ChatView />, handle: { title: 'CHAT' } },
      { path: 'chat/Streaming', element: <ChatViewStreaming />, handle: { title: 'CHAT STREAMING ' } }
    ],
  },
];

export default createBrowserRouter(routes);

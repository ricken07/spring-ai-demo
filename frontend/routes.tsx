import MainLayout from 'Frontend/views/MainLayout.js';
import { createBrowserRouter, RouteObject } from 'react-router-dom';
import ChatView from "Frontend/views/chat/ChatView";
import ChatView2 from "Frontend/views/chat-simple/ChatView2";
import ImageView from "Frontend/views/image/ImageView";


export const routes: RouteObject[] = [
  {
    element: <MainLayout />,
    handle: { title: 'Main' },
    children: [
      { path: '/', element: <ChatView />, handle: { title: 'CHAT' } },
      { path: 'chat/Streaming', element: <ChatView2 />, handle: { title: 'STREAMING CHAT' } },
      { path: 'image', element: <ImageView />, handle: { title: 'Image generation' } }
    ],
  },
];

export default createBrowserRouter(routes);

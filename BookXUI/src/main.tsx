import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';

async function bootstrap() {
  // In "mock" mode, patch the API client with dummy data before rendering.
  // This import is completely tree-shaken in production / normal dev builds.
  if (import.meta.env.MODE === 'mock') {
    await import('./mocks/setup');
  }

  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </StrictMode>,
  );
}

bootstrap();

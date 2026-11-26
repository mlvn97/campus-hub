import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'associations/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'events/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'news/:id',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];

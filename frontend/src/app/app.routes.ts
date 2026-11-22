import { Routes } from '@angular/router';
import { AuthComponent } from './pages/auth/auth.component';
import { DirectoryComponent } from './pages/directory/directory.component';
import { HomepageComponent } from './pages/homepage/homepage.component';

export const routes: Routes = [
    { path: '', component: HomepageComponent },
    { path: 'auth', component: AuthComponent },
    { path: 'associations', component: DirectoryComponent, data: { section: 'associations' } },
    { path: 'evenements', component: DirectoryComponent, data: { section: 'evenements' } },
    { path: 'actualites', component: DirectoryComponent, data: { section: 'actualites' } },
    { path: '**', redirectTo: '' }
];

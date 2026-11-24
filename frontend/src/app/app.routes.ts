import { Routes } from '@angular/router';
import { AccountComponent } from './pages/account/account.component';
import { AuthComponent } from './pages/auth/auth.component';
import { DirectoryComponent } from './pages/directory/directory.component';
import { EventsComponent } from './pages/events/events.component';
import { NewsComponent } from './pages/news/news.component';
import { HomepageComponent } from './pages/homepage/homepage.component';

export const routes: Routes = [
    { path: '', component: HomepageComponent },
    { path: 'auth', component: AuthComponent },
    { path: 'account', component: AccountComponent },
    { path: 'associations', component: DirectoryComponent },
    { path: 'events', component: EventsComponent },
    { path: 'news', component: NewsComponent },
    { path: '**', redirectTo: '' }
];

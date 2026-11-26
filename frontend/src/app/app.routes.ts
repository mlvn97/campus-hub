import { Routes } from '@angular/router';
import { AccountComponent } from './pages/account/account.component';
import { AuthComponent } from './pages/auth/auth.component';
import { AssociationComponent } from './pages/association/association.component';
import { EventsComponent } from './pages/events/events.component';
import { EventDetailComponent } from './pages/events/event-detail.component';
import { NewsComponent } from './pages/news/news.component';
import { NewsDetailComponent } from './pages/news/news-detail.component';
import { AssociationDetailComponent } from './pages/association/association-detail.component';
import { HomepageComponent } from './pages/homepage/homepage.component';

export const routes: Routes = [
    { path: '', component: HomepageComponent },
    { path: 'auth', component: AuthComponent },
    { path: 'account', component: AccountComponent },
    { path: 'associations', component: AssociationComponent },
    { path: 'associations/:id', component: AssociationDetailComponent },
    { path: 'events', component: EventsComponent },
    { path: 'events/:id', component: EventDetailComponent },
    { path: 'news', component: NewsComponent },
    { path: 'news/:id', component: NewsDetailComponent },
    { path: '**', redirectTo: '' }
];

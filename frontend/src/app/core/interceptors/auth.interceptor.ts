import { HttpInterceptorFn } from "@angular/common/http";


// L'intercepteur permet d'ajouter le Bearer Token dans chaque requête
export const authInterceptor: HttpInterceptorFn = (request, next) => {

    const token = localStorage.getItem('token');

    if (token) {
        const authReq = request.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
        return next(authReq);
    }

    return next(request);
}
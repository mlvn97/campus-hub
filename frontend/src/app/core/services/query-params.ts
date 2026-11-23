import { HttpParams } from '@angular/common/http';

export function queryParams(
    filters: Record<string, string | number | readonly string[] | undefined>
): HttpParams {
    let params = new HttpParams();
    for (const [name, value] of Object.entries(filters)) {
        if (typeof value === 'string' || typeof value === 'number') {
            params = params.set(name, value);
        } else if (value !== undefined) {
            params = params.appendAll({ [name]: value });
        }
    }
    return params;
}

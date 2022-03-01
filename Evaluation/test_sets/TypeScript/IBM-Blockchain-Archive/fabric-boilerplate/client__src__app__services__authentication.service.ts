import {Injectable} from '@angular/core';
import {Http, Response, Headers} from '@angular/http';
import {Observable} from 'rxjs';
import {Configuration} from '../app.constants';
import 'rxjs/add/operator/map';

@Injectable()
export class AuthenticationService {
  public actionUrl: string;
  public token: string;
  private TOKEN_KEY = 'token';
  private USER_KEY: string = 'currentUser';
  public user: any;

  public constructor(private _http: Http,
                     private _configuration: Configuration) {
    this.actionUrl = `${_configuration.apiHost}${_configuration.apiPrefix}login`;
    // set token if saved in local storage
    this.token = this.getToken();
  }

  public login(username: string, password: string): Observable<any> {
    return this._http.post(this.actionUrl, {username: username, password: password})
      .map((response: Response) => {
        if (!response || !response.json || !response.json()) {
          return false;
        }

        let user = response.json().user;
        let token = response.json().token;
        if (!token) {
          return false; // Login unsuccessful if there's no token in the response
        }
        this.token = token;

        // store username and jwt token in local storage to keep user logged in between page refreshes
        localStorage.setItem(this.TOKEN_KEY, JSON.stringify({token}));
        localStorage.setItem(this.USER_KEY,  JSON.stringify({user}));

        return true;
      }).catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

  // clear token and remove user from local storage to log user out
  public logout(): void {
    this.token = null;
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.TOKEN_KEY);
  }

  public createAuthorizationHeader(): Headers {
    let headers = new Headers();
    headers.append('x-access-token', this.getToken());
    headers.append('Content-Type', 'application/json');
    return headers;
  }

  private getToken(): string {
    let userToken = JSON.parse(localStorage.getItem(this.TOKEN_KEY));
    return userToken ? userToken.token : null;
  }
}

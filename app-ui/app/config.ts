import { CustomConfig } from './ng2-ui-auth/config.service';

export const GOOGLE_CLIENT_ID = '174400993910-63csgump26j3jjh4havakk0lmhhee1tl.apps.googleusercontent.com';

export class MyAuthConfig extends CustomConfig {
    defaultHeaders = {'Content-Type': 'application/json'};
    baseUrl = '/';
    loginUrl = '/signIn';
    signupUrl = '/signUp';
    tokenName = 'token';
    tokenPrefix = 'ng2-ui-auth'; // Local Storage name prefix
    authHeader = 'X-Auth-Token';
    storageType = 'sessionStorage' as 'sessionStorage';
    providers = {
        google: {
            clientId: GOOGLE_CLIENT_ID,
            url: '/authenticate/google'
        }
    };
}

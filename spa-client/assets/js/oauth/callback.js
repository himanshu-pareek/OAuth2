import { CLIENT_ID, REDIRECT_URI_SUFFIX } from "../constants.js"
import { generateAccessTokenUrl } from "../utils.js";

const contentDiv = document.getElementById('content');

contentDiv.innerHTML = 'Getting access token...';

// 2. Accept code and state at redirect url path
const query = new URLSearchParams (window.location.search);
const code = query.get('code');
const state = query.get('state');

const fun = async () => {
    // 3. Get Access token from Authorization URL
    const accessTokenUrl = generateAccessTokenUrl(code);
    console.log ({accessTokenUrl});
    const urlParams = new URLSearchParams ({
        client_id: CLIENT_ID,
        grant_type: 'authorization_code',
        code: code,
        redirect_uri: window.location.origin + REDIRECT_URI_SUFFIX
    });
    const response = await fetch (accessTokenUrl, {
        method: 'POST',
        body: urlParams,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
            // 'Content-Type': 'application/json'
        }
    });
    console.log({response});
    const tokenResponse = await response.json();
    console.log({tokenResponse});
    if (tokenResponse.access_token) {
        localStorage.setItem('access_token', tokenResponse.access_token);
    }
    window.location.replace (window.location.origin);
};

if (!code) {
    contentDiv.innerHTML = `code not present`;
} else if (!state || state != localStorage.getItem('state')) {
    contentDiv.innerHTML = 'Invalid state';
} else {
    fun().then(console.log)
        .catch(console.error);
}

import { CLIENT_ID, REDIRECT_URI_SUFFIX } from "./constants.js";
import { generateAccessTokenUrl } from "./util.js";

const errorDiv = document.getElementById('errorDiv');

// 2. Retrieve state and code from query parameters in redirect uri
const query = new URLSearchParams(window.location.search);

const code = query.get('code');
const state = query.get('state');

errorDiv.innerHTML = `Code = ${code} -- State = ${state}`;

const fetchAccessToken = async () => {
  const accessTokenUrl = generateAccessTokenUrl();
  const urlSearchParams = new URLSearchParams({
    client_id: CLIENT_ID,
    grant_type: 'authorization_code',
    code: code,
    redirect_uri: window.location.origin + REDIRECT_URI_SUFFIX,
    code_verifier: localStorage.getItem('code_verifier'),
  });
  localStorage.clear();
  const response = await fetch(accessTokenUrl, {
    method: 'POST',
    body: urlSearchParams,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });
  const tokenResponse = await response.json();
  return tokenResponse;
};

if (!code) {
  errorDiv.innerHTML = 'Code is not present.';
} else if (!state || state != localStorage.getItem('state')) {
  errorDiv.innerHTML = 'Invalid state.';
} else {
  // 3. Fetch access token from authorization server
  fetchAccessToken()
    .then(tokenResponse => {
      const accessToken = tokenResponse.access_token;
      if (accessToken) {
        localStorage.setItem('access_token', accessToken);
        const idToken = tokenResponse.id_token;
      }

      const idToken = tokenResponse.id_token;
      if (idToken) {
        localStorage.setItem ('id_token', idToken);
      }
      
      window.location.replace(window.location.origin);
    });
}

import { USERINFO_ENDPOINT } from "./constants.js";
import { generateAuthorizationUrl } from "./util.js";

const contentDiv = document.getElementById('content');

const accessToken = localStorage.getItem('access_token');

const fetchUserInformation = async () => {
  const response = await fetch(USERINFO_ENDPOINT, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`    // 4. Send access token to resource server for authorization
    }
  });
  const userInfoResponse = await response.json();
  console.log({userInfoResponse});
  contentDiv.append(JSON.stringify(userInfoResponse));
};

if (accessToken) {
  contentDiv.innerHTML = `Access Token = ${accessToken}`;
  fetchUserInformation();
} else {
  // 1. Access token is not present - Redirect the user to authorization server for login
  // and authorizing application
  const authorizationUrl = generateAuthorizationUrl();
  contentDiv.innerHTML = `<a href="${authorizationUrl}">Connect your account</a>`;
}

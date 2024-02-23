import { API_ENDPOINT } from "./constants.js";
import { generateAuthorizationUrl } from "./utils.js";

const contentDiv = document.getElementById('content');

const accessToken = localStorage.getItem('access_token');

localStorage.removeItem ('state');
if (accessToken) {
    // 4. Send request to Resource Server with Access Token
    fetch (API_ENDPOINT, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${accessToken}`,
        }
    }).then(response => response.json())
    .then(user => {
        contentDiv.innerHTML = JSON.stringify (user)
    })
    .catch(error => {
        console.error(error);
        contentDiv.innerHTML = error;
    });
} else {
    // 1. Redirect User to Authorization URL
    const authorizationUrl = generateAuthorizationUrl();
    console.log(authorizationUrl);
    contentDiv.innerHTML = `
            <a href="${authorizationUrl}">Connect Your Account</a>
        `
}

import { USERINFO_ENDPOINT, CONTACTS_ENDPOINT } from "./constants.js";
import { generateAuthorizationUrl } from "./util.js";

const contentDiv = document.getElementById('content');

const accessToken = localStorage.getItem('access_token');

const fetchUserInformation = async () => {
    try {
        const response = await fetch(USERINFO_ENDPOINT, {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${accessToken}`    // 4. Send access token to resource server for authorization
            }
        });
        const userInfoResponse = await response.json();
        console.log({userInfoResponse});
        contentDiv.append(JSON.stringify(userInfoResponse));
    } catch (e) {
        console.error (e);
    }

};

const fetchContacts = async () => {
    try {
        const response = await fetch(CONTACTS_ENDPOINT, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });
        const contacts = await response.json();
        console.log ({contacts});
        let content = "<ul>";
        for (let contact of contacts) {
            content += "<li>";
            content += `<h3>${contact.name}</h3>`;
            content += `<h4>${contact.email}</h4>`;
            content += "</li>";
        }
        content += "</ul>";
        contentDiv.innerHTML += content;
    } catch (e) {
        console.error (e);
    }
}

if (accessToken) {
  contentDiv.innerHTML = `Access Token = ${accessToken}`;
  fetchUserInformation();
  fetchContacts();
} else {
  // 1. Access token is not present - Redirect the user to authorization server for login
  // and authorizing application
  generateAuthorizationUrl()
    .then(authorizationUrl => {
        contentDiv.innerHTML = `<a href="${authorizationUrl}">Connect your account</a>`;
    })
    .catch(e => console.error (e));
}

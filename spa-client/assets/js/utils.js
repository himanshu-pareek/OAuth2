import { 
    AUTHORIZE_ENDPOINT,
    CLIENT_ID,
    REDIRECT_URI_SUFFIX,
    TOKEN_ENDPOINT
} from "./constants.js";

const DEFAULT_LAKE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890";

export const generateAuthorizationUrl = () => {
    const redirectUri = window.location.origin + REDIRECT_URI_SUFFIX;
    const state = generateRandomString (16);

    localStorage.clear();
    localStorage.setItem ('state', state);

    return `${AUTHORIZE_ENDPOINT}?client_id=${CLIENT_ID}&response_type=code&state=${state}&redirect_uri=${redirectUri}&scope=openid email profile`;
};

export const generateAccessTokenUrl = (code) => {
    if (!code) {
        throw "Authorization code is required";
    }
    return TOKEN_ENDPOINT;
};

const generateRandomString = (
    len = 10, lake = DEFAULT_LAKE
) => {
    let result = "";
    const n = lake.length;
    for (let i = 0; i < len; i++) {
        const index = Math.floor(Math.random() * n);
        result += lake[index];
    }
    return result;
};

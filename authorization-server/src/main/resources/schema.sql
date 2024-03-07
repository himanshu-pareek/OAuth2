DELETE FROM client_secrets;
DELETE FROM clients;

CREATE TABLE IF NOT EXISTS realms (
    id varchar(20) NOT NULL PRIMARY KEY,
    name varchar(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS clients (
    id CHAR(32) NOT NULL PRIMARY KEY,
    realm_id VARCHAR (20) NOT NULL,
    is_confidential BOOLEAN NOT NULL,
    name VARCHAR (50) NOT NULL,
    icon_url VARCHAR (100),
    home_page_url VARCHAR (100),
    description VARCHAR (1000),
    privacy_policy_url VARCHAR (100),
    sign_in_redirect_uris TEXT,
    sign_out_redirect_uris TEXT,
    web_origins TEXT,
    FOREIGN KEY (realm_id) REFERENCES realms(id)
);

DROP TABLE client_secrets;

CREATE TABLE IF NOT EXISTS client_secrets (
    id SERIAL NOT NULL PRIMARY KEY,
    client_id CHAR(32) NOT NULL,
    secret TEXT NOT NULL,
    salt TEXT NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

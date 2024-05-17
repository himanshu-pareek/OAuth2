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

CREATE TABLE IF NOT EXISTS client_secrets (
    id SERIAL NOT NULL PRIMARY KEY,
    client_id CHAR(32) NOT NULL,
    secret TEXT NOT NULL,
    salt TEXT NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS realm_scopes (
    realm_id VARCHAR(20) NOT NULL,
    name VARCHAR(20) NOT NULL,
    description VARCHAR(100) NOT NULL,
    PRIMARY KEY (realm_id, name),
    FOREIGN KEY (realm_id) REFERENCES realms(id)
);

DELETE FROM realm_scopes;

INSERT INTO realm_scopes (realm_id, name, description)
    VALUES ('java', 'email.read', 'Read emails in your inbox.'),
    ('java', 'email.write', 'Write emails on behalf of you.'),
    ('java', 'contact.read', 'Read your contacts.'),
    ('java', 'contact.write', 'Edit your contacts'),
    ('rush', 'post.read', 'Read posts on your timeline.'),
    ('rush', 'post.write', 'Write posts on behalf of you.');

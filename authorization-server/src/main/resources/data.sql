-- Delete everything
DELETE FROM realm_scopes;
DELETE FROM client_secrets;
DELETE FROM clients;
DELETE FROM realms;

-- Insert realms
INSERT INTO `realms` VALUES ('java','Java'),('rush','Rush');

-- Insert scopes
INSERT INTO `realm_scopes` VALUES
    ('java','contact.read','can read your contacts.'),
    ('java','contact.write','can edit your contacts.'),
    ('rush','post.read','can read your posts.'),
    ('rush','post.write','can edit your contacts.');

-- Insert clients
INSERT INTO `clients` VALUES
    ('kxOFV4HF3ENLFYhglcboZawUzhi5ceG4','java',1,'Trip','','','This is trips app','','http://localhost:8081/oauth/callback','',''),
    ('YoPg4yWFCEDNbhIsQnfUEZz9KKT5FFlf','rush',0,'Shopping App','','','This is shopping app','','http://localhost:5001/oauth/callback','','http://localhost:5001');

-- Insert client secrets
INSERT INTO `client_secrets` VALUES (2,'kxOFV4HF3ENLFYhglcboZawUzhi5ceG4','jQp0Sgrr8L+LK48r2Syjw/J+ve/v2QppQbjCjftBlBtgv2T7EnflqLfB65Mv7VnuYInhCygFOJdVSNpmiWEyhA==','j+vrGM/PO0CpbSSJk5GQaWQDj19PsPRJg9lTXO30g0I=');
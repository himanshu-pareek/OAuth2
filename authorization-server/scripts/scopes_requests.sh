#!/bin/bash

curl -X POST \
-H "Content-Type:application/json" \
-d '{"realmId": "java", "name": "contact.read", "description": "Delete your posts"}' \
http://localhost:9000/realms/java/scopes \
| jq

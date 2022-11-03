This project uses TestContainer to simulate an Azure Redis with password.

token as password

1. generate a fake AAD token
2. using this token as redis password to start a redis container
3. provide a ChianedTokenCredential to mock the fake AAD token


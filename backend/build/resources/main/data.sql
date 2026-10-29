MERGE INTO app_users (id, email, password, role)
    KEY(id)
    VALUES (1, 'accountant@example.com', '42Abc4242', 'ACCOUNTANT');
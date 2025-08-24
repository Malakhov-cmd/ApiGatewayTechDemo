-- Добавление ролей
INSERT INTO root.roles (name) VALUES
                                  ('ROLE_USER'),
                                  ('ROLE_ADMIN');

-- Добавление пользователей
INSERT INTO root.users (username, password, enabled) VALUES
                                                         ('user1', '$2a$10$ronozFRMNLkkP.D86d5kBOX6J2Sq9CNxQB3va.OXvIM/bZxzPKVzG', true),
                                                         ('user2', '$2a$10$qVlV5I2sdC2QeEIZQVQ8qetCeTlYc9YpDp093cwBVj6nKoHsF/aNG', true);

-- Присваивание ролей пользователям
INSERT INTO root.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM root.users u
         JOIN root.roles r ON r.name = 'ROLE_USER'
WHERE u.username IN ('user1', 'user2');
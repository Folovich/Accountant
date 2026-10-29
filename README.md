# Accountant

Локальный сайт бухгалтерских услуг с backend на Spring Boot и базой H2.

## Запуск в Windows

Нужен JDK 17 или новее. В терминале VS Code из папки проекта выполните:

```powershell
.\mvnw.cmd spring-boot:run
```

При первом запуске скрипт скачает Maven 3.9.9 в папку пользователя.

Откройте в браузере <http://localhost:8080>. Spring Boot раздаёт `index.html`, `style.css` и `script.js` с того же адреса, поэтому API уже подключён и отдельная настройка CORS не требуется.

Данные заявок и сообщений сохраняются в локальной базе `data/accountant`.

## API

- `GET /api/services` — проверка доступности backend.
- `POST /api/appointments/public` — создание заявки на встречу.
- `POST /api/contacts` — сохранение сообщения из контактной формы.
- `POST /api/auth/register` и `POST /api/auth/login` — регистрация и вход.

Формы встречи и обратной связи доступны без регистрации.

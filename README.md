# ☁️ Cloud File Storage

Web-приложение для хранения и управления файлами в облаке. Пользователь может зарегистрироваться, загружать файлы и создавать папки, искать, переименовывать, перемещать и удалять ресурсы, скачивать файлы и папки. 

## 🚀 Технологии

### Backend Core
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.3-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Session](https://img.shields.io/badge/Spring_Session-Redis-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Data & Persistence
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-Silo-C72E49?style=for-the-badge&logo=minio&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?style=for-the-badge&logo=liquibase&logoColor=white)

### Infrastructure
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Caddy](https://img.shields.io/badge/Caddy-2-1F88C0?style=for-the-badge&logo=caddy&logoColor=white)

### Frontend
![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-6-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![MUI](https://img.shields.io/badge/MUI-6-007FFF?style=for-the-badge&logo=mui&logoColor=white)
![React Router](https://img.shields.io/badge/React_Router-7-CA4245?style=for-the-badge&logo=reactrouter&logoColor=white)

## 📦 Функциональность

* регистрация и авторизация пользователей (сессии хранятся в Redis)
* создание, переименование, перемещение и удаление папок
* просмотр содержимого папки
* загрузка одного или нескольких ресурсов (файлов и папок)
* поиск по названию ресурсов
* скачивание одного или нескольких ресурсов (папки скачиваются в формате .zip)

## ⚙️ Переменные окружения

Для запуска приложения необходимо задать переменные окружения. Значения в примерах — демонстрационные, замените их на свои.

| Переменная | Описание | Пример |
|---|---|---|
| `DB_NAME` | Имя базы данных | `cloud_storage_db` |
| `DB_USERNAME` | Имя пользователя PostgreSQL | `postgres` |
| `DB_PASSWORD` | Пароль пользователя PostgreSQL | `postgres123` |
| `DB_PORT` | Порт PostgreSQL | `5432` |
| `MINIO_ROOT_USER` | Логин администратора MinIO | `minioadmin` |
| `MINIO_ROOT_PASSWORD` | Пароль администратора MinIO | `minioadmin123` |
| `MINIO_BUCKET_NAME` | Имя bucket'а в MinIO | `test-bucket` |
| `MINIO_API_PORT` | Порт API MinIO | `9000` |
| `MINIO_CONSOLE_PORT` | Порт web-консоли MinIO | `9001` |
| `MINIO_URL` | URL MinIO | `http://localhost:9000` |
| `REDIS_PORT` | Порт Redis | `6379` |

## 🔧 Запуск локально

1. Клонируйте репозиторий и перейдите в папку проекта:
   ```bash
   git clone https://github.com/j0797/cloud-file-storage.git
   cd cloud-file-storage
   ```

2. Создайте `.env` на основе `.env.example` и заполните переменные.

3. Поднимите инфраструктуру в Docker:
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```
   Это запустит PostgreSQL, MinIO и Redis.

4. Запустите приложение:
   ```bash
   ./gradlew bootRun
   ```

5. Откройте в браузере:
   - **Приложение:** [http://localhost:8080](http://localhost:8080)
   - **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **MinIO Console:** [http://localhost:9001](http://localhost:9001)

## 🛠 Тесты

```bash
./gradlew test
```

## 🌐 Демо

Проект будет доступен до 01.10.2026 по адресу:
➡️ **[http://159.194.234.138](http://159.194.234.138)**

**API-документация (Swagger UI):** [http://159.194.234.138/api/swagger-ui.html](http://159.194.234.138/api/swagger-ui.html)

## 📬 Контакты

По вопросам, касающимся проекта, можно писать в телеграм: @jf0797

Проект выполнен в рамках учебного курса: [zhukovsd/java-backend-learning-course](https://github.com/zhukovsd/java-backend-learning-course)

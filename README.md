# 🗂️ Project Name: Task Manager

## 📦 Project Setup Guide

## 🚀 Clone the Project

```bash
git clone https://github.com/aminegrioui/taskmanager.git
```

## 🐳 Run PostgreSQL Container

Start the PostgreSQL container using Docker:

```bash
docker run -d \
  --name postgres-container-db-tm \
  -e POSTGRES_DB=taskmanager_db \
  -e POSTGRES_USER=username \
  -e POSTGRES_PASSWORD=password \
  -p 5438:5432 \
  postgres:15
```

## 🏃 Run the Application

Start the Spring Boot application:

## 🔗 Test APIs

Use tools like **Postman** or **cURL** to test the APIs:

## 📥 Import Insomnia Collection

Import into your Insomnia workspace:
    - Click on **Import/Export** → **Import Data** → **From File**.
    - Select: `tm.json` file.

This collection contains pre-configured API requests for quick testing.



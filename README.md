# Антиплагиат система

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-blue.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.3-green.svg)](https://vuejs.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org)

Иинструмент для проверки оригинальности Java кода.  
Система анализирует текст и структуру кода, выявляя совпадения с базой проектов. Например, незначимые строки, перемещение переменных и большие совпадения.

## Стек технологий
- **Backend**: Kotlin + Spring Boot + PostgreSQL
- **Frontend**: Vue.js + Vite
- **Анализ кода**: JavaParser, ANTLR

## Функциональность
1. Анализ абстрактных синтаксических деревьев (AST)
2. Сравнение текста и структуры кода
3. Поиск модифицированных идентификаторов и переменных
4. Детекция перестановки блоков кода
5. Проверки блоков кода, файлов и целых проектов
6. Учет комментариев и документации
7. REST API для интеграции
8. Web-интерфейс для удобства использования

## Установка
```bash
# 0. Clone
git clone https://github.com/radonious/graduate-work.git

# 1. Set Up your .env
cp .env.example .env

# 2.1 Run locally (Fast, Requires running DB, 5-15sec) 
gradle clean bootRun

# 2.2. Build locally & Run in Docker (Recommended, 15-30sec)
gradle clean bootBuildImage
docker compose up -d --no-build

# 2.2. Run fully in Docker (Slow, ~3min)
docker compose up -d
```

## Лицензия
MIT License. Подробности в файле LICENSE

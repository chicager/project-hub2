# Пример работы с исключениями в Spring Boot

------------
### Описание:
- Пример обработки ошибок в Spring Boot проекте.
- В проекте используется пример наполнения базы данными при старте
- Реализован красивый вывод SQL в консоль
- Показан правильный механизм создания Entity классов
- Хранятся тестовые запросы в папке http (согласно best practices in Spring Boot apps)
- Реализованы интеграционные тесты (согласно best practices in Spring Boot apps)
- Реализован пример валидации
- В проекте используется база H2, подключение:
  - Через браузер:
    - Заходим на http://localhost:8080/h2-console
    - Вводим JDBC URL: jdbc:h2:mem:testdb
    - Username: sa
    - Password: (оставьте пустым)
    - Driver Class: org.h2.Driver
  - Через IntelliJ IDEA:
    - Откройте вкладку Database (обычно справа)
    - Нажмите + → Data Source → H2
    - Введите те же данные подключения:
    - URL: jdbc:h2:mem:testdb
    - User: sa
    - Password: (оставьте пустым)
  - Важно помнить, что так как вы используете базу данных в памяти
    (mem:testdb), данные будут доступны только пока запущено приложение.
    После остановки приложения все данные будут удалены.

------------
#### Подробное Описание:
- Давайте разберем основные моменты этого проекта:
  - Иерархия исключений:
    - BaseException - базовый класс для всех наших кастомных исключений
    - Специфические исключения наследуются от него и содержат ключ сообщения и аргументы
  - Обработка ошибок:
    - @RestControllerAdvice используется для глобальной обработки исключений
    - Разные типы исключений обрабатываются по-разному
    - Используется MessageSource для получения сообщений из messages.properties
  - Логирование:
    - Используется SLF4J для логирования
    - Разные уровни логирования для разных ситуаций
    - Логируются все исключения с контекстом
  - Валидация:
    - Используются аннотации валидации на уровне модели
    - Обработка ошибок валидации в GlobalExceptionHandler
  - Структурированный ответ об ошибке:
    - Класс ErrorResponse содержит всю необходимую информацию об ошибке
    - Включает временную метку, сообщение, код ошибки и путь запроса
  - Интернационализация:
    - Сообщения об ошибках хранятся в messages.properties
    - Поддерживается подстановка параметров в сообщения
- Чтобы протестировать это приложение, вы можете:
  - Попробовать создать пользователя с некорректными данными
  - Попробовать создать двух пользователей с одинаковым email
  - Попробовать получить несуществующего пользователя
- Каждый случай будет обработан соответствующим образом с правильным HTTP статусом, сообщением об ошибке и логированием
# Руководство по настройке проекта МедКарта

## Шаг 1: Установка Android Studio

1. Скачайте Android Studio с официального сайта: https://developer.android.com/studio
2. Установите Android Studio
3. При первом запуске установите необходимые SDK компоненты

## Шаг 2: Открытие проекта

1. Распакуйте архив `MedCardSystem.zip`
2. Откройте Android Studio
3. Выберите File → Open
4. Выберите папку `MedCardSystem`
5. Дождитесь синхронизации Gradle (может занять несколько минут)

## Шаг 3: Настройка Firebase

### 3.1 Создание проекта Firebase

1. Перейдите на https://console.firebase.google.com/
2. Нажмите "Добавить проект"
3. Введите имя проекта: "MedCardSystem"
4. Отключите Google Analytics (не обязательно)
5. Нажмите "Создать проект"

### 3.2 Добавление Android приложения

1. В консоли Firebase нажмите на иконку Android
2. Введите имя пакета: `com.medcard.system`
3. Введите псевдоним приложения: "МедКарта"
4. Скачайте файл `google-services.json`
5. Поместите файл в директорию `app/` проекта (замените существующий)

### 3.3 Настройка Authentication

1. В Firebase Console откройте раздел "Authentication"
2. Перейдите на вкладку "Sign-in method"
3. Включите метод "Email/Password"
4. Нажмите "Сохранить"

### 3.4 Настройка Firestore Database

1. В Firebase Console откройте раздел "Firestore Database"
2. Нажмите "Создать базу данных"
3. Выберите "Начать в тестовом режиме"
4. Выберите регион (например, europe-west)
5. Нажмите "Включить"

### 3.5 Настройка правил безопасности Firestore

1. В разделе Firestore перейдите на вкладку "Правила"
2. Вставьте следующие правила:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Пациенты - доступ только авторизованным пользователям
    match /patients/{patientId} {
      allow read, write: if request.auth != null;
    }
    
    // Пользователи - чтение для всех авторизованных, запись только для себя
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    // Журналы аудита - только чтение и запись для авторизованных
    match /audit_logs/{logId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

3. Нажмите "Опубликовать"

### 3.6 Настройка Storage (опционально)

1. В Firebase Console откройте раздел "Storage"
2. Нажмите "Начать"
3. Выберите правила безопасности "Начать в тестовом режиме"
4. Выберите регион
5. Нажмите "Готово"

## Шаг 4: Создание тестового пользователя

1. В Firebase Console откройте "Authentication"
2. Перейдите на вкладку "Users"
3. Нажмите "Add user"
4. Введите email: `doctor@medcard.test`
5. Введите пароль: `Test123456`
6. Нажмите "Add user"

## Шаг 5: Запуск приложения

### 5.1 На физическом устройстве

1. Включите режим разработчика на Android устройстве:
   - Откройте Настройки → О телефоне
   - Нажмите 7 раз на "Номер сборки"
2. Включите отладку по USB:
   - Откройте Настройки → Для разработчиков
   - Включите "Отладка по USB"
3. Подключите устройство к компьютеру через USB
4. В Android Studio выберите ваше устройство в списке
5. Нажмите Run (зеленая кнопка Play)

### 5.2 На эмуляторе

1. В Android Studio откройте AVD Manager (Tools → AVD Manager)
2. Нажмите "Create Virtual Device"
3. Выберите устройство (например, Pixel 6)
4. Выберите системный образ (API 34 или выше)
5. Нажмите "Finish"
6. Запустите эмулятор
7. Нажмите Run в Android Studio

## Шаг 6: Первый вход

1. Откроется экран входа
2. Введите email: `doctor@medcard.test`
3. Введите пароль: `Test123456`
4. Нажмите "Войти"
5. Вы попадете на главный экран приложения

## Возможные проблемы и решения

### Проблема: Ошибка синхронизации Gradle

**Решение:**
1. File → Invalidate Caches / Restart
2. Перезапустите Android Studio
3. Проверьте подключение к интернету

### Проблема: Google Services plugin ошибка

**Решение:**
1. Убедитесь, что файл `google-services.json` находится в папке `app/`
2. Проверьте, что имя пакета в `google-services.json` совпадает с `com.medcard.system`

### Проблема: Firebase Authentication не работает

**Решение:**
1. Проверьте, что метод Email/Password включен в Firebase Console
2. Убедитесь, что пользователь создан в разделе Authentication
3. Проверьте подключение к интернету

### Проблема: Биометрия не работает

**Решение:**
1. Биометрия работает только на физических устройствах
2. Убедитесь, что на устройстве настроен отпечаток пальца или Face ID
3. На эмуляторе можно настроить виртуальный отпечаток в настройках

## Дополнительная информация

### Структура базы данных Firestore

```
firestore/
├── patients/
│   └── {patientId}
│       ├── id: string
│       ├── lastName: string
│       ├── firstName: string
│       ├── middleName: string
│       ├── dateOfBirth: timestamp
│       ├── phone: string
│       ├── email: string
│       └── ...
├── users/
│   └── {userId}
│       ├── id: string
│       ├── email: string
│       ├── role: string
│       └── ...
└── audit_logs/
    └── {logId}
        ├── userId: string
        ├── action: string
        ├── timestamp: timestamp
        └── ...
```

### Тестирование функций

1. **Добавление пациента**: Нажмите кнопку "+" на главном экране
2. **Поиск пациента**: Используйте иконку поиска в верхнем меню
3. **Просмотр карты**: Нажмите на карточку пациента в списке
4. **Журнал аудита**: Откройте меню → "Журнал аудита"

## Контакты поддержки

При возникновении проблем:
1. Проверьте README.md
2. Изучите документацию Firebase: https://firebase.google.com/docs
3. Проверьте логи в Android Studio (Logcat)

Успешной работы с приложением МедКарта!

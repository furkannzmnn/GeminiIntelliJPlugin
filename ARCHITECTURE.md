# Clean Architecture - Gemini AI IntelliJ Plugin

Bu proje, Robert C. Martin'ın Clean Architecture prensiplerine uygun olarak tasarlanmıştır. Kod, SOLID prensiplerini takip eder ve test edilebilir, maintainable ve scalable bir yapıya sahiptir.

## 🏗️ Architecture Layers

### 1. Domain Layer (`domain/`)
**Business logic'in kalbi - hiçbir external dependency yok**

- `FileChangeEvent.kt` - Domain events (value objects)
- `FileChangeRepository.kt` - Repository interface (dependency inversion)
- `FileChangeService.kt` - Domain service (business logic)

### 2. Application Layer (`application/`)
**Use cases ve application logic**

- `FileChangeUseCase.kt` - Business operations orchestration
- `FileChangeEventHandler.kt` - Event handling logic
- `FileChangePresenter.kt` - Presentation interface (dependency inversion)

### 3. Infrastructure Layer (`infrastructure/`)
**External dependencies ve concrete implementations**

- `FileChangeRepositoryImpl.kt` - Repository concrete implementation
- IntelliJ API integrations
- File system operations

### 4. Presentation Layer (`presentation/`)
**UI ve user interaction**

- `FileChangePresenterImpl.kt` - Presentation logic implementation
- `CustomDiffDialog.kt` - UI components
- `GeminiToolWindowFactory.kt` - Main UI factory

## 🔄 Dependency Flow

```
Presentation → Application → Domain ← Infrastructure
     ↓              ↓           ↑           ↑
   UI/UX      Use Cases    Business    External
  Layer       Layer       Logic       APIs
```

## 🎯 SOLID Principles

### Single Responsibility Principle (SRP)
- Her sınıf tek bir sorumluluğa sahip
- `FileChangeService` sadece business logic
- `FileChangeRepository` sadece data access
- `FileChangePresenter` sadece presentation

### Open/Closed Principle (OCP)
- Yeni file change types eklemek için extension
- Repository pattern ile farklı storage implementations
- Presenter interface ile farklı UI implementations

### Liskov Substitution Principle (LSP)
- Repository interface implementations interchangeable
- Presenter implementations swappable

### Interface Segregation Principle (ISP)
- Küçük, focused interfaces
- `FileChangeRepository` sadece gerekli metodları expose eder

### Dependency Inversion Principle (DIP)
- Domain layer hiçbir external dependency'ye bağımlı değil
- Application layer sadece domain interfaces'e bağımlı
- Infrastructure layer domain interfaces'i implement eder

## 🧪 Testability

### Unit Tests
- Domain layer tamamen test edilebilir (no dependencies)
- Application layer mock'lar ile test edilebilir
- Infrastructure layer integration tests

### Mock Examples
```kotlin
// Domain service test
val mockRepository = mock<FileChangeRepository>()
val service = FileChangeService(mockRepository, project)

// Use case test
val mockService = mock<FileChangeService>()
val mockPresenter = mock<FileChangePresenter>()
val useCase = FileChangeUseCase(mockService, mockPresenter)
```

## 🚀 Benefits

1. **Maintainability** - Her layer bağımsız olarak değiştirilebilir
2. **Testability** - Her component izole test edilebilir
3. **Scalability** - Yeni features kolayca eklenebilir
4. **Flexibility** - UI veya storage değişiklikleri domain'i etkilemez
5. **Readability** - Kod akışı net ve anlaşılır

## 📁 File Organization

```
src/main/kotlin/com/yourcompany/geminiplugin/
├── domain/                    # Business logic
│   ├── FileChangeEvent.kt
│   ├── FileChangeRepository.kt
│   └── FileChangeService.kt
├── application/               # Use cases
│   ├── FileChangeUseCase.kt
│   ├── FileChangeEventHandler.kt
│   └── FileChangePresenter.kt
├── infrastructure/            # External dependencies
│   └── FileChangeRepositoryImpl.kt
├── presentation/              # UI layer
│   ├── FileChangePresenterImpl.kt
│   └── CustomDiffDialog.kt
└── GeminiToolWindowFactory.kt # Main entry point
```

## 🔧 Dependency Injection

Dependencies manual olarak inject edilir (DI framework kullanılmaz):

```kotlin
private fun initializeCleanArchitecture(project: Project) {
    fileChangeRepository = FileChangeRepositoryImpl(project)
    fileChangeService = FileChangeService(fileChangeRepository, project)
    fileChangePresenter = FileChangePresenterImpl(project, fileChangeService, streamedOutputListModel)
    fileChangeUseCase = FileChangeUseCase(fileChangeService, fileChangePresenter)
    fileChangeEventHandler = FileChangeEventHandler(fileChangeUseCase)
}
```

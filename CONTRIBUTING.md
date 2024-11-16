# Contributing to Tracks

First off, thank you for considering contributing to Tracks! It's people like you who make Tracks such a great tool for connecting people through music.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Development Setup](#development-setup)
- [How Can I Contribute?](#how-can-i-contribute)
- [Style Guidelines](#style-guidelines)
- [Project Structure](#project-structure)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Community](#community)

## Code of Conduct

### Our Pledge
We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone, regardless of age, body size, visible or invisible disability, ethnicity, sex characteristics, gender identity and expression, level of experience, education, socio-economic status, nationality, personal appearance, race, caste, color, religion, or sexual identity and orientation.

### Standards
Examples of behavior that contributes to a positive environment:
- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

Examples of unacceptable behavior:
- The use of sexualized language or imagery
- Trolling, insulting/derogatory comments, and personal or political attacks
- Public or private harassment
- Publishing others' private information without permission
- Other conduct which could reasonably be considered inappropriate in a professional setting

## Development Setup

### Prerequisites
- Android Studio Koala | 2024.1.1 or newer
- JDK 17
- Android SDK API level 26+
- Git
- Kotlin Plugin (bundled with Android Studio)
- Jetpack Compose Plugin (bundled with Android Studio)

### Required Plugins
Make sure these plugins are installed and enabled in Android Studio:
- Kotlin
- Compose Multiplatform IDE Support
- KMM (Kotlin Multiplatform Mobile)

### Getting Started
1. Fork the repository
2. Clone your fork
```bash
git clone https://github.com/yourusername/tracks.git
```
3. Add the upstream repository
```bash
git remote add upstream https://github.com/original/tracks.git
```
4. Create a new branch for your feature
```bash
git checkout -b feature/your-feature-name
```

### Building the Project
1. Open the project in Android Studio
2. Sync project with Gradle files
3. Build the project using:
```bash
./gradlew build
```

## Project Structure
```
app/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  ├─ com/
│  │  │  │  ├─ litecodez/
│  │  │  │  │  ├─ tracksc/
│  │  │  │  │  │  ├─ screens/      # Individual screens/routes of the app
│  │  │  │  │  │  ├─ ui/           
│  │  │  │  │  │  │  ├─ theme/     # App theming and styling
│  │  │  │  │  │  ├─ models/       # Data models and state classes
│  │  │  │  │  │  ├─ components/   # Reusable Compose components
│  │  │  │  │  │  ├─ objects/      # Singleton objects, classes and constants
│  │  │  │  │  │  ├─ services/     # Background and foreground services
│  │  │  │  │  │  └─ launchers/    # App entry points and initialization
│  │  ├─ res/                      # Resources (layouts, drawables, values, etc.)
```

Each directory serves a specific purpose:
- `screens/`: Contains all the app screens implemented using Jetpack Compose
- `models/`: Data classes and models used throughout the app
- `components/`: Reusable Compose components and UI elements
- `objects/`: Singleton objects, classes, constants, and static utilities
- `services/`: Background foreground servicesand core functionality
- `launchers/`: App services initialization and entry point logic
- `res/`: Android resources including drawables, values, and other assets

## Style Guidelines

### Kotlin Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Utilize Kotlin's functional programming features appropriately
- Keep functions concise and focused
- Use meaningful names for variables, functions, and classes

### Jetpack Compose Guidelines
- Follow [Compose Best Practices](https://developer.android.com/jetpack/compose/best-practices)
- Keep composables small and reusable
- Use proper state management
- Follow unidirectional data flow
- Use proper composition local where necessary
- Implement proper theme handling

### Documentation
- Add KDoc comments for public functions and classes
- Document complex business logic
- Update README.md for new features
- Include usage examples in documentation
- Document compose previews

## Commit Messages
Use clear and meaningful commit messages following this format:
```
feat(scope): add hat wobble
^--^^-----^  ^------------^
|   |        |
|   |        +-> Summary in present tense
|   +----------> Scope: screens, ui, models, etc.
+--------------> Type: feat, fix, docs, style, refactor, test, chore
```

Example commit messages:
- `feat(screens): add music player screen`
- `fix(services): resolve music sync delay`
- `docs(readme): update build instructions`

## Pull Request Process

### Before Submitting
1. Update documentation to reflect changes
2. Test your changes thoroughly
3. Ensure your code follows style guidelines
4. Write meaningful commit messages

### Pull Request Checklist
- [ ] Tests pass
- [ ] Code follows Kotlin and Compose style guidelines
- [ ] Documentation updated
- [ ] PR description is clear
- [ ] Linked to relevant issues
- [ ] Compose previews are working
- [ ] Proper state handling implemented

### After Submitting
1. Respond to review comments
2. Make requested changes
3. Update PR with new commits
4. Squash commits once approved

## Community

### Getting Help
- Check existing issues and discussions
- Create a new issue for bugs
- Start a discussion for general questions
- Be clear and provide context

### Communication Channels
- GitHub Issues & Discussions
- Project Discord (link in README)

Remember: The best way to ensure your contribution is accepted is to follow these guidelines, write good code, and communicate effectively with the community.
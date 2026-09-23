# AndroidWeatherAPP

> Aplicación Android nativa de meteorología con integración de sensores hardware, desarrollada en Kotlin con Jetpack Compose y arquitectura moderna de Android.

---

## 📋 Tabla de Contenidos

- [Descripción](#descripción)
- [Características](#características)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Instalación y Configuración](#instalación-y-configuración)
- [API Key](#api-key)
- [Uso](#uso)
- [Capturas de Pantalla](#capturas-de-pantalla)

---

## Descripción

**Weather Shaker** es una aplicación Android nativa que muestra información meteorológica en tiempo real para múltiples ciudades de todo el mundo. Su característica diferencial es la integración del **acelerómetro del dispositivo**: agitar el móvil cambia automáticamente la ciudad mostrada de forma aleatoria, ofreciendo una experiencia de usuario intuitiva y dinámica.

El proyecto consume la **API REST de OpenWeatherMap** para obtener datos actualizados de temperatura, sensación térmica, humedad, presión y viento, presentándolos con una interfaz moderna construida íntegramente con **Jetpack Compose** y **Material Design 3**.

---

## Características

- Consulta meteorológica en tiempo real vía API REST (OpenWeatherMap)
- **Shake-to-refresh**: agitar el dispositivo cambia la ciudad aleatoriamente (acelerómetro)
- Soporte para más de 20 ciudades internacionales preconfiguradas
- Búsqueda manual de ciudades con filtro en tiempo real
- Información completa: temperatura actual, sensación térmica, mín/máx, humedad, presión y viento
- UI reactiva y declarativa con Jetpack Compose
- Diseño adaptativo según orientación del dispositivo
- Timestamp de última actualización en cada consulta
- Gestión de estados de carga y error con feedback visual
- Scroll vertical para layouts de mayor contenido

---

## Arquitectura

La aplicación sigue una arquitectura **Single Activity** con separación de responsabilidades en capas:

```
┌────────────────────────────────────────────────┐
│              UI Layer (Compose)                │
│   WeatherApp() · WeatherCard() · CityDialog()  │
│   Material Design 3 · Responsive Layout        │
└──────────────────────┬─────────────────────────┘
                       │  mutableStateOf (reactive)
┌──────────────────────▼─────────────────────────┐
│           MainActivity (ViewModel-like)         │
│   Estado: weatherData · isLoading · errorMsg   │
│   Sensor: SensorManager · SensorEventListener  │
└──────────────────────┬─────────────────────────┘
                       │  lifecycleScope (coroutines)
┌──────────────────────▼─────────────────────────┐
│            Repository Layer                    │
│   WeatherRepository · Retrofit · WeatherApi    │
└──────────────────────┬─────────────────────────┘
                       │  HTTP REST
┌──────────────────────▼─────────────────────────┐
│         OpenWeatherMap API (External)          │
│   https://api.openweathermap.org/data/2.5/     │
└────────────────────────────────────────────────┘
```

---

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 1.9.x | Lenguaje principal |
| Jetpack Compose | BOM 2024.x | UI declarativa |
| Material Design 3 | — | Sistema de diseño |
| Retrofit | 2.x | Cliente HTTP para API REST |
| Gson Converter | — | Deserialización JSON |
| Kotlin Coroutines | 1.7.x | Operaciones asíncronas |
| Android SensorManager | — | Lectura del acelerómetro |
| Android SDK | API 31+ | Target platform |
| Gradle | 8.x | Build system |

---

## Estructura del Proyecto

```
ThridProjectWeatherApp/
├── app/
│   └── src/main/
│       ├── java/com/example/thridprojectweatherapp/
│       │   ├── MainActivity.kt              # Activity principal + lógica sensor
│       │   ├── WeatherRepository.kt         # Capa de datos + Retrofit + modelos
│       │   └── ui/theme/
│       │       ├── Color.kt                 # Paleta de colores
│       │       ├── Theme.kt                 # Tema Material 3
│       │       └── Type.kt                  # Tipografía
│       ├── res/
│       │   ├── drawable/                    # Iconos y recursos gráficos
│       │   ├── values/
│       │   │   ├── colors.xml
│       │   │   ├── strings.xml
│       │   │   └── themes.xml
│       │   └── xml/
│       │       └── backup_rules.xml
│       └── AndroidManifest.xml              # Permisos y configuración
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Instalación y Configuración

### Prerrequisitos

- Android Studio Hedgehog o superior
- JDK 17
- Android SDK API 31 o superior
- Conexión a Internet para llamadas a la API

### Pasos

1. Clonar o descomprimir el proyecto
2. Abrir la carpeta raíz con **Android Studio**
3. Esperar la sincronización de Gradle
4. Configurar la API Key (ver sección siguiente)
5. Ejecutar en dispositivo o emulador (`Run → Run 'app'`)

---

## API Key

La aplicación usa la **API de OpenWeatherMap** (gratuita hasta 60 llamadas/minuto).

1. Regístrate en [openweathermap.org](https://openweathermap.org/api)
2. Obtén tu API Key gratuita desde el dashboard
3. Sustitúyela en `WeatherRepository.kt`:

```kotlin
private const val API_KEY = "TU_API_KEY_AQUI"
```

---

## Uso

### Consultar el tiempo

Al abrir la app, se carga automáticamente el tiempo de la primera ciudad configurada.

### Cambiar ciudad manualmente

Pulsa el icono de búsqueda y escribe el nombre de la ciudad deseada del listado.

### Shake-to-refresh

Agita el dispositivo para cambiar a una ciudad aleatoria del listado. El sistema usa el acelerómetro para detectar el gesto:

```kotlin
// Detección del gesto de agitación
val speed = Math.abs(deltaX + deltaY + deltaZ) / timeDelta * 10000
if (speed > ShakeThreshold) {
    loadWeatherForRandomCity()
}
```

### Permisos requeridos

| Permiso | Motivo |
|---|---|
| `INTERNET` | Llamadas a la API de OpenWeatherMap |
| `ACCESS_NETWORK_STATE` | Verificar disponibilidad de red |
| `android.hardware.sensor.accelerometer` | Detección del gesto de agitación |

---

## Ciudades Preconfiguradas

La app incluye más de 20 ciudades preconfiguradas con sus coordenadas GPS:

Londres · Madrid · París · Tokio · Nueva York · Berlín · Roma · Sídney · Barcelona · Ámsterdam · Varsovia · Cracovia · Gdańsk · Wrocław · Poznań · Łódź · Szczecin · Białystok · Lublin · Katowice · Praga · Viena · Budapest






## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

* [Create](https://docs.gitlab.com/user/project/repository/web_editor/#create-a-file) or [upload](https://docs.gitlab.com/user/project/repository/web_editor/#upload-a-file) files
* [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.com/pirot10/androidweatherapp.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

* [Set up project integrations](https://gitlab.com/pirot10/androidweatherapp/-/settings/integrations)

## Collaborate with your team

* [Invite team members and collaborators](https://docs.gitlab.com/user/project/members/)
* [Create a new merge request](https://docs.gitlab.com/user/project/merge_requests/creating_merge_requests/)
* [Automatically close issues from merge requests](https://docs.gitlab.com/user/project/issues/managing_issues/#closing-issues-automatically)
* [Enable merge request approvals](https://docs.gitlab.com/user/project/merge_requests/approvals/)
* [Set auto-merge](https://docs.gitlab.com/user/project/merge_requests/auto_merge/)

## Test and Deploy

Use the built-in continuous integration in GitLab.

* [Get started with GitLab CI/CD](https://docs.gitlab.com/ci/quick_start/)
* [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/user/application_security/sast/)
* [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/topics/autodevops/requirements/)
* [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/user/clusters/agent/)
* [Set up protected environments](https://docs.gitlab.com/ci/environments/protected_environments/)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.

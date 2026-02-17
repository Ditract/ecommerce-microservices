
## Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CLIENTE                                     │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │ HTTP
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          API GATEWAY :8080                               │
│  Routes: /api/auth → :8081  |  /api/users, /api/roles → :8082          │
└────────┬────────────────────────────┬─────────────────────────────────┘
         │                            │
         ▼                            ▼
┌──────────────────────┐    ┌──────────────────────┐
│   AUTH-SERVICE       │    │   USER-SERVICE       │
│   :8081              │───►│   :8082              │
│                      │Feign                      │
│ • Credenciales       │    │ • Usuarios           │
│ • JWT                │    │ • Roles              │
│ • Compensación       │    │ • Asignación roles   │
└──────┬───────────────┘    └──────┬───────────────┘
       │                           │
       ▼                           ▼
┌──────────────────────┐    ┌──────────────────────┐
│   AUTH_DB            │    │   USER_DB            │
│   PostgreSQL :5433   │    │   PostgreSQL :5432   │
│                      │    │                      │
│ • credentials        │    │ • users              │
│   - user_id          │    │ • roles              │
│   - email            │    │ • user_roles         │
│   - password_hash    │    │                      │
│   - is_active        │    │                      │
└──────────────────────┘    └──────────────────────┘

```

## Por qué microservicios

Entiendo que, al momento de decidir qué arquitectura implementar, es necesario haber respondido diversas preguntas, tan importantes como: ¿Qué problema específico tengo hoy que un monolito no puede resolver bien? Y es que posiblemente a la mayoría de proyectos les baste con un monolito modular. Sin embargo, este repositorio lo creé con la intención de aprender, por lo que me tomaré la molestia de implementar microservicios.


## Decisiones técnicas

**Service Discovery**

Inicialmente usaba Netflix Eureka, pero tal parece que ya no es el estándar de facto en la industria. En su lugar, decidí usar Docker Compose para la comunicación entre servicios vía DNS interno y su orquestación en desarrollo. De cara al futuro puede que haya sido una mejor decisión, ya que Kubernetes es el estándar en producción, y tanto Docker Compose como Kubernetes orquestan contenedores creados a partir de imágenes construidas con Docker.

**Auth vs User**

Aquí fue donde surgieron numerosas dudas y preguntas: ¿quién debería llevar los roles? ¿Cómo hacer los servicios realmente independientes? Preguntas que he ido respondiendo a medida que avanzo.

Decidí que user-service sea el responsable de los roles porque estos no son solo etiquetas técnicas: responden a reglas del negocio (suscripciones, antigüedad, moderación, etc.). Esa lógica no debería pertenecer al servicio de autenticación.

Sin embargo, hacerlo de esa forma conlleva que auth-service consulte user-service en cada operación de autenticación para obtener los roles y agregarlos al JWT. Esto genera un acoplamiento que rompe la independencia de servicios, lo que a su vez abre nuevas preguntas: ¿qué pasa si un servicio falla al momento del registro? ¿Se deberían cachear los roles?

En este punto aprendí conceptos interesantes como la compensación y el patrón saga, que terminé aplicando en el registro. El flujo es: primero creo el perfil en user-service, obtengo el ID, luego creo las credenciales inactivas en auth-service, y solo si todo sale bien las activo. Si algo falla (por ejemplo, user-service no responde después de crear el perfil), deshago lo que pueda: desactivo las credenciales para que esa cuenta no pueda iniciar sesión. No es una saga perfecta (todavía no limpio el usuario en user-service si falla la activación), pero al menos evito cuentas fantasma con credenciales válidas.

Antes de pasar al servicio de productos, planeo corregir el problema de los roles y mejorar la compensación al momento del registro. Sin embargo, el registro y el login funcionan correctamente.

## Probar en local

### Requisitos previos
- Docker y Docker Compose instalados

1. **Clonar el repositorio**
```bash
git clone 
cd ecommerce-microservices
```

2. **Levantar todos los servicios**
```bash
docker compose up --build -d
```

4. **Registrar un usuario**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "password":"Password123",
    "firstName":"Test",
    "lastName":"User"
  }'
```

5. **Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "password":"Password123"
  }'
```

### Detener servicios
```bash
docker compose down
```




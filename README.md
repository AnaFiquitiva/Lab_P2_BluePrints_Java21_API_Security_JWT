# Escuela Colombiana de Ingeniería Julio Garavito
## Arquitectura de Software – ARSW
### Laboratorio – Parte 2: BluePrints API con Seguridad JWT (OAuth 2.0)

Este laboratorio extiende la **Parte 1** ([Lab_P1_BluePrints_Java21_API](https://github.com/DECSIS-ECI/Lab_P1_BluePrints_Java21_API)) agregando **seguridad a la API** usando **Spring Boot 3, Java 21 y JWT (OAuth 2.0)**.  
El API se convierte en un **Resource Server** protegido por tokens Bearer firmados con **RS256**.  
Incluye un endpoint didáctico `/auth/login` que emite el token para facilitar las pruebas.

---

## Objetivos
- Implementar seguridad en servicios REST usando **OAuth2 Resource Server**.
- Configurar emisión y validación de **JWT**.
- Proteger endpoints con **roles y scopes** (`blueprints.read`, `blueprints.write`).
- Integrar la documentación de seguridad en **Swagger/OpenAPI**.

---

## Requisitos
- JDK 21
- Maven 3.9+
- Git

---

## Ejecución del proyecto
1. Clonar o descomprimir el proyecto:
   ```bash
   git clone https://github.com/DECSIS-ECI/Lab_P2_BluePrints_Java21_API_Security_JWT.git
   cd Lab_P2_BluePrints_Java21_API_Security_JWT
   ```
   ó si el profesor entrega el `.zip`, descomprimirlo y entrar en la carpeta.

2. Ejecutar con Maven:
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```

3. Verificar que la aplicación levante en `http://localhost:8080`.

---

## Endpoints principales

### 1. Login (emite token)
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "student",
  "password": "student123"
}
```
Respuesta:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### 2. Consultar blueprints (requiere scope `blueprints.read`)
```
GET http://localhost:8080/api/blueprints
Authorization: Bearer <ACCESS_TOKEN>
```

### 3. Crear blueprint (requiere scope `blueprints.write`)
```
POST http://localhost:8080/api/blueprints
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "name": "Nuevo Plano"
}
```

---

## Swagger UI
- URL: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Pulsa **Authorize**, ingresa el token en el formato:
  ```
  Bearer eyJhbGciOi...
  ```

---

## Estructura del proyecto
```
src/main/java/co/edu/eci/blueprints/
  ├── api/BlueprintController.java       # Endpoints protegidos
  ├── auth/AuthController.java           # Login didáctico para emitir tokens
  ├── config/OpenApiConfig.java          # Configuración Swagger + JWT
  └── security/
       ├── SecurityConfig.java
       ├── MethodSecurityConfig.java
       ├── JwtKeyProvider.java
       ├── InMemoryUserService.java
       └── RsaKeyProperties.java
src/main/resources/
  └── application.yml
```

---

## Actividades propuestas
1. Revisar el código de configuración de seguridad (`SecurityConfig`) e identificar cómo se definen los endpoints públicos y protegidos.

   **Respuesta:**  
   En la clase `SecurityConfig.java` se define un bean `SecurityFilterChain` que configura las reglas de acceso a través del método `authorizeHttpRequests`:

   ```java
   .authorizeHttpRequests(auth -> auth
       .requestMatchers("/actuator/health", "/auth/login").permitAll()
       .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
       .requestMatchers("/api/**").hasAnyAuthority("SCOPE_blueprints.read", "SCOPE_blueprints.write")
       .anyRequest().authenticated()
   )
   ```

   - **Endpoints públicos** (`permitAll()` — no requieren token):
     - `/actuator/health` – estado de salud de la aplicación.
     - `/auth/login` – endpoint didáctico para obtener el JWT.
     - `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` – documentación OpenAPI/Swagger.

   - **Endpoints protegidos** (requieren JWT con scope válido):
     - Cualquier ruta bajo `/api/**` exige que el token contenga el scope `blueprints.read` **o** `blueprints.write` (Spring los expone como autoridades `SCOPE_blueprints.*`).

   - **Resto de peticiones** (`.anyRequest().authenticated()`): cualquier otra URL requiere autenticación genérica.

   Adicionalmente, la configuración:
   - Deshabilita CSRF (`csrf.disable()`) porque la API es stateless (no usa sesiones de navegador).
   - Declara la aplicación como **OAuth2 Resource Server** validando tokens JWT firmados con RS256 mediante `oauth2ResourceServer(oauth2 -> oauth2.jwt(...))`.

2. Explorar el flujo de login y analizar las claims del JWT emitido.

   **Respuesta:**

   **Flujo de login paso a paso:**

   1. El cliente envía `POST /auth/login` con `{ "username": "student", "password": "student123" }`.
   2. `AuthController` delega la validación a `InMemoryUserService.isValid()` (compara credenciales codificadas en BCrypt).
   3. Si las credenciales son correctas, se construye un `JwtClaimsSet` con todas las claims.
   4. Se firma el token con el algoritmo **RS256** usando la clave privada RSA (`JwtKeyProvider`).
   5. Se retorna un `TokenResponse` con el token, el tipo `Bearer` y la duración en segundos.

   **Claims del JWT emitido** (payload decodificado en Base64):

   ```json
   {
     "iss": "https://decsis-eci/blueprints",
     "sub": "student",
     "iat": 1772178791,
     "exp": 1772182391,
     "scope": "blueprints.read blueprints.write"
   }
   ```

   | Claim | Descripción |
   |-------|-------------|
   | `iss` | Emisor del token (`issuer` definido en `application.yml`) |
   | `sub` | Sujeto — nombre del usuario autenticado |
   | `iat` | Timestamp de emisión (*issued at*) |
   | `exp` | Timestamp de expiración (`iat + ttl`, por defecto 3600 s = 1 hora) |
   | `scope` | Permisos concedidos; Spring los convierte en autoridades `SCOPE_blueprints.read` y `SCOPE_blueprints.write` |

   El header del JWT indica el algoritmo: `{ "alg": "RS256" }`.

3. Extender los scopes (`blueprints.read`, `blueprints.write`) para controlar otros endpoints de la API, del laboratorio P1 trabajado.

   **Respuesta:**  
   Se agregaron tres nuevos endpoints en `BlueprintController.java`, cada uno anotado con `@PreAuthorize` para aplicar el scope correspondiente:

   | Método | Ruta | Scope requerido | Descripción |
   |--------|------|-----------------|-------------|
   | `GET` | `/api/blueprints` | `blueprints.read` | Listar todos los blueprints |
   | `GET` | `/api/blueprints/{id}` | `blueprints.read` | Consultar un blueprint por ID |
   | `POST` | `/api/blueprints` | `blueprints.write` | Crear un nuevo blueprint |
   | `PUT` | `/api/blueprints/{id}` | `blueprints.write` | Actualizar un blueprint existente |
   | `DELETE` | `/api/blueprints/{id}` | `blueprints.write` | Eliminar un blueprint |

   El control se aplica mediante `@PreAuthorize` en cada método del controlador:

   ```java
   @GetMapping("/{id}")
   @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
   public Map<String, String> getById(@PathVariable String id) { ... }

   @PutMapping("/{id}")
   @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
   public Map<String, String> update(@PathVariable String id, @RequestBody Map<String, String> in) { ... }

   @DeleteMapping("/{id}")
   @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
   public Map<String, String> delete(@PathVariable String id) { ... }
   ```

   Esto garantiza que:
   - Un token con solo `blueprints.read` puede **consultar** pero no modificar ni eliminar.
   - Un token con `blueprints.write` puede **crear, actualizar y eliminar**.

4. Modificar el tiempo de expiración del token y observar el efecto.

   **Respuesta:**  
   El tiempo de vida del token se controla con la propiedad `token-ttl-seconds` en `application.yml`.

   **Cambio aplicado** en `src/main/resources/application.yml`:

   ```yaml
   # Antes:
   token-ttl-seconds: 3600   # 1 hora

   # Después:
   token-ttl-seconds: 60     # 1 minuto
   ```

   Internamente, `AuthController` usa ese valor para calcular la claim `exp` del JWT:

   ```java
   long ttl = props.tokenTtlSeconds() != null ? props.tokenTtlSeconds() : 3600;
   Instant exp = now.plusSeconds(ttl);
   ```

   **Efecto observado:**

   | Situación | Resultado |
   |-----------|----------|
   | Token emitido → petición inmediata (`< 60 s`) | `200 OK` – acceso permitido |
   | Token emitido → petición tras 60 s | `401 Unauthorized` – token expirado |
   | Campo `expires_in` en la respuesta del login | Muestra `60` en lugar de `3600` |

   El Resource Server de Spring Security valida automáticamente la claim `exp`; si el instante actual supera ese valor, rechaza el token con un error `invalid_token` sin necesidad de código adicional.

   > **Nota:** Para restaurar el comportamiento original, cambiar el valor de vuelta a `3600`.

5. Documentar en Swagger los endpoints de autenticación y de negocio.

   **Respuesta:**  
   Se agregaron anotaciones de **SpringDoc / OpenAPI 3** en ambos controladores:

   **`AuthController.java`** (endpoint público):
   - `@Tag` — agrupa el endpoint bajo la sección *Autenticación* en Swagger UI.
   - `@Operation` — describe el propósito del login y los usuarios disponibles.
   - `@ApiResponses` — documenta los códigos `200` (token emitido) y `401` (credenciales inválidas).
   - `@SecurityRequirements` vacío — indica que este endpoint **no requiere** token en Swagger UI.

   **`BlueprintController.java`** (endpoints protegidos):
   - `@Tag` a nivel de clase — agrupa todos los endpoints bajo *Blueprints*.
   - `@SecurityRequirement(name = "bearer-jwt")` a nivel de clase — Swagger UI muestra el candado en todos los métodos.
   - `@Operation` por método — describe cada operación y el scope necesario.
   - `@ApiResponses` por método — documenta `200`, `401` y `403` en cada endpoint.

   **Pasos para probar en Swagger UI:**
   1. Abrir `http://localhost:8080/swagger-ui/index.html`.
   2. En la sección *Autenticación*, ejecutar `POST /auth/login` con `student / student123`.
   3. Copiar el `access_token` de la respuesta.
   4. Pulsar **Authorize** (🔒) en la parte superior y pegar el token.
   5. Ejecutar cualquier endpoint de la sección *Blueprints* — Swagger enviará el `Authorization: Bearer ...` automáticamente.

---

## Lecturas recomendadas
- [Spring Security Reference – OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Boot – Securing Web Applications](https://spring.io/guides/gs/securing-web/)
- [JSON Web Tokens – jwt.io](https://jwt.io/introduction)

---

## Licencia
Proyecto educativo con fines académicos – Escuela Colombiana de Ingeniería Julio Garavito.

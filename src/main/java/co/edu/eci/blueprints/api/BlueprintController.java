package co.edu.eci.blueprints.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blueprints")
@Tag(name = "Blueprints", description = "CRUD de blueprints — requiere token JWT con scope blueprints.read o blueprints.write")
@SecurityRequirement(name = "bearer-jwt")
public class BlueprintController {

    @Operation(summary = "Listar blueprints", description = "Requiere scope blueprints.read")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de blueprints"),
        @ApiResponse(responseCode = "401", description = "Token ausente o expirado"),
        @ApiResponse(responseCode = "403", description = "Scope insuficiente")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public List<Map<String, String>> list() {
        return List.of(
            Map.of("id", "b1", "name", "Casa de campo"),
            Map.of("id", "b2", "name", "Edificio urbano")
        );
    }

    @Operation(summary = "Obtener blueprint por ID", description = "Requiere scope blueprints.read")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Blueprint encontrado"),
        @ApiResponse(responseCode = "401", description = "Token ausente o expirado"),
        @ApiResponse(responseCode = "403", description = "Scope insuficiente")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public Map<String, String> getById(@PathVariable String id) {
        return Map.of("id", id, "name", "Blueprint " + id, "status", "activo");
    }

    @Operation(summary = "Crear blueprint", description = "Requiere scope blueprints.write")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Blueprint creado"),
        @ApiResponse(responseCode = "401", description = "Token ausente o expirado"),
        @ApiResponse(responseCode = "403", description = "Scope insuficiente")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public Map<String, String> create(@RequestBody Map<String, String> in) {
        return Map.of("id", "new", "name", in.getOrDefault("name", "nuevo"));
    }

    @Operation(summary = "Actualizar blueprint", description = "Requiere scope blueprints.write")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Blueprint actualizado"),
        @ApiResponse(responseCode = "401", description = "Token ausente o expirado"),
        @ApiResponse(responseCode = "403", description = "Scope insuficiente")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public Map<String, String> update(@PathVariable String id, @RequestBody Map<String, String> in) {
        return Map.of("id", id, "name", in.getOrDefault("name", "actualizado"), "updated", "true");
    }

    @Operation(summary = "Eliminar blueprint", description = "Requiere scope blueprints.write")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Blueprint eliminado"),
        @ApiResponse(responseCode = "401", description = "Token ausente o expirado"),
        @ApiResponse(responseCode = "403", description = "Scope insuficiente")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public Map<String, String> delete(@PathVariable String id) {
        return Map.of("id", id, "deleted", "true");
    }
}

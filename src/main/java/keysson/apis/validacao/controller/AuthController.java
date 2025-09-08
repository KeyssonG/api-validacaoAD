package keysson.apis.validacao.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import keysson.apis.validacao.dto.request.ConfirmResetPassword;
import keysson.apis.validacao.dto.request.LoginRequest;
import keysson.apis.validacao.dto.request.RequestResetPassword;
import keysson.apis.validacao.dto.request.RequestUpdatePassword;
import keysson.apis.validacao.dto.response.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.sql.SQLException;

public interface AuthController {

    @PostMapping("/login-multithread")
    @Operation(
            summary = "Login do usuário",
            description = "Endpoint que autentica usuário através do username e password, e gera Token.",
            requestBody = @RequestBody(
                    description = "Dados de login do usuário.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
    )
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws SQLException;

    @PutMapping("/alterar/senha")
    @Operation(
            summary = "Alterar senha",
            description = "Endpoint para alterar senha de acesso do usuário. " +
                    "Requer um token JWT válido no cabeçalho Authorization.",
            requestBody = @RequestBody(
                    description = "Nova senha do usuário.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RequestUpdatePassword.class))
            )
    )
    void updatePassword(@RequestBody RequestUpdatePassword request,
                        @RequestHeader("Authorization") String token) throws SQLException;

    @PostMapping("/reset-senha/solicitar")
    @Operation(
            summary = "Solicitar reset de senha",
            description = "Endpoint para solicitar reset de senha. Valida usuário e email na base de dados e envia token para fila do RabbitMQ.",
            requestBody = @RequestBody(
                    description = "Username e email do usuário para reset de senha.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RequestResetPassword.class))
            )
    )
    ResponseEntity<Void> solicitarResetSenha(@RequestBody RequestResetPassword request) throws SQLException;

    @PostMapping("/reset-senha/confirmar")
    @Operation(
            summary = "Confirmar reset de senha",
            description = "Endpoint para confirmar reset de senha usando token recebido e definir nova senha.",
            requestBody = @RequestBody(
                    description = "Token e nova senha do usuário.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ConfirmResetPassword.class))
            )
    )
    ResponseEntity<Void> confirmarResetSenha(@RequestBody ConfirmResetPassword request) throws SQLException;
}

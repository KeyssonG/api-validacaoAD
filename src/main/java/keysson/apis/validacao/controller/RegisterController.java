package keysson.apis.validacao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import keysson.apis.validacao.dto.request.RequestRegister;
import keysson.apis.validacao.exception.BusinessRuleException;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.SQLException;

public interface RegisterController {

    @PostMapping("/register")
    @Operation(
            summary = "Cadastrar uma nova empresa",
            description = "Endpoint para cadastrar uma nova empresa, cria usuário administrativo.",
            requestBody = @RequestBody(
                    description = "Dados da nova empresa",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RequestRegister.class)
                    )
            )
    )
    public void register(@RequestBody RequestRegister requestRegister)
            throws BusinessRuleException, SQLException;
}

package keysson.apis.validacao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagensPendentes {
    private int idFuncionario;
    private String email;
    private String username;
}

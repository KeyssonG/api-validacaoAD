package keysson.apis.validacao.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import keysson.apis.validacao.Utils.JwtUtil;
import keysson.apis.validacao.dto.PasswordResetEvent;
import keysson.apis.validacao.dto.request.LoginRequest;
import keysson.apis.validacao.dto.request.RequestUpdatePassword;
import keysson.apis.validacao.dto.response.LoginResponse;
import keysson.apis.validacao.exception.BusinessRuleException;
import keysson.apis.validacao.exception.enums.ErrorCode;
import keysson.apis.validacao.model.PasswordResetToken;
import keysson.apis.validacao.model.User;
import keysson.apis.validacao.repository.ValidacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ValidacaoRepository validacaoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login (LoginRequest request) throws SQLException {
            User user = validacaoRepository.findByUsername(request.getUsername());
            if (user == null) {
                throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND);
            }

            Boolean checkPassword = passwordEncoder.matches(request.getPassword(), user.getPassword());

            if (checkPassword == false) {
                throw new BusinessRuleException(ErrorCode.BAD_PASSWORD);
            }

            boolean isPrimeiroAcesso = user.isPrimeiroAcesso();
            if (isPrimeiroAcesso) {
                    validacaoRepository.updateFirstAccess( false, user.getId());
            }

            String token = jwtUtil.generateToken(
                    user.getId(),
                    user.getCompanyId(),
                    user.getConsumerId());

            return new LoginResponse(token, jwtUtil.getExpirationDate(), isPrimeiroAcesso);

    }


    public void updatePasswordUser(RequestUpdatePassword request) throws SQLException {

        String token = (String) httpRequest.getAttribute("CleanJwt");

        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não encontrado no token.");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
        }

        String novaSenhaCriptografada = passwordEncoder.encode(request.getNewPassword());
        validacaoRepository.saveNewPassword(novaSenhaCriptografada, userId);
    }

    @Transactional
    public void solicitarResetSenha(String username, String email) throws SQLException {
        // Busca o usuário pelo username e email na tabela contatos
        User user = validacaoRepository.findByUsernameAndEmail(username, email);
        if (user == null) {
            throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND);
        }

        // Gera um token único
        int tokenInt = 100000 + (int)(Math.random() * 900000);
        String token = String.valueOf(tokenInt);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // Token válido por 1 hora

        // Salva o token no banco
        validacaoRepository.saveResetToken((long) user.getId(), token, expiresAt);

        // Cria o evento para enviar para a fila
        PasswordResetEvent event = new PasswordResetEvent(email, token, user.getUsername());

        // Publica na fila do RabbitMQ
        rabbitService.publishPasswordResetEvent(event);
    }

    @Transactional
    public void confirmarResetSenha(String token, String novaSenha) throws SQLException {
        // Busca o token válido
        PasswordResetToken resetToken = validacaoRepository.findValidResetToken(token);
        if (resetToken == null) {
            throw new BusinessRuleException(ErrorCode.USER_NOT_FOUND); // Pode criar um erro específico para token inválido
        }

        // Valida o tamanho da nova senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
        }

        // Criptografa a nova senha
        String novaSenhaCriptografada = passwordEncoder.encode(novaSenha);

        // Atualiza a senha do usuário
        validacaoRepository.saveNewPassword(novaSenhaCriptografada, resetToken.getUserId().intValue());

        // Marca o token como usado
        validacaoRepository.markTokenAsUsed(token);
    }
}

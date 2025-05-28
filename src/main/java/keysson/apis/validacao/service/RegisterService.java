package keysson.apis.validacao.service;

import keysson.apis.validacao.dto.FuncionarioCadastradoEvent;
import keysson.apis.validacao.dto.request.RequestRegister;
import keysson.apis.validacao.dto.response.FuncionarioRegistroResultado;
import keysson.apis.validacao.exception.BusinessRuleException;
import keysson.apis.validacao.exception.enums.ErrorCode;
import keysson.apis.validacao.repository.RegisterRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

@Service
public class RegisterService {

    private final RegisterRepository registerRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public RegisterService(RegisterRepository registerRepository, RegisterRepository registerRepository1, RabbitService rabbitService, RabbitTemplate rabbitTemplate) {
        this.registerRepository = registerRepository;
        this.rabbitService = rabbitService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.rabbitTemplate = rabbitTemplate;
    }

    public void registerEmployee (RequestRegister requestRegister) throws BusinessRuleException, SQLException {

        if (registerRepository.existsByCpf(requestRegister.getCpf())) {
            throw new BusinessRuleException(ErrorCode.CPF_JA_CADASTRADO);
        }

        String encodedPassword = passwordEncoder.encode(requestRegister.getPassword());

        String consumerId = UUID.randomUUID().toString();

        FuncionarioRegistroResultado resultado = registerRepository.save(
                requestRegister.getEmail(),
                encodedPassword,
                requestRegister.getUsername(),
                requestRegister.getDepartamento()
        );

        if (resultado.getResultCode() == 0) {
            FuncionarioCadastradoEvent event = new FuncionarioCadastradoEvent(
                    requestRegister.getEmail(),
                    requestRegister.getUsername()
            );
            try {
                rabbitTemplate.convertAndSend("funcionario.fila", event);

                rabbitService.saveMessagesInBank(event, 1);
            } catch (Exception ex) {
                rabbitService.saveMessagesInBank(event, 0);
                throw new RuntimeException("Erro ao enviar mensagem ao RabbitMQ: " + ex.getMessage());
            }
        } else if (resultado.getResultCode() == 1) {
            throw new BusinessRuleException(ErrorCode.ERRO_CADASTRAR);
        }
    }
}

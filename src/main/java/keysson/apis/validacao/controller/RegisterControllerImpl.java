package keysson.apis.validacao.controller;

import keysson.apis.validacao.dto.request.RequestRegister;
import keysson.apis.validacao.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
public class RegisterControllerImpl implements RegisterController{

    private RegisterService registerService;

    @Autowired
    public void CompanyControllerImpl(RegisterService registerService) {
        this.registerService = registerService;
    }

    public RegisterControllerImpl(RegisterService registerService) {
        this.registerService = registerService;
    }

    @Override
    public void register(@RequestBody RequestRegister requestRegister) throws SQLException {
        registerService.registerEmployee(requestRegister);
    }
}

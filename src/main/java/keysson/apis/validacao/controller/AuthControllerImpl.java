package keysson.apis.validacao.controller;

import keysson.apis.validacao.dto.request.LoginRequest;
import keysson.apis.validacao.dto.request.RequestUpdatePassword;
import keysson.apis.validacao.dto.response.LoginResponse;
import keysson.apis.validacao.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController{

    private final AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws SQLException {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Override
    public void updatePassword(RequestUpdatePassword request, String token) throws SQLException {
        authService.updatePasswordUser(request, token);
    }


}

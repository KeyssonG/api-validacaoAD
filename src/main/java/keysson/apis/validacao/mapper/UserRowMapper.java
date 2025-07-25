package keysson.apis.validacao.mapper;

import keysson.apis.validacao.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setCompanyId(rs.getInt("company_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setStatus(rs.getInt("status"));
        user.setConsumerId(UUID.fromString(rs.getString("consumer_id")));
        user.setPrimeiroAcesso(rs.getBoolean("primeiro_acesso"));

        return user;
    }
}
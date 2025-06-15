package keysson.apis.validacao.repository;

import keysson.apis.validacao.exception.BusinessRuleException;
import keysson.apis.validacao.exception.enums.ErrorCode;
import keysson.apis.validacao.mapper.UserRowMapper;
import keysson.apis.validacao.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class ValidacaoRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRowMapper userRowMapper;


    private static final String FIND_BY_USERNAME = """
            SELECT
              u.id,
              u.company_id,
              u.username,
              u.password,
              u.status,
              c.consumer_id,
              u.primeiro_acesso 
            FROM users u
            JOIN companies c ON u.company_id = c.id
            WHERE u.username = ? AND c.id = 0;
            """;

    private static final String ACCOUNT_ACTIVATION = """
            UPDATE USERS SET STATUS = 2 WHERE ID = ? AND COMPANY_ID = ? AND USERNAME = ?
            """;

    private static final String FIND_STATUS_COMPANY = """
            SELECT STATUS FROM COMPANIES WHERE id = ?
            """;

    private static final String CHANGE_FIRST_ACCESS = """
            UPDATE users SET primeiro_acesso = ? WHERE id = ?
            """;

    public User findByUsername(String username) {
        return jdbcTemplate.query(FIND_BY_USERNAME, new Object[]{username}, rs -> {
            if (rs.next()) {
                return userRowMapper.mapRow(rs, 1);
            }
            return null;
        });
    }
    public void activeAccount (Long idUser, Long idEmpresa, String username) {
        try {
            jdbcTemplate.update(ACCOUNT_ACTIVATION, idUser, idEmpresa, username);
        } catch (Exception e) {
            throw new BusinessRuleException(ErrorCode.ERROR_ACTIVE_ACCOUNT);
        }
    }

    public int findStatusCompany(int idEmpresa) {
        try {
            return jdbcTemplate.queryForObject(FIND_STATUS_COMPANY, new Object[]{idEmpresa}, Integer.class);
        } catch (Exception e) {
            throw new BusinessRuleException(ErrorCode.ERRO_STATUS_COMPANY);
        }
    }

    public void updateFirstAccess(int userId, boolean primeiroAcesso) throws SQLException {
        try {
            jdbcTemplate.update(CHANGE_FIRST_ACCESS, userId, primeiroAcesso);
        } catch (Exception ex) {
            throw new SQLException("Erro ao alterar o status do primeiro acesso" +  ex.getMessage(), ex);
        }
    }
}

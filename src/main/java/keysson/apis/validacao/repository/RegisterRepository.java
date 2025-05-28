package keysson.apis.validacao.repository;

import keysson.apis.validacao.dto.response.FuncionarioRegistroResultado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Repository
public class RegisterRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    public RegisterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String CHECK_EXISTS_CPF= """
        SELECT COUNT(*) 
        FROM companies 
        WHERE cnpj = ?
        """;

    public boolean existsByCpf(String cpf) {
        Long count = jdbcTemplate.queryForObject(CHECK_EXISTS_CPF, Long.class, cpf);
        return count != null && count > 0;
    }

    public FuncionarioRegistroResultado save(String email, String password,
                                         String username, String departamento) {

        UUID consumerId = UUID.randomUUID();

        String sql = "CALL proc_registrar_funcionario(?, ?, ?, ?)";

        Map<String, Object> result = jdbcTemplate.call(connection -> {
            CallableStatement cs = connection.prepareCall(sql);

            cs.setString(1, email);
            cs.setString(2, username);
            cs.setString(3, password);
            cs.setString(4, departamento);

            cs.registerOutParameter(5, Types.INTEGER); // out_result
            cs.registerOutParameter(6, Types.INTEGER); // out_company_id

            return cs;
        }, Arrays.asList(
                new SqlParameter("p_email", Types.VARCHAR),
                new SqlParameter("p_username", Types.VARCHAR),
                new SqlParameter("p_password", Types.VARCHAR),
                new SqlParameter("departamento", Types.VARCHAR),
                new SqlOutParameter("out_result", Types.INTEGER),
                new SqlOutParameter("out_company_id", Types.INTEGER)
        ));

        System.out.println("Map result: " + result);

        Integer resultCode = (Integer) result.get("out_result");
        Integer idFuncionario = (Integer) result.get("out_employee _id");
        return new FuncionarioRegistroResultado(resultCode, idFuncionario);
    }
}

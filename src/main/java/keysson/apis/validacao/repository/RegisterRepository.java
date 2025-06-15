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
        FROM funcionarios 
        WHERE cpf = ?
        """;

    private static final String CHECK_EXISTS_USERNAME= """
        SELECT COUNT(*) 
        FROM funcionarios 
        WHERE cpf = ?
        """;

    private static final String CHECK_EXISTS_NUMERO_CONTA = """
            SELECT COUNT(*)
            FROM USERS
            WHERE username  = ? and company_id = 0
        """;

    public boolean existsByCpf(String cpf) {
        Long count = jdbcTemplate.queryForObject(CHECK_EXISTS_CPF, Long.class, cpf);
        return count != null && count > 0;
    }

    public boolean existsByUsername(String name) {
        Long count = jdbcTemplate.queryForObject(CHECK_EXISTS_USERNAME, Long.class, name);
        return count != null && count > 0;
    }

    public FuncionarioRegistroResultado save(String nome, String email, String cpf, String password,
                                             String username, String departamento, int numeroConta) {

        String sql = "CALL proc_registrar_funcionario(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Map<String, Object> result = jdbcTemplate.call(connection -> {
            CallableStatement cs = connection.prepareCall(sql);


            cs.setString(1, nome);
            cs.setString(2, departamento);
            cs.setString(3, email);
            cs.setString(4, cpf);
            cs.setString(5, username);
            cs.setString(6, password);
            cs.setInt(7, numeroConta);

            cs.registerOutParameter(8, Types.INTEGER);
            cs.registerOutParameter(9, Types.INTEGER);

            return cs;
        }, Arrays.asList(
                new SqlParameter("p_nome", Types.VARCHAR),
                new SqlParameter("p_departamento", Types.VARCHAR),
                new SqlParameter("p_email", Types.VARCHAR),
                new SqlParameter("p_cpf", Types.VARCHAR),
                new SqlParameter("p_username", Types.VARCHAR),
                new SqlParameter("p_password", Types.VARCHAR),
                new SqlParameter("p_numero_conta", Types.INTEGER),
                new SqlOutParameter("out_result", Types.INTEGER),
                new SqlOutParameter("out_user_id", Types.INTEGER)
        ));

        System.out.println("Map result: " + result);

        Integer resultCode = (Integer) result.get("out_result");
        Integer idFuncionario = (Integer) result.get("out_user_id");

        return new FuncionarioRegistroResultado(resultCode, idFuncionario);
    }


    public boolean existsByNumeroConta(int numeroConta) {
        Long count = jdbcTemplate.queryForObject(CHECK_EXISTS_NUMERO_CONTA, Long.class, numeroConta);
        return count != null && count > 0;
    }
}

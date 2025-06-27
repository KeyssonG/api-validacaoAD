# Etapa 1: Build da aplicação Java
FROM maven:3.9.9-amazoncorretto-21 AS builder

# Define o diretório de trabalho
WORKDIR /app

# Copia todo o conteúdo do projeto (inclusive pom.xml e src)
COPY . .

# Executa o build do projeto
RUN mvn clean package -DskipTests

# Renomeia o JAR gerado
RUN mv target/*.jar /app/validacaoad.jar

# Etapa 2: Imagem final para execução
FROM amazoncorretto:21

WORKDIR /app

# Instala ferramentas para gerenciamento de certificados
RUN yum update -y && \
    yum install -y openssl ca-certificates && \
    yum clean all

# Cria diretório para certificados
RUN mkdir -p /app/certs

# Copia o JAR da etapa de build
COPY --from=builder /app/validacaoad.jar /app/validacaoad.jar

# Copia certificados SSL (se existirem)
COPY src/main/resources/*.p12 /app/certs/ 2>/dev/null || true

# Expõe as portas HTTP e HTTPS
EXPOSE 8089 8443

# Comando de inicialização
CMD ["java", "-jar", "/app/validacaoad.jar"]

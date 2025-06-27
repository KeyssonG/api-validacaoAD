# Script simples para gerar certificado SSL
Write-Host "=== Gerador de Certificado SSL ===" -ForegroundColor Green

# Verifica Java
$javaCheck = java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "Java encontrado" -ForegroundColor Green
} else {
    Write-Host "Java nao encontrado. Instale o Java primeiro." -ForegroundColor Red
    exit 1
}

# Cria diretório se não existir
$certDir = "src\main\resources"
if (!(Test-Path $certDir)) {
    New-Item -ItemType Directory -Path $certDir -Force | Out-Null
    Write-Host "Diretorio criado: $certDir" -ForegroundColor Green
}

# Configurações
$keystorePath = "$certDir\keystore.p12"
$alias = "validacao-cert"
$password = "changeit"

Write-Host "Gerando certificado..." -ForegroundColor Yellow
Write-Host "Arquivo: $keystorePath" -ForegroundColor Cyan

# Comando keytool
$keytoolCmd = "keytool -genkeypair -alias $alias -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore `"$keystorePath`" -validity 365 -storepass $password -dname `"CN=localhost, OU=Dev, O=Empresa, L=Cidade, S=Estado, C=BR`""

# Executa o comando
Write-Host "Executando: $keytoolCmd" -ForegroundColor Gray
Invoke-Expression $keytoolCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host "Certificado gerado com sucesso!" -ForegroundColor Green
    
    if (Test-Path $keystorePath) {
        $size = (Get-Item $keystorePath).Length
        Write-Host "Tamanho: $size bytes" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "=== Configuracao ===" -ForegroundColor Yellow
    Write-Host "SSL_KEYSTORE_PASSWORD=$password" -ForegroundColor Cyan
    Write-Host "SSL_KEY_ALIAS=$alias" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Para executar: ./mvnw spring-boot:run -Dspring.profiles.active=dev" -ForegroundColor Cyan
    Write-Host "Acesse: https://localhost:8089" -ForegroundColor Cyan
} else {
    Write-Host "Erro ao gerar certificado" -ForegroundColor Red
} 
# Script para gerar certificado SSL no Windows
# Execute este script como Administrador

Write-Host "=== Gerador de Certificado SSL para API de Validação ===" -ForegroundColor Green
Write-Host ""

# Verifica se o Java está instalado
$javaVersion = java -version 2>&1 | Select-String "version"
if ($javaVersion) {
    Write-Host "✓ Java encontrado: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "✗ Java não encontrado. Instale o Java primeiro." -ForegroundColor Red
    exit 1
}

# Cria diretório para certificados se não existir
$certDir = "src\main\resources"
if (!(Test-Path $certDir)) {
    New-Item -ItemType Directory -Path $certDir -Force
    Write-Host "✓ Diretório de certificados criado: $certDir" -ForegroundColor Green
}

# Configurações do certificado
$keystorePath = "$certDir\keystore.p12"
$alias = "validacao-cert"
$password = "changeit"
$validity = "365"

Write-Host "Gerando certificado SSL..." -ForegroundColor Yellow
Write-Host "Keystore: $keystorePath" -ForegroundColor Cyan
Write-Host "Alias: $alias" -ForegroundColor Cyan
Write-Host "Senha: $password" -ForegroundColor Cyan
Write-Host "Validade: $validity dias" -ForegroundColor Cyan
Write-Host ""

# Comando para gerar o certificado
$keytoolCmd = "keytool -genkeypair -alias $alias -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore `"$keystorePath`" -validity $validity -storepass $password"

Write-Host "Executando comando: $keytoolCmd" -ForegroundColor Gray
Write-Host ""

# Executa o comando keytool
try {
    Invoke-Expression $keytoolCmd
    Write-Host ""
    Write-Host "✓ Certificado gerado com sucesso!" -ForegroundColor Green
    Write-Host "✓ Arquivo: $keystorePath" -ForegroundColor Green

    # Verifica se o arquivo foi criado
    if (Test-Path $keystorePath) {
        $fileSize = (Get-Item $keystorePath).Length
        Write-Host "✓ Tamanho do arquivo: $fileSize bytes" -ForegroundColor Green
    }
}
catch {
    Write-Host "✗ Erro ao gerar certificado: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Configuração Manual Necessária ===" -ForegroundColor Yellow
Write-Host "1. Configure as variáveis de ambiente:" -ForegroundColor White
Write-Host "   SSL_KEYSTORE_PASSWORD=$password" -ForegroundColor Cyan
Write-Host "   SSL_KEY_ALIAS=$alias" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Para executar a aplicação com HTTPS:" -ForegroundColor White
Write-Host "   ./mvnw spring-boot:run -Dspring.profiles.active=dev" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Acesse a aplicação em: https://localhost:8089" -ForegroundColor White
Write-Host "   (Aceite o aviso de certificado autoassinado)" -ForegroundColor Yellow
Write-Host ""
Write-Host "=== Fim do Script ===" -ForegroundColor Green

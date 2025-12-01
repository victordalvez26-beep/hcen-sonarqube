# Script de prueba para el servicio SOAP PDI - DNIC
# Prueba todos los casos: persona mayor, menor, no existe, y extranjeros

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  PDI - Test del Servicio SOAP DNIC    " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# URL del servicio SOAP
$soapUrl = "http://localhost:8083/ws"

Write-Host ""

# Función para hacer request SOAP
function Test-SoapRequest {
    param(
        [string]$TestName,
        [string]$XmlFile,
        [string]$ExpectedResult
    )
    
    Write-Host "Probando: $TestName" -ForegroundColor Yellow
    Write-Host "Archivo: $XmlFile" -ForegroundColor Gray
    
    if (-not (Test-Path $XmlFile)) {
        Write-Host "   ERROR: No se encuentra el archivo $XmlFile" -ForegroundColor Red
        return
    }
    
    $body = Get-Content $XmlFile -Raw
    
    try {
        $headers = @{
            "Content-Type" = "text/xml; charset=utf-8"
            "SOAPAction" = ""
        }
        
        $response = Invoke-WebRequest -Uri $soapUrl -Method Post -Headers $headers -Body $body
        
        if ($response.StatusCode -eq 200) {
            Write-Host "   OK Status: 200 OK" -ForegroundColor Green
            
            # Parsear XML de respuesta
            [xml]$xmlResponse = $response.Content
            
            # Namespaces
            $ns = New-Object System.Xml.XmlNamespaceManager($xmlResponse.NameTable)
            $ns.AddNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/")
            $ns.AddNamespace("ns2", "http://agesic.gub.uy/pdi/services/dnic/1.0")
            
            # Verificar si es persona o error
            $persona = $xmlResponse.SelectSingleNode("//ns2:persona", $ns)
            $error = $xmlResponse.SelectSingleNode("//ns2:error", $ns)
            
            if ($persona) {
                $nombre = $persona.SelectSingleNode("ns2:primerNombre", $ns).InnerText
                $apellido = $persona.SelectSingleNode("ns2:primerApellido", $ns).InnerText
                $documento = $persona.SelectSingleNode("ns2:numeroDocumento", $ns).InnerText
                $nacionalidad = $persona.SelectSingleNode("ns2:nacionalidad", $ns).InnerText
                $fechaNac = $persona.SelectSingleNode("ns2:fechaNacimiento", $ns).InnerText
                
                Write-Host "   OK Persona encontrada: $nombre $apellido" -ForegroundColor Green
                Write-Host "   - Documento: $documento" -ForegroundColor Gray
                Write-Host "   - Nacionalidad: $nacionalidad" -ForegroundColor Gray
                Write-Host "   - Fecha Nacimiento: $fechaNac" -ForegroundColor Gray
                
                if ($ExpectedResult -eq "ENCONTRADA") {
                    Write-Host "   OK RESULTADO ESPERADO" -ForegroundColor Green
                } else {
                    Write-Host "   ERROR Se esperaba: $ExpectedResult" -ForegroundColor Red
                }
            }
            elseif ($error) {
                $codigo = $error.SelectSingleNode("ns2:codigo", $ns).InnerText
                $mensaje = $error.SelectSingleNode("ns2:mensaje", $ns).InnerText
                
                Write-Host "   OK Error devuelto (esperado para prueba negativa)" -ForegroundColor Yellow
                Write-Host "   - Codigo: $codigo" -ForegroundColor Gray
                Write-Host "   - Mensaje: $mensaje" -ForegroundColor Gray
                
                if ($ExpectedResult -eq "ERROR") {
                    Write-Host "   OK RESULTADO ESPERADO" -ForegroundColor Green
                } else {
                    Write-Host "   ERROR Se esperaba: $ExpectedResult" -ForegroundColor Red
                }
            }
        }
    } catch {
        Write-Host "   ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
}

# Ejecutar pruebas
Write-Host "Probando casos de prueba SOAP:" -ForegroundColor Yellow
Write-Host ""

Test-SoapRequest `
    -TestName "Persona Mayor de Edad (Victor - CI 50830691)" `
    -XmlFile "ejemplos/request-persona-mayor.xml" `
    -ExpectedResult "ENCONTRADA"

Test-SoapRequest `
    -TestName "Persona Menor de Edad (Pedro - CI 39178531)" `
    -XmlFile "ejemplos/request-persona-menor.xml" `
    -ExpectedResult "ENCONTRADA"

Test-SoapRequest `
    -TestName "Persona No Existe (CI 99999999)" `
    -XmlFile "ejemplos/request-persona-no-existe.xml" `
    -ExpectedResult "ERROR"

Test-SoapRequest `
    -TestName "Persona Brasilera (Roberto - PASAPORTE 26347848)" `
    -XmlFile "ejemplos/request-persona-brasilera.xml" `
    -ExpectedResult "ENCONTRADA"

Test-SoapRequest `
    -TestName "Persona Argentina (Martin - PASAPORTE AB789456)" `
    -XmlFile "ejemplos/request-persona-argentina.xml" `
    -ExpectedResult "ENCONTRADA"

Test-SoapRequest `
    -TestName "Persona Chilena (Carla - PASAPORTE CH456123)" `
    -XmlFile "ejemplos/request-persona-chilena.xml" `
    -ExpectedResult "ENCONTRADA"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Tests completados                     " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Script simple para actualizar el rol usando GET
$uid = "uy-ci-52453518"
$rol = "AD"
$url = "http://localhost:8080/api/users/$uid/role/$rol"

Write-Host "Actualizando rol del usuario $uid a $rol..."
Write-Host "URL: $url"

try {
    $response = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)"
    Write-Host "Respuesta: $($response.Content)"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Respuesta del servidor: $responseBody"
    }
}


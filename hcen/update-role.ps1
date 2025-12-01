# Script para actualizar el rol de un usuario
$uid = "uy-ci-52453518"
$rol = "AD"
$url = "http://localhost:8080/api/users/$uid/role"
$body = @{
    rol = $rol
} | ConvertTo-Json

try {
    Write-Host "Actualizando rol del usuario $uid a $rol..."
    $response = Invoke-RestMethod -Uri $url -Method POST -ContentType "application/json" -Body $body
    Write-Host "Respuesta: $($response | ConvertTo-Json)"
    Write-Host "Rol actualizado exitosamente!"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Respuesta del servidor: $responseBody"
    }
}


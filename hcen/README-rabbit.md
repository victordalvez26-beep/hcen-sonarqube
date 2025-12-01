# RabbitMQ for local development

This repo includes a docker-compose file to run a local RabbitMQ broker with the management UI.

Start RabbitMQ:

```powershell
cd C:\Users\CaroH\Documents\fing\TSE\lab\hcen
docker-compose up -d
```

Default credentials (from docker-compose.yml):
- user: hcen
- password: hcen

Access management UI: http://localhost:15672
AMQP URI: amqp://hcen:hcen@localhost:5672/

How to test with the app
1. Export env vars so the app's RMQ client uses the correct URI and credentials:

```powershell
$env:RABBITMQ_URI = 'amqp://localhost:5672'
$env:RABBITMQ_USER = 'hcen'
$env:RABBITMQ_PASSWORD = 'hcen'
```

2. Deploy the EAR (see previous instructions) and POST to `/hcen-web/api/rabbit` using `queue = 'RabbitQueue'`.

3. Watch server logs or the RabbitClientConsumer logs to see the message.

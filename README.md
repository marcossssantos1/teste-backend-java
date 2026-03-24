# Teste Estapar 

Sistema backend para gerenciamento de estacionamento — controle de vagas, entrada/saída de veículos e cálculo de receita.

---

## 🛠️ Tecnologias

- Java 21
- Spring Boot 4.0.4
- MySQL 8
- Maven
- Docker (simulador)
- WSL2 (Ubuntu)

---

## 📋 Pré-requisitos

- Java 21 instalado
- MySQL instalado e rodando
- Git instalado
- WSL2 com Ubuntu instalado (Windows)
- Docker Engine instalado no Ubuntu

---

## 🚀 Como rodar o projeto

### 1. Clone o repositório

```bash
git clone https://github.com/marcossssantos1/teste-backend-java.git
```

### 2. Configure o banco de dados

O banco é criado automaticamente pela aplicação na primeira execução. Basta garantir que o MySQL está rodando e configurar as variáveis de ambiente com suas credenciais:

**Windows (PowerShell):**
```powershell
$env:DB_USER="root"
$env:DB_PASS="sua_senha_aqui"
```

**Linux/macOS:**
```bash
export DB_USER=root
export DB_PASS=sua_senha_aqui
```

O arquivo `src/main/resources/application.properties` já está configurado para usar essas variáveis:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/parking_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
```

### 3. Compile o projeto

```bash
mvn clean install
```

### 4. Configure o simulador no Ubuntu (WSL2)

Abra o terminal Ubuntu e instale o Docker Engine (caso não tenha):

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io
sudo usermod -aG docker $USER
```

Instale o socat:

```bash
sudo apt-get install -y socat
```

### 5. Descubra o IP do Windows visto pelo WSL2

```bash
ip route | grep default
```

Anote o IP que aparecer (ex: `172.28.144.1`).

### 6. Suba tudo na ordem correta

> ⚠️ A ordem é importante — o simulador deve estar no ar antes da aplicação iniciar.

**Passo 1 — No terminal Ubuntu, inicie o Docker e suba o simulador:**

```bash
sudo service docker start
docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
```

**Passo 2 — Confirme que o simulador está rodando:**

```bash
curl http://localhost:3000/garage
```

**Passo 3 — Inicie a aplicação** pela IDE ou via terminal:

```bash
mvn spring-boot:run
```

A aplicação vai buscar automaticamente os dados da garagem no simulador ao iniciar.

**Passo 4 — No terminal Ubuntu, suba o socat** substituindo pelo seu IP:

```bash
socat TCP-LISTEN:3003,fork TCP:SEU_IP_AQUI:3003 &
```

**Passo 5 — Acompanhe os eventos:**

```bash
docker logs -f $(docker ps -q)
```

Você deve ver eventos como:
```
INFO garage_simulator: Entry successful for plate: ZUL0001
INFO garage_simulator: Parked successful for plate: ZUL0001
INFO garage_simulator: Exit successful for plate: ZUL0001
INFO garage_simulator: Current revenue per sector: {"A": 81.0, "B": 8.2}
```

---

## 📡 API

### Webhook — recebe eventos do simulador

**POST** `/webhook`

```json
// ENTRY
{
  "license_plate": "ZUL0001",
  "entry_time": "2026-03-24T10:00:00",
  "event_type": "ENTRY"
}

// PARKED
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}

// EXIT
{
  "license_plate": "ZUL0001",
  "exit_time": "2026-03-24T12:00:00",
  "event_type": "EXIT"
}
```

### Receita por setor e data

**GET** `/revenue`

Request:
```json
{
  "date": "2026-03-24",
  "sector": "A"
}
```

Response:
```json
{
  "amount": 81.00,
  "currency": "BRL",
  "timestamp": "2026-03-24T10:00:00"
}
```

---

## Regras de negócio

### Cobrança
- Primeiros **30 minutos**: gratuito
- Após 30 minutos: tarifa por hora cheia (arredonda para cima)

### Preço dinâmico por lotação
| Lotação | Ajuste |
|---|---|
| < 25% | -10% (desconto) |
| 25% a 50% | sem alteração |
| 50% a 75% | +10% |
| 75% a 100% | +25% |

### Lotação
- Com 100% de lotação o setor é fechado
- Só reabre após a saída de um veículo

---

## 🧪 Testes

```bash
mvn test
```

Cobertura:
- `PricingServiceTest` — regras de preço dinâmico e cobrança
- `ParkingServiceTest` — fluxo de ENTRY, PARKED e EXIT
- `WebhookControllerTest` — endpoints do webhook
- `RevenueControllerTest` — endpoint de receita

---

## 🔄 Simulação manual (modo debug)

Para testar o fluxo completo manualmente com mais de 30 minutos:

```bash
# 1. ENTRY
curl -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{"license_plate":"TEST001","entry_time":"2026-03-24T10:00:00","event_type":"ENTRY"}'

# 2. PARKED
curl -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{"license_plate":"TEST001","lat":-23.561684,"lng":-46.655981,"event_type":"PARKED"}'

# 3. EXIT após 2 horas
curl -X POST http://localhost:3003/webhook \
  -H "Content-Type: application/json" \
  -d '{"license_plate":"TEST001","exit_time":"2026-03-24T12:00:00","event_type":"EXIT"}'

# 4. Consulta receita
curl -X GET http://localhost:3003/revenue \
  -H "Content-Type: application/json" \
  -d '{"date":"2026-03-24","sector":"A"}'
```

# Bolsa de Valores RMI - Java

Este projeto simula uma bolsa de valores simples utilizando Java RMI (Remote Method Invocation). O sistema permite múltiplos investidores conectados, atualização automática de preços de ações e um book de ofertas para operações de compra e venda.

## ✨ Funcionalidades

- **Atualização automática de preços**: Múltiplas threads atualizam os preços das ações de forma aleatória, simulando o mercado em tempo real.
- **Investidores reativos**: Investidores (em threads separadas) recebem notificações e reagem a mudanças de preço das ações que acompanham.
- **Book de ofertas**: Implementação de um book de ofertas, permitindo registrar ordens de compra e venda, casar ordens e exibir as melhores ofertas.
- **Comunicação distribuída via Controller**: Toda a comunicação entre clientes (investidores) e o servidor da bolsa é feita via Java RMI, mas agora a camada de aplicação só acessa os serviços por meio do controller remoto, garantindo separação de camadas.

## 🗂️ Estrutura do Projeto

- 📈 `app/ServidorBolsaValores.java`: Inicializa o servidor da bolsa, registra o controller remoto RMI e inicia a simulação de preços.
- 👨‍💻 `app/InvestidorApp.java`: Cliente que representa um investidor, conecta-se ao controller remoto, recebe notificações e pode visualizar o book de ofertas.
- 🧑‍⚖️ `controller/BolsaValoresController.java`: Controller centraliza as operações de consulta e envio de ordens, sendo a única ponte entre app e service.
- ⚙️ `service/BolsaValoresService.java`: Lógica de negócio da bolsa, atualização de preços e gerenciamento das ações.
- 📚 `service/BookDeOfertas.java`: Gerencia o book de ofertas, casando ordens de compra e venda.
- 🗃️ `model/Acao.java` e `model/Ordem.java`: Modelos de dados para ações e ordens.
- 🛰️ `rmi/BolsaValoresControllerRemote.java` e `rmi/BolsaValoresControllerRemoteImpl.java`: Interface e implementação do controller remoto exposto via RMI.
- 🧩 `interfaces/`: Interfaces auxiliares para listeners e callbacks remotos.

## ⚙️ Como funciona

1. O servidor inicia e cria várias threads, cada uma responsável por atualizar o preço de uma ação aleatoriamente.
2. O controller é exposto via RMI, centralizando todas as operações remotas.
3. Investidores se conectam ao controller remoto, registrando-se para receber notificações de mudanças de preço e alterações no book de ofertas.
4. Quando o preço de uma ação muda, todos os investidores interessados são notificados.
5. Investidores podem visualizar as melhores ofertas de compra e venda (book de ofertas) e enviar ordens.
6. O book de ofertas casa ordens compatíveis automaticamente.

## 🚀 Execução

1. Compile o projeto:
   ```sh
   javac -d bin src/**/*.java
   ```
2. Inicie o servidor:
   ```sh
   java -cp bin app.ServidorBolsaValores
   ```
3. Em outro terminal, inicie um ou mais investidores:
   ```sh
   java -cp bin app.InvestidorApp
   ```

## 📝 Observações
- O controller remoto é a única interface entre a camada de aplicação e os serviços da bolsa.
- O projeto é totalmente multi-threaded e distribuído.
- O código foi organizado para garantir separação de responsabilidades e facilitar manutenção.

## Checklist de Responsabilidades

**✅ Múltiplas threads atualizam preços de ações aleatoriamente**
```java
// src/service/BolsaValoresService.java
public void iniciarSimulacao() {
    for (String simbolo : acoes.keySet()) {
        Thread atualizadorPreco = new Thread(new AtualizadorPreco(simbolo));
        atualizadorPreco.setDaemon(true);
        atualizadorPreco.start();
    }
}
```

**✅ Outros threads (investidores) reagem a mudanças específicas de preço**
```java
// src/app/InvestidorApp.java
@Override
public void notificarMudancaPreco(String simboloAcao, double precoAntigo, double novoPreco) throws RemoteException {
    if (acoesSeguidas.containsKey(simboloAcao)) {
        System.out.printf("[ATUALIZAÇÃO] %s: R$%.2f -> R$%.2f\n", simboloAcao, precoAntigo, novoPreco);
    }
}
```

**✅ Implemente um book de ofertas com operações de compra/venda**
```java
// src/service/BookDeOfertas.java
public void adicionarOrdem(Ordem ordem) {
    // ...adiciona ordem e tenta casar ordens...
    verificarExecucaoOrdens(simbolo);
    notificarAlteracaoBook(simbolo);
}
```

**✅ Garanta consistência nos dados com alta concorrência**
```java
// src/service/BookDeOfertas.java
private final Map<String, List<Ordem>> ordensCompra = new ConcurrentHashMap<>();
private final Map<String, List<Ordem>> ordensVenda = new ConcurrentHashMap<>();
// Uso de synchronized e listas sincronizadas para garantir consistência
```

**✅ Uso do RMI**
```java
// src/app/ServidorBolsaValores.java
Registry registry = LocateRegistry.createRegistry(1099);
registry.bind("BolsaValores", controllerRemoto);

// src/app/InvestidorApp.java
Registry registry = LocateRegistry.getRegistry(serverAddress, 1099);
BolsaValoresControllerRemote bolsa = (BolsaValoresControllerRemote) registry.lookup("BolsaValores");
```
---

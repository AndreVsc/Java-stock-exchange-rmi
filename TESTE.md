# Guia de Execução do Sistema RMI Java em Múltiplas Máquinas

---

## 1. Pré-requisitos

- As duas máquinas devem estar na mesma rede (ou a porta 1099 deve estar aberta no firewall/roteador do servidor).
- O Java deve estar instalado em ambas as máquinas.
- O código-fonte e os arquivos `.class` devem estar presentes em ambas as máquinas, ou você pode compilar no servidor e copiar apenas os `.class` para o cliente.

---

## 2. No Servidor (máquina que vai rodar o `ServidorBolsaValores`)

1. **Descubra o IP do servidor**  
   No Windows, execute no terminal:
   ```powershell
   ipconfig
   ```
   Procure pelo endereço IPv4 (exemplo: `192.168.1.100`).

2. **Inicie o servidor normalmente**  
   No diretório do projeto, rode:
   ```powershell
   java -cp bin app.ServidorBolsaValores
   ```
   O servidor vai criar o registro RMI na porta 1099 e aguardar conexões.

3. **(Opcional) Libere a porta 1099 no firewall**  
   Se o cliente não estiver na mesma rede local, você deve liberar a porta 1099 no firewall do servidor e, se necessário, redirecionar a porta no roteador.

---

## 3. No Cliente (máquina que vai rodar o `InvestidorApp`)

1. **Tenha o mesmo código compilado**  
   Copie a pasta bin (ou os `.class`) para a máquina cliente.

2. **Conecte usando o IP do servidor**  
   Ao rodar o cliente, quando for perguntado:
   ```
   Digite o endereço do servidor da bolsa (ou deixe em branco para localhost):
   ```
   Digite o IP do servidor, por exemplo:
   ```
   192.168.1.100
   ```

3. **Inicie o cliente normalmente**  
   ```powershell
   java -cp bin app.InvestidorApp
   ```

---

## 4. Para um servidor real (na nuvem, VPS, etc.)

- O processo é o mesmo, mas:
  - Use o IP público do servidor.
  - Certifique-se de liberar a porta 1099 no firewall do sistema operacional e do provedor de nuvem.
  - Se o servidor estiver atrás de NAT/roteador, faça o redirecionamento da porta 1099 para o IP interno do servidor.

---

## 5. Dicas importantes

- **Evite usar “localhost”** no servidor se for acessar de outra máquina. Sempre use o IP real.
- **Firewall:** Se não conseguir conectar, verifique se a porta 1099 está aberta no servidor.
- **RMI e NAT:** Para ambientes de nuvem/NAT, pode ser necessário configurar a propriedade `java.rmi.server.hostname` para o IP público do servidor:
  ```powershell
  java -Djava.rmi.server.hostname=SEU_IP_PUBLICO -cp bin app.ServidorBolsaValores
  ```
- **Sincronize os arquivos:** Certifique-se de que as classes do cliente e do servidor são compatíveis (mesma versão).

---

Se quiser um passo a passo para um provedor específico (AWS, Azure, DigitalOcean, etc.), me avise!

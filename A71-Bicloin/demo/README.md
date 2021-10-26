# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Lançar o *registry*

Para lançar o *ZooKeeper*, ir à pasta `zookeeper/bin` e correr o comando  
`./zkServer.sh start` (Linux) ou `zkServer.cmd` (Windows).

É possível também lançar a consola de interação com o *ZooKeeper*, novamente na pasta `zookeeper/bin` e correr `./zkCli.sh` (Linux) ou `zkCli.cmd` (Windows).

### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

Ou

```sh
$ mvn clean compile install -DskipTests
```

### 1.3. Lançar e testar o *rec*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *rec* .
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn compile exec:java
```

Este comando vai colocar o *rec* no endereço *localhost* e na porta *8091*. Os argumentos passados ao rec estão definidos no rec/pom.xml e incluem o port e o host do servidor Zookeeper (assume-se o localhost e o port 2181).

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd rec-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.

Estes testes incluem verificações corretas de input, assim como teste de PINGs, de escritas e de leituras corretas.


### 1.4. Lançar e testar o *hub*

É preciso em primeiro lugar lançar o servidor *hub* .
Para isso basta ir à pasta *hub* e executar:

```sh
$ mvn compile exec:java
```

Este comando vai colocar o *hub* no endereço *localhost* e na porta *8081*. Os argumentos passados ao hub estão definidos no hub/pom.xml e incluem o port e o host do servidor Zookeeper (assume-se o localhost e o port 2181), assim como o path dos ficheiros de utilizadores e estações e a flag initRec. 

É importante que a primeira execução do hub tenha a flag initRec nos argumentos (está como pré-definição), para inicializar a informação mutável dos ficheiros da pasta demo no rec visto que se assume que é a primeira vez que os servidores se ligam.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd hub-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

**ATENÇÃO:** Estes testes assumem que os dados nos servidores são os iniciais dos ficheiros presentes na pasta demo e que apenas os testes do rec-tester foram executados até agora. 

Todos os testes devem ser executados sem erros.

Estes testes incluem verificações corretas de input, de informação de utilizadores e de estações, assim como execução correta de pedidos que incluem levantamento de bicicletas, carregamento de salto, etc...

### 1.5. *App*

Iniciar a aplicação com a utilizadora alice:

```sh
$ mvn clean compile exec:java -D"exec.args"="localhost 2181 alice +35191102030 38.7376 -9.3031" [WINDOWS]
```

```sh
$ mvn clean compile exec:java -Dexec.args="localhost 2181 alice +35191102030 38.7376 -9.3031" [LINUX]
```

Ou 

```sh
$ app localhost 2181 alice +35191102030 38.7376 -9.3031
```

```sh
$ ./app localhost 2181 alice +35191102030 38.7376 -9.3031
```

**ATENÇÃO:** Ao correr o app através do comando "mvn clean compile exec:java" ou equivalente na dirétoria app, não é necessário passar os argumentos referentes ao utilizador "alice", visto que estes estão definidos no app/pom.xml. 

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Abrir outra consola, e iniciar a aplicação com o utilizador bruno.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*.

### 2.1 *ping*

Em primeiro lugar, quer com o utilizador "alice" ou "bruno", utilizar o comando

```sh
$ ping
```

na consola. Este comando deve devolver *Hello Friend!* como sinal de vida do hub.

### 2.2. *balance*

De seguida, com o utilizador "alice", escrever 

```sh
$ balance
```
na consola. Este comando deve devolver: *alice 94 BIC*. 
Esta conta não tem 0 BIC pois o utilizador "alice" foi utilizado nos testes do hub-tester.

Ao fazer o mesmo com o utilizador "bruno", o resultado deverá ser: *bruno 0 BIC*.

Para poder testar os casos de erro para este comando seria necessário reiniciar a app com um utilizador que não esteja guardado
no hub e no rec. O hub faz a verificação da existência do utilizador em todos os pedidos. Estes testes são feitos no hub-tester.

### 2.3 *top-up*

Vamos agora carregar ambas as contas com saldo. Neste caso, podemos começar com testes de casos de erro. 
Ao fazer 

```sh
$ top-up X
```

em que X é uma quantia maior que 20 ou menor que 1, deve receber uma exceção com a seguinte mensagem: *ERRO: Invalid amount of EUR!*.

No caso de o utilizador não existir ou de enviar um número de telemóvel diferente do que está guardado no hub, iria ser recebida também uma exceção. Estes casos são testados no hub-tester pois, tal como no comando "balance", seria necessário iniciar a app com um utilizador não existente ou com um número de telémovel incorreto.

Agora, com a conta da alice fazer, por exemplo:

```sh
$ top-up 10
```

Este comando carrega o saldo da conta da "alice" com 10 euros (100 BIC), pelo que é recebida
uma resposta que indica o saldo atual: *alice 194 BIC*.

### 2.4 *infoStation*

Para este comando, com qualquer um dos utilizadores, introduzir o input 

```sh
$ info istt
```

Deve ser recebida uma mensagem com a informação toda dessa estação.
Para consultar mais estações podemos fazer, por exemplo: "info ista", "info stao", info "jero", etc...

Ao correr o comando com uma estação que não exista (por exemplo, *info aaaa*), deve ser recebida a exceção com mensagem: *ERRO: Station does not exist!*.

### 2.5 *locateStation*

Outra vez, com qualquer um dos utilizadores, introduzir o input 

```sh
$ scan 5
```

Deve ser recebida uma resposta com a informação das 5 estações mais próximas do utilizador, ordenadas crescentemente por distância. 

Ao utilizar o input *scan 0* deve receber uma resposta vazia e ao realizar *scan x* em que x é um número maior que o número de estações presentes no hub,
deve receber uma resposta com informação sobre todas as estações presentes no hub.

Ao utilizar *scan x*, em que x é número negativo, deve receber uma exceção com a mensagem: *ERRO: Invalid amount of stations!*. 

### 2.6 *bike-up*

Vamos agora alugar uma bicicleta.

Para testar alguns casos de erro, podemos começar por correr com a utilizador "alice", o comando:

```sh
$ bike-up stao
```

 Deve ser recebida a mensagem: *ERRO: User is too far from the station!*, visto que o utilizador se encontra a mais de 200 metros da estação. Podemos também tentar fazer:

```sh
$ bike-up aaaa
```
que deve devolver a mensagem: *ERRO: Station does not exist!*.

Agora, se fizermos:

```sh
$ bike-up istt
```

devemos receber a confirmação *OK* que confirma que alugámos com sucesso uma bicicleta. Ao repetir o comando *bike-up istt*, deve-se receber a mensagem: *ERRO: User already has a bike!*.
Alteramos agora de utilizador, para o utilizador "bruno".

Se, com o utilizador "bruno", corremos o comando:

```sh
$ at
```

devemos receber uma mensagem com a localização atual do utilizador. Vamos agora criar uma tag com o comando: 

```sh
$ tag 38.7097 -9.1336 tag1
```

Podemos agora correr: 

```sh
$ move tag1
```

que altera a localização do utilizador para as novas coordenadas.
Corremos agora o comando *bike-up cate* que deve retornar a exceção com a mensagem: *"ERRO: User doesn't have enough BIC to pay!"*. Corremos então o comando
*top-up 10* que deve retornar: *bruno 100 BIC*.

Corremos agora o comando *bike-up cate* novamente que deve retornar a exceção com a mensagem: *" ERRO: No bikes at this station!"*.

### 2.7 *bike-down*

Vamos agora devolver uma bicicleta.

Com a conta do utilizador "bruno" podemos correr o comando:

```sh
$ bike-down cate
```

que deve devolver a exceção com a mensagem: *"ERRO: User does not have a bike!".*
Podemos também correr o input *bike-down aaaa* que deve devolver a mensagem: *"ERRO: Station does not exist!".*

Passamos agora para a conta do utilizador "alice".

Com o utilizador "alice", executar o comando *move 38.7376  -9.1545*. Agora, realizar o comando *bike-down gulb* que deve devolver: "ERRO: This station is full!".

Podemos então executar o comando para uma estação com docas livres: *bike-down istt* que deve devolver: "ERRO: User is too far from the station!".

Finalmente, podemos executar os comandos *move 38.7372 -9.3023* e *bike-down istt* para fazer devolver a bicicleta. Deve-se receber a confirmação *"OK"*.

### 2.8 *sysStatus*

Finalmente, podemos correr em qualquer um dos utilizadores o comando:

```sh
$ sys_status
```

 que devolve o estado de todos os hubs e recs presentes no sistema.

----

## 3. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código. Como o rec guarda dados de forma persistente, os resultados dos testes e os valores devolvidos pelos comandos acima descritos poderão ser diferentes se não forem executados pela ordem descrita neste guião
começando com o rec sem dados. Para apagar os dados persistentes do rec, basta apagar os ficheiros: availableBikesFile, stationDevFile, stationLevFile, userBalancesFile, e userHasBikeFile (criados na primeira execução do rec) da diretoria /rec e re-lançar o rec e o hub com a opção initRec.

A app lê também input a partir de ficheiros de texto, que pode ser testado com os comandos:

```sh
$ Get-Content path/file.txt | mvn clean compile exec:java -D"exec.args"="localhost 2181 alice +35191102030 38.7376 -9.3031" [WINDOWS]
```

```sh
$ Get-Content path/file.txt | mvn clean compile exec:java -Dexec.args="localhost 2181 alice +35191102030 38.7376 -9.3031" [LINUX]
```

Ou 

```sh
$ app localhost 2181 alice +35191102030 38.7376 -9.3031 < path/file.txt
```

```sh
$ ./app localhost 2181 alice +35191102030 38.7376 -9.3031 < path/file.txt
```

**ATENÇÃO**: Onde path é o path para a diretoria onde está o ficheiro txt a partir da diretoria app.

Estão também já implementadas funcionalidades e mecanismos de tratamento de falhas no caso da app não conseguir contactar um hub, tal que se existirem várias réplicas do hub, a app deve ser capaz de as encontrar no zookeeper e ligar-se a estas.

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## Replicação e Tolerância a faltas

## 1. Preparação do sistema

Esta secção é dedicada ao teste do sistema com replicação do rec e à sua tolerância de faltas. Assume-se que nenhum dos componentes do sistema está ativo.

### 1.1. Lançar o *registry*

Para lançar o *ZooKeeper*, ir à pasta `zookeeper/bin` e correr o comando  
`./zkServer.sh start` (Linux) ou `zkServer.cmd` (Windows).

É possível também lançar a consola de interação com o *ZooKeeper*, novamente na pasta `zookeeper/bin` e correr `./zkCli.sh` (Linux) ou `zkCli.cmd` (Windows).
Com o comando:

```sh
$ ls /grpc/bicloin/rec
```

pode-se garantir que não existe ainda nenhum registo ou que apenas existe o 1. No caso de existirem outros, basta correr o comando:

```sh
$ delete /grpc/bicloin/rec/x
```

em que x é o numero do registo.

### 1.2. Compilar o projeto

Se ainda não foi feito, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

Ou

```sh
$ mvn clean compile install -DskipTests
```

### 1.3. Lançar múltiplos *recs*

Vamos agora lançar várias réplicas do rec, o que permitirá o teste e a verificação da tolerância de faltas.
Para tal, em terminais diferentes e na diretoria /rec, corremos os 5 comandos:

```sh
$ mvn compile exec:java -D"exec.args"="localhost 2181 localhost 8091 /grpc/bicloin/rec/1" [WINDOWS]
```

```sh
$ mvn compile exec:java -D"exec.args"="localhost 2181 localhost 8092 /grpc/bicloin/rec/2" [WINDOWS]
```

```sh
$ mvn compile exec:java -D"exec.args"="localhost 2181 localhost 8093 /grpc/bicloin/rec/3" [WINDOWS]
```

```sh
$ mvn compile exec:java -D"exec.args"="localhost 2181 localhost 8094 /grpc/bicloin/rec/4" [WINDOWS]
```

```sh
$ mvn compile exec:java -D"exec.args"="localhost 2181 localhost 8095 /grpc/bicloin/rec/5" [WINDOWS]
```

Para Linux os comandos são do formato:

```sh
$ mvn clean compile exec:java -Dexec.args="localhost 2181 localhost 8091 /grpc/bicloin/rec/1" [LINUX]
```

Estes comandos vão colocar os *recs* no endereço *localhost* e nas portas *809x*. 

Para confirmar a inicialização correta de cada rec, pode-se conferir se a mensagem "Server started" está presente nos terminais.


### 1.4. Lançar e testar o *hub*

É necessário ligar agora o hub. Para tal, basta ir à dirétoria /hub e correr o comando:

```sh
$ mvn compile exec:java
```

Este comando vai colocar o *hub* no endereço *localhost* e na porta *8081*. A inicialização de hub pode demorar ligeiramente mais tempo do que a do rec, visto que o hub é responsável por enviar os dados de cada utilizador/estação para o rec, inicializando assim os registos destes. Este procedimento garante que todos os recs começam com os mesmos dados iniciais.

Outra vez, para confirmar a inicialização correta do hub, pode-se conferir se a mensagem "Server started" está presente no terminal.

### 1.5. *App*

Vamos agora iniciar um utilizador.

Iniciar a aplicação com a utilizadora alice:

```sh
$ mvn clean compile exec:java -D"exec.args"="localhost 2181 alice +35191102030 38.7376 -9.3031" [WINDOWS]
```

```sh
$ mvn clean compile exec:java -Dexec.args="localhost 2181 alice +35191102030 38.7376 -9.3031" [LINUX]
```

Ou 

```sh
$ app localhost 2181 alice +35191102030 38.7376 -9.3031
```

```sh
$ ./app localhost 2181 alice +35191102030 38.7376 -9.3031
```

**ATENÇÃO:** Ao correr o app através do comando "mvn clean compile exec:java" ou equivalente na dirétoria app, não é necessário passar os argumentos referentes ao utilizador "alice", visto que estes estão definidos no app/pom.xml. 

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para testar as réplicas e a tolerância a faltas.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar a replicação do rec e a tolerância de faltas do sistema.

### 2.1 *Funcionamento normal do sistema*

Com as 5 réplicas do rec, o hub, e o cliente ativos, podemos correr no cliente "alice" o comando:

```sh
$ balance
```

Este comando realiza apenas uma leitura como pode ser conferido pela consola do hub e dos recs, devendo devolver *alice 0 BIC*.

De seguida, podemos verificar o funcionamento de operações de escrita com o comando:

```sh
$ top-up 10
```

que deverá devolver *alice 100 BIC*. Este comando realiza ambas escritas e leituras, que estão descritas na consola do hub e dos recs.
Ambos estes comandos leêm e escrevem em todos os recs, sendo apenas necessário a resposta de um número x de recs dependendo da operação.

Continuando, podemos confirmar o funcionamento normal do sistema com os seguintes comandos:

```sh
$ bike-up istt
```

deve-se receber a confirmação *OK* que confirma que alugámos com sucesso uma bicicleta.

```sh
$ bike-down istt
```

que deve devolver a confirmação de retorna da bicicleta.

```sh
$ info istt
```

retorna informação sobre a estação que utilizámos previamente.

```sh
$ scan 5
```

que deve devolver informação sobre as 5 estações mais próximas do utilizador.

Podemos ver nas consolas do hub e dos recs os reads e os writes a ser feitos, assim como a quem estão a ser enviados os pedidos.
Agora que verificámos o funcionamento correto do sistema, podemos testar a tolerância deste a faltas.

### 2.2 *Tolerância a faltas*

Sendo que estamos a correr 5 recs, o quoron da operação de leitura será 2, enquanto o de escrita será 4. Estamos assim a tolerar uma falta na operação de escrita e três na operação de leitura. Este cálculo está detalhado e explicado no relatório. Sendo assim, vamos agora desativar o rec que está registado como /grpc/bicloin/rec/5. Basta fazer CTRL + C no terminal deste.

Estão agora 4 recs ativos, o que ainda deverá ser suficiente para ambas as operações de leitura e de escrita. Podemos testar executando:

```sh
$ balance
```

e

```sh
$ top-up 10
```

e conferir que respondem corretamente. Confirma-se assim que o sistema, com 5 recs totais, tolera pelo menos a falha de um destes.

Podemos agora voltar a ativar o rec 5 com o comando:

```sh
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8096 /grpc/bicloin/rec/5" [WINDOWS]
```

e desativar o rec 4 utilizando o CTRL + C. O rec 5 está agora a correr num novo porto. Se voltarmos a correr os comandos *balance*, *top-up* ou qualquer outro comando verificamos que o sistema, apesar da falha do rec 4, consegue recuperar o rec 5 e continuar em funcionamento.

### 2.3 *Faltas não-toleradas*

Com o rec 4 desativado, podemos voltar a desativar o rec 5, ficando assim apenas com 3 recs ativos. Visto que o quorun de escrita é 4, esta operação deverá estar impossibilitada, comprometendo o funcionamento correto do sistema. Porém, sendo o quorun de leitura apenas 2, a leitura ainda deverá ser possível. Podemos testar utilizando uma operação que apenas provoca reads no rec:

```sh
$ balance
```

ou

```sh
$ info gulb
```

Ao correr uma instrução que envolva fazer uma operação de escrita, o sistema vai falhar. Com 5 recs, apenas 1 falha é tolerada para as escritas e o hub não vai conseguir receber 4 confirmações de escrita visto que existem apenas 3 recs ativos. Para comprovar podemos correr um comando que faça escritas:

```sh
$ bike-up istt
```

que deve esperar 5 segundos e falhar, tentando ligar o cliente a outro hub inexistente.

## 3. Considerações Finais 

Para a primeira entrega o grupo desenvolveu a possibilidade de replicação do hub. Mesmo não sendo pedida para a segunda entrega, foi decidido deixar os mecanismos desta no projeto visto que não alteram o funcionamento correto do projeto e este fica mais completo.










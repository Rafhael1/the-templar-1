# Fase 2 Às portas de Constantinopla

Identificador da fase `constantinople`. Textos da versão atual em português e inglês.

## História e conversas

Em 1096–1097, companhias da Primeira Cruzada chegaram a Constantinopla. Os líderes discutiram com Aleixo I o destino de
antigos territórios bizantinos e prosseguiram rumo à Ásia Menor. No jogo, o percurso vai das muralhas ao Bósforo. O
Capitão do Porto barra a passagem pela alfândega em um duelo ficcional.

### Sinopse cadastrada

A sinopse está cadastrada no manifesto. A fase apresenta a história por conversas e avisos durante o percurso.

**Português**

> Os líderes cruzados negociam com Aleixo I a devolução de antigos territórios bizantinos e a travessia rumo à Ásia.

**Inglês**

> The Crusader leaders negotiate with Alexios I over former Byzantine territories and passage to Asia.

Chaves no jogo `level.constantinople.story`.

### Aleixo I no bairro imperial

Conversa opcional. Pressione F junto ao personagem.

**Português**

> ALEIXO I / Os juramentos\
> Entre 1096 e 1097, os líderes chegaram a Constantinopla em momentos diferentes. Aleixo I pediu que jurassem devolver
> ao Império as cidades reconquistadas que antes lhe pertenciam. Em troca, os cruzados contavam com apoio bizantino para
> seguir viagem.

**Inglês**

> ALEXIOS I / The oaths\
> Between 1096 and 1097, the leaders reached Constantinople at different times. Alexios I asked them to swear to return
> recaptured cities that had once belonged to the Empire. In return, the Crusaders counted on Byzantine support to
> continue their journey.

Chaves no jogo `level.constantinople.oath.title`, `level.constantinople.oath.body`.

### Enviado dos cruzados no mercado

Conversa opcional. Pressione F junto ao personagem.

**Português**

> ENVIADO DOS CRUZADOS / As divergências\
> Cada companhia tinha seus próprios interesses. Raimundo de Toulouse resistiu ao juramento que outros líderes haviam
> prestado. Segundo algumas crônicas, ele se comprometeu a respeitar a vida e a honra do imperador.

**Inglês**

> CRUSADER ENVOY / The disagreements\
> Each company had interests of its own. Raymond of Toulouse resisted the oath other leaders had taken. According to
> some chronicles, he pledged to respect the emperor's life and honor.

Chaves no jogo `level.constantinople.rivalry.title`, `level.constantinople.rivalry.body`.

### Intendente bizantino no porto

Conversa opcional. Pressione F junto ao personagem.

**Português**

> INTENDENTE BIZANTINO / Rumo à Ásia Menor\
> A travessia levou os cruzados à Ásia Menor em grupos. Em 1097, os contingentes seguiram por rotas diferentes antes de
> se reunir diante de Niceia para o cerco.

**Inglês**

> BYZANTINE STEWARD / On to Asia Minor\
> The crossing brought the Crusaders to Asia Minor in groups. In 1097, contingents took separate routes before joining
> forces outside Nicaea for the siege.

Chaves no jogo `level.constantinople.crossing.title`, `level.constantinople.crossing.body`.

## Textos do percurso e do desfecho

| Momento             | Português                                                                         | Inglês                                                                                | Chave no jogo                  |
|---------------------|-----------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|--------------------------------|
| Título da fase      | Às portas de Constantinopla                                                       | At the Gates of Constantinople                                                        | `level.constantinople.title`   |
| Muralhas            | FASE 2 / ÀS PORTAS DE CONSTANTINOPLA / 1096-1097                                  | STAGE 2 / AT THE GATES OF CONSTANTINOPLE / 1096-1097                                  | `level.constantinople.chapter` |
| Bairro imperial     | CONSTANTINOPLA - Os líderes cruzados se reúnem.                                   | CONSTANTINOPLE - The Crusader leaders assemble.                                       | `level.constantinople.city`    |
| Mercado             | MERCADO - Entre as arcadas, mercadorias e viajantes a caminho do porto.           | MARKET - Goods and travelers pass through the arcades on the way to the harbor.       | `level.constantinople.market`  |
| Porto               | BÓSFORO - As embarcações aguardam no porto.                                       | BOSPHORUS - The ships await in the harbor.                                            | `level.constantinople.harbor`  |
| Objetivo inicial    | SIGA ATÉ O BÓSFORO                                                                | REACH THE BOSPHORUS                                                                   | `level.constantinople.travel`  |
| Objetivo da arena   | DERROTE O CAPITÃO                                                                 | DEFEAT THE CAPTAIN                                                                    | `level.constantinople.fight`   |
| Chefe               | CAPITÃO DO PORTO                                                                  | HARBOR CAPTAIN                                                                        | `level.constantinople.boss`    |
| Entrada na arena    | O Capitão do Porto bloqueia a alfândega. Salte ou esquive ao ver a linha de mira. | The Harbor Captain blocks the customs house. Jump or dodge when the aim line appears. | `level.constantinople.arena`   |
| Abertura das portas | OS ANJOS ABREM O CAMINHO                                                          | ANGELS OPEN THE WAY                                                                   | `hud.angels`                   |
| Objetivo de saída   | EMBARQUE PARA A ÁSIA                                                              | BOARD FOR ASIA                                                                        | `level.constantinople.embark`  |

### Vitória após o embarque

**Português**

> DO OUTRO LADO DO BÓSFORO\
> Com o apoio de Aleixo I e as companhias reunidas,\
> a frota cruza o Bósforo rumo à Ásia.
>
> ENTER - Nova jornada

**Inglês**

> ACROSS THE BOSPHORUS\
> With Alexios I's support and the companies united,\
> the fleet crosses the Bosphorus into Asia.
>
> ENTER - New journey

Chaves no jogo `level.constantinople.complete`, `level.constantinople.victory`.

## Pontos para validação histórica

- Os relatos sobre os juramentos e a reação de Raimundo de Toulouse variam. Convém revisar o alcance da promessa de
  restituição e a formulação da promessa atribuída a Raimundo à luz das diferentes
  crônicas. [Relatos da chegada dos cruzados a Constantinopla](https://sourcebooks.fordham.edu/source/cde-atcp.asp)
- Ana Comnena registra a divisão dos contingentes e sua reunião diante de Niceia. A fala do intendente resume o
  episódio. O Capitão do Porto, o duelo na alfândega e a abertura das portas pelos anjos pertencem à ficção do
  jogo. [Alexíada de Ana Comnena livro XI](https://sourcebooks.fordham.edu/basis/AnnaComnena-Alexiad11.asp)

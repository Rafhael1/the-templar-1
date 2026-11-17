# Fase 3 As muralhas de Niceia

Identificador da fase `nicaea`. Textos da versão atual em português e inglês.

## História e conversas

Em 1097, cruzados e bizantinos reuniram forças para sitiar Niceia, então sob domínio seljúcida. No jogo, o personagem
cruza o acampamento e a margem do lago Ascaniano, atravessa as obras de cerco e chega ao reduto nas colinas. As duas
conversas são obrigatórias. Pressione F para abrir cada conversa e F de novo para confirmar o plano. As confirmações
persistem se o jogador morrer e voltar ao santuário.

### Sinopse cadastrada

O manifesto registra a sinopse da fase.

**Português**

> Em 1097, cruzados e bizantinos cercam Niceia. Diante das forças de socorro, o bloqueio do lago e as negociações
> decidem a quem a cidade se rende.

**Inglês**

> In 1097, the Crusaders and Byzantines besiege Nicaea. With a relief force approaching, the blockade of the lake and
> negotiations decide who receives the city's surrender.

Chaves no jogo `level.nicaea.story`.

### Oficial cruzado no acampamento

Primeiro ponto de coordenação. O oficial orienta o jogador a procurar o enviado à margem do lago.

**Português**

> OFICIAL CRUZADO / O cerco por terra\
> Cada companhia vigia um trecho da muralha. A força de socorro do sultão pode romper o cerco se um setor ceder.
> Confirme o plano aqui e procure Manuel Butumita na margem do lago.

**Inglês**

> CRUSADER OFFICER / The siege by land\
> Each company watches a stretch of the wall. The sultan's relief force could break the siege if one sector gives way.
> Confirm the plan here and find Manuel Boutoumites on the lakeshore.

Chaves no jogo `level.nicaea.land.title`, `level.nicaea.land.body`.

### Manuel Butumita na margem do lago

Segundo ponto de coordenação. Confirmar o sinal aciona o bloqueio. Ao confirmar os dois planos, o jogador libera a
entrada do reduto.

**Português**

> MANUEL BUTUMITA / O bloqueio e o acordo\
> Os barcos de Aleixo I chegaram por terra ao lago. Confirme o sinal para fecharmos a rota de suprimentos. Enquanto
> vocês contêm o socorro, vou negociar a rendição da guarnição ao imperador.

**Inglês**

> MANUEL BOUTOUMITES / The blockade and the terms\
> Alexios I's boats reached the lake overland. Confirm the signal so we can cut off the supply route. While you hold
> back the relief force, I will negotiate the garrison's surrender to the emperor.

Chaves no jogo `level.nicaea.blockade.title`, `level.nicaea.blockade.body`.

| Momento           | Português           | Inglês           | Chave no jogo          |
|-------------------|---------------------|------------------|------------------------|
| Abrir conversa    | [F] Conversar       | [F] Talk         | `conversation.open`    |
| Confirmar o plano | [F] Confirmar plano | [F] Confirm plan | `conversation.confirm` |

## Textos do percurso e do desfecho

| Momento                  | Português                                                                                                                    | Inglês                                                                                                              | Chave no jogo             |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|---------------------------|
| Título da fase           | As muralhas de Niceia                                                                                                        | The Walls of Nicaea                                                                                                 | `level.nicaea.title`      |
| Acampamento              | FASE 3 / AS MURALHAS DE NICEIA / 1097                                                                                        | STAGE 3 / THE WALLS OF NICAEA / 1097                                                                                | `level.nicaea.chapter`    |
| Margem do lago           | LAGO ASCANIANO - Niceia recebe suprimentos pela água.                                                                        | LAKE ASCANIA - Nicaea receives supplies across the water.                                                           | `level.nicaea.lake`       |
| Obras de cerco           | OBRAS DE CERCO - As companhias avançam por diques e plataformas diante das muralhas.                                         | SIEGE EARTHWORKS - The companies advance along embankments and platforms outside the walls.                         | `level.nicaea.earthworks` |
| Colinas                  | COLINAS DE NICEIA - A força de socorro tenta romper o cerco.                                                                 | HILLS OF NICAEA - The relief force tries to break the siege.                                                        | `level.nicaea.relief`     |
| Objetivo inicial         | CONFIRME OS PLANOS COM OS DOIS ALIADOS [F]                                                                                   | CONFIRM THE PLANS WITH BOTH ALLIES [F]                                                                              | `level.nicaea.travel`     |
| Após confirmar os planos | PLANOS CONFIRMADOS / SIGA ATÉ O REDUTO                                                                                       | PLANS CONFIRMED / PROCEED TO THE REDOUBT                                                                            | `hud.briefingsConfirmed`  |
| Entrada na arena         | O Duelista do Socorro ataca em dois tempos. O segundo corte rompe a guarda. Esquive ou apare e contra-ataque na recuperação. | The Relief Duelist strikes twice. The second cut breaks a held guard. Dodge or parry, then counter during recovery. | `level.nicaea.arena`      |
| Objetivo da arena        | DETENHA O DUELISTA                                                                                                           | STOP THE DUELIST                                                                                                    | `level.nicaea.fight`      |
| Chefe                    | DUELISTA DO SOCORRO                                                                                                          | RELIEF DUELIST                                                                                                      | `level.nicaea.boss`       |
| Abertura dos portões     | OS ANJOS ABREM O CAMINHO                                                                                                     | ANGELS OPEN THE WAY                                                                                                 | `hud.angels`              |
| Objetivo de saída        | SIGA ATÉ O ESTANDARTE BIZANTINO                                                                                              | REACH THE BYZANTINE STANDARD                                                                                        | `level.nicaea.exit`       |

### Vitória ao chegar ao estandarte

**Português**

> NICEIA SOB CONTROLE BIZANTINO\
> Com o socorro repelido e o lago bloqueado, a guarnição aceitou o acordo negociado por Manuel Butumita. Em junho de
> 1097, Niceia foi entregue a Aleixo I.
>
> ENTER - Nova jornada

**Inglês**

> NICAEA UNDER BYZANTINE CONTROL\
> With the relief force repelled and the lake blockaded, the garrison accepted the terms negotiated by Manuel
> Boutoumites. In June 1097, Nicaea was handed over to Alexios I.
>
> ENTER - New journey

Chaves no jogo `level.nicaea.complete`, `level.nicaea.victory`, `overlay.victory.restart`.

## Pontos para validação histórica

- Relatos latinos descrevem a divisão do cerco por terra e o combate contra o socorro seljúcida. Eles e Ana Comnena
  relatam o uso de barcos transportados por terra para bloquear o abastecimento pelo
  lago. [Relatos do cerco de Niceia](https://sourcebooks.fordham.edu/source/cde-nicea.asp) · [Alexíada livro XI](https://sourcebooks.fordham.edu/basis/AnnaComnena-Alexiad11.asp)
- Ana Comnena atribui a negociação a Manuel Butumita e relata que o acordo foi mantido em segredo dos cruzados. O jogo
  condensa os acontecimentos e mostra o bloqueio antes do confronto final. A fala de Butumita torna pública a intenção
  de negociar. [Alexíada livro XI](https://sourcebooks.fordham.edu/basis/AnnaComnena-Alexiad11.asp)
- As falas, o oficial e o reduto são dramatizações. O Duelista do Socorro e a abertura dos portões pelos anjos pertencem
  à ficção do jogo.

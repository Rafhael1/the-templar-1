# Instruções para o modelo: fases, bosses e mecânicas

Ao receber um prompt que referencie este arquivo, aplique as instruções abaixo à tarefa solicitada em **O Templário**.
Este arquivo é contexto operacional para você, o modelo. Não gere um tutorial para o usuário nem novos modelos de
prompt; planeje, implemente e valide o conteúdo pedido. Se o usuário solicitar somente planejamento, entregue o plano
sem implementar.

Respeite o escopo do prompt e as instruções vigentes do projeto. Use julgamento para detalhes rotineiros e avance sem
pedir confirmações já concedidas. Pergunte somente quando uma decisão ausente impedir o trabalho ou alterar
materialmente seu escopo. Não invente suporte a funcionalidades futuras.

Antes de editar, leia [ADICIONAR_FASES.md](ADICIONAR_FASES.md), [ARQUITETURA.md](ARQUITETURA.md) e, para mudanças com
impacto de renderização ou simulação, [PERFORMANCE.md](PERFORMANCE.md). Verifique o código atual: as observações deste
arquivo foram levantadas em 23/10/2026 e não substituem as implementações vigentes.

## 1. Execute a tarefa nesta ordem

1. Inspecione o estado do Git, as instruções do projeto, a campanha e as classes afetadas. Preserve alterações locais
   existentes.
2. Apresente um plano curto: identidade da fase, percurso, encontros, boss, recursos novos e mudanças de contrato
   necessárias.
3. Se o pedido já autorizar implementar, prossiga após apresentar o plano. Pergunte somente por decisões que impeçam o
   trabalho ou mudem materialmente o escopo.
4. Implemente uma parte jogável por vez: percurso, encontros, boss e acabamento. Valide cada mudança relevante.
5. Entregue conteúdo registrado na campanha, traduções, geradores reproduzíveis, evidência visual e aplicativo
   atualizado quando solicitado um resultado jogável local.

Mudanças devem ser pequenas e focadas. Use um caminho explícito para cada responsabilidade, erros claros para conteúdo
inválido e os tipos do projeto. Introduza abstrações quando houver uma necessidade concreta. Uma nova fase com as regras
atuais deve ser conteúdo; uma nova regra de combate exige implementação e testes próprios.

## 2. Confira as capacidades antes de projetar

| Área             | Estado atual                                                                                                    |
|------------------|-----------------------------------------------------------------------------------------------------------------|
| Tecnologia       | Java 21, libGDX, módulos `core` e `lwjgl3`                                                                      |
| Campanha         | Fases ordenadas em `assets/levels/campaign.xml`; `Journey` controla avanço e reinício                           |
| Formato          | Mapas TMX horizontais finitos; câmera exterior acompanha X, com área visível de 960 × 540                       |
| Geometria        | Retângulos sólidos; plataformas também bloqueiam laterais e teto                                                |
| Progressão       | Um spawn, um checkpoint, uma arena, um boss, um portão de saída e uma saída por fase                            |
| Inimigos comuns  | Sentinela, arqueiro, lanceiro, escudeiro, bruto e lançador de óleo                                              |
| Boss de produção | Guardião; existe uma base `BossBehavior` e um teste com outro conjunto de habilidades                           |
| Jogador          | Espada lateral, golpe aéreo para baixo, escudo/aparada, esquiva e salto                                         |
| Persistência     | Checkpoint e coletas durante a sessão; não há salvamento em disco                                               |
| Apresentação     | Regiões com transição, passagens, marcos, interior da arena, poses por fase de ataque e efeitos sonoros/visuais |

Fases verticais, múltiplos checkpoints, múltiplas arenas, saída sem boss e desbloqueio persistente de habilidades
**exigem mudança de contrato**. Não apresente essas funcionalidades como existentes ou como atributos que bastaria
acrescentar ao XML.

Use a primeira fase como referência de acabamento e integração. Não replique automaticamente sua extensão, quantidade de
regiões, inimigos, cura ou aparência. Verifique esses dados no conteúdo atual quando precisar compará-los.

## 3. Projete a fase antes de gerar o mapa

### Identidade e composição

Defina tema, paleta, silhueta dominante, arquitetura, clima e história ambiental. Cada região deve ter um marco
reconhecível e uma função no percurso. Riqueza vem da composição: fundo distante, arquitetura intermediária, elementos
próximos e detalhes no piso, preservando a leitura do jogador, dos ataques e das bordas transitáveis.

A primeira fase usa mosteiro/acampamento, aqueduto/cemitério, claustro e muralhas. Uma próxima fase precisa de
identidade própria: reutilize recursos coerentes com seu tema e produza os que faltarem. Dê um papel a sepulturas,
carroças, inscrições, altares, luzes e ruínas; evite preencher todo espaço com objetos repetidos.

Mantenha detalhes estáticos ancorados em coordenadas do mundo. O início da área visível da câmera serve para recorte,
nunca como origem para regenerar vegetação ou escolher variações de pedras.

### Percurso e encontros

Antes de gerar o TMX, descreva cada trecho em uma tabela:

| Trecho / faixa X          | Piso e rota                         | Encontro e intenção                        | Descanso / recompensa         | Marco visual                   |
|---------------------------|-------------------------------------|--------------------------------------------|-------------------------------|--------------------------------|
| Entrada                   | Piso seguro e leitura dos controles | Apresentação de uma ameaça                 | Espaço para experimentar      | Identidade da região           |
| Desenvolvimento           | Patamares e rota opcional           | Combinação que testa a habilidade ensinada | Recompensa pelo desvio        | Ruína ou estrutura distinta    |
| Aproximação do checkpoint | Percurso legível                    | Conclusão de um grupo de encontros         | Recuperação e pausa           | Santuário reconhecível         |
| Segunda metade            | Variações do que já foi aprendido   | Combinações mais exigentes                 | Cura proporcional ao desgaste | Mudança de atmosfera           |
| Boss                      | Arena adequada às habilidades       | Preparações claras e janelas de resposta   | Vitória e saída               | Composição própria do encontro |

Preencha a tabela com coordenadas e encontros concretos da fase solicitada. Ajuste a quantidade de trechos ao percurso.
Use-a no plano; não acrescente esse formato ao contrato do jogo.

Distribua inimigos pela intenção do encontro e pela superfície real. Apresente um tipo novo em situação que permita
aprender sua resposta antes de combiná-lo com outros. Considere visão, oclusão, alcance, perseguição, rotas de salto e a
possibilidade de inimigos convergirem. Dois spawns separados podem formar um grupo excessivo durante a perseguição.

Rotas elevadas e opcionais precisam de propósito: recompensa, encontro, vista ou atalho verificável. Reserve espaço para
reposicionamento contra brutos, lanceiros e fogo. O checkpoint deve oferecer uma pausa real; a arena deve permitir
reagir às habilidades do boss.

Distribua cura conforme o desgaste observado no percurso. Verifique a rota principal com os três corações padrão e os
controles normais; recompensas opcionais não devem ser a única maneira de compensar um encontro obrigatório
desequilibrado. O validador de mapa não demonstra esse equilíbrio.

### Geometria

- Use `LevelLoader`, `LevelValidator` e o guia técnico como contrato vigente.
- Alinhe o desenho dos tiles às colisões e confira o apoio de todo o corpo dos spawns.
- Planeje subidas em relação ao salto real: inimigos sobem degraus de até 32 pixels e saltam cerca de 110 pixels em
  altura, com trajetórias verificadas. Teste a combinação de altura, vão, teto e largura do patamar.
- Evite pisos que aprisionem uma queda, tetos que bloqueiem um salto necessário e inimigos pousados em beiradas sem
  apoio completo.
- No TMX, Y cresce para baixo; no mundo, cresce para cima. Para ponto: `worldY = mapHeightPixels - tiledY`. Para
  retângulo: subtraia também sua altura.
- A altura do arquivo pode ser 544 pixels por causa da grade; objetos físicos continuam limitados à faixa visível de 540
  pixels.
- Dimensione a arena pelo corpo do boss, pelos movimentos e pela câmera. Atualmente ela exige piso plano contínuo até a
  saída; não desenhe plataformas físicas dentro dela sem primeiro alterar o contrato.

## 4. Coloque conteúdo e regras nos responsáveis corretos

```text
assets/
  levels/
    campaign.xml
    <id-da-fase>/
      maps/<id-da-fase>.tmx
      scenery/region-0.png ...
      chapel/                 # cinco imagens do interior no contrato atual
      ...                     # recursos exclusivos, com nomes claros
  sprites/                    # recursos efetivamente compartilhados
  audio/                      # sons e músicas efetivamente compartilhados
  i18n/messages_pt.properties
  i18n/messages_en.properties
  maps/                       # tiles e projeto Tiled compartilhados

tools/
  generate_<conteudo>.py       # gera somente os arquivos que declara

core/src/main/java/br/com/thecruzader/
  level/                      # conteúdo, leitura e validação
  gameplay/                   # regras e estado; sem libGDX
  input/                      # intenções e mapeamento de controles
  presentation/               # imagem, áudio, HUD e efeitos
  screen/                     # coordenação das telas
```

O nome `chapel/` é a convenção atual. O `<interior>` recebe caminhos explícitos; não é necessário chamar toda arena de
capela nem impor essa aparência a todo boss.

`LevelResources` carrega e descarta os recursos declarados pela fase. `GameAssets` carrega os compartilhados. Se
introduzir sprites ou áudio exclusivos que ainda não tenham campos no manifesto, implemente sua declaração e seu ciclo
de vida explicitamente. Criar uma pasta no disco não faz o jogo descobrir esses recursos automaticamente.

Geradores devem ter destino e escopo claros. Ao criar uma fase nova, não execute um gerador da primeira fase sobre o TMX
novo nem regrave a arte original inadvertidamente. Se houver autoria procedural, mantenha semente determinística e um
comando específico para reproduzir a fase. O gerador e o mapa entregue precisam corresponder.

## 5. Ao adicionar um boss, integre o tipo completo

### O que já pode ser reaproveitado

`EnemyBehavior` define decisões, atributos e contato ofensivo. `BossBehavior` compartilha aproximação, ciclo de ataque e
recuperação. `GuardianBehavior` implementa o conjunto de habilidades do primeiro chefe. Cada inimigo tem sua própria
instância de comportamento.

`EnemyKind` contém dimensões e a marca `boss`. `LevelData` exige exatamente um inimigo marcado como boss. O mundo já
encontra esse inimigo pela marca; a seleção do comportamento passa por `Enemy.behaviorFor`. Para introduzir um novo
tipo, atualize o enum, esse switch, o contrato de entidades do `LevelLoader`, as classes do Tiled e a apresentação
correspondente. O tipo precisa existir antes de ser usado no TMX.

### Acoplamentos que precisam ser revistos ao adicionar o próximo boss

| Código atual                                                                                              | Consequência para um boss diferente                                                                                                                             |
|-----------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GameWorld.guardian()` e seus consumidores                                                                | O objeto é encontrado pela marca `boss`, mas o nome sugere um único personagem. Renomear para um termo genérico quando integrar o segundo chefe                 |
| `WorldRenderer` usa `e.boss()` para escudo do guardião, escala 1,3 e offsets                              | Todo chefe novo herdaria apresentação do guardião. Selecionar arte, equipamento e dimensões visuais pelo tipo                                                   |
| `BossBehavior` acelera o tempo abaixo de meia vida, fixa percepção e aproximação                          | Reutilizar essas decisões somente quando fizerem parte do novo design; extrair a política necessária quando houver diferença concreta                           |
| `CombatSystem` usa `boss()` para resistência ao recuo, atordoamento de 0,40 s e abertura de contra-ataque | Essas propriedades não devem ser assumidas para todo chefe; delegar a resposta específica ao comportamento ao introduzir uma exceção                            |
| `Enemy.comboSword`, `nextMove` e estados de habilidades no enum comum                                     | Há estado específico do guardião na entidade compartilhada. Novas habilidades próprias devem manter seu estado no comportamento, quando não forem regras comuns |
| `Attack` representa um único ciclo de três fases                                                          | Combo, rajada ou ataque com múltiplas janelas precisam de uma sequência explícita de ações; uma animação não pode criar dano por conta própria                  |
| `BossRelease`, anjos e fluxo de conclusão                                                                 | Todo boss atual usa a mesma liberação dos portões. Um encerramento diferente exige uma política explícita e atualização de testes                               |

Ajuste esses pontos junto com a integração do segundo boss, preservando o primeiro. Uma classe nova de comportamento,
isoladamente, não completa essa integração. Evite condicionar regras por ID de fase dentro de `GameWorld` ou duplicar
`PlayScreen` para cada chefe.

### Defina o contrato do boss antes de implementá-lo

Defina nome e silhueta, corpo físico, tamanho visual, vida, locomoção, habilidades e critério de seleção. Para cada
golpe, descreva preparação, janela ativa, recuperação, alcance/área, compromisso de direção, bloqueio/aparada/esquiva,
interrupção e resposta ao erro. Acrescente fases de vida, vulnerabilidades, sons, efeitos, morte, liberação da arena e o
que reinicia no respawn.

Reutilize `BossBehavior` quando o ciclo servir ao design. Se uma habilidade exigir estado próprio, mantenha-o na
instância do comportamento. Extraia métodos pequenos de política quando necessário; não crie antecipadamente um motor
genérico de habilidades, ECS, sistema de plugins ou linguagem de scripts.

## 6. Ao adicionar uma mecânica, percorra o fluxo completo

Uma habilidade do jogador atravessa entrada, simulação e apresentação. Planeje esse fluxo antes de modificar o mapa.

| Responsabilidade                        | Lugar atual / direção recomendada                                                     |
|-----------------------------------------|---------------------------------------------------------------------------------------|
| Tecla ou botão                          | `InputMapper` traduz para intenção; não aplica dano                                   |
| Comando por passo                       | `PlayerInput`; pressionamento consumido uma vez, inclusive durante hit stop           |
| Recurso, recarga e estado da habilidade | `Player` e um sistema de gameplay específico quando a regra justificar a separação    |
| Linha do tempo                          | `Attack` se o ciclo existente servir; sequência explícita se houver outras janelas    |
| Movimento especial                      | Sistema da habilidade em coordenação com `MovementSystem`/`CollisionWorld`            |
| Acerto, dano e defesa                   | `CombatSystem`, ou sistema próprio para um projétil/área com ciclo de vida distinto   |
| Animação e efeitos                      | Renderizador específico, lendo estado e fases reais da simulação                      |
| Som                                     | Eventos de transição ou impacto, consumidos uma vez por `AudioPlayer`                 |
| Indicador de recarga/recurso            | `Hud`, consultando a simulação                                                        |
| Disponibilidade entre fases             | Regra explícita da sessão/campanha; `Journey` se precisar atravessar a troca de mundo |

Antes de implementar, decida custo, recarga, dano, alcance, interação com espada/escudo/esquiva/salto, cancelamento,
invulnerabilidade e comportamento em pausa, morte e transição. Se houver desbloqueio, decida se dura uma vida, fase,
jornada ou se precisa de salvamento em disco. O projeto não possui hoje uma configuração genérica de especiais em
`GameRules`.

O golpe novo deve compartilhar a aplicação de dano e as regras de defesa onde essas regras forem iguais. Evite criar um
segundo combate paralelo ou acrescentar toda a habilidade a `GameWorld.update`. Sistemas especializados podem ser
coordenados pelo mundo, como já ocorre com projéteis e óleo.

Se a fase depender da mecânica, ensine-a em um trecho seguro e teste sua disponibilidade no respawn e ao entrar na fase.
Não deixe um salto obrigatório depender de uma habilidade que o jogador possa perder sem recuperá-la.

## 7. Entregue animação, áudio e efeitos coerentes com o combate

Use a experiência de maça, lança e óleo como padrão de acabamento, não como obrigação de copiar seus ataques:

- Corpo e arma têm preparação, ação e recuperação. Sincronize a pose pela fase/progresso, incluindo orientação e ponto
  de pega.
- O aviso deve comunicar o ataque a tempo de responder. Faíscas, rastro e tremor complementam a silhueta e o movimento.
- Impactos seguem colisões reais. Um pote contra uma parede quebra sem produzir fogo flutuante; uma preparação cancelada
  não deve tocar o impacto final.
- Sons de preparação, lançamento e impacto são disparados uma vez pela simulação. O renderizador não dispara áudio a
  cada quadro.
- Recursos visuais de efeitos não causam dano. Duração visual, área perigosa e som precisam ser coerentes com o estado
  do ataque.
- Respeite pausa, hit stop, mute, morte, respawn, conclusão e descarte de recursos. Sons e efeitos não devem continuar
  de uma fase anterior.
- Reutilize `GameEvent`, `CombatEffect` e os sistemas existentes quando representarem a mesma responsabilidade. Se
  posições/atenuação forem necessárias para novos sons, estenda o evento de forma explícita; o enum atual não carrega
  coordenadas.
- Preserve arte em pixel art com escala e filtros coerentes. Não altere os tempos ou as áreas de dano apenas para fazer
  uma animação encaixar sem registrar uma mudança de balanceamento.

## 8. Valide o resultado e entregue evidências

Preserve o passo fixo de 1/120 s e a interpolação. A configuração desktop atual alinha o limite de FPS à frequência do
monitor e mantém VSync. Detalhes estáticos são preparados ao carregar a fase, e somente o conteúdo visível é desenhado.
Carregue texturas e sons pelos responsáveis existentes; evite recriar recursos, reler arquivos ou gerar geometria
estática durante cada quadro.

Mantenha efeitos transitórios com duração limitada. Meça se aumentar muito o número de inimigos, projéteis, áreas ativas
ou detalhes. Compare o mesmo cenário e ambiente, incluindo p95/p99 e intervalos entre quadros; uma média baixa não
demonstra estabilidade. Não reduza a riqueza visual sem identificar o custo que precisa ser removido.

```sh
./gradlew test
./gradlew :lwjgl3:smokeTest
./gradlew :lwjgl3:performanceTest
./gradlew :lwjgl3:framePacingTest
./gradlew :lwjgl3:appImage
```

Execute os benchmarks separadamente, sem outro teste gráfico ou compilação concorrente. Escolha os checks pertinentes à
mudança; alterações apenas documentais não exigem executar o jogo.

`CampaignTest` valida os mapas registrados, arquivos e traduções. `BossBehaviorTest` demonstra reaproveitamento do
ciclo, mas não prova um segundo boss completo em produção. O smoke test e os benchmarks contêm posições/roteiros da
primeira fase: **adicione cobertura explícita da fase nova**, inclusive saltos, boss, derrota, respawn, vitória e avanço
da campanha. Adapte os casos visuais e de desempenho para visitar os pontos de maior custo da nova fase.

Ao entregar uma fase, registre:

- Identidade, percurso e diferenças de gameplay.
- Arquivos e recursos alterados, incluindo geradores e traduções PT/EN.
- Testes executados, capturas/sequências e resultado do percurso até a saída.
- Medições quando pertinentes, com máquina, resolução e limites do teste.
- Estado do aplicativo local: `Jogar.command` abre o pacote; recompilar classes não atualiza esse pacote.
- Limitações conhecidas e qualquer parte do percurso que ainda não tenha sido verificada.

## 9. Critérios de conclusão

Antes de encerrar a tarefa, confira os itens aplicáveis ao pedido:

- A fase está registrada na ordem correta da campanha e pode ser iniciada e concluída pelo fluxo normal do jogo.
- Arte, áudio, traduções PT/EN e caminhos dos recursos estão integrados, sem depender de arquivos temporários ou
  destinos externos ao projeto.
- O layout tem identidade própria, encontros intencionais, rotas alcançáveis, cura e descanso coerentes com a
  dificuldade.
- O boss possui comportamento, apresentação e regras de defesa próprios; o guardião original continua funcionando.
- Mecânicas novas têm entrada, estado, regras, dano, apresentação e HUD necessários, com comportamento definido em
  pausa, morte, respawn e troca de fase.
- Preparação, ação, impacto e recuperação estão sincronizados; efeitos e sons não se repetem indevidamente nem
  sobrevivem à fase anterior.
- As verificações específicas do novo conteúdo passaram. Não use o percurso automático da primeira fase como prova de
  uma fase diferente.
- Foram preservados o passo fixo, a interpolação, a estabilidade dos detalhes no mundo e o carregamento explícito de
  recursos.
- O aplicativo local foi reconstruído quando o pedido exige uma entrega jogável pelo `Jogar.command`.

Na resposta final, informe brevemente o que foi entregue, o que foi efetivamente verificado, onde ver ou jogar o
resultado e qualquer limitação pendente. Não declare testes, medições, inspeção visual ou empacotamento sem evidência da
execução. Se alguma parte permanecer bloqueada, identifique-a em vez de apresentar a tarefa como concluída.

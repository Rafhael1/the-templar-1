# Arquitetura — O Templário

## Decisão central

Dois módulos Gradle, Java 21, libGDX 1.14.2 e orientação a objetos simples. O objetivo é permitir mudar o combate e construir fases sem misturar regras com desenho ou teclado.

`core` contém o jogo e seus recursos. `lwjgl3` depende de `core` e fornece a entrada desktop, configuração da janela e empacotamento. As regras de `gameplay` usam somente Java e os tipos imutáveis de `level`; não acessam libGDX, AWT ou Swing.

## Responsabilidades

| Componente | Responsabilidade |
|---|---|
| `CrusaderGame` | Ciclo de vida dos recursos e transição entre telas |
| `IntroScreen` / `IntroAnimation` | Sequência/legendas/entrada e desenho das camadas animadas, respectivamente |
| `MenuScreen` | Menu Scene2D e solicitação de início |
| `PlayScreen` | Coordenação de entrada, simulação, apresentação e pausa |
| `InputMapper` | Teclas para intenções; retém pressionamentos até um passo da simulação |
| `PlayerInput` | Comando imutável para um passo: direção, salto, ataque e esquiva |
| `GameWorld` | Estado da sessão, ordem de atualização e progressão |
| `MovementSystem` | Velocidade, gravidade, salto, esquiva e tolerância de salto |
| `CollisionWorld` | Resolução AABB por eixo e bloqueio físico do portão |
| `EnemySystem` | Detecção, perseguição finita, patrulha, preparação e ataque |
| `EnemyPerception` | Visão inicial por distância/direção/oclusão, observação e memória da última posição vista |
| `EnemyNavigation` | Superfícies transitáveis, conexões de caminhada/salto e escolha de rota |
| `CombatSystem` | Sobreposição de armas, dano, invulnerabilidade e um acerto por alvo |
| `Attack` | Linha do tempo compartilhada de preparação, golpe ativo e recuperação |
| `CampaignLoader` / `Campaign` | Registro único e ordenado de fases |
| `LevelDefinition` | Caminho do mapa, regiões, decorações e chaves de texto por fase |
| `Journey` | Progresso da campanha e mundo atual, sem acesso a arquivos ou gráficos |
| `LevelLoader` / `LevelValidator` | Contrato TMX, conversão e validação do formato de fase suportado |
| `LevelResources` | Mapa e cenários de uma tela, com descarte ao sair |
| `LevelData` | Geometria e posições imutáveis do cenário |
| `WorldRenderer` | Cenário, tilemap, sprites, efeitos e câmera |
| `Hud` | Interface Scene2D e sobreposições de pausa/morte/vitória |
| `AudioPlayer` | Eventos de jogo para áudio; mudo e pausa |
| `GameAssets` | Carregamento e descarte único de sprites compartilhados, sons e skin |

Não há ECS, contêiner de injeção, framework de IA ou motor de física adicional. A simulação usa classes concretas e referências explícitas. Os campos mutáveis das entidades são internos ao pacote de gameplay; a apresentação consulta métodos públicos.

## Fluxo de uma partida

1. O launcher configura LWJGL3 e inicia `CrusaderGame`.
2. `GameAssets` carrega os recursos; falhas de arquivo ou formato interrompem a inicialização.
3. `MenuScreen` apresenta o menu; ao iniciar, o jogo cria `PlayScreen` e descarta a tela anterior.
4. `PlayScreen` lê os comandos e avança `GameWorld` em passos fixos de 1/120 s.
5. O mundo atualiza cronômetros, direção/ataque do jogador, movimento, inimigos, dano e progressão.
6. Eventos são consumidos uma vez pela apresentação para sons e mensagens.
7. O renderizador consulta o mundo e desenha; a HUD desenha a interface.

Tudo ocorre na thread principal de libGDX. O delta acumulado é limitado a 100 ms para evitar uma sequência longa de passos após travamentos. A apresentação usa `FitViewport` 960 × 540 com barras nas proporções diferentes. Pixel art usa filtro nearest-neighbor; escala fracionária pode produzir pixels de larguras ligeiramente diferentes.

Pausar limpa comandos e acumulador. Perder foco pausa a partida. A volta depende de Esc, evitando que o personagem se mova ao alternar de aplicativo.

## Combate

O ataque do jogador tem 100 ms de preparação, 120 ms ativos e 220 ms de recuperação. Somente a fase ativa causa dano. O identificador do golpe evita múltiplos acertos no mesmo inimigo. Os inimigos têm preparação mais longa, sinalizada visualmente; a direção do golpe é travada quando ele começa.

A esquiva dura 200 ms, oferece 240 ms de invulnerabilidade e tem recarga de 800 ms. Ataque e início de esquiva são mutuamente exclusivos. O jogador possui cinco pontos de vida. O santuário cura e define o retorno. Morrer repovoa inimigos, preservando o checkpoint e a bandagem da sessão.

O salto usa gravidade e uma pequena tolerância após sair da borda (80 ms) e antes de tocar o chão (100 ms). O cenário usa retângulos sólidos, inclusive laterais e teto. O portão participa da colisão enquanto o guardião estiver vivo.

## Mapas e recursos

O Tiled é o editor da fase. O carregador transforma a origem superior esquerda do TMX em coordenadas com Y crescente para cima. A camada visual usa `OrthogonalTiledMapRenderer`; a geometria da simulação vem da camada de objetos `collisions`.

O mapa declara classes no arquivo `.tiled-project`, carregado também pelo libGDX. Recursos ficam em `core` como classpath resources, portanto funcionam no aplicativo empacotado independentemente do diretório atual do terminal.

`GameAssets` possui o `AssetManager` e a skin. As telas possuem seus renderizadores e stages. Ao trocar de tela, os recursos específicos da tela são descartados; ao fechar o jogo, os recursos compartilhados também são descartados. Não existem gerenciadores estáticos globais.

## Build e testes

O Gradle Wrapper tem versão e checksum fixos. As dependências possuem lockfiles. O compilador trata warnings como erros. JUnit Jupiter testa o domínio sem contexto gráfico e o carregador usa o mapa real do projeto.

O teste de percurso completo avança a partida com comandos de movimento, ataque e esquiva, sem alterar saúde ou posições artificialmente. Os testes unitários usam cenários controlados para isolar colisões e fases do combate.

O teste desktop usa OpenGL real e captura o framebuffer. O pacote `appImage` é produzido com `jpackage` e inclui Java; precisa ser gerado no sistema e arquitetura de destino. A versão 1.0.0 identifica o protótipo e também atende ao formato exigido pelo bundle macOS.

## Próximas evoluções naturais

O próximo investimento deve ser na sensação do combate: animações próprias para cada golpe, efeitos de impacto e balanceamento com jogadores. Novas fases usam o formato TMX e entram na lista de `assets/levels/campaign.xml`; consulte `ADICIONAR_FASES.md`. Persistência e gamepad devem entrar somente quando se tornarem requisitos concretos.

Os inimigos produzem uma velocidade horizontal na IA; `CollisionWorld` aplica essa intenção ao terreno e ao portão, além de gravidade durante movimento e ataque. Degraus de até 32 pixels são transpostos somente quando o inimigo está no chão e há espaço livre para o corpo na posição elevada. Paredes maiores bloqueiam a passagem.

`LevelData` contém uma lista de unidades de bandagens e spawns tipados de sentinela, arqueiro e guardião. `GameWorld` mantém a coleta por índice entre respawns. `EnemySystem` produz os disparos do arqueiro e delega os movimentos do guardião a `BossSystem`; `ProjectileSystem` resolve flechas contra terreno, portão e jogador na ordem do impacto ao longo do deslocamento.


A apresentação interpola posições anteriores/atuais da simulação e arredonda a câmera para preservar a grade de pixels. `WorldRenderer` controla somente o tempo visual de morte; `TemplarDeathAnimation` desenha as doze poses do templário, oração e luz durante 3,8 segundos. `Hud` e `PlayScreen` usam essa mesma duração para exibir a mensagem e liberar o respawn. Os estados terminais da simulação permanecem congelados. `AudioPlayer` trata `DEATH` com um réquiem próprio não repetitivo, suspende a ambiência, respeita pausa/mute e impede que uma música já concluída reinicie ao retomar. Respawn/entrada numa nova tela de jogo restauram o áudio ambiente. Antes do tilemap, o batch compartilhado volta à cor branca opaca. Cores e regiões de armas são reutilizadas, objetos fora da câmera não são desenhados e colisões consultam coordenadas diretamente sem criar retângulos em cada comparação. `DisplaySettings` expressa os presets em pixels do framebuffer e compensa Retina.


`i18n/GameText` carrega os dois catálogos UTF-8 uma única vez e rejeita traduções incompletas. `Language` define PT e EN. `GameAssets` compartilha a consulta entre as telas; `CrusaderGame` persiste a escolha, e o menu reconstrói apenas a página de opções ao mudar o idioma. Textos de interface ficam em `assets/i18n/`, sem carregar arquivos a cada frame ou duplicar traduções nas classes das telas.


A arena é um retângulo obrigatório em `LevelData`/Tiled. `GameWorld` ativa o encontro ao entrar, mantém o chefe inativo antes disso e reinicia a arena no respawn. `CollisionWorld` compartilha as listas de sólidos estáticas e dos portões com movimento, separação de corpos e flechas. `EnclosedAreaRenderer` desenha a fachada na estrada e um interior separado ao entrar; `RoomTransition` cobre a troca de câmera com um fade de 0,3 segundo. `PlayScreen` suspende a simulação durante essa transição. `WorldRenderer` centraliza a câmera no interior e filtra entidades pela vista; `Hud` apresenta a vida do chefe. A morte do guardião inicia o relógio de `BossRelease` em `GameWorld`: a sequência suspende o combate, `AngelReleaseAnimation` desenha descida/voo/ascensão, e o progresso compartilhado abre visualmente as portas entre 1,8 e 3,2 segundos. Em 3,2 segundos, as colisões são removidas e `GATES_OPEN` anuncia a passagem; em 4,6 segundos, o controle retorna. Pausa congela o relógio e respawn o reinicia; a vista só retorna à estrada ao sair fisicamente da capela. Respawn reinicia a vista e os portões. As cinco texturas e as luzes locais são conteúdo explícito de `LevelDefinition.Interior`, carregado e liberado por `LevelResources`. Mapas, cenários e imagens da capela ficam em `assets/levels/<id>/`; os caminhos completos vêm do manifesto. Tiles/classes do Tiled e sprites usados por várias fases permanecem compartilhados.


## Campanha e recursos de fase

`CrusaderGame` carrega o manifesto e cria `Journey`. A abertura pertence à campanha e aparece ao pedir nova jornada. `Journey.currentLevel()` escolhe a definição; `PlayScreen` possui `LevelResources`, que valida a geometria e carrega o TMX gráfico do mesmo caminho. O renderizador recebe esse recurso explicitamente, sem mapa global. O menu usa a primeira definição como fundo.

A vitória congela o mundo atual. Enter chama `continueAfterVictory`: a jornada exige um mundo vencedor, avança o índice (ou volta ao primeiro após a última fase) e libera a criação de um mundo novo. Morte/respawn atua somente no mundo atual e preserva suas coletas e checkpoint. Entre fases, vida, inimigos, flechas, arena, coletas e checkpoint reiniciam; a nova tela também reinicia câmera, animações, entrada e anúncios. Não há salvamento em disco.

`GameAssets` possui apenas recursos compartilhados. Cada tela possui seu próprio `LevelResources`/`AssetManager` com o tilemap e as texturas das regiões. O descarte da tela libera stages, renderizador e depois os recursos da fase. Durante uma troca, a nova tela é construída antes de descartar a anterior; cada bundle possui suas próprias referências. Falha de conteúdo interrompe o carregamento com ID/caminho da fase e descarta o gerenciador parcial.

Regiões têm limites e texturas explícitos; os anúncios usam uma lista de booleanos, sem limite de 32 regiões por bitmask. Decorações têm posição, tipo e cor no manifesto. A câmera continua horizontal em 960 × 540; `LevelValidator` rejeita arenas e posições incompatíveis. As dimensões dos corpos são definidas em `LevelData` e compartilhadas com jogador, inimigos e validação de mapas. Testes de sistemas podem construir geometria sintética diretamente; mapas autorados passam pelo validador de apresentação ao carregar o TMX.


## Navegação dos inimigos comuns

`EnemyNavigation` constrói um grafo das superfícies do `LevelData`, descontando paredes/tetos no espaço do corpo. Conexões usam degraus caminháveis ou arcos de salto validados contra os retângulos sólidos. A previsão inclui a integração de gravidade do passo fixo. A escolha de rota usa distâncias em saltos até a superfície do alvo e prefere a aproximação mais próxima entre conexões equivalentes. Os saltos mantêm a velocidade horizontal até a aterrissagem; ataques novos não interrompem um salto.

`EnemySystem` mantém a última posição detectada, tempo de perseguição, ausência de progresso e intervalo de calma. Desistir muda o centro de patrulha para o local atual, sem teleporte ou clamp de coordenadas em torno do spawn. `CollisionWorld` continua sendo o único resolvedor físico e verifica a linha de tiro com o estado real dos portões. BossSystem conserva o comportamento do guardião. O grafo de superfícies pertence ao mundo e permanece fixo durante a sessão. Os estados de perseguição são dos inimigos e reiniciam no respawn; não há cache global.

# Arquitetura — The Cruzader

## Decisão central

Dois módulos Gradle, Java 21, libGDX 1.14.2 e orientação a objetos simples. O objetivo é permitir mudar o combate e construir fases sem misturar regras com desenho ou teclado.

`core` contém o jogo e seus recursos. `lwjgl3` depende de `core` e fornece a entrada desktop, configuração da janela e empacotamento. As regras de `gameplay` usam somente Java e os tipos imutáveis de `level`; não acessam libGDX, AWT ou Swing.

## Responsabilidades

| Componente | Responsabilidade |
|---|---|
| `CrusaderGame` | Ciclo de vida dos recursos e transição entre telas |
| `MenuScreen` | Menu Scene2D e solicitação de início |
| `PlayScreen` | Coordenação de entrada, simulação, apresentação e pausa |
| `InputMapper` | Teclas para intenções; retém pressionamentos até um passo da simulação |
| `PlayerInput` | Comando imutável para um passo: direção, salto, ataque e esquiva |
| `GameWorld` | Estado da sessão, ordem de atualização e progressão |
| `MovementSystem` | Velocidade, gravidade, salto, esquiva e tolerância de salto |
| `CollisionWorld` | Resolução AABB por eixo e bloqueio físico do portão |
| `EnemySystem` | Patrulha, perseguição, preparação, ataque e recuperação |
| `CombatSystem` | Sobreposição de armas, dano, invulnerabilidade e um acerto por alvo |
| `Attack` | Linha do tempo compartilhada de preparação, golpe ativo e recuperação |
| `LevelLoader` | Contrato de objetos TMX e conversão de coordenadas |
| `LevelData` | Geometria e posições imutáveis do cenário |
| `WorldRenderer` | Cenário, tilemap, sprites, efeitos e câmera |
| `Hud` | Interface Scene2D e sobreposições de pausa/morte/vitória |
| `AudioPlayer` | Eventos de jogo para áudio; mudo e pausa |
| `GameAssets` | Carregamento e descarte único de texturas, mapa, sons e skin |

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

A esquiva dura 200 ms, oferece 240 ms de invulnerabilidade e tem recarga de 800 ms. Ataque e início de esquiva são mutuamente exclusivos. O jogador possui cinco pontos de vida. O santuário cura e define o retorno. Morrer repovoa inimigos, preservando o checkpoint e a relíquia da sessão.

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

O próximo investimento deve ser na sensação do combate: animações próprias para cada golpe, efeitos de impacto e balanceamento com jogadores. Novas fases podem reutilizar o formato TMX. Persistência e gamepad devem entrar somente quando se tornarem requisitos concretos.

Os inimigos produzem uma velocidade horizontal na IA; `CollisionWorld` aplica essa intenção ao terreno e ao portão, além de gravidade durante movimento e ataque. Degraus de até 32 pixels são transpostos somente quando o inimigo está no chão e há espaço livre para o corpo na posição elevada. Paredes maiores bloqueiam a passagem.

`LevelData` contém uma lista de unidades de Pão da Vida e spawns tipados de sentinela, arqueiro e guardião. `GameWorld` mantém a coleta por índice entre respawns. `EnemySystem` produz os disparos do arqueiro e delega os movimentos do guardião a `BossSystem`; `ProjectileSystem` resolve flechas contra terreno, portão e jogador na ordem do impacto ao longo do deslocamento.


A apresentação interpola posições anteriores/atuais da simulação e arredonda a câmera para preservar a grade de pixels. `WorldRenderer` controla somente o tempo e poses visuais de morte; os estados terminais da simulação permanecem congelados. Antes do tilemap, o batch compartilhado volta à cor branca opaca. Cores e regiões de armas são reutilizadas, objetos fora da câmera não são desenhados e colisões consultam coordenadas diretamente sem criar retângulos em cada comparação. `DisplaySettings` expressa os presets em pixels do framebuffer e compensa Retina.

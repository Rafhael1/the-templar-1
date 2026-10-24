# O Templário

Jogo simples de ação e plataforma 2D em **Java 21 + libGDX**, com um cavaleiro templário como protagonista. O protótipo
tem uma fase completa, arte própria e ambientação fictícia nas Cruzadas. A versão de build está em `gameVersion`, no
`gradle.properties`. A infraestrutura de campanha permite registrar novas fases sem duplicar as regras do jogo.

## Jogar no Mac deste computador

Abra **`Jogar.command`** na raiz do projeto. Ele abre o aplicativo já construído em:

```text
lwjgl3/build/app-image/O Templário.app
```

O aplicativo contém seu próprio runtime Java. Não precisa de Gradle, rede ou de Java instalado para jogar. O pacote presente foi construído e testado em macOS ARM64 (Apple Silicon).

Para usar o Game Mode, abra pelo **Jogar.command** e ative **Opções → Tela cheia**. O pacote declara a categoria de jogo
e o suporte ao Game Mode; a ativação é controlada pelo macOS. A entrada e a saída da tela cheia foram verificadas no
aplicativo, mas o indicador de Game Mode ainda não foi confirmado.

| Controle                         | Ação                                                               |
|----------------------------------|--------------------------------------------------------------------|
| Enter                            | Confirmar opção do menu / próxima fase ou nova jornada após vencer |
| A/D ou setas                     | Mover                                                              |
| Espaço, W ou seta para cima      | Saltar                                                             |
| J ou Z                           | Espada                                                             |
| S ou seta para baixo + J/Z no ar | Golpe para baixo                                                   |
| L ou X (segurar)                 | Bloquear com o escudo                                              |
| Shift ou K                       | Esquiva                                                            |
| Esc                              | Pausar / continuar                                                 |
| M                                | Ativar / desativar som                                             |
| R ou Enter após morrer           | Retornar ao início ou ao santuário                                 |

O menu inicial oferece **Nova jornada**, **Instruções**, **Opções** (som, resolução Full HD/720p, idioma PT/EN e tela
cheia) e **Sair**. Navegue com W/S ou cima/baixo e confirme com Enter; Esc volta das instruções e opções. Os controles
ficam na página de instruções. Durante a partida, o HUD mostra três corações por padrão, escudo, recarga da esquiva e
contagem de bandagens; não há barra inferior.

Cada golpe exige um novo pressionamento. Nos últimos 0,12 segundo da recuperação, pressionar espada prepara um único
golpe seguinte, preservando a direção solicitada. A esquiva pode cancelar os últimos 0,10 segundo da recuperação e
descarta o golpe preparado. O golpe aéreo para baixo mantém sua direção até terminar, deixa de causar dano ao aterrissar
e dá um impulso para cima ao acertar, uma vez por golpe.

O escudo bloqueia golpes pela frente, inclusive no ar, e reduz a velocidade; não permite atacar ou esquivar enquanto
estiver levantado. Erguê-lo até 0,12 segundo antes do impacto apara o golpe: interrompe o atacante, abre espaço para
contra-atacar e produz um brilho e som próprios. Segurá-lo bloqueia golpes comuns; investidas e saltos do guardião,
sinalizados em vermelho, atravessam o bloqueio e exigem esquiva ou aparada. Ataques pelas costas causam dano. Flechas
também podem ser aparadas e são removidas no impacto.

Acertos têm uma pausa breve de 0,035 segundo, faíscas direcionais e tremor discreto de câmera; aparadas pausam por 0,055
segundo. Comandos de pressionamento recebidos durante essa pausa são preservados. Sentinelas e arqueiros reagem por 0,10
segundo quando atingidos fora de um ataque, sem renovar um atordoamento existente. Golpes comuns não interrompem
preparação, fase ativa ou recuperação; arqueiros buscam distância antes do próximo disparo, recalculando a mira enquanto
preparam cada disparo e enxergam o alvo. O guardião tem 16 pontos de vida, resiste ao recuo e concede um único
contra-ataque de dano dobrado após uma aparada, dentro de 0,40 segundo; essa abertura aparece em azul. Golpes pesados
errados deixam uma recuperação de 0,45–0,50 segundo com dano normal. Abaixo de metade da vida, sua investida emenda um
golpe de espada com preparação visível e uma janela de recuperação completa. O aviso sobre o inimigo indica a preparação
do ataque. A esquiva oferece invulnerabilidade breve e tem recarga de 1,4 segundo entre ativações. Perder o foco da
janela pausa a partida; pressione Esc para continuar.

## Objetivo e conteúdo

Abra a passagem para os peregrinos. Atravesse a estrada, enfrente doze sentinelas, quatro arqueiros, três lanceiros,
três escudeiros, dois brutos e dois lançadores de óleo, ative o santuário e derrote o guardião para abrir o portão
final. O **conjunto de bandagens de linho** aparece em treze pontos da fase e recupera até dois pontos de vida por
coleta. Cada unidade permanece coletada após morrer.

O santuário tem uma imagem de Nossa Senhora com manto azul, auréola e mãos postas, em um nicho de pedra com cruz dourada ornamentada, flores e velas. Ao ativá-lo, as duas velas se acendem. Ele restaura a vida e define onde o personagem retorna após morrer. Os inimigos reaparecem. Checkpoint e bandagens persistem durante a sessão; não há salvamento em disco. Enter após vencer avança à próxima fase registrada; depois da última fase, reinicia a campanha. Cada fase começa com vida cheia e seus próprios inimigos, checkpoint e coletas.

A história usa as Cruzadas como cenário de ficção, com soldados muçulmanos medievais fictícios e um comandante como guardião. Não representa uma batalha documentada. Nome, sprites, cenários e sons foram criados para este protótipo; não são assets de Blasphemous.

## Desenvolver

Requisitos: **JDK 21 instalado**; a toolchain do Gradle usa JDK 21. Importe a pasta raiz no IntelliJ IDEA como projeto Gradle. Use sempre o Wrapper incluído.

```sh
./gradlew :lwjgl3:run
./gradlew test
./gradlew build
```

No Windows, use `gradlew.bat` com as mesmas tarefas. A primeira execução baixa Gradle e dependências. Python e Tiled não são necessários para compilar ou jogar; somente para editar/regenerar conteúdo.

Versões fixadas: libGDX 1.14.2, Gradle 9.4.1 e JUnit Jupiter 6.1.3. Os arquivos `gradle.lockfile` fixam também as dependências transitivas. O Wrapper verifica o SHA-256 da distribuição do Gradle. A compilação usa UTF-8, `--release 21`, `-Xlint:all` e `-Werror`.

## Construir um aplicativo com Java incluído

Para gerar o instalador do sistema atual:

```sh
./gradlew test :lwjgl3:installer
```

O resultado fica em `lwjgl3/build/installer/`: **DMG** no macOS, **EXE** no Windows e **DEB** no Linux (Debian/Ubuntu).
Cada instalador inclui o runtime Java; quem joga não precisa instalar Java ou Gradle. No Mac, abra o DMG e arraste **O
Templário.app** para **Aplicativos**. No Windows, execute o EXE; a instalação é por usuário, com atalhos no menu Iniciar
e na área de trabalho. No Debian/Ubuntu, instale com `sudo apt install ./arquivo.deb`.

Gere cada pacote no sistema e arquitetura de destino, com JDK 21. Windows exige **WiX Toolset 3.x** no `PATH`;
Debian/Ubuntu exige `fakeroot` e as ferramentas de empacotamento Debian (`dpkg-deb`). O instalador Windows usa um UUID
estável para identificar atualizações; não altere esse UUID entre versões. Para atualizar no Mac, substitua o aplicativo
em Aplicativos; no Linux, instale o DEB de versão superior. Não há atualização automática.

Os pacotes locais não possuem assinatura de distribuição nem notarização Apple. Windows e Linux precisam ser validados
em suas plataformas antes de distribuir. A configuração usa as opções oficiais
do [jpackage do JDK 21](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html).

### Versionamento e lançamento

`gameVersion` em `gradle.properties` é a fonte única: os JARs, as distribuições ZIP, os metadados nativos, o menu
principal e o comando `--version` recebem a mesma versão. Use **MAJOR.MINOR.PATCH**, por exemplo `1.0.1`: PATCH para
correções, MINOR para novos recursos e MAJOR para mudanças incompatíveis. Para atender aos instaladores nativos, MAJOR
deve estar entre 1 e 255, MINOR entre 0 e 255 e PATCH entre 0 e 65535; sufixos como `-beta` são rejeitados.

1. Atualize `gameVersion` e registre as mudanças em `CHANGELOG.md`.
2. Execute `./gradlew clean test :lwjgl3:installer` em cada plataforma de destino.
3. Instale e abra o pacote; confirme a versão no menu e teste a substituição da versão anterior.
4. Faça o commit da versão e crie a tag correspondente, por exemplo `git tag -a v1.0.1 -m "O Templário 1.0.1"`. Publique
   os instaladores por sistema/arquitetura junto à tag.

Para consultar a versão sem abrir uma janela:

```sh
./gradlew :lwjgl3:run --args=--version
```

Para gerar somente o aplicativo:

```sh
./gradlew :lwjgl3:appImage
```

Usa o `jpackage` do JDK 21. O resultado fica em `lwjgl3/build/app-image/`. A tarefa reconstrói somente essa pasta de saída.

Gere o aplicativo no sistema e arquitetura de destino. A compilação e execução em Windows/Linux ainda não foram verificadas. O pacote Mac local não é um instalador assinado/notarizado para distribuição pública.

Para uma distribuição de desenvolvimento que requer Java 21:

```sh
./gradlew :lwjgl3:distZip
```

O ZIP fica em `lwjgl3/build/distributions/`. Os scripts são configurados para o sistema da máquina de build, incluindo `-XstartOnFirstThread` no Mac. Gere o pacote no sistema de destino.

## Ritmo da primeira fase

A estrada alterna pequenos acampamentos, terraços sólidos, galerias quebradas e uma subida opcional pelas ruínas. O
entorno do santuário fica aberto, criando uma pausa entre os encontros. Caixas, bandeiras e luzes marcam locais
específicos, em vez de se repetirem a intervalos fixos. As bandagens combinam pontos na estrada com recompensas nas
rotas elevadas.

Para reproduzir somente o mapa da primeira fase, sem regenerar imagens ou áudio:

```sh
python3 tools/generate_first_level.py --map-output assets/levels/exiles-road/maps/exiles-road.tmx
```

O comando sobrescreve o TMX de destino com o layout definido no gerador. As decorações continuam em
`assets/levels/campaign.xml`.

Os quatro fundos combinam mosteiro e acampamento, aqueduto e cemitério, claustro e muralhas. Trinta marcos adicionais —
arcos quebrados, altares votivos, sepulturas, árvores secas, carroças e estandartes — estão posicionados no manifesto.
Musgo, hera e fragmentos de pedra acompanham apenas as bordas expostas do terreno. A fachada da galeria tem arcadas e
relevos; a serra tem estratos de rocha. As decorações não alteram colisões.

`python3 tools/generate_environment.py` reproduz somente os quatro fundos e `assets/sprites/environment.png` (Pillow
necessário). O gerador completo de atmosfera usa esse mesmo script. As rotas superiores do aqueduto e do claustro agora
têm guardas, e duas bandagens adicionais apoiam os trechos antes do santuário e na subida da serra.

## Editar a fase

Abra `assets/maps/the-cruzader.tiled-project` no **Tiled** e depois `assets/levels/exiles-road/maps/exiles-road.tmx`.

- `terrain`: desenho da fase com tiles de 32 × 32.
- `collisions`: retângulos sólidos; desenho e colisões precisam permanecer alinhados.
- `entities`: pontos de spawn/checkpoint/inimigos e retângulos de bandagem/portão/saída.

As camadas de objetos estão ocultas por padrão para não aparecerem na renderização do mapa. Ative o olho dessas camadas no editor quando quiser modificá-las.

Classes de objeto:

| Classe          | Forma                                     | Quantidade   |
|-----------------|-------------------------------------------|--------------|
| `spawn`         | Ponto: canto inferior esquerdo do jogador | Exatamente 1 |
| `checkpoint`    | Ponto no piso                             | Exatamente 1 |
| `sentinel`      | Ponto no piso                             | Zero ou mais |
| `archer`        | Ponto no piso                             | Zero ou mais |
| `spearman`      | Ponto no piso                             | Zero ou mais |
| `shield_bearer` | Ponto no piso                             | Zero ou mais |
| `brute`         | Ponto no piso                             | Zero ou mais |
| `oil_thrower`   | Ponto no piso                             | Zero ou mais |
| `guardian`      | Ponto no piso                             | Exatamente 1 |
| `bandage`       | Retângulo da área de coleta de bandagens  | Uma ou mais  |
| `arena`         | Retângulo da área livre do chefe          | Exatamente 1 |
| `gate`          | Retângulo sólido enquanto o guardião vive | Exatamente 1 |
| `exit`          | Retângulo que conclui a fase              | Exatamente 1 |

Use mapas ortogonais finitos, camadas na raiz sem offsets/paralaxe, colisões retangulares sem rotação e as classes declaradas no projeto Tiled. Spawn, checkpoint e inimigos devem ser pontos sem rotação. O carregador lê o atributo `type` exportado pelo Tiled. Tipos desconhecidos, camadas obrigatórias duplicadas, entidades ausentes/duplicadas e posições incompatíveis com a câmera/arena interrompem o carregamento com erro. Consulte [Como adicionar fases](docs/ADICIONAR_FASES.md) para o contrato completo.

Os inimigos detectam o jogador a maior distância, mantêm o alerta por dois segundos ao perdê-lo de vista e seguem o templário entre chão e plataformas por rotas de caminhada e saltos. Só iniciam golpes quando a arma alcança o jogador; a direção fica comprometida durante o ataque. Sentinelas preparam golpes de espada por 450 ms e recuperam por 650 ms; o guardião prepara sua espada por 550 ms e recupera por 650 ms. A janela ativa permanece em 160 ms.

Os inimigos colidem com o terreno e o portão, sofrem gravidade e sobem degraus de até 32 pixels quando há espaço.
Sentinelas e arqueiros saltam por rotas verificadas contra o terreno; paredes altas e tetos continuam bloqueando
passagens. Saltos alcançam cerca de 110 pixels de altura e usam velocidade horizontal de 220 pixels/s. O jogador colide
com chão, laterais e teto; as plataformas são sólidas. A fase tem **280 tiles / 8960 pixels** de largura, 23
plataformas, duas escadarias com patamares e descidas, doze sentinelas, quatro arqueiros, três lanceiros, três
escudeiros, dois brutos, dois lançadores de óleo e um guardião. As bandagens aparecem em plataformas e pontos de
descanso; o santuário está no centro da jornada, e a arena final e o portão ocupam o extremo direito.

Arqueiros preparam o disparo por 650 ms e atualizam a mira a cada passo de simulação enquanto enxergam o jogador, até
soltar a flecha. Podem virar durante a preparação e acompanhar saltos. Ao perder a visão, mantêm a última posição
observada. A flecha mantém sua trajetória depois de lançada. Flechas atingem o terreno e o portão, podem ser bloqueadas
pela frente e são removidas ao acertar ou após três segundos. O respawn limpa as flechas em trânsito.

O guardião tem 12 pontos de vida, armadura própria e escudo. Alterna bloqueio frontal, investida, salto e espada; preparação e recuperação deixam oportunidades de esquiva e contra-ataque. Ataques por trás e golpes aéreos para baixo atravessam a guarda. Ao chegar à metade da vida, as preparações ficam mais curtas e a investida mais rápida.

## Testes e verificação visual

```sh
./gradlew test
./gradlew :lwjgl3:smokeTest
./gradlew :lwjgl3:performanceTest
```

`test` roda sem janela. `smokeTest` precisa de uma sessão desktop e abre uma janela real por alguns segundos. Usa o mesmo renderizador, telas, assets e mapeamento de entrada do jogo. Capturas ficam em `lwjgl3/build/screenshots/`.

Verificação: **171 testes JUnit passaram** usando o mapa atual, incluindo aparadas, comandos preparados, cancelamento de
recuperação, impulso aéreo, recuo com colisão e combo do guardião. Objetos de colisão poligonais ou rotacionados são
rejeitados com uma mensagem explícita. As entidades usam o atributo `type` exportado pelo Tiled. O teste gráfico captura
a rota das bandagens, o santuário, a escada, a capela e a vitória; verifica entrada, interior, pausa no fade e respawn;
um teste de simulação coleta as bandagens por saltos e retorna ao piso. As capturas `32-combat-*.png`, `33-combat-*.png`
e `34-combat-*.png` verificam aparada, pausa dos efeitos, contra-ataque e impulso do golpe aéreo.

## Estrutura

```text
assets/      sprites, cenários, mapa Tiled e áudio
core/        regras, telas libGDX, controles e apresentação
lwjgl3/      launcher desktop, teste gráfico e empacotamento
tools/       gerador dos assets originais e suas dependências
/docs/       arquitetura e decisões técnicas
```

Leia `docs/ARQUITETURA.md` para o fluxo de atualização e a divisão das responsabilidades. Assets gerados estão versionáveis e prontos para uso. Para reproduzir os arquivos originais, use `tools/generate_assets.py` em um ambiente Python com `tools/requirements.txt` instalado. **Esse script regrava os assets compartilhados e específicos de exiles-road; por padrão, não escreve mapas.** Para gerar também o mapa original, passe um destino explícito: `python tools/generate_assets.py --map-output assets/levels/exiles-road/maps/exiles-road.tmx`. Essa opção sobrescreve o arquivo indicado.

O conteúdo de produção permanece uma fase. O registro ordenado em `assets/levels/campaign.xml` está pronto para receber mais mapas; não há inventário, árvore de habilidades, controle/gamepad ou persistência em disco. Um segundo mapa pequeno existe somente nos recursos de teste para verificar a progressão.


A nova jornada começa com uma abertura animada em pixel art, com três cenas sobre o chamado de Urbano II, com ENTER/ESPAÇO/clique para avançar e ESC para pular. O protagonista templário é uma licença de ficção histórica; a ordem surgiu depois da Primeira Cruzada. O diálogo é original, não uma citação histórica.

A fase representa uma marcha condensada por acampamento, Anatólia, Antioquia e muralhas de Jerusalém. Cada região tem um
cenário próprio, com nuvens, bandeiras ao vento, suprimentos e fumaça. Há arqueiros em plataformas e treze conjuntos de
bandagens; eles só são consumidos quando falta vida. O santuário fica em x=4480, no centro do percurso.

Três trilhas originais de inspiração gregoriana compartilham um motivo em ré dórico, com coro masculino sintetizado sem
palavras, cordas dedilhadas e sinos. **Votum — O Juramento** (`menu-chant.wav`, 80 s, 72 BPM) acompanha o menu e a
abertura; **Via Peregrinorum — Caminho dos Peregrinos** (`ambience.wav`, 120 s, 64 BPM) deixa espaço para os efeitos
durante a exploração; **Custos Portae — O Guardião do Portão** (`boss-chant.wav`, 103 s, 112 BPM) acrescenta ostinato e
tambores na arena. As faixas são estéreo, com caudas de reverberação circulares para repetição contínua. Entrada na
arena e derrota do chefe trocam a música mesmo com mute ativo; morte dá prioridade ao réquiem e respawn retorna à
exploração. Pausa e mute controlam a faixa selecionada. Menu e abertura pausam o áudio ao perder foco; a tecla M também
funciona na abertura.

Ao derrotar o guardião da capela, **Victoria — A Vitória** (`victory-chant.wav`, 21,8 s, 88 BPM) substitui a música de
combate. O tema em ré maior combina coro, sinos e cordas e continua em loop durante a saída da capela e a tela de
vitória, respeitando pausa e mute. A próxima fase retoma a exploração.

`python3 tools/generate_music.py` reproduz somente as quatro trilhas, com NumPy instalado conforme
`tools/requirements.txt`. São timbres sintetizados, sem gravações de cantores ou samples externos.
`tools/generate_atmosphere.py` reproduz somente os cenários; `tools/generate_intro.py` regenera somente as camadas e os
atlas animados da abertura, sem regravar o mapa, o áudio ou os outros sprites.

Contexto histórico: [Urbano II e o chamado em Clermont](https://www.metmuseum.org/de/essays/the-crusades-1095-1291), [fundação posterior dos Templários](https://en.wikisource.org/wiki/1911_Encyclop%C3%A6dia_Britannica/Templars).

A janela inicia com framebuffer **1920 × 1080**, compensando a escala Retina no macOS. Opções alterna Full HD e 1280 ×
720 no modo janela. Em **Opções → Tela cheia**, o macOS usa a tela cheia nativa, também acessível pelo botão verde da
janela; a resolução acompanha a tela e o seletor fica desativado até voltar ao modo janela. O mundo continua em 960 ×
540, com pixels em escala inteira no Full HD; o limite de quadros acompanha a frequência do monitor, com VSync para
alinhar a apresentação. Jogador, inimigos e câmera usam interpolação entre passos de simulação.

As mortes dos inimigos usam oito poses: impacto, queda, arma solta e repouso, com desaparecimento gradual. O templário tem uma sequência própria de 3,8 segundos e doze poses: joelhos cedem, espada plantada como cruz, sinal da cruz, oração, inclinação e queda. A cena escurece, uma luz dourada envolve o cavaleiro e uma cruz com partículas sobe. O corpo permanece no chão. Só então aparecem a oração "Em tuas mãos entrego o meu espírito" e os controles de respawn. Pausa e perda de foco congelam a sequência; Esc permite retomar também após a morte.

O réquiem original de 6,4 segundos combina sino, timbres de órgão e coro sem palavras, com reverberação e cadência plagal. Toca uma vez no lugar da trilha ambiente, respeitando pausa e mute; ao retornar, o réquiem para e a ambiência retoma. `tools/generate_templar_death.py` gera somente `assets/sprites/templar-death.png` e `assets/audio/templar-requiem.wav`, compartilhados por todas as fases. `tools/generate_deaths.py` continua reproduzindo as poses dos inimigos. O teste gráfico verifica música, pausa/mute, bloqueio de respawn antecipado e retomada; `death-frames/` contém a sequência capturada no jogo.

O teste gráfico também compara pixels do chão com o tileset nos quatro cenários, verifica resolução Full HD e registra as duas animações de morte. O renderizador restaura a cor opaca antes do tilemap: efeitos de fumaça não podem alterar a opacidade do chão e das plataformas.

A medição de desempenho usa oito trechos com movimento e combate, 120 quadros de aquecimento e 360 amostras por trecho.
`./gradlew :lwjgl3:performanceTest` mede o trabalho com conclusão da GPU e sem limite de FPS;
`./gradlew :lwjgl3:framePacingTest` mede os intervalos entre quadros com a configuração normal de apresentação. Os
relatórios incluem p50/p95/p99/máximo, simulação, renderização, alocações e coletas de lixo. Medições e limites estão
em [docs/PERFORMANCE.md](docs/PERFORMANCE.md). A vegetação e o desgaste das superfícies são calculados no carregamento
da fase e permanecem ancorados ao mapa ao mover a câmera.


Textos usam um atlas DejaVu Sans de 96 pixels por glifo, com mipmaps e filtragem próprios para a interface. Os sprites mantêm nearest-neighbor. `tools/generate_font.py` reproduz o atlas usando o TTF e a licença incluídos em `assets/ui/`. A nota amarela de ficção foi removida da abertura.

Templário e inimigos vivos colidem pelos dois lados ao caminhar. Durante a esquiva, o templário atravessa os corpos sem
empurrá-los; paredes e portão continuam sólidos. É possível saltar por cima; ao cair sobre um inimigo, o templário
desliza pela lateral sem ficar apoiado na cabeça. A separação respeita paredes e portão; corpos mortos deixam a passagem
livre. Encostar normalmente não causa dano, e a investida do chefe mantém sua regra de um golpe por movimento.


As regiões misturam seus cenários ao longo de 640 pixels de estrada, com entrada e saída suaves. O fundo não troca inteiro ao cruzar um limite; o paralaxe permanece contínuo. Anúncios de capítulo aparecem uma vez por região e entram/saem gradualmente. As capturas `20-transition-*` registram as três passagens, verificando que o chão continua opaco durante a mistura.


## Idiomas PT / EN

Em **Opções → Idioma**, alterne português e inglês. A página muda imediatamente e a preferência fica salva para as próximas aberturas. O nome **O Templário** permanece nos dois idiomas. Menus, instruções, cutscene, objetivos, avisos e telas de pausa/morte/vitória consultam os mesmos catálogos.

- `assets/i18n/messages_pt.properties`: textos em português.
- `assets/i18n/messages_en.properties`: textos em inglês.
- `core/src/main/java/br/com/thecruzader/i18n/GameText.java`: carregamento e consulta.
- `core/src/main/java/br/com/thecruzader/i18n/Language.java`: idiomas disponíveis.

Os arquivos usam UTF-8 e chaves agrupadas em `game`, `menu`, `instructions`, `options`, `intro`, `hud`, `notice` e `overlay`. Para adicionar um texto, crie a mesma chave nos dois catálogos e consulte `assets.texts.text("grupo.chave")`; use `format` com `%s` para valores dinâmicos e `\n` para quebras de linha. Os 63 textos de cada catálogo são carregados uma vez; trocar o idioma apenas seleciona o catálogo em memória. Chaves diferentes entre idiomas, textos vazios e consultas a chaves inexistentes geram erro, sem tradução substituta.

Os testes verificam acentos, formatação, cobertura da fonte, ausência de traduções e troca sem releitura dos arquivos. O teste desktop muda PT → EN → PT nas opções, confirma a preferência salva e captura as telas em ambos os idiomas, incluindo avisos de pausa, morte e vitória.


## Capela do guardião

A área final tem 736 pixels de piso livre e cabe inteira na câmera de 960 × 540. Na estrada, aparece somente o exterior da capela, com torre sineira, cruz, vitrais, contrafortes e portal. Ao atravessar a entrada em x=8032, a cena muda para dentro com um fade de 0,3 segundo. A câmera permanece fixa no interior durante a luta. O guardião só começa a agir após a entrada, e a simulação fica suspensa durante o fade. A barra de vida e os avisos acompanham o idioma escolhido.

O interior tem abóbadas nervuradas, pilares em primeiro plano, rosácea, quatro vitrais, altar com cruz dourada sobre o
tabernáculo, cadeiras de coro entalhadas, livros, púlpito, lanternas, velas animadas e piso de pedra com tapete bordado.
Durante a luta, a capela tem iluminação escura e fria. Ao derrotar o guardião, a luz muda gradualmente para um tom
quente e alegre, e a cruz passa a brilhar. A mudança acompanha a sequência dos anjos, congela na pausa e reinicia no
respawn. As camadas preservam os pixels e deixam a faixa de combate livre.

As portas de entrada e saída bloqueiam tanto o templário quanto as investidas/saltos do chefe; a esquiva continua atravessando inimigos, mas não as barreiras da arena. Flechas vindas da estrada são bloqueadas. Derrotar o guardião inicia uma sequência de 4,6 segundos: dois anjos descem batendo asas, pairam junto aos portais e abrem as duas portas em luz dourada, antes de ascender. O movimento/combate fica suspenso durante a sequência. As portas começam a abrir em 1,8 segundo e deixam de bloquear a passagem ao terminar, em 3,2 segundos; o controle retorna após a ascensão. A pausa congela anjos e portas, e o respawn limpa o evento. A sequência toca uma vez por derrota e usa o mesmo relógio para animação e colisões. `tools/generate_angels.py` gera somente o atlas compartilhado `assets/sprites/angels.png`; as capturas ficam em `angel-frames/`. O interior permanece visível até sair fisicamente; retornar pela entrada permite rever a fachada e reentrar sem reiniciar a luta. Morrer e retornar ao santuário reinicia o encontro, a vista exterior e a porta de entrada.

No Tiled, o retângulo obrigatório `arena` na camada `entities` define a área: sua borda direita coincide com o portão de saída. O portão de entrada é derivado da borda esquerda. As escadas finais ficam antes da arena, sem sentinelas ou arqueiros no espaço de combate. As capturas `28-chapel-*.png` mostram fachada, transição, interior e respawn. O manifesto exige texturas e luzes de interior por fase; o guia de novas fases documenta o formato. `tools/generate_chapel.py` regenera somente `assets/levels/exiles-road/chapel/`.
O ícone do aplicativo e da janela usa o elmo e o escudo do templário, em pixel art. No macOS, `MacDockIcon` aplica a imagem diretamente ao Dock durante a criação da janela, inclusive ao executar pelo Gradle ou pela IDE. Os arquivos PNG, ICNS (macOS) e ICO (Windows) ficam em `assets/branding/`. Para reproduzir somente o ícone, execute `tools/generate_icon.py` com Pillow instalado.

## Desenvolvimento de novas fases

Ao pedir novas fases, bosses ou mecânicas, referencie
as [instruções para o modelo](docs/GUIA_DE_PROMPTS_E_NOVAS_FASES.md). O arquivo determina como o modelo deve planejar,
implementar, validar e entregar essas mudanças.

O registro da campanha, os limites do editor, o ciclo de vida dos recursos e o roteiro de validação estão em [docs/ADICIONAR_FASES.md](docs/ADICIONAR_FASES.md). `LevelDefinition` descreve mapa, regiões, decorações e chaves de texto; `Journey` controla a ordem, e `LevelResources` carrega o mesmo mapa para renderização e simulação. O smoke test verifica também duas fases, troca de mapa, estado limpo e reinício da campanha.

O antigo sprite `assets/sprites/bread-of-life.png` foi preservado para uso futuro e não participa da cura atual. O pickup e o HUD usam `assets/sprites/bandages.png`; `tools/generate_bandages.py` regenera somente esse novo sprite. As bandagens mantêm a cura de até dois corações e a coleta apenas quando falta vida.

## Abertura animada

A abertura usa camadas e sprites em uma grade de 480 × 200, ampliada em escala inteira: tecido das bandeiras ao vento, velas e brasas, respiração dos cavaleiros, bênção/fala de Urbano II, cavaleiros ajoelhando e uma coluna em marcha. O céu, as muralhas e o primeiro plano da marcha têm velocidades distintas. A segunda cena aproxima o enquadramento com zoom inteiro; as bordas são recortadas ao painel, sem suavizar os pixels.

Enter, Espaço ou clique avançam uma cena; Esc pula para o jogo. Cada cena continua animada enquanto o jogador lê. Perder
foco congela a abertura e sua música, que retomam ao voltar à janela. Na partida, a pausa continua exigindo Esc. A
abertura usa a trilha do menu e textos PT/EN.

`IntroScreen` controla cenas, relógio, legendas e entrada; `presentation/IntroAnimation` desenha a coreografia. Os recursos ficam em `assets/sprites/intro/` e são compartilhados por `GameAssets`. As ilustrações antigas `council.png` e `march.png` continuam preservadas, mas a abertura usa as novas camadas.

O smoke test compara pixels entre momentos de cada cena, verifica congelamento/retomada e os controles de avanço/pulo. Capturas ficam em `lwjgl3/build/screenshots/26-animated-intro-*.png`; a sequência `intro-frames/` pode ser montada em um preview animado.

`tools/generate_sanctuary.py` regenera somente o sprite do santuário (`assets/sprites/sanctuary.png`), usando a paleta pixel art existente. O desenho não altera a área de ativação ou as regras de checkpoint.

## Patrulha e perseguição

Os 26 inimigos comuns estão distribuídos em 7/6/7/6 por região; 19 começam sobre plataformas, com o corpo inteiro
apoiado no piso. O carregador rejeita spawns sem apoio. A posição dos inimigos fica no TMX e no gerador do mapa
original.

A percepção inicial usa 230 pixels horizontais para sentinelas e 280 para arqueiros, com visão voltada para a direção da patrulha e limitada em altura/ângulo. Terreno e portões físicos bloqueiam a visão entre os olhos do inimigo e do templário. Entrar na tela não ativa a perseguição. Ao avistar o jogador, o inimigo para por 0,4 segundo com um pequeno `?` acima da cabeça; só então persegue ou prepara o ataque. Um encontro muito próximo (até 80 pixels horizontais e 60 verticais), com visão livre, provoca reação imediata dos dois lados. Receber dano também alerta imediatamente, interrompendo o período de calma. Um vislumbre interrompido reinicia o tempo de observação.

Depois de alertado, o inimigo acompanha o alvo visível até 420 pixels e mantém por dois segundos a última posição vista quando a visão é bloqueada; não atualiza o destino através de paredes. Preparações de golpes já iniciadas permanecem comprometidas. Os testes de percepção cobrem distância, direção, pausa de observação, paredes, tetos de plataformas, memória e reação a dano. `30-enemy-notice.png` mostra o estado de observação no jogo.

O limite invisível de 230 pixels em torno do spawn foi removido. A perseguição considera as superfícies transitáveis e
arcos de salto livres de obstáculos, incluindo uma aproximação pelo lado da plataforma; não tenta saltar através do
teto. A perseguição anda a 150 pixels/s; escudeiros usam 55% dessa velocidade e brutos, 65%. A patrulha normal fica
perto do ponto de início e evita cair das bordas. Arqueiros procuram uma rota quando o terreno bloqueia a linha de
disparo.

Inimigos comuns desistem após 12 segundos de perseguição, dois segundos sem detectar o alvo ou três segundos sem deslocamento durante a perseguição. Uma preparação/golpe já iniciado termina antes de desistir. Eles passam a patrulhar perto de onde pararam e esperam quatro segundos antes de detectar novamente o templário. O guardião mantém as regras próprias da arena. Paredes, bordas do mapa e portões físicos continuam sólidos.

Os testes verificam saltos dos dois lados, sequência de plataformas, plataformas de um tile, abandono de alvos inalcançáveis, perseguição além do antigo limite e patrulha segura. O smoke test captura a subida de uma sentinela até o templário em `27-platform-pursuit-*.png` e percorre a fase redistribuída até a vitória.

Lanceiros têm três pontos de vida e uma estocada horizontal de 104 pixels à altura da cintura: preparação de 0,60
segundo, janela ativa de 0,14 segundo e recuperação de 0,90 segundo. A direção permanece fixa durante o golpe. O escudo
bloqueia a estocada; aparar interrompe o lanceiro, e saltar sobre a arma permite um golpe aéreo para baixo. O primeiro
aparece sozinho no trecho plano após o santuário (x=4640); o segundo fica no terraço depois do santuário, perto de uma
sentinela (x=5980, y=128). Seus sprites são gerados por `tools/generate_spearman.py`; depois, `tools/generate_deaths.py`
atualiza a animação de morte.

## Escudeiro, bruto e lançador de óleo

A fase tem três escudeiros, dois brutos e dois lançadores de óleo, além de doze sentinelas, quatro arqueiros, três
lanceiros e o guardião: 27 inimigos ao todo.

- **Escudeiro:** quatro pontos de vida, avanço lento e escudo frontal enquanto está no chão. Mantém a guarda na
  preparação, baixa o escudo para atacar e o ergue durante o primeiro quarto da recuperação; só então volta a bloquear.
  Uma aparada deixa o escudeiro atordoado por 0,45 s e abre sua guarda. Golpes pelas costas e ataques aéreos para baixo
  passam pelo escudo. A espada prepara por 0,65 s e recupera por 0,95 s. O primeiro fica em x=980; o segundo, em x=4910,
  protege um arqueiro no mesmo terraço.
- **Bruto com maça:** cinco pontos de vida e movimento lento. Prepara o golpe por 0,95 s, com aviso vermelho; o ataque
  atravessa o bloqueio comum, mas admite aparada e esquiva. A direção permanece fixa e a recuperação de 1,25 s permite
  contra-atacar. Aparece em x=2900 e x=7600.
- **Lançador de óleo:** dois pontos de vida, busca distância e prepara o arremesso por 0,85 s. A marca no chão indica a
  posição escolhida no começo da preparação. O pote segue um arco e respeita terreno e portões; impactos laterais e no
  teto o quebram. Ao cair num piso, deixa fogo por três segundos, limitado às bordas e paredes. O fogo ignora o escudo e
  respeita a invulnerabilidade; o pote em voo pode ser bloqueado ou aparado. Aparece em x=3740 e x=7888. Morte do
  jogador, entrada na arena e derrota do chefe limpam potes e fogo.

`tools/generate_enemy_variants.py` gera os corpos e equipamentos; `tools/generate_deaths.py` atualiza as mortes.
`EnemyVariantsTest` verifica combate, trajetória, cobertura, fogo e transições. O smoke test OpenGL captura os três
ciclos de ataque e o pote/fogo em `lwjgl3/build/screenshots/36-*.png`.

## Animações e efeitos dos especialistas

Os sete tipos de inimigo usam 168 poses de ataque em `sprites/enemy-attacks.png`, vinculadas à preparação, fase ativa e
recuperação. Sentinela, escudeiro, arqueiro e guardião também têm tronco e braços próprios por fase, sem caminhar no
lugar durante o golpe. Espadas descrevem arcos com rastros discretos, o escudo baixa e volta à guarda, e o arqueiro puxa
a corda antes de soltar a flecha. A maça acompanha a mão, tem rastro de movimento, brilho de antecipação, detritos no
impacto e tremor curto de câmera. A lança recua, avança e retorna com um rastro curto. O pote gira com brasas em voo,
quebra em fragmentos e produz uma ignição; as chamas têm camadas, brasas ascendentes e desaparecimento gradual.

Nove sons próprios acompanham elevação e golpe da maça, impacto, recuo e estocada da lança, preparação e arremesso do
pote, cerâmica e fogo. O crepitar dura os três segundos do fogo. Os sons são disparados uma vez por transição da
simulação; pausa, mute, morte, respawn e entrada na arena controlam sua reprodução. As áreas de dano, durações dos
ataques e regras de defesa foram preservadas. Os detritos são somente visuais, expiram em 0,45 segundo e congelam com
pausa e hit stop.

`python3 tools/generate_enemy_attack_assets.py` reproduz somente esse atlas e os nove WAVs, usando os sprites originais
como base e Pillow para as poses. O gerador completo chama esse script depois dos corpos. `EnemyEffectsTest` cobre
emissão única, cancelamento, ignição, pausa e limpeza; o teste gráfico toca as nove pistas e grava `37-*-impact.png` e
`specialist-frames/`.

Ao cair sobre um inimigo vivo, o templário desliza gradualmente pela lateral, a 150 pixels/s, e continua descendo até o
terreno. A velocidade de queda durante o contato fica limitada a 100 pixels/s; a gravidade normal retoma assim que ele
sai do corpo. Corpos não concedem apoio nem reiniciam o salto; um golpe aéreo para baixo bem-sucedido mantém seu impulso
de combate.

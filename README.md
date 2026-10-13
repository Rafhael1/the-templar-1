# O Templário

Jogo simples de ação e plataforma 2D em **Java 21 + libGDX**, com um cavaleiro templário como protagonista. Esta é a versão 1.0 do protótipo, com uma fase completa, arte própria e ambientação fictícia nas Cruzadas.

## Jogar no Mac deste computador

Abra **`Jogar.command`** na raiz do projeto. Ele abre o aplicativo já construído em:

```text
lwjgl3/build/app-image/O Templário.app
```

O aplicativo contém seu próprio runtime Java. Não precisa de Gradle, rede ou de Java instalado para jogar. O pacote presente foi construído e testado em macOS ARM64 (Apple Silicon).

| Controle | Ação |
|---|---|
| Enter | Confirmar opção do menu / nova jornada após vencer |
| A/D ou setas | Mover |
| Espaço, W ou seta para cima | Saltar |
| J ou Z | Espada |
| S ou seta para baixo + J/Z no ar | Golpe para baixo |
| L ou X (segurar) | Bloquear com o escudo |
| Shift ou K | Esquiva |
| Esc | Pausar / continuar |
| M | Ativar / desativar som |
| R ou Enter após morrer | Retornar ao início ou ao santuário |

O menu inicial oferece **Nova jornada**, **Instruções**, **Opções** (som, resolução Full HD/720p e idioma PT/EN) e **Sair**. Navegue com W/S ou cima/baixo e confirme com Enter; Esc volta das instruções e opções. Os controles ficam na página de instruções. Durante a partida, o HUD mostra cinco corações, escudo, recarga da esquiva e contagem de Pão da Vida; não há barra inferior.

Cada golpe exige um novo pressionamento. O golpe aéreo para baixo mantém sua direção até terminar e deixa de causar dano ao aterrissar. O escudo bloqueia golpes pela frente, inclusive no ar, e reduz a velocidade; não permite atacar ou esquivar enquanto estiver levantado. Golpes pelas costas causam dano. O aviso sobre o inimigo indica a preparação do ataque. A esquiva oferece invulnerabilidade breve e tem recarga. Perder o foco da janela pausa a partida; pressione Esc para continuar.

## Objetivo e conteúdo

Abra a passagem para os peregrinos. Atravesse a estrada, enfrente onze sentinelas e cinco arqueiros, ative o santuário e derrote o guardião para abrir o portão final. O **Pão da Vida**, representado por uma hóstia redonda com uma pequena cruz central, aparece em dez pontos da fase e recupera até dois pontos de vida por coleta. Cada unidade permanece coletada após morrer.

O santuário restaura a vida e define onde o personagem retorna após morrer. Os inimigos reaparecem. Checkpoint e Pão da Vida persistem durante a sessão; não há salvamento em disco. Enter após vencer inicia uma sessão nova.

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

## Editar a fase

Abra `assets/maps/the-cruzader.tiled-project` no **Tiled** e depois `exiles-road.tmx`.

- `terrain`: desenho da fase com tiles de 32 × 32.
- `collisions`: retângulos sólidos; desenho e colisões precisam permanecer alinhados.
- `entities`: pontos de spawn/checkpoint/inimigos e retângulos de relíquia/portão/saída.

As camadas de objetos estão ocultas por padrão para não aparecerem na renderização do mapa. Ative o olho dessas camadas no editor quando quiser modificá-las.

Classes de objeto:

| Classe | Forma | Quantidade |
|---|---|---|
| `spawn` | Ponto: canto inferior esquerdo do jogador | Exatamente 1 |
| `checkpoint` | Ponto no piso | Exatamente 1 |
| `sentinel` | Ponto no piso | Zero ou mais |
| `archer` | Ponto no piso | Zero ou mais |
| `guardian` | Ponto no piso | Exatamente 1 |
| `relic` | Retângulo da área de coleta de Pão da Vida | Uma ou mais |
| `gate` | Retângulo sólido enquanto o guardião vive | Exatamente 1 |
| `exit` | Retângulo que conclui a fase | Exatamente 1 |

Use mapas ortogonais finitos, colisões retangulares sem rotação e as classes declaradas no projeto Tiled. O carregador lê o atributo `type` exportado pelo Tiled. Tipos desconhecidos e entidades obrigatórias ausentes/duplicadas interrompem o carregamento com erro.

Os inimigos detectam o jogador a maior distância, mantêm o alerta por dois segundos ao perdê-lo de vista e acompanham saltos pelo chão. Só iniciam golpes quando a arma alcança o jogador; a direção fica comprometida durante o ataque. Sentinelas preparam golpes de espada por 450 ms e recuperam por 650 ms; o guardião prepara sua espada por 550 ms e recupera por 650 ms. A janela ativa permanece em 160 ms.

Os inimigos colidem com o terreno e o portão, sofrem gravidade e sobem degraus de até 32 pixels quando há espaço. Paredes maiores bloqueiam o movimento; sentinelas e arqueiros não saltam. O jogador colide com chão, laterais e teto; as plataformas são sólidas. A fase tem **280 tiles / 8960 pixels** de largura, 23 plataformas, duas escadarias com patamares e descidas, onze sentinelas, cinco arqueiros e um guardião. O Pão da Vida aparece em plataformas e pontos de descanso; o santuário está no centro da jornada, e a arena final e o portão ocupam o extremo direito.

Arqueiros preparam o disparo por 650 ms e apontam para a posição do jogador no início da preparação. Flechas atingem o terreno e o portão, podem ser bloqueadas pela frente e são removidas ao acertar ou após três segundos. O respawn limpa as flechas em trânsito.

O guardião tem 12 pontos de vida, armadura própria e escudo. Alterna bloqueio frontal, investida, salto e espada; preparação e recuperação deixam oportunidades de esquiva e contra-ataque. Ataques por trás e golpes aéreos para baixo atravessam a guarda. Ao chegar à metade da vida, as preparações ficam mais curtas e a investida mais rápida.

## Testes e verificação visual

```sh
./gradlew test
./gradlew :lwjgl3:smokeTest
./gradlew :lwjgl3:performanceTest
```

`test` roda sem janela. `smokeTest` precisa de uma sessão desktop e abre uma janela real por alguns segundos. Usa o mesmo renderizador, telas, assets e mapeamento de entrada do jogo. Capturas ficam em `lwjgl3/build/screenshots/`.

Verificação: **91 testes JUnit passaram** usando o mapa atual, incluindo a escada com subida, patamar e descida. Objetos de colisão poligonais ou rotacionados são rejeitados com uma mensagem explícita. As entidades usam o atributo `type` exportado pelo Tiled. O teste gráfico captura a rota do Pão da Vida, o santuário, a escada, a arena e a vitória; um teste de simulação coleta o Pão da Vida por saltos e retorna ao piso.

## Estrutura

```text
assets/      sprites, cenários, mapa Tiled e áudio
core/        regras, telas libGDX, controles e apresentação
lwjgl3/      launcher desktop, teste gráfico e empacotamento
tools/       gerador dos assets originais e suas dependências
/docs/       arquitetura e decisões técnicas
```

Leia `docs/ARQUITETURA.md` para o fluxo de atualização e a divisão das responsabilidades. Assets gerados estão versionáveis e prontos para uso. Para reproduzir os arquivos originais, use `tools/generate_assets.py` em um ambiente Python com `tools/requirements.txt` instalado. **Esse script regrava os assets e o mapa original**, portanto não o execute sobre uma fase já editada que deseja preservar.

O escopo permanece pequeno: uma fase, sem inventário, árvore de habilidades, controle/gamepad, campanha completa ou persistência em disco.


A nova jornada começa com uma abertura ilustrada em três cenas sobre o chamado de Urbano II, com ENTER/ESPAÇO/clique para avançar e ESC para pular. O protagonista templário é uma licença de ficção histórica; a ordem surgiu depois da Primeira Cruzada. O diálogo é original, não uma citação histórica.

A fase representa uma marcha condensada por acampamento, Anatólia, Antioquia e muralhas de Jerusalém. Cada região tem um cenário próprio, com nuvens, bandeiras ao vento, suprimentos e fumaça. Há arqueiros em plataformas e dez Pães da Vida; eles só são consumidos quando falta vida. O santuário fica em x=4480, no centro do percurso.

A trilha instrumental original dura 48 segundos e combina cordas sintetizadas, melodia, bordão, percussão e vento. Respeita pausa e a opção de som. `tools/generate_atmosphere.py` reproduz a trilha e as novas ilustrações sem regravar o mapa.

Contexto histórico: [Urbano II e o chamado em Clermont](https://www.metmuseum.org/de/essays/the-crusades-1095-1291), [fundação posterior dos Templários](https://en.wikisource.org/wiki/1911_Encyclop%C3%A6dia_Britannica/Templars).


A janela inicia com framebuffer **1920 × 1080**, compensando a escala Retina no macOS. Opções alterna Full HD e 1280 × 720. O mundo continua em 960 × 540, com pixels em escala inteira no Full HD; vsync controla a apresentação sem um limite de FPS concorrente. Jogador, inimigos e câmera usam interpolação entre passos de simulação.

As mortes usam oito poses por personagem: impacto, queda, arma solta e repouso. Corpos de inimigos desaparecem gradualmente; a tela de morte do templário aparece após um segundo. A pausa congela a animação, e o respawn reinicia os estados visuais. `tools/generate_deaths.py` reproduz o atlas usando os sprites existentes.

O teste gráfico também compara pixels do chão com o tileset nos quatro cenários, verifica resolução Full HD e registra as duas animações de morte. O renderizador restaura a cor opaca antes do tilemap: efeitos de fumaça não podem alterar a opacidade do chão e das plataformas.

Medição local com `performanceTest`: 300 frames após aquecimento, incluindo simulação, HUD e conclusão da GPU, sem a espera de vsync. Em 1920 × 1080, mediana de **1,308 ms → 0,676 ms**, percentil 95 de **3,521 ms → 1,466 ms**. Isso mede custo por frame nesta máquina, não latência de entrada nem FPS sustentado em outras máquinas.


Textos usam um atlas DejaVu Sans de 96 pixels por glifo, com mipmaps e filtragem próprios para a interface. Os sprites mantêm nearest-neighbor. `tools/generate_font.py` reproduz o atlas usando o TTF e a licença incluídos em `assets/ui/`. A nota amarela de ficção foi removida da abertura.

Templário e inimigos vivos colidem pelos dois lados ao caminhar. Durante a esquiva, o templário atravessa os corpos sem empurrá-los; paredes e portão continuam sólidos. É possível saltar por cima e aterrissar sobre um inimigo. A separação respeita paredes e portão; corpos mortos deixam a passagem livre. Encostar normalmente não causa dano, e a investida do chefe mantém sua regra de um golpe por movimento.


As regiões misturam seus cenários ao longo de 640 pixels de estrada, com entrada e saída suaves. O fundo não troca inteiro ao cruzar um limite; o paralaxe permanece contínuo. Anúncios de capítulo aparecem uma vez por região e entram/saem gradualmente. As capturas `20-transition-*` registram as três passagens, verificando que o chão continua opaco durante a mistura.


## Idiomas PT / EN

Em **Opções → Idioma**, alterne português e inglês. A página muda imediatamente e a preferência fica salva para as próximas aberturas. O nome **O Templário** permanece nos dois idiomas. Menus, instruções, cutscene, objetivos, avisos e telas de pausa/morte/vitória consultam os mesmos catálogos.

- `assets/i18n/messages_pt.properties`: textos em português.
- `assets/i18n/messages_en.properties`: textos em inglês.
- `core/src/main/java/br/com/thecruzader/i18n/GameText.java`: carregamento e consulta.
- `core/src/main/java/br/com/thecruzader/i18n/Language.java`: idiomas disponíveis.

Os arquivos usam UTF-8 e chaves agrupadas em `game`, `menu`, `instructions`, `options`, `intro`, `hud`, `notice` e `overlay`. Para adicionar um texto, crie a mesma chave nos dois catálogos e consulte `assets.texts.text("grupo.chave")`; use `format` com `%s` para valores dinâmicos e `\n` para quebras de linha. Os 63 textos de cada catálogo são carregados uma vez; trocar o idioma apenas seleciona o catálogo em memória. Chaves diferentes entre idiomas, textos vazios e consultas a chaves inexistentes geram erro, sem tradução substituta.

Os testes verificam acentos, formatação, cobertura da fonte, ausência de traduções e troca sem releitura dos arquivos. O teste desktop muda PT → EN → PT nas opções, confirma a preferência salva e captura as telas em ambos os idiomas, incluindo avisos de pausa, morte e vitória.


## Arena do chefe

A área final tem 736 pixels de piso livre e cabe inteira na câmera de 960 × 540. Ao atravessar a entrada em x=8032, o portão de trás desce, a câmera se posiciona suavemente no centro e permanece fixa durante a luta. O guardião só começa a agir após a entrada. A barra de vida e os avisos acompanham o idioma escolhido.

O portão de entrada e a saída bloqueiam tanto o templário quanto as investidas/saltos do chefe; a esquiva continua atravessando inimigos, mas não as barreiras da arena. Flechas vindas da estrada são bloqueadas. Derrotar o guardião levanta os dois portões e libera a câmera. Morrer e retornar ao santuário reinicia o encontro e reabre a entrada.

No Tiled, o retângulo obrigatório `arena` na camada `entities` define a área: sua borda direita coincide com o portão de saída. O portão de entrada é derivado da borda esquerda. As escadas finais ficam antes da arena, sem sentinelas ou arqueiros no espaço de combate. As capturas `24-sealed-arena.png` e `24-english-arena.png` mostram a área fechada em PT/EN.
O ícone do aplicativo e da janela usa o elmo e o escudo do templário, em pixel art. No macOS, `MacDockIcon` aplica a imagem diretamente ao Dock durante a criação da janela, inclusive ao executar pelo Gradle ou pela IDE. Os arquivos PNG, ICNS (macOS) e ICO (Windows) ficam em `assets/branding/`. Para reproduzir somente o ícone, execute `tools/generate_icon.py` com Pillow instalado.

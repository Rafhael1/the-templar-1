# The Cruzader

Jogo simples de ação e plataforma 2D em **Java 21 + libGDX**, com um cavaleiro templário como protagonista. Esta é a versão 1.0 do protótipo, com uma fase completa, arte própria e ambientação fictícia nas Cruzadas.

## Jogar no Mac deste computador

Abra **`Jogar.command`** na raiz do projeto. Ele abre o aplicativo já construído em:

```text
lwjgl3/build/app-image/The Cruzader.app
```

O aplicativo contém seu próprio runtime Java. Não precisa de Gradle, rede ou de Java instalado para jogar. O pacote presente foi construído e testado em macOS ARM64 (Apple Silicon).

| Controle | Ação |
|---|---|
| Enter | Iniciar / nova jornada após vencer |
| A/D ou setas | Mover |
| Espaço, W ou seta para cima | Saltar |
| J ou Z | Espada |
| S ou seta para baixo + J/Z no ar | Golpe para baixo |
| L ou X (segurar) | Bloquear com o escudo |
| Shift ou K | Esquiva |
| Esc | Pausar / continuar |
| M | Ativar / desativar som |
| R ou Enter após morrer | Retornar ao início ou ao santuário |

Cada golpe exige um novo pressionamento. O golpe aéreo para baixo mantém sua direção até terminar e deixa de causar dano ao aterrissar. O escudo bloqueia golpes pela frente, inclusive no ar, e reduz a velocidade; não permite atacar ou esquivar enquanto estiver levantado. Golpes pelas costas causam dano. O aviso sobre o inimigo indica a preparação do ataque. A esquiva oferece invulnerabilidade breve e tem recarga. Perder o foco da janela pausa a partida; pressione Esc para continuar.

## Objetivo e conteúdo

Abra a passagem para os peregrinos. Atravesse a estrada, enfrente quatro sentinelas, ative o santuário e derrote o guardião para abrir o portão final. A relíquia opcional fica sobre as plataformas e recupera até dois pontos de vida.

O santuário restaura a vida e define onde o personagem retorna após morrer. Os inimigos reaparecem. Checkpoint e relíquia persistem durante a sessão; não há salvamento em disco. Enter após vencer inicia uma sessão nova.

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
| `guardian` | Ponto no piso | Exatamente 1 |
| `relic` | Retângulo da área de coleta | Exatamente 1 |
| `gate` | Retângulo sólido enquanto o guardião vive | Exatamente 1 |
| `exit` | Retângulo que conclui a fase | Exatamente 1 |

Use mapas ortogonais finitos, colisões retangulares sem rotação e as classes declaradas no projeto Tiled. O carregador lê o atributo `type` exportado pelo Tiled. Tipos desconhecidos e entidades obrigatórias ausentes/duplicadas interrompem o carregamento com erro.

Os inimigos detectam o jogador a maior distância, mantêm o alerta por dois segundos ao perdê-lo de vista e acompanham saltos pelo chão. Só iniciam golpes quando a arma alcança o jogador; a direção fica comprometida durante o ataque. Sentinelas e guardião perseguem mais rápido e têm recuperação mais curta.

Os inimigos colidem com o terreno e o portão, sofrem gravidade e sobem degraus de até 32 pixels quando há espaço. Paredes maiores bloqueiam o movimento; a IA não salta. O jogador colide com chão, laterais e teto; as plataformas são sólidas. A fase tem 3200 pixels de largura, uma rota elevada opcional para a relíquia, um santuário central e uma escada larga que desemboca na arena do guardião.

## Testes e verificação visual

```sh
./gradlew test
./gradlew :lwjgl3:smokeTest
```

`test` roda sem janela. `smokeTest` precisa de uma sessão desktop e abre uma janela real por alguns segundos. Usa o mesmo renderizador, telas, assets e mapeamento de entrada do jogo. Capturas ficam em `lwjgl3/build/screenshots/`.

Verificação: **45 testes JUnit passaram** usando o mapa atual, incluindo a escada com subida, patamar e descida. Objetos de colisão poligonais ou rotacionados são rejeitados com uma mensagem explícita. As entidades usam o atributo `type` exportado pelo Tiled. O teste gráfico captura a rota da relíquia, o santuário, a escada, a arena e a vitória; um teste de simulação coleta a relíquia por saltos e retorna ao piso.

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

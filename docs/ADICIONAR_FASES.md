# Como adicionar fases

As [instruções para o modelo](GUIA_DE_PROMPTS_E_NOVAS_FASES.md) orientam a execução de pedidos de fases, bosses e
mecânicas. Este documento detalha o contrato técnico dos mapas.

O jogo mantém regras compartilhadas e conteúdo de fase separado. Novas fases são registradas em
`assets/levels/campaign.xml` na ordem em que devem ser jogadas.

O conteúdo exclusivo de cada fase fica em `assets/levels/<id>/`: `maps/` para TMX, `scenery/` para fundos e `chapel/` para fachada/interior/portas. `assets/levels/campaign.xml` registra os caminhos completos. Novas imagens e sons exclusivos devem permanecer dentro da pasta da fase, usando subpastas com nomes claros. Recursos reutilizados (personagens, bandagens, santuário, céu, áudio comum, fontes, UI, tiles e classes do Tiled) permanecem nas pastas compartilhadas. A fase de teste segue a mesma organização dentro de `core/src/test/resources/levels/test-road/`, sem duplicar as texturas de produção que reutiliza.

O tileset compartilhado `assets/maps/stone.png` é referenciado por `../../../maps/stone.png` nos TMX dentro de `levels/<id>/maps/`. O projeto Tiled compartilhado inclui `../levels` na árvore de arquivos. Atualize também os geradores se adicionar conteúdo gerado: eles devem escrever diretamente na pasta da fase.

## 1. Autorar o mapa no Tiled

Crie um TMX em `assets/levels/<id>/maps/` e use as classes de `the-cruzader.tiled-project`. Tilesets/imagens devem ter caminhos relativos válidos a partir do TMX. O mapa gráfico e as colisões da simulação são carregados do mesmo arquivo.

O contrato atual é intencionalmente horizontal:

- Mapa ortogonal finito, largura mínima de 960 e altura mínima de 540 pixels.
- Câmera fixa em Y, mostrando os primeiros 540 pixels do mundo (Y cresce para cima na simulação). Objetos/colisões devem permanecer nesse espaço visível.
- Camadas na raiz, sem grupos aninhados, deslocamentos ou paralaxe do Tiled. A renderização dos cenários usa seu próprio paralaxe.
- Uma camada `collisions` com retângulos sólidos sem rotação; uma camada `entities` com as classes abaixo. Desenho do terreno e colisões devem coincidir.
- Spawn/checkpoint/inimigos são pontos sem rotação. Áreas de coleta, arena, portão e saída são retângulos sem rotação.

| Classe                                                                    | Quantidade | Regra                                                                     |
|---------------------------------------------------------------------------|------------|---------------------------------------------------------------------------|
| `spawn`                                                                   | 1          | Corpo do jogador livre de terreno e antes da entrada da arena             |
| `checkpoint`                                                              | 1          | Corpo do jogador livre de terreno e antes da entrada da arena             |
| `sentinel`, `archer`, `spearman`, `shield_bearer`, `brute`, `oil_thrower` | 0 ou mais  | Corpos livres de terreno e antes da entrada da arena                      |
| `guardian`, `harbor_captain`, `relief_duelist` ou `mounted_commander`     | 1 no total | Corpo inteiro dentro da arena, sobre seu piso                             |
| `bandage`                                                                 | 1 ou mais  | Área livre de terreno/portões; representa bandagens                       |
| `arena`                                                                   | 1          | Piso plano e livre; largura entre 70 e 896 pixels; altura mínima de 240   |
| `gate`                                                                    | 1          | X coincide com a borda direita da arena; mesmo piso; altura mínima de 176 |
| `exit`                                                                    | 1          | Depois do portão, no mesmo piso e livre de terreno                        |

O portão de entrada tem 32 pixels de largura e fica imediatamente antes da arena. Um retângulo de piso deve sustentar
continuamente a arena até o fim da saída. A arena e as duas barreiras devem caber na câmera centrada: metade da largura
da arena mais a maior largura de portão deve ser no máximo 480. Assim, um portão de saída de 40 pixels limita a arena a
880 pixels. Portões não podem sobrepor terreno estático. O validador usa corpos reais: jogador 28 × 52, inimigos comuns
30 × 48, guardião e capitão 42 × 68 duelista 38 × 62 e comandante montado 88 × 76.

O carregamento rejeita formatos e posições incompatíveis, mas não prova que todos os saltos/coletas sejam alcançáveis. Ensaie a rota. Inimigos sobem degraus de até 32 pixels e saltam entre superfícies por trajetórias livres de colisão, com cerca de 110 pixels de altura máxima. Plataformas são sólidas; spawns devem ter apoio para o corpo inteiro. Fases verticais, mais checkpoints, chefes diferentes ou objetivos sem chefe exigem mudanças explícitas nesse contrato.

## 2. Registrar o conteúdo

Acrescente um `<level>` ao manifesto. IDs devem ser únicos; Os atributos de identificação, geometria e texto são
obrigatórios. Caminhos são internos aos recursos de `assets/` e não dependem do diretório de execução do aplicativo.

```xml
<level reward="GOLDEN_STRIKE" id="new-road" map="levels/new-road/maps/new-road.tmx" blendWidth="200"
       chapter="level.newRoad.chapter" title="level.newRoad.title"
       story="level.newRoad.story" travelObjective="level.newRoad.travel"
       arenaNotice="level.newRoad.arena" bossObjective="level.newRoad.fight"
       bossName="level.newRoad.boss" exitObjective="level.newRoad.exit"
       completionTitle="level.newRoad.complete" completionText="level.newRoad.victory">
    <region start="0" texture="levels/new-road/scenery/region-0.png"
            announcement="level.newRoad.region.0" />
    <decoration kind="TORCH" x="230" y="64" />
    <decoration kind="FLAG" x="480" y="64" color="743844" />
    <interior daylight="false" exterior="levels/new-road/chapel/exterior.png"
              background="levels/new-road/chapel/interior.png"
              foreground="levels/new-road/chapel/foreground.png"
              door="levels/new-road/chapel/door.png" doorFrame="levels/new-road/chapel/door-frame.png">
        <light x="184" y="176" />
        <light x="356" y="176" />
        <light x="604" y="176" />
        <light x="776" y="176" />
    </interior>
</level>
```

`reward` registra um desbloqueio de habilidade. Omita o atributo quando o chefe não conceder uma habilidade. As
recompensas são
`GOLDEN_STRIKE` e `BOW`. `Journey` mantém os desbloqueios e equipa automaticamente todos os poderes
ao iniciar cada fase, na ordem Golpe Dourado (Q) e Arco e flecha (E) quando ambos estão disponíveis. O arco entra no
espaço livre ao derrotar o chefe que o concede. As três flechas
são repostas no início da próxima fase, enquanto as cargas do Golpe Dourado também são repostas no respawn.

Uma fase pode incluir `<conversation x="424" y="64" text="level.newRoad.oath" sprite="levels/new-road/envoy.png" />`.
Posicione os personagens sobre o piso, antes da arena. A tecla **F** abre a conversa quando o jogador se aproxima.
O atributo `text` é o prefixo dos textos `.title` e `.body`. Use `required="true"` para exigir a confirmação
da conversa com F antes de liberar a entrada da arena. As confirmações persistem no respawn.
`tools/generate_constantinople.py` e `tools/generate_nicaea.py` recriam o TMX e as imagens de cada fase.

A primeira região começa em zero. Os limites seguintes ficam em ordem, dentro da largura do mapa e separados por pelo menos `blendWidth`, para que as transições não se sobreponham. Use texturas de cenário com o formato visual 960 × 540. Uma textura pode servir a várias regiões da mesma fase e é carregada uma vez por bundle.

As decorações são opcionais: `CRATES`, `FLAG`, `SIGN`, `BRAZIER`, `TORCH`, `ARCH`, `SHRINE`, `GRAVES`, `DEAD_TREE`,
`WAGON` e `PENNANT`. X/Y são a base no mundo com Y crescente para cima; ajuste ao piso local. `FLAG` exige cor RGB com
seis dígitos hexadecimais. Não há posicionamento automático baseado no comprimento da estrada. A queda visual dos corpos
consulta os pisos do mapa, sem assumir Y=64.

Cada fase exige um único `<interior>` com cinco texturas. A fachada tem 960 × 448 pixels: entrada no centro X=112, módulo central repetível entre X=256 e 736, saída no centro X=848. O renderizador ancora as portas aos portões físicos e recorta o módulo central em pixels inteiros para ajustar a largura. Fundo e primeiro plano do interior têm 960 × 540; o primeiro plano usa transparência. A porta tem 64 × 256 (duas folhas de 32 pixels) e o arco tem 96 × 288. Dimensões incorretas interrompem o carregamento.

O interior fica centrado na arena; seu piso está 64 pixels acima da base da imagem. A câmera acompanha a altura do piso do mapa. Desenhe arquitetura e mobiliário atrás da faixa de combate, sem criar obstáculos visuais que aparentem colisões inexistentes. `<light>` é opcional e posiciona velas animadas dentro da imagem, em coordenadas locais X/Y com Y crescente para cima; os pontos precisam estar dentro de 960 × 540.

Cruzar a entrada troca fachada/estrada pelo interior com fade de 0,3 segundo, durante o qual a simulação fica suspensa. A pausa congela o fade. Inimigos, flechas e coletas são desenhados apenas na vista correspondente. A derrota do guardião inicia a descida de dois anjos junto aos portões autorados no mapa. O atlas é compartilhado por todas as fases; as posições são derivadas da entrada/saída e da altura do piso. As portas abrem entre 1,8 e 3,2 segundos e a ascensão termina em 4,6 segundos, quando o controle retorna. O relógio da simulação sincroniza arte e colisões, pausa congela a sequência e respawn a reinicia. O interior permanece visível até atravessar uma saída. Voltar pela entrada permite rever a fachada e reentrar sem reiniciar o encontro; o respawn sincroniza a vista com o checkpoint. `tools/generate_chapel.py` regenera somente as cinco imagens da capela.

## 3. Traduzir os textos e incluir as imagens

Mantenha um roteiro por fase em [História e diálogos](narrativa/). Registre a sinopse, as falas, o contexto
em que aparecem no jogo e as referências para a revisão histórica externa. Atualize o roteiro quando mudar os
textos em `assets/i18n/`.

Crie todas as chaves nos dois arquivos `assets/i18n/messages_pt.properties` e `messages_en.properties`. Use UTF-8, `\n` para quebras de linha e `GameText.format` para valores dinâmicos. A fonte precisa conter os caracteres usados; `GameTextTest` verifica os catálogos.

Os textos do menu/fundo vêm da primeira fase. Objetivos, nome do chefe, anúncios de região e título de conclusão vêm da
definição atual. A abertura ilustrada continua sendo da campanha. O desfecho de cada fase usa `completionText`. O jogo
acrescenta `overlay.levelComplete.next` nas fases intermediárias e `overlay.victory.restart` na última.

Sprites comuns, áudio e skin pertencem a `GameAssets`; TMX/tilesets, cenários e texturas do interior pertencem ao bundle `LevelResources` da tela. Não adicione cada novo mapa ou cenário ao carregador compartilhado.

## 4. Validar e jogar

```sh
./gradlew test
./gradlew :lwjgl3:smokeTest
./gradlew :lwjgl3:run
```

`CampaignTest` percorre todas as fases registradas, valida mapas/metadata, arquivos de cenário e traduções PT/EN. `JourneyTest` verifica progressão em dois mapas, respawn sem avanço, estado novo na segunda fase e reinício após a última. O smoke test usa OpenGL real, conserva a rota visual da fase original e verifica uma segunda fase pequena somente de teste. As capturas adicionais ficam em `lwjgl3/build/screenshots/25-*.png`.

**A rota automática e as capturas da fase original não verificam a jogabilidade de todo mapa novo.** Adicione testes/capturas específicos para os saltos, encontros e vitória de sua nova fase; jogue seu percurso até o final. O segundo mapa de teste fica em `core/src/test/resources` e não entra na distribuição de produção.

Cada nova fase começa com vida cheia, inimigos novos, nenhuma coleta, checkpoint desativado, arena aberta e nenhuma flecha. Ao morrer, o jogador fica na mesma fase e preserva checkpoint/coletas dessa fase. Enter após a última vitória reinicia pelo primeiro mapa; Nova jornada no menu também reinicia e mostra a abertura.

## 5. Reconstruir o aplicativo

```sh
./gradlew build
./gradlew :lwjgl3:appImage
```

`Jogar.command` abre o aplicativo empacotado, não as classes recém-compiladas pelo Gradle. Refaça `appImage` depois de alterar mapas/código. Recursos adicionados em `assets/` entram automaticamente no JAR de `core`. Gere pacotes no sistema/arquitetura de destino.

O gerador `tools/generate_assets.py` regenera os assets compartilhados e específicos de exiles-road e não escreve TMX por padrão. Para gerar o mapa original, forneça `--map-output <destino>` explicitamente; esse arquivo será sobrescrito. Use o Tiled para novas fases. Não é necessário executar o gerador para compilar ou jogar.

### Passagens exploráveis e relevo

A camada de objetos `passages` aceita retângulos `cave`, `gallery` e `mountain`,
em coordenadas Tiled. Cavernas e galerias desenham o fundo e ocultam a fachada
quando o centro do jogador entra no retângulo; a transição pausa junto com o jogo.
As duas extremidades ficam abertas. O tipo `mountain` desenha os picos ao fundo.
Essa camada é visual: pisos, tetos e degraus continuam na camada `collisions`,
com os mesmos limites de altura do mapa. Deixe entradas com espaço para o corpo
inteiro do jogador e patamares amplos antes dos combates.

No level 1, a cripta ocupa X=1792–2560, sob o aqueduto; a galeria ocupa
X=4768–5600, com telhado acessível; a serra sobe até Y=384 antes da capela.
O gerador `tools/generate_first_level.py` mantém os tiles e as colisões alinhados.

### Marcos do cenário

O manifesto aceita `ARCH`, `SHRINE`, `GRAVES`, `DEAD_TREE`, `WAGON` e `PENNANT`, além das decorações existentes. Os seis
quadros do atlas compartilhado `sprites/environment.png` têm 192 × 256 pixels; `x` e `y` posicionam a base inferior
esquerda do quadro. Altares `SHRINE` são desenhados a 96 × 128 para caberem nas rotas elevadas. Desenhe-os atrás do
terreno e dos personagens, sem colisões. Use `tools/generate_environment.py` para reproduzir o atlas e os fundos da
primeira fase. `EnvironmentRenderer` acrescenta desgaste, vegetação e fragmentos nas superfícies expostas, sem ocupar
espaço físico.

### Defesa de Dorileia

O tipo `mounted_commander` ativa a defesa de acampamento em `CampDefense`: 45 segundos acumulados na área azul para
chamar apoio aliado. Sair da área pausa o contador e não causa dano.
O apoio restaura vida/especiais e atordoa o chefe uma vez. A derrota do chefe encerra a defesa e inicia a liberação
imediatamente, mesmo antes dos reforços. Consulte
[narrativa da fase 4](narrativa/04-a-batalha-de-dorileia.md) para os tempos, regras e gerador.

O atributo obrigatório `daylight` de `<interior>` mantém arenas abertas sob luz diurna (`true`) ou aplica a
iluminação interna compartilhada (`false`). Dorileia usa `true`; as demais fases preservam `false`.

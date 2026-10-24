# Cadência e desempenho

Medição local de 23/10/2026, Apple M4, framebuffer 1920 × 1080, monitor reportado a 60 Hz. Valores abaixo são
milissegundos; um quadro a 60 FPS tem orçamento de 16,67 ms.

## O que foi observado e corrigido

O teste antigo media 375 quadros com personagens quase parados. A mediana baixa desse teste não demonstrava cadência
constante durante movimento e combate.

O novo teste encontrou variação na apresentação com VSync e FPS ilimitado: em seis trechos, o intervalo mediano ficou
perto de 10–11 ms mesmo com o monitor reportando 60 Hz, alternando com intervalos próximos de 17 ms. A configuração
normal agora combina VSync com um limite igual à frequência do monitor. A mediana ficou próxima de 16,67 ms em todos os
trechos. Picos isolados continuaram aparecendo; esta medição não demonstra ausência absoluta de travadas.

A vegetação e as pedras pequenas também eram reconstruídas por quadro usando o limite esquerdo da câmera como início da
distribuição. Ao cruzar um tile, mudavam de posição no mundo. `EnvironmentRenderer` agora calcula a geometria uma vez
por fase e só desenha os detalhes visíveis. O teste OpenGL verifica a mesma folha em seis posições de câmera, incluindo
cruzamentos de tiles.

## Intervalos entre quadros

A coluna anterior usa o mesmo teste, antes das correções. A medição final inclui o limite de quadros e a geometria
estática.

| Trecho    | Mediana anterior | Mediana final | p95 final | p99 final | Máximo final |
|-----------|-----------------:|--------------:|----------:|----------:|-------------:|
| camp      |           16.639 |        16.667 |    16.700 |    16.715 |       17.593 |
| aqueduct  |           16.562 |        16.659 |    17.508 |    18.864 |       24.740 |
| ruins     |           10.746 |        16.665 |    17.575 |    17.925 |       18.012 |
| sanctuary |           10.933 |        16.656 |    17.507 |    17.720 |       18.127 |
| gallery   |           10.598 |        16.663 |    17.432 |    17.916 |       18.143 |
| ridge     |           10.675 |        16.666 |    17.291 |    17.528 |       17.622 |
| chapel    |           11.277 |        16.661 |    17.682 |    18.451 |       30.989 |
| arena     |           10.782 |        16.644 |    17.590 |    17.941 |       21.656 |

Não houve coleta de lixo nas janelas de amostragem. No teste sem limite, incluindo conclusão da GPU, o p95 do trabalho
ficou entre 1,25 e 1,89 ms após as correções. A simulação e a renderização têm folga em relação aos 16,67 ms; os
intervalos observados incluem também apresentação e agendamento do sistema. Os resultados são locais e variam com foco
da janela, carga de outras aplicações e monitor.

## Reproduzir

```sh
./gradlew :lwjgl3:performanceTest
./gradlew :lwjgl3:framePacingTest
./gradlew test :lwjgl3:smokeTest
```

Execute as medições separadamente, sem outra compilação ou aplicativo de teste em paralelo. Cada medição percorre oito
trechos com comandos de movimento, salto, ataque e bloqueio. São 120 quadros de aquecimento e 360 amostras por trecho:
2.880 amostras, além do aquecimento. A IA, as colisões, o renderizador e o HUD são os do jogo. O roteiro usa dois passos
de simulação por quadro e não reproduz entrada humana ou os sons da partida.

`performanceTest` desliga o limite e o VSync e usa `glFinish` para incluir conclusão da GPU. `framePacingTest` usa a
configuração normal de apresentação e não força `glFinish`: o intervalo entre inícios de quadros inclui a troca de
buffers e a espera do limitador. O tempo de renderização desse modo mede submissão na CPU, sem atribuir a ela o tempo
completo da GPU.

Os relatórios mostram simulação, renderização, trabalho total, intervalo entre quadros, alocações da thread principal,
draw calls, trocas de textura e contagem de GC. A instrumentação é exclusiva do modo de medição. Os logs desta execução
ficam em `lwjgl3/build/profiles/` e são artefatos locais, removíveis pelo clean.

`smokeTest` também verifica o percurso até a vitória e a estabilidade da vegetação durante movimento da câmera. Refaça
`./gradlew :lwjgl3:appImage` para levar as alterações ao aplicativo aberto por `Jogar.command`.

## Efeitos dos especialistas

Após adicionar poses e efeitos de maça, lança e óleo, o mesmo teste sem limite em Full HD no Apple M4 registrou p95 do
trabalho entre 1,40 e 2,31 ms nos oito trechos, sem GC nas janelas de amostragem. O máximo de trabalho registrado foi
9,00 ms. Os detritos têm vida de 0,45 segundo, os rastros são desenhados a partir da fase do ataque e os sons são
carregados junto com os assets. O teste gráfico verifica emissão das nove pistas, cancelamento e congelamento dos
efeitos na pausa. O log local está em `lwjgl3/build/profiles/enemy-effects-work.log`.

## Tela cheia e Game Mode — 24/10/2026

O aplicativo macOS agora declara `public.app-category.games`, `LSSupportsGameMode` e `GCSupportsGameMode`. A opção de
tela cheia usa o Space nativo do AppKit, preservando o retorno ao tamanho da janela. A resolução acompanha a tela
durante esse modo. Abra o pacote pelo `Jogar.command`; executar Java pelo Gradle não valida a identidade do aplicativo
para o Game Mode.

Entrada e saída foram verificadas manualmente no `.app`. A presença do indicador de Game Mode ainda não foi confirmada;
os metadados e a tela cheia tornam o pacote elegível, mas a ativação pertence ao macOS. A assinatura ad hoc local é
verificada pelo empacotamento; ela não equivale à assinatura de distribuição ou notarização Apple. O executável interno
usa nome ASCII para evitar uma falha de selagem observada com o nome acentuado; o nome visível continua O Templário.

Nova medição em janela, Apple M4, 1920 × 1080 e 60 Hz, com as animações e a IA atuais. Estes resultados não medem ganho
do Game Mode.

| Trecho    | Mediana |    p95 |    p99 | Máximo |
|-----------|--------:|-------:|-------:|-------:|
| camp      |  16.667 | 18.729 | 18.927 | 21.354 |
| aqueduct  |  16.667 | 18.707 | 18.830 | 19.687 |
| ruins     |  16.666 | 18.681 | 18.857 | 19.700 |
| sanctuary |  16.666 | 18.613 | 18.778 | 18.873 |
| gallery   |  16.666 | 18.521 | 20.217 | 25.449 |
| ridge     |  16.667 | 18.530 | 18.742 | 18.846 |
| chapel    |  16.666 | 18.506 | 18.631 | 18.789 |
| arena     |  16.666 | 18.508 | 18.700 | 18.861 |

Intervalos em ms. A mediana ficou em 16,67 ms (aproximadamente 60 FPS), mas houve variações até 25,45 ms. No teste
separado sem limite, com conclusão da GPU, o p95 do trabalho ficou entre 1,24 e 2,56 ms e o máximo em 8,24 ms. Nenhuma
das duas medições registrou GC nas amostras. Há folga de processamento neste roteiro; isso não exclui problemas de
apresentação ou cenas não cobertas pelo teste. Logs locais: `lwjgl3/build/profiles/game-mode-work.log` e
`game-mode-pacing.log`.

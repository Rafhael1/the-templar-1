# Fase 1 A Primeira Cruzada

Identificador da fase `exiles-road`. Textos da versão atual em português e inglês.

## História e abertura

Na abertura da campanha, o Concílio de Clermont dá início à expedição. O jogo acompanha um cavaleiro templário que jurou
proteger os peregrinos. Na primeira fase, a rota passa pelo acampamento, pela Anatólia, por Antioquia e por Jerusalém. O
confronto com o Guardião da Capela encerra o percurso.

### Apresentação no menu

**Português**

> CRÔNICAS DAS CRUZADAS / CAPÍTULO I\
> A PRIMEIRA CRUZADA\
> Um cavaleiro templário.\
> Um juramento entre ruínas.
>
> Abra a passagem e proteja\
> os peregrinos da estrada.

**Inglês**

> CRUSADE CHRONICLES / CHAPTER I\
> THE FIRST CRUSADE\
> A Templar knight.\
> An oath among the ruins.
>
> Open the passage and protect\
> the pilgrims on the road.

Chaves no jogo `menu.chapter`, `menu.level`, `menu.story`.

### Clermont

**Português**

> CLERMONT / 1095\
> O papa Urbano II reúne os cavaleiros em Clermont.\
> Uma expedição ao Oriente está prestes a mudar suas vidas.

**Inglês**

> CLERMONT / 1095\
> Pope Urban II gathers the knights at Clermont.\
> An expedition to the East is about to change their lives.

Chaves no jogo `intro.council.title`, `intro.council.text`.

### Fala de Urbano II

Fala dramatizada escrita para o jogo.

**Português**

> O CHAMADO\
> URBANO II\
> Cavaleiros, tomai a cruz e marchai ao Oriente.\
> Respondei ao chamado de guerra. Protegei os peregrinos na estrada.

**Inglês**

> THE CALL\
> URBAN II\
> Knights, take up the cross and march to the East.\
> Answer the call to war. Protect the pilgrims on the road.

Chaves no jogo `intro.call.title`, `intro.call.text`.

### A marcha

**Português**

> A LONGA MARCHA / 1096 - 1099\
> Do acampamento às estradas da Anatólia, de Antioquia a Jerusalém.\
> Encontre abrigo, preserve suas forças e atravesse as muralhas.

**Inglês**

> THE LONG MARCH / 1096 - 1099\
> From camp to the roads of Anatolia, from Antioch to Jerusalem.\
> Find shelter, preserve your strength and cross the walls.

Chaves no jogo `intro.march.title`, `intro.march.text`.

## Textos do percurso e do desfecho

Esta fase usa a abertura animada e os avisos do percurso. O mapa da fase inclui a capela e o duelo com o Guardião,
seguido pela abertura das portas pelos anjos.

| Momento                     | Português                                               | Inglês                                             | Chave no jogo      |
|-----------------------------|---------------------------------------------------------|----------------------------------------------------|--------------------|
| Acampamento                 | I / O ACAMPAMENTO - A marcha começa                     | I / THE CAMP - The march begins                    | `notice.region.0`  |
| Anatólia                    | II / ANATÓLIA - Pelas estradas do Oriente               | II / ANATOLIA - Along the roads of the East        | `notice.region.1`  |
| Antioquia                   | III / ANTIOQUIA - Entre ruínas e emboscadas             | III / ANTIOCH - Among ruins and ambushes           | `notice.region.2`  |
| Jerusalém                   | IV / JERUSALÉM - A capela nos espera                    | IV / JERUSALEM - The chapel awaits                 | `notice.region.3`  |
| Objetivo inicial e de saída | ALCANCE A CAPELA                                        | REACH THE CHAPEL                                   | `hud.gate`         |
| Objetivo da arena           | DERROTE O GUARDIÃO                                      | DEFEAT THE GUARDIAN                                | `hud.guardian`     |
| Chefe                       | GUARDIÃO DA CAPELA                                      | GUARDIAN OF THE CHAPEL                             | `hud.bossName`     |
| Entrada na arena            | AS PORTAS DA CAPELA ESTÃO SELADAS - Derrote o guardião. | THE CHAPEL DOORS ARE SEALED - Defeat the guardian. | `notice.arena`     |
| Após derrotar o Guardião    | OS ANJOS ABREM O CAMINHO                                | ANGELS OPEN THE WAY                                | `hud.angels`       |
| Abertura das portas         | AS PORTAS DA CAPELA ESTÃO ABERTAS - Ide em paz.         | THE CHAPEL DOORS ARE OPEN - Go forth in peace.     | `notice.gatesOpen` |

### Vitória na campanha atual

**Português**

> ALÉM DAS MURALHAS\
> A passagem está livre. A jornada continua.
>
> ENTER - Próxima fase

**Inglês**

> BEYOND THE WALLS\
> The passage is clear. The journey continues.
>
> ENTER - Next level

Chaves no jogo `overlay.victory.title`, `overlay.levelComplete.text`.

## Pontos para validação histórica

- Clermont em 1095 é a referência histórica da abertura. As versões preservadas do discurso diferem. A fala atribuída a
  Urbano II no jogo é
  dramatizada. [Versões do discurso de Urbano II em Clermont](https://sourcebooks.fordham.edu/source/urban2-5vers.asp)
- O protagonista templário é uma licença histórica. A Ordem do Templo foi fundada no século XII, depois da Primeira
  Cruzada. O Guardião da Capela e a intervenção dos anjos são elementos
  ficcionais. [English Heritage sobre os Cavaleiros Templários](https://production.english-heritage.org.uk/visit/places/knights-templar-church-dover/history/)
- A ordem jogável inverte parte da cronologia. A fase 1 já mostra a Anatólia, Antioquia e Jerusalém. A fase 2 volta à
  chegada a Constantinopla em 1096–1097. O cerco de Antioquia ocorreu em 1097–1098 e Jerusalém foi tomada em 1099. A
  transição atual não indica esse recuo no
  tempo. [Relatos da chegada dos cruzados a Constantinopla](https://sourcebooks.fordham.edu/source/cde-atcp.asp) · [Relatos do cerco e da captura de Antioquia](https://sourcebooks.fordham.edu/source/cde-antioch.asp) · [Relatos do cerco e da captura de Jerusalém](https://sourcebooks.fordham.edu/source/cde-jlem.asp)

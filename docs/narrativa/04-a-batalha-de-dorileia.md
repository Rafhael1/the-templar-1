# Fase 4 - A batalha de Dorileia

ID `dorylaeum`, após Niceia. Planície seca da Anatólia, coluna de carroças, ravina seca, tendas e defesa de um
acampamento aberto.
O mapa tem 8.960 pixels de extensão, com 2.048 pixels adicionais de ravina e três encontros antes do acampamento.
O contexto é a vanguarda isolada, atacada por forças turcas, até a chegada da outra coluna cruzada.
Referências consultadas: [History of War](https://www.historyofwar.org/articles/battles_dorylaeum.html) e
[EBSCO](https://www.ebsco.com/research-starters/military-history-and-science/battle-dorylaeum).
O Comandante do Crescente é fictício; o duelo, a faixa de formação e os 45 segundos são adaptações
jogáveis. Não representam a duração histórica da batalha nem um duelo documentado com o sultão.

## História e falas

Os textos completos PT/EN estão em `assets/i18n/messages_{pt,en}.properties`, prefixo `level.dorylaeum`.

- Sinopse (`story`): após Niceia, o exército avança dividido; a vanguarda precisa defender o acampamento.
- Boemundo (`orders.title/body`), X=384: orienta permanecer na área azul para chamar aliados, bloquear as salvas do
  comandante e saltar ou aparar suas investidas. F abre a conversa e F confirma. A confirmação persiste na morte.
- Regiões: marcha e carroças; planície exposta; ravina seca; círculo de carroças.
- Entrada (`arena`): a vanguarda está cercada, mantenha a formação.
- Conclusão (`victory`): os reforços chegam, os atacantes recuam e a marcha continua pela Anatólia.

## Encontro e chefe

O santuário fica antes da arena. A área azul mede 360 pixels e é marcada por templários com escudos azuis.
Eles são aliados e nunca atacam o jogador. Dentro da formação, o contador de apoio avança até completar 45 segundos;
fora dela, apenas pausa. Não há integridade de acampamento, dano por sair da área nem morte por contador.
Pausa, transição e hit stop congelam a simulação. A HUD explica quando a formação está ativa ou precisa ser retomada.

Ao completar o contador, os aliados restauram vida e cargas equipadas, removem flechas hostis já em voo e atordoam
o comandante por pelo menos 1,4 segundo. Esse apoio ocorre uma vez, anunciado com mensagem e som. Os soldados
adicionais têm aparência de templários, distinguindo-se do inimigo montado.

**Comandante do Crescente**: 18 de vida, corpo 88 x 76 e atlas de dez poses de 192 x 128. Cavalo escuro com detalhes
claros no rosto, sela bordada em turquesa e dourado, armadura lamelar, elmo e capa. Há poses de arco, galope,
preparação de investida, dois passos da investida, recuperação e queda. Nenhuma arma compartilhada flutua fora das mãos.

- Flechas: 0,85 segundo de preparação, linha pontilhada na mira, três projéteis dourados a 300 pixels/s com 0,4 segundo
  de intervalo. A HUD mostra “FLECHAS - bloqueie com o escudo”. A mira é comprometida no início da preparação.
- Investida: 1 segundo de aviso, sabre levantado e faixa vermelha até o destino real comprometido. Avança a 440
  pixels/s;
  o contato perigoso fica no corpo do cavalo (76 x 56), permitindo saltar por cima. Pode ser aparada ou esquivada;
  bloqueio comum não basta. A HUD mostra “INVESTIDA - salte ou apare!”.
- Recuperação: 1,5 segundo para punição, com pose própria e “ABERTURA - ataque agora!”.

Derrotá-lo encerra o encontro imediatamente e libera a sequência compartilhada de abertura dos portões, mesmo antes
do apoio. Morte reinicia o chefe e o contador, preservando checkpoint/coletas e a confirmação das ordens.

A HUD informa a origem do dano efetivo por três segundos, inclusive flecha do comandante e investida do cavalo.
A causa do último dano também aparece na tela de morte. Bloqueio, aparada e invulnerabilidade não geram razões falsas.

## Recursos e validação

Gerador determinístico: `tools/generate_dorylaeum.py`, com Pillow de `tools/requirements.txt`. Escreve somente em
`assets/levels/dorylaeum/`. Cenários, acampamento, NPC, TMX, piso e dez poses do chefe pertencem à fase.
`CampDefense` cuida do objetivo; `MountedCommanderBehavior` controla o chefe. A escolha é pelo tipo de inimigo,
sem regras condicionadas pelo ID da fase. Flechas, colisão, dano, aparada, áudio e liberação usam os sistemas comuns.

`DorylaeumTest` verifica saída da formação sem morte, reset, confirmação persistente, pausa, apoio aliado, vitória
antecipada, salto sobre o cavalo, três flechas, aparada e quebra de escudo. `DamageFeedbackTest` verifica a causa de
um acerto real e a ausência de aviso falso em bloqueios. O smoke test percorre a rota de produção com comandos de
jogador,
confirma as ordens e captura formação, salvas, investida, reforços e vitória (45–50 em `lwjgl3/build/screenshots/`).

O checkpoint repõe todas as cargas equipadas na entrada, na reentrada após sair e no respawn. Permanecer sobre ele
não repõe cada uso continuamente; não concede habilidades ainda bloqueadas.

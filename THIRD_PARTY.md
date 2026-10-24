# Assets e dependências

Sprites, cenários, mapa e arquivos WAV em `assets/` são originais deste projeto e foram produzidos por `tools/generate_assets.py`. Não há imagens, personagens, trilhas ou trechos de código extraídos de Blasphemous.

A interface usa DejaVu Sans, rasterizada em alta resolução por `tools/generate_font.py`. O TTF original e o aviso de licença Bitstream/DejaVu/Arev estão em `assets/ui/DejaVuSans.ttf` e `assets/ui/DejaVu-LICENSE.txt`. O runtime inclui libGDX, LWJGL e suas dependências de áudio/native loading; os avisos distribuídos pelos autores são mantidos nos artefatos originais. Principais projetos:

- libGDX — Apache 2.0: https://github.com/libgdx/libgdx/blob/master/LICENSE
- LWJGL — BSD: https://github.com/LWJGL/lwjgl3/blob/master/LICENSE.md
- Gradle — Apache 2.0: https://github.com/gradle/gradle/blob/master/LICENSE
- JUnit — EPL 2.0 (somente testes): https://github.com/junit-team/junit-framework/blob/main/LICENSE.md
- Eclipse Temurin/OpenJDK — GPLv2 com Classpath Exception; licenças em `Contents/runtime/Contents/Home/legal` no bundle Mac.

A geração opcional de assets usa Pillow e NumPy; não são dependências do runtime. `tools/generate_music.py`, chamado
pelo gerador completo, compõe e sintetiza as trilhas originais de menu, exploração, combate e vitória, sem samples ou
gravações externas. Este documento identifica a origem dos recursos e não altera suas licenças.

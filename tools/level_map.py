import xml.etree.ElementTree as ET


def write_map(path, columns, solids, points, areas):
    rows, tile = 17, 32
    height = rows * tile
    grid = [[0] * columns for _ in range(rows)]
    for x, y, width, depth in solids:
        for column in range(x // tile, (x + width) // tile):
            for row in range(y // tile, (y + depth) // tile):
                grid[rows - row - 1][column] = 2
    for row in range(1, rows):
        for column in range(columns):
            if grid[row][column] and not grid[row - 1][column]:
                grid[row][column] = 1

    tmx = ET.Element('map', {'version': '1.10', 'tiledversion': '1.11.2', 'orientation': 'orthogonal',
                            'renderorder': 'right-down', 'width': str(columns), 'height': str(rows),
                            'tilewidth': '32', 'tileheight': '32', 'infinite': '0', 'nextlayerid': '4'})
    tiles = ET.SubElement(tmx, 'tileset', firstgid='1', name='paving', tilewidth='32', tileheight='32',
                          tilecount='2', columns='2')
    ET.SubElement(tiles, 'image', source='paving.png', width='64', height='32')
    layer = ET.SubElement(tmx, 'layer', id='1', name='terrain', width=str(columns), height=str(rows))
    ET.SubElement(layer, 'data', encoding='csv').text = '\n' + ',\n'.join(
        ','.join(map(str, row)) for row in grid) + '\n'
    collisions = ET.SubElement(tmx, 'objectgroup', id='2', name='collisions')
    entities = ET.SubElement(tmx, 'objectgroup', id='3', name='entities')
    object_id = 1

    def place(group, kind, x, y, w=0, h=0):
        nonlocal object_id
        node = ET.SubElement(group, 'object', id=str(object_id), x=str(x), y=str(height - y - h))
        object_id += 1
        if kind:
            node.set('type', kind)
        if w:
            node.set('width', str(w))
            node.set('height', str(h))
        else:
            ET.SubElement(node, 'point')

    for solid in solids:
        place(collisions, None, *solid)
    for kind, x, y in points:
        place(entities, kind, x, y)
    for kind, x, y, w, h in areas:
        place(entities, kind, x, y, w, h)
    tmx.set('nextobjectid', str(object_id))
    ET.indent(tmx, space='  ')
    path.parent.mkdir(parents=True, exist_ok=True)
    ET.ElementTree(tmx).write(path, encoding='UTF-8', xml_declaration=True)

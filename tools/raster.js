const { Resvg } = require('@resvg/resvg-js');
const fs = require('fs');
const path = require('path');

const svgDir = path.join(__dirname, 'pieces_svg');
const outDir = path.join(__dirname, '..', 'app', 'src', 'main', 'res', 'drawable-nodpi');
const pieces = ['wP','wN','wB','wR','wQ','wK','bP','bN','bB','bR','bQ','bK'];

function render(svg, size, outFile, background) {
  const resvg = new Resvg(svg, {
    fitTo: { mode: 'width', value: size },
    background: background,
  });
  fs.writeFileSync(outFile, resvg.render().asPng());
  console.log('wrote', outFile);
}

for (const p of pieces) {
  const svg = fs.readFileSync(path.join(svgDir, p + '.svg'), 'utf8');
  render(svg, 256, path.join(outDir, 'piece_' + p + '.png'));
}

// Green chess.com-style logo pawn (recolor wP)
let pawn = fs.readFileSync(path.join(svgDir, 'wP.svg'), 'utf8');
pawn = pawn.replace('fill="#fff"', 'fill="#81B64C"').replace('stroke="#000"', 'stroke="#435C25"');
render(pawn, 512, path.join(outDir, 'logo_pawn.png'));

console.log('done');

// Renders the Squadfy store kit with Playwright: icons, feature graphic and framed screenshots (ES/EN).
// Usage: node render.mjs <outDir>
import { createRequire } from 'node:module';
const { chromium } = createRequire(import.meta.url)('playwright');
import { cpSync, existsSync, mkdirSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import * as S from './screens.mjs';

const here = dirname(fileURLToPath(import.meta.url));
const out = resolve(process.argv[2] ?? join(here, 'out'));

// The pages load the brand fonts and logos from a build folder next to them (repo paths, or copies next to the script)
const build = join(out, '.build');
const repoFonts = resolve(here, '../../../core/designsystem/src/commonMain/composeResources/font');
const repoLogos = resolve(here, '../../brand/logos');
mkdirSync(build, { recursive: true });
cpSync(existsSync(repoFonts) ? repoFonts : join(here, 'fonts'), join(build, 'fonts'), { recursive: true });
cpSync(existsSync(repoLogos) ? repoLogos : here, build, { recursive: true, filter: (f) => !f.endsWith('.png') && !f.includes('/out') });

const HEAD = {
  es: [
    ['Tu partido semanal,', 'organizado solo', 'Convocatoria automática, plazas y lista de espera.', 'match', 'tiza'],
    ['Equipos parejos', 'en un toque', 'Sorteo por nivel y posición. Sin discusiones.', 'teams', 'inverse'],
    ['Clasificación', 'por temporada', 'De septiembre a junio, partido a partido.', 'table', 'volt'],
    ['Cada gol', 'cuenta', 'Goles, asistencias, tarjetas y minutos en el acta.', 'report', 'tiza'],
    ['Mismo día y hora,', 'cada semana', 'Elige la hora en dos toques.', 'schedule', 'inverse'],
    ['¿Festivo? Cancela', 'y explica por qué', 'Todo el club ve el motivo al momento.', 'weekOff', 'volt'],
    ['Tu club, listo', 'en un minuto', 'Invita a tu gente con un código.', 'join', 'tiza']
  ],
  en: [
    ['Your weekly match,', 'sorted', 'Automatic sign-ups, spots and a waiting list.', 'match', 'tiza'],
    ['Fair teams', 'in one tap', 'Drawn by level and position. No arguments.', 'teams', 'inverse'],
    ['Standings', 'by season', 'September to June, match by match.', 'table', 'volt'],
    ['Every goal', 'counts', 'Goals, assists, cards and minutes in the report.', 'report', 'tiza'],
    ['Same day and time,', 'every week', 'Pick the kickoff time in two taps.', 'schedule', 'inverse'],
    ['Holiday? Call it off', 'and say why', 'The whole club sees the reason right away.', 'weekOff', 'volt'],
    ['Your club, ready', 'in a minute', 'Invite your people with a code.', 'join', 'tiza']
  ]
};

const THEMES = {
  tiza: { bg: '#F6F5EE', ink: '#101A14', accent: '#0F4D36', sub: '#4A5750', line: 'rgba(16,26,20,.18)', node: '#C5F65A', mark: '#0E1A12' },
  inverse: { bg: '#0E1A12', ink: '#F1F2EA', accent: '#C5F65A', sub: '#B3BFB5', line: 'rgba(197,246,90,.35)', node: '#C5F65A', mark: '#C5F65A' },
  volt: { bg: '#C5F65A', ink: '#0E1A12', accent: '#0E1A12', sub: '#2B3A2F', line: 'rgba(14,26,18,.25)', node: '#0E1A12', mark: '#0E1A12' }
};

// Decorative triangulation at the sides of the phone: three nodes joined by dashed passes (the brand's watermark)
const art = (th, w, h) => {
  const tri = (pts) => `<path d="M${pts.map(([x, y]) => `${w * x} ${h * y}`).join(' L')} Z"/>`;
  const left = [[0.03, 0.55], [0.13, 0.62], [0.05, 0.74]];
  const right = [[0.90, 0.40], [0.98, 0.50], [0.88, 0.58]];
  return `<svg style="position:absolute;inset:0" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}">
<g stroke="${th.line}" stroke-width="${w * 0.004}" stroke-dasharray="${w * 0.014} ${w * 0.011}" fill="none">${tri(left)}${tri(right)}</g>
${[...left, ...right].map(([x, y], i) => `<circle cx="${w * x}" cy="${h * y}" r="${w * (i % 3 === 0 ? 0.013 : 0.009)}" fill="${th.node}"/>`).join('')}
</svg>`;
};

function screenshotPage({ w, h, lang, i, ios }) {
  const [l1, l2, sub, screen, theme] = HEAD[lang][i];
  const th = THEMES[theme];
  const u = w / 100;
  // iPhone canvases fit the whole phone; on 9:16 (Play) a smaller phone keeps the whole screen in view
  const phoneW = (h / w > 2 ? 76 : 64) * u;
  const scale = (phoneW - 2.4 * u * 2) / 393;
  const topText = h * (h / w > 2 ? 0.07 : 0.05);
  return `<!doctype html><html><head><meta charset="utf-8"><style>${S.css}
body{width:${w}px;height:${h}px;overflow:hidden;background:${th.bg};position:relative;display:flex;flex-direction:column;align-items:center}
.cutc{position:absolute;top:0;right:0;width:${16 * u}px;height:${16 * u}px;background:${th.ink === '#F1F2EA' ? '#C5F65A' : '#0E1A12'};clip-path:polygon(0 0,100% 0,100% 100%)}
.h{position:relative;align-self:stretch;margin:${topText}px ${14 * u}px 0 ${7 * u}px;font-family:Bric;font-weight:800;color:${th.ink};font-size:${(h / w > 2 ? 8.4 : 7.4) * u}px;line-height:1.02;letter-spacing:-.02em}
.h em{font-style:normal;color:${th.accent};display:block}
.h{min-height:${(h / w > 2 ? 8.4 : 7.4) * u * 1.02 * 2 + 2 * u + 3.8 * u * 1.3 * 2}px}
.s{margin-top:${2 * u}px;font-family:Inst;font-weight:500;font-size:${3.8 * u}px;line-height:1.3;color:${th.sub};letter-spacing:0}
.phone{position:relative;flex:none;margin-top:${5 * u}px;width:${phoneW}px;height:${phoneW * 2.17}px;border-radius:${10 * u}px;background:#0B0F0C;padding:${2.4 * u}px;box-shadow:0 ${3 * u}px ${8 * u}px rgba(0,0,0,.28)}
.inner{width:393px;height:852px;transform:scale(${scale});transform-origin:0 0;border-radius:${(10 * u - 2.4 * u) / scale}px;overflow:hidden}
.island{position:absolute;top:${4.2 * u * phoneW / (76 * u)}px;left:50%;transform:translateX(-50%);width:${22 * u * phoneW / (76 * u)}px;height:${6.5 * u * phoneW / (76 * u)}px;border-radius:${4 * u}px;background:#0B0F0C;z-index:3}
</style></head><body>${art(th, w, h)}<div class="cutc"></div>
<div class="h">${l1}<em>${l2}</em><div class="s">${sub}</div></div>
<div class="phone"><div class="island"></div><div class="inner">${S[screen](lang, ios)}</div></div>
</body></html>`;
}

const iconPage = (size) => `<!doctype html><html><head><style>body{margin:0;width:${size}px;height:${size}px}</style></head><body>
<img src="app-icon-full.svg" width="${size}" height="${size}" style="display:block"></body></html>`;

function featurePage(lang) {
  const t = lang === 'es'
    ? ['Tu fútbol semanal,', 'organizado', 'Convocatoria, equipos parejos y clasificación por temporada.']
    : ['Your weekly football,', 'sorted', 'Sign-ups, fair teams and season standings.'];
  return `<!doctype html><html><head><meta charset="utf-8"><style>${S.css}
body{width:1024px;height:500px;overflow:hidden;background:#0E1A12;position:relative;font-family:Inst}
.cutc{position:absolute;top:0;right:0;width:120px;height:120px;background:#C5F65A;clip-path:polygon(0 0,100% 0,100% 100%)}
</style></head><body>
<svg style="position:absolute;right:40px;top:60px" width="420" height="380" viewBox="0 0 420 380">
<g stroke="rgba(197,246,90,.55)" stroke-width="3" stroke-dasharray="12 9" fill="none"><path d="M60 300L210 60L360 300Z"/><path d="M210 60L360 300M60 300L360 300"/><path d="M130 190L290 190"/></g>
<circle cx="60" cy="300" r="26" fill="#F1F2EA"/><circle cx="360" cy="300" r="26" fill="#FF8A63"/><circle cx="210" cy="60" r="34" fill="#C5F65A"/>
<circle cx="130" cy="190" r="16" fill="#C5F65A"/><circle cx="290" cy="190" r="16" fill="#F1F2EA"/></svg>
<div class="cutc"></div>
<img src="corte-lockup-inverse.svg" style="position:absolute;left:64px;top:64px;height:56px">
<div style="position:absolute;left:64px;top:170px;width:560px;font-family:Bric;font-weight:800;font-size:58px;line-height:1.02;color:#F1F2EA;letter-spacing:-.02em">${t[0]}<span style="display:block;color:#C5F65A">${t[1]}</span></div>
<div style="position:absolute;left:64px;top:330px;width:520px;font-size:24px;line-height:1.35;color:#B3BFB5;font-weight:500">${t[2]}</div>
</body></html>`;
}

const SIZES = [
  { dir: 'capturas/google-play', w: 1080, h: 1920, ios: false },
  { dir: 'capturas/iphone-6.9', w: 1320, h: 2868, ios: true },
  { dir: 'capturas/iphone-6.5', w: 1284, h: 2778, ios: true }
];

async function shoot(browser, html, w, h, file) {
  const tmp = join(build, `page-${w}x${h}.html`);
  writeFileSync(tmp, html);
  const page = await browser.newPage({ viewport: { width: w, height: h }, deviceScaleFactor: 1 });
  await page.goto('file://' + tmp);
  await page.evaluate(() => document.fonts.ready);
  await page.waitForTimeout(150);
  mkdirSync(dirname(file), { recursive: true });
  await page.screenshot({ path: file, type: 'png' });
  await page.close();
}

const browser = await chromium.launch();
await shoot(browser, iconPage(512), 512, 512, join(out, 'iconos/google-play-icono-512.png'));
await shoot(browser, iconPage(1024), 1024, 1024, join(out, 'iconos/app-store-icono-1024.png'));
for (const lang of ['es', 'en']) {
  await shoot(browser, featurePage(lang), 1024, 500, join(out, `graficos/google-play-grafico-destacado-1024x500-${lang}.png`));
  for (const size of SIZES) {
    for (let i = 0; i < HEAD[lang].length; i++) {
      const name = `${String(i + 1).padStart(2, '0')}-${HEAD[lang][i][3]}.png`;
      await shoot(browser, screenshotPage({ w: size.w, h: size.h, lang, i, ios: size.ios }), size.w, size.h, join(out, size.dir, lang, name));
    }
  }
}
await browser.close();
rmSync(build, { recursive: true, force: true });
console.log('done', out);

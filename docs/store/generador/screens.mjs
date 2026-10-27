// Squadfy store kit: app screens recreated with the «Triangulación» tokens (Tiza theme), at a 393 × 852 base.
// Illustrative content (fictional club and players); replace the inner screens with real captures when available.

export const T = {
  es: {
    club: 'Albaicín FC', members: '16 miembros', tabs: ['Partido', 'Equipos', 'Tabla', 'Miembros'],
    nav: ['Inicio', 'Clubes', 'Chat', 'Perfil'],
    next: 'Próximo partido', day: 'Jueves 30 oct', format: '5 contra 5 · 60 min', closesIn: 'Cierra en',
    d: 'Días', h: 'Horas', m: 'Min', places: 'Plazas', of: 'de', join: 'Apuntarme', called: 'Convocados', waitlist: 'Lista de espera',
    you: 'TÚ', status: 'Convocado · tienes plaza', teamA: 'Equipo A', teamB: 'Equipo B', balance: 'Equilibrio', even: 'Muy parejo',
    expected: 'Probabilidad de ganar', season: '2026/27 · en curso', seasonOld: '2025/26 · cerrada', rating: 'Rating', goals: 'Goles',
    assists: 'Asist.', played: 'PJ', final: 'Final', report: 'Acta', goal: 'Gol', assist: 'Asistencia', yellow: 'Amarilla',
    schedule: 'Horario semanal', every: 'Jueves 20:00 (Madrid) · 5 contra 5 · 60 min', pickTitle: 'Hora del partido', usual: 'Horas habituales',
    hours: 'Horas', use: 'Usar 20:00', cancel: 'Cancelar', special: 'Semanas especiales', cancelled: '6 nov · Cancelado', reason: 'Festivo local',
    weekOff: 'Esta semana no hay partido', reasonLine: 'Motivo: festivo local', nextOne: 'Siguiente: jueves 13 nov, 20:00',
    joinTitle: 'Únete a un club', joinHint: 'Escribe el código que te ha pasado tu capitán.', code: 'ALBA7K2Q', joinCta: 'Unirme al club',
    createHint: '¿Eres el organizador?', createCta: 'Crear un club', provisional: 'Prov.', pts: 'de rating'
  },
  en: {
    club: 'Albaicín FC', members: '16 members', tabs: ['Match', 'Teams', 'Table', 'Members'],
    nav: ['Home', 'Clubs', 'Chat', 'Profile'],
    next: 'Next match', day: 'Thu, Oct 30', format: '5-a-side · 60 min', closesIn: 'Closes in',
    d: 'Days', h: 'Hours', m: 'Min', places: 'Spots', of: 'of', join: 'Count me in', called: 'Called up', waitlist: 'Waiting list',
    you: 'YOU', status: "You're in · spot confirmed", teamA: 'Team A', teamB: 'Team B', balance: 'Balance', even: 'Very even',
    expected: 'Chance of winning', season: '2026/27 · in progress', seasonOld: '2025/26 · closed', rating: 'Rating', goals: 'Goals',
    assists: 'Assists', played: 'MP', final: 'Full time', report: 'Match report', goal: 'Goal', assist: 'Assist', yellow: 'Yellow card',
    schedule: 'Weekly schedule', every: 'Thursday 20:00 (Madrid) · 5-a-side · 60 min', pickTitle: 'Kickoff time', usual: 'Usual times',
    hours: 'Hours', use: 'Use 20:00', cancel: 'Cancel', special: 'Special weeks', cancelled: 'Nov 6 · Cancelled', reason: 'Local holiday',
    weekOff: 'No match this week', reasonLine: 'Reason: local holiday', nextOne: 'Next: Thursday Nov 13, 20:00',
    joinTitle: 'Join a club', joinHint: 'Type the code your captain sent you.', code: 'ALBA7K2Q', joinCta: 'Join the club',
    createHint: 'Are you the organiser?', createCta: 'Create a club', provisional: 'Prov.', pts: 'rating'
  }
};

const PLAYERS = [
  ['Javi', 'GK', 7], ['Lucía', 'DEF', 4], ['Dani', 'MID', 8], ['Marta', 'FWD', 9], ['Sergio', 'DEF', 2],
  ['Álex', 'MID', 10], ['Rubén', 'FWD', 11], ['Iván', 'GK', 1], ['Pablo', 'DEF', 5], ['Carlos', 'MID', 6]
];

const glyph = (pos, color) => ({
  GK: `<svg width="9" height="9" viewBox="0 0 10 10"><rect x="1" y="1" width="8" height="8" fill="${color}"/></svg>`,
  DEF: `<svg width="10" height="10" viewBox="0 0 10 10"><path d="M5 0L10 5L5 10L0 5Z" fill="${color}"/></svg>`,
  MID: `<svg width="9" height="9" viewBox="0 0 10 10"><circle cx="5" cy="5" r="4.5" fill="${color}"/></svg>`,
  FWD: `<svg width="10" height="10" viewBox="0 0 10 10"><path d="M5 0L10 9H0Z" fill="${color}"/></svg>`
})[pos];

const posShort = { es: { GK: 'POR', DEF: 'DEF', MID: 'MED', FWD: 'DEL' }, en: { GK: 'GK', DEF: 'DEF', MID: 'MID', FWD: 'FWD' } };
const posChip = (lang, pos) => `<span class="pos">${glyph(pos, 'var(--ink)')}${posShort[lang][pos]}</span>`;
const avatar = (name, size = 40, cls = '') => `<span class="av ${cls}" style="width:${size}px;height:${size}px;font-size:${size * 0.36}px">${name.slice(0, 1)}</span>`;

const icon = {
  back: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M15 6l-6 6 6 6"/></svg>',
  gear: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"><circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1"/></svg>',
  home: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linejoin="round"><path d="M4 11l8-7 8 7v9h-5v-6H9v6H4z"/></svg>',
  clubs: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linejoin="round"><path d="M7 3h10l3 3v9a6 6 0 0 1-6 6h-4a6 6 0 0 1-6-6V6z"/><path d="M9 15l3-6 3 6z"/></svg>',
  chat: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linejoin="round"><path d="M4 5h16v11H9l-5 4z"/></svg>',
  user: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"><circle cx="12" cy="8" r="4"/><path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6"/></svg>',
  clock: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>',
  calendar: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4"/></svg>',
  ball: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75"><circle cx="12" cy="12" r="9"/><path d="M12 7l4 3-1.5 5h-5L8 10z"/></svg>',
  minus: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M6 12h12"/></svg>',
  plus: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M6 12h12M12 6v12"/></svg>',
  alert: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round"><path d="M12 3l9 17H3z"/><path d="M12 10v4M12 17v.5"/></svg>',
  trophy: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linejoin="round"><path d="M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M7 6H4a3 3 0 0 0 3 4M17 6h3a3 3 0 0 1-3 4M12 14v4M8 21h8"/></svg>'
};

export const css = `
@font-face{font-family:Bric;src:url(fonts/bricolagegrotesque_semibold.ttf);font-weight:600}
@font-face{font-family:Bric;src:url(fonts/bricolagegrotesque_bold.ttf);font-weight:700}
@font-face{font-family:Bric;src:url(fonts/bricolagegrotesque_extrabold.ttf);font-weight:800}
@font-face{font-family:Inst;src:url(fonts/instrumentsans_regular.ttf);font-weight:400}
@font-face{font-family:Inst;src:url(fonts/instrumentsans_medium.ttf);font-weight:500}
@font-face{font-family:Inst;src:url(fonts/instrumentsans_semibold.ttf);font-weight:600}
@font-face{font-family:Inst;src:url(fonts/instrumentsans_bold.ttf);font-weight:700}
:root{--s100:#F6F5EE;--s200:#FFFFFF;--s300:#EBE9DC;--inv:#0E1A12;--onInv:#F1F2EA;--onInvM:#B3BFB5;--ink:#101A14;--inkM:#4A5750;--inkS:#5F6C64;
--line:#DCD9CA;--lineS:#7D8A81;--brand:#0F4D36;--volt:#C5F65A;--onVolt:#0E1A12;--acc:#17623F;--teamB:#FF8A63;--succ:#DFF5C8;--warn:#7A5200;--warnS:#FBEFC9;
--danger:#B3261E;--dangerS:#FBE0DC;--yellow:#F2C230}
*{box-sizing:border-box;margin:0;padding:0}
.scr{width:393px;height:852px;background:var(--s100);font-family:Inst;color:var(--ink);position:relative;overflow:hidden;font-size:15px}
.sb{height:50px;display:flex;align-items:center;justify-content:space-between;padding:14px 30px 0;font:600 16px Inst}
.sb .r{display:flex;gap:6px;align-items:center}
.caps{font:700 11px Inst;letter-spacing:.08em;text-transform:uppercase;color:var(--inkM)}
.bric{font-family:Bric}
.tab-wrap{padding:0 16px}
.tabs{display:flex;background:var(--s300);border-radius:14px;padding:4px;gap:2px}
.tabs div{flex:1;text-align:center;padding:11px 0;border-radius:10px;font:600 15px Inst;color:var(--inkM)}
.tabs .on{background:var(--s200);color:var(--ink);box-shadow:0 1px 2px rgba(16,26,20,.12)}
.hdr{display:flex;align-items:center;gap:12px;padding:6px 16px 14px}
.rbtn{width:44px;height:44px;border-radius:50%;background:var(--s200);display:grid;place-items:center;box-shadow:0 1px 2px rgba(16,26,20,.12)}
.crest{width:44px;height:44px;border-radius:12px;background:var(--volt);position:relative;clip-path:polygon(0 0,calc(100% - 12px) 0,100% 12px,100% 100%,0 100%);display:grid;place-items:center;font:800 17px Bric;color:var(--onVolt)}
.crest:after{content:"";position:absolute;top:0;right:0;width:12px;height:12px;background:var(--inv);clip-path:polygon(0 0,100% 100%,0 100%)}
.card{background:var(--s200);border-radius:14px;box-shadow:0 1px 2px rgba(16,26,20,.10);padding:16px}
.cut{clip-path:polygon(0 0,calc(100% - 24px) 0,100% 24px,100% 100%,0 100%);position:relative}
.cut.fold:after{content:"";position:absolute;top:0;right:0;width:24px;height:24px;background:var(--volt);clip-path:polygon(0 0,100% 100%,0 100%)}
.ticket{background:var(--inv);color:var(--onInv);border-radius:20px;padding:20px}
.btn{height:56px;border-radius:14px;display:flex;align-items:center;justify-content:center;font:700 17px Inst;gap:8px}
.btn.volt{background:var(--volt);color:var(--onVolt);clip-path:polygon(0 0,calc(100% - 14px) 0,100% 14px,100% 100%,0 100%);box-shadow:0 8px 20px rgba(197,246,90,.5)}
.btn.brand{background:var(--brand);color:var(--s100);clip-path:polygon(0 0,calc(100% - 14px) 0,100% 14px,100% 100%,0 100%)}
.btn.sec{background:var(--s200);border:1.5px solid var(--lineS);color:var(--ink)}
.pos{display:inline-flex;align-items:center;gap:5px;background:var(--s300);border-radius:6px;padding:3px 7px;font:700 11px Inst;letter-spacing:.06em}
.av{display:inline-grid;place-items:center;border-radius:50%;background:var(--s300);font-family:Bric;font-weight:700;color:var(--ink);flex:none}
.av.me{background:var(--volt);color:var(--onVolt)}
.av.b{background:var(--teamB);color:#1C0D07}
.row{display:flex;align-items:center;gap:12px}
.chip{display:inline-flex;align-items:center;gap:6px;height:44px;padding:0 14px;border-radius:10px;border:1.5px solid var(--lineS);background:var(--s200);font:600 14px Inst;white-space:nowrap}
.chip.on{background:var(--ink);border-color:var(--ink);color:var(--s100)}
.nav{position:absolute;left:0;right:0;bottom:0;height:92px;background:var(--s200);box-shadow:0 -2px 12px rgba(16,26,20,.08);display:flex;justify-content:space-evenly;padding-top:10px}
.nav div{display:flex;flex-direction:column;align-items:center;gap:4px;font:500 12px Inst;color:var(--inkM);width:80px}
.nav .ic{width:64px;height:34px;border-radius:10px;display:grid;place-items:center}
.nav .on{color:var(--ink);font-weight:700}.nav .on .ic{background:var(--volt);color:var(--onVolt)}
.seg{display:flex;gap:4px}
.seg i{flex:1;height:14px;transform:skewX(-18deg);background:rgba(241,242,234,.16);border-radius:2px}
.seg i.f{background:var(--volt)}
.cd{display:flex;gap:6px;align-items:center}
.cd .c{min-width:62px;background:rgba(241,242,234,.08);border-radius:10px;padding:6px 8px;text-align:center}
.cd .n{font:700 26px Bric;font-variant-numeric:tabular-nums}
.cd .l{font:700 10px Inst;letter-spacing:.08em;color:var(--onInvM);text-transform:uppercase}
.list .it{display:flex;align-items:center;gap:12px;padding:10px 0;border-bottom:1px solid var(--line)}
.rk{display:flex;align-items:center;gap:12px;background:var(--s200);border-radius:14px;padding:12px 14px;box-shadow:0 1px 2px rgba(16,26,20,.08)}
.rk.me{background:var(--succ);box-shadow:none}
.rk .p{width:36px;height:36px;border-radius:10px;display:grid;place-items:center;font:700 18px Bric;background:var(--s300)}
.rk .p1{background:var(--volt)}.rk .p2{background:var(--inv);color:var(--onInv)}.rk .p3{background:transparent;border:2px solid var(--ink)}
.delta{display:inline-block;font:700 12px Inst;padding:3px 7px;border-radius:5px;background:var(--succ);color:var(--acc)}
.delta.dn{background:var(--dangerS);color:var(--danger)}
.notice{display:flex;gap:12px;background:var(--warnS);color:var(--warn);border-radius:14px;padding:14px 16px}
.sheet{position:absolute;left:0;right:0;bottom:0;background:var(--s100);border-radius:28px 28px 0 0;box-shadow:0 -10px 40px rgba(16,26,20,.25);padding:12px 20px 30px}
.handle{width:36px;height:4px;border-radius:2px;background:var(--lineS);margin:0 auto 16px}
.seg2{min-height:72px;min-width:92px;padding:0 12px;border-radius:14px;display:grid;place-items:center;font:800 56px Bric;font-variant-numeric:tabular-nums;background:var(--s200);border:1.5px solid var(--lineS)}
.seg2.on{background:var(--volt);border-color:var(--volt);color:var(--onVolt)}
.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:8px}
.grid div{height:48px;border-radius:10px;border:1.5px solid var(--lineS);background:var(--s200);display:grid;place-items:center;font:600 18px Bric}
.grid .on{background:var(--ink);color:var(--s100);border-color:var(--ink)}
`;

const statusBar = () => `<div class="sb"><span>9:41</span><span class="r">
<svg width="18" height="12" viewBox="0 0 18 12"><rect x="0" y="8" width="3" height="4" rx="1" fill="#101A14"/><rect x="5" y="5" width="3" height="7" rx="1" fill="#101A14"/><rect x="10" y="2" width="3" height="10" rx="1" fill="#101A14"/><rect x="15" y="0" width="3" height="12" rx="1" fill="#101A14"/></svg>
<svg width="27" height="13" viewBox="0 0 27 13"><rect x=".5" y=".5" width="23" height="12" rx="3.5" fill="none" stroke="#101A14"/><rect x="2" y="2" width="18" height="9" rx="2" fill="#101A14"/><rect x="25" y="4" width="2" height="5" rx="1" fill="#101A14"/></svg></span></div>`;

const nav = (t, sel, ios) => {
  const items = [[icon.home, t.nav[0]], [icon.clubs, t.nav[1]], ...(ios ? [] : [[icon.chat, t.nav[2]]]), [icon.user, t.nav[3]]];
  return `<div class="nav">${items.map(([ic, l], i) => `<div class="${i === sel ? 'on' : ''}"><span class="ic">${ic}</span>${l}</div>`).join('')}</div>`;
};

const clubHeader = (t, tab) => `
<div class="hdr"><span class="rbtn">${icon.back}</span><span class="crest">A</span>
<div style="flex:1"><div class="bric" style="font-weight:700;font-size:20px">${t.club}</div><div class="caps">${t.members}</div></div>
<span class="rbtn">${icon.gear}</span></div>
<div class="tab-wrap"><div class="tabs">${t.tabs.map((x, i) => `<div class="${i === tab ? 'on' : ''}">${x}</div>`).join('')}</div></div>`;

// 1 · Match tab: ticket, places, countdown, CTA and the called-up list
export function match(lang, ios) {
  const t = T[lang];
  const seg = Array.from({ length: 10 }, (_, i) => `<i class="${i < 9 ? 'f' : ''}"></i>`).join('');
  const list = PLAYERS.slice(0, 5).map(([n, p, num], i) => `<div class="it">${avatar(n, 38)}<div style="flex:1"><div style="font:600 15px Inst">${n}</div><div style="color:var(--inkM);font-size:13px">#${num}</div></div>${posChip(lang, p)}</div>`).join('');
  return `<div class="scr">${statusBar()}${clubHeader(t, 0)}
<div style="padding:16px">
 <div class="ticket cut fold">
  <div class="caps" style="color:var(--onInvM)">${t.next}</div>
  <div style="display:flex;justify-content:space-between;align-items:flex-end;margin-top:6px">
   <div><div class="bric" style="font-weight:700;font-size:24px">${t.day}</div><div style="color:var(--onInvM);font-size:14px;margin-top:2px">${t.format}</div></div>
   <div class="bric" style="font-weight:800;font-size:52px;line-height:1;color:var(--volt);font-variant-numeric:tabular-nums">20:00</div>
  </div>
  <div style="display:flex;justify-content:space-between;margin:18px 0 8px"><span class="caps" style="color:var(--onInvM)">${t.places}</span><span style="font:700 15px Inst"><b class="bric" style="font-size:20px">9</b> ${t.of} 10</span></div>
  <div class="seg">${seg}</div>
  <div style="display:flex;align-items:center;gap:6px;margin:18px 0 8px;color:var(--onInvM)">${icon.clock}<span class="caps" style="color:var(--onInvM)">${t.closesIn}</span></div>
  <div class="cd"><div class="c"><div class="n">01</div><div class="l">${t.d}</div></div><b>:</b><div class="c"><div class="n">04</div><div class="l">${t.h}</div></div><b>:</b><div class="c"><div class="n">32</div><div class="l">${t.m}</div></div></div>
 </div>
 <div class="btn volt" style="margin-top:16px">${icon.ball}${t.join}</div>
 <div class="bric" style="font-weight:600;font-size:22px;margin:22px 0 6px">${t.called} (9)</div>
 <div class="list">${list}</div>
</div>${nav(t, 1, ios)}</div>`;
}

// 2 · Teams on the pitch with the triangulation lines
export function teams(lang, ios) {
  const t = T[lang];
  const A = [[50, 10, 'Javi'], [25, 23, 'Lucía'], [75, 23, 'Sergio'], [38, 36, 'Dani'], [64, 38, 'Marta']];
  const B = [[50, 90, 'Iván'], [26, 76, 'Pablo'], [74, 77, 'Carlos'], [38, 63, 'Rubén'], [63, 62, 'Álex']];
  const lines = (P) => { const out = []; for (let i = 0; i < P.length; i++) for (let j = i + 1; j < P.length; j++) { const d = Math.hypot(P[i][0] - P[j][0], P[i][1] - P[j][1]); if (d < 30) out.push(`<line x1="${P[i][0]}" y1="${P[i][1]}" x2="${P[j][0]}" y2="${P[j][1]}"/>`); } return out.join(''); };
  const disc = ([x, y, n], b) => `<div style="position:absolute;left:${x}%;top:${y}%;transform:translate(-50%,-50%);text-align:center">
<div style="width:40px;height:40px;border-radius:50%;margin:0 auto;display:grid;place-items:center;font:800 15px Bric;background:${b ? 'var(--teamB)' : 'var(--volt)'};color:var(--onVolt);box-shadow:0 0 0 3px rgba(14,26,18,.6)${n === 'Lucía' ? ',0 0 0 6px #F1F2EA' : ''}">${n.slice(0, 1)}</div>
<div style="font:600 11px Inst;color:var(--onInv);margin-top:4px">${n}</div></div>`;
  return `<div class="scr">${statusBar()}${clubHeader(t, 1)}
<div style="padding:16px">
 <div style="position:relative;height:430px;border-radius:20px;background:var(--inv);overflow:hidden">
  <svg viewBox="0 0 100 100" preserveAspectRatio="none" style="position:absolute;inset:0;width:100%;height:100%">
   ${Array.from({ length: 5 }, (_, i) => `<rect x="0" y="${i * 20}" width="100" height="10" fill="rgba(241,242,234,.035)"/>`).join('')}
   <g fill="none" stroke="rgba(241,242,234,.28)" stroke-width=".5"><rect x="4" y="3" width="92" height="94" rx="2"/><line x1="4" y1="50" x2="96" y2="50"/><circle cx="50" cy="50" r="9"/><rect x="30" y="3" width="40" height="12"/><rect x="30" y="85" width="40" height="12"/></g>
   <g stroke="rgba(197,246,90,.75)" stroke-width=".6" stroke-dasharray="1.6 1.2">${lines(A)}</g>
   <g stroke="rgba(255,138,99,.75)" stroke-width=".6" stroke-dasharray="1.6 1.2">${lines(B)}</g>
  </svg>
  ${A.map((p) => disc(p, false)).join('')}${B.map((p) => disc(p, true)).join('')}
 </div>
 <div class="card" style="margin-top:14px">
  <div style="display:flex;justify-content:space-between;align-items:center"><span class="caps">${t.balance}</span><span style="font:600 13px Inst;color:var(--acc)">${t.even}</span></div>
  <div style="display:flex;align-items:center;gap:10px;margin-top:10px">
   <span style="font:700 13px Inst">A</span>
   <div style="flex:1;display:flex;gap:3px">${Array.from({ length: 10 }, (_, i) => `<i style="flex:1;height:14px;transform:skewX(-18deg);border-radius:2px;background:${i < 5 ? 'var(--volt)' : 'var(--teamB)'}"></i>`).join('')}</div>
   <span style="font:700 13px Inst">B</span></div>
  <div style="display:flex;justify-content:space-between;margin-top:10px;font:600 14px Inst"><span>${t.teamA} · <b class="bric">1084</b></span><span>${t.teamB} · <b class="bric">1079</b></span></div>
 </div>
</div>${nav(t, 1, ios)}</div>`;
}

// 3 · Match report: scoreboard and timeline
export function report(lang, ios) {
  const t = T[lang];
  const ev = [[8, 'A', t.goal, 'Marta', 'g'], [21, 'B', t.goal, 'Rubén', 'g'], [33, 'A', t.assist, 'Dani', 'a'], [33, 'A', t.goal, 'Lucía', 'g'], [47, 'B', t.yellow, 'Pablo', 'y'], [55, 'A', t.goal, 'Marta', 'g']];
  const ic = (k) => k === 'g' ? `<span style="width:30px;height:30px;border-radius:50%;background:var(--volt);display:grid;place-items:center">${icon.ball}</span>`
    : k === 'y' ? '<span style="width:30px;height:30px;border-radius:50%;background:var(--s300);display:grid;place-items:center"><i style="width:11px;height:15px;background:var(--yellow);border-radius:2px;display:block"></i></span>'
    : '<span style="width:30px;height:30px;border-radius:50%;background:var(--s300);display:grid;place-items:center;font:700 14px Inst">→</span>';
  const card = (e) => `<div class="card" style="padding:10px 12px;display:flex;gap:10px;align-items:center;${e[1] === 'B' ? 'flex-direction:row-reverse;text-align:right' : ''}">${ic(e[4])}<div><div style="font:600 15px Inst">${e[3]}</div><div style="font-size:13px;color:var(--inkM)">${e[2]}</div></div></div>`;
  const rows = ev.map((e) => `<div style="display:grid;grid-template-columns:1fr 60px 1fr;align-items:center;margin-bottom:10px">
<div style="display:flex;justify-content:flex-end">${e[1] === 'A' ? card(e) : ''}</div>
<div style="display:grid;place-items:center"><span style="font:700 13px Inst;background:var(--s100);border:1.5px solid var(--lineS);border-radius:6px;padding:2px 8px">${e[0]}'</span></div>
<div>${e[1] === 'B' ? card(e) : ''}</div></div>`).join('');
  return `<div class="scr">${statusBar()}
<div class="hdr"><span class="rbtn">${icon.back}</span><div style="flex:1;text-align:center" class="bric"><span style="font-weight:700;font-size:20px">${t.report}</span></div><span style="width:44px"></span></div>
<div style="padding:4px 16px 16px">
 <div class="ticket cut fold" style="text-align:center">
  <div class="caps" style="color:var(--onInvM)">${t.final} · ${t.day}</div>
  <div style="display:flex;align-items:center;justify-content:center;gap:22px;margin-top:10px">
   <div><div style="width:46px;height:46px;border-radius:12px;background:var(--volt);color:var(--onVolt);display:grid;place-items:center;font:800 20px Bric;margin:0 auto">A</div><div style="font-size:13px;color:var(--onInvM);margin-top:6px">${t.teamA}</div></div>
   <div class="bric" style="font-weight:800;font-size:64px;font-variant-numeric:tabular-nums;line-height:1">3<span style="color:var(--onInvM);margin:0 8px">–</span>1</div>
   <div><div style="width:46px;height:46px;border-radius:12px;background:var(--teamB);color:#1C0D07;display:grid;place-items:center;font:800 20px Bric;margin:0 auto">B</div><div style="font-size:13px;color:var(--onInvM);margin-top:6px">${t.teamB}</div></div>
  </div>
 </div>
 <div style="margin-top:18px">${rows}</div>
</div>${nav(t, 1, ios)}</div>`;
}

// 4 · Season standings
export function table(lang, ios) {
  const t = T[lang];
  const rows = [['Marta', 'FWD', 9, 1132, 14], ['Dani', 'MID', 8, 1098, 6], ['Lucía', 'DEF', 4, 1071, 11], ['Álex', 'MID', 10, 1046, -4], ['Javi', 'GK', 7, 1022, 3], ['Rubén', 'FWD', 11, 1003, -7]];
  return `<div class="scr">${statusBar()}${clubHeader(t, 2)}
<div style="padding:14px 16px;display:flex;flex-direction:column;gap:10px">
 <div style="display:flex;gap:8px"><span class="chip on">${icon.calendar}${t.season}</span><span class="chip">${t.seasonOld}</span></div>
 <div style="display:flex;gap:8px"><span class="chip on">${icon.trophy}${t.rating}</span><span class="chip">${icon.ball}${t.goals}</span><span class="chip">${t.assists}</span><span class="chip">${t.played}</span></div>
 ${rows.map(([n, p, num, r, d], i) => `<div class="rk ${n === 'Lucía' ? 'me' : ''}"><span class="p p${i + 1}">${i + 1}</span>${avatar(n, 40, n === 'Lucía' ? 'me' : '')}
 <div style="flex:1"><div style="display:flex;gap:8px;align-items:center"><span class="bric" style="font-weight:600;font-size:18px">${n}</span>${n === 'Lucía' ? `<span class="caps" style="color:var(--acc)">${t.you}</span>` : ''}</div>
 <div style="display:flex;gap:8px;align-items:center;margin-top:4px"><span style="font-size:13px;color:var(--inkM)">#${num}</span>${posChip(lang, p)}</div></div>
 <div style="text-align:right"><div class="bric" style="font-weight:700;font-size:24px;font-variant-numeric:tabular-nums">${r}</div><span class="delta ${d < 0 ? 'dn' : ''}">${d < 0 ? '▼ ' + -d : '▲ ' + d}</span></div></div>`).join('')}
</div>${nav(t, 1, ios)}</div>`;
}

// 5 · Fixed weekly schedule with the new time picker
export function schedule(lang, ios) {
  const t = T[lang];
  const hours = [16, 17, 18, 19, 20, 21, 22, 23];
  return `<div class="scr">${statusBar()}
<div class="hdr"><span class="rbtn">${icon.back}</span><div style="flex:1" class="bric"><span style="font-weight:700;font-size:20px">${t.schedule}</span></div></div>
<div style="padding:4px 16px;display:flex;flex-direction:column;gap:12px">
 <div class="card"><div class="bric" style="font-weight:600;font-size:17px">${t.every}</div></div>
 <div class="card"><div class="caps">${t.special}</div><div style="display:flex;justify-content:space-between;margin-top:10px"><div><div style="font:600 15px Inst">${t.cancelled}</div><div style="font-size:13px;color:var(--inkM)">${t.reason}</div></div></div></div>
</div>
<div style="position:absolute;inset:0;background:rgba(16,26,20,.45)"></div>
<div class="sheet"><div class="handle"></div>
 <div class="bric" style="font-weight:600;font-size:18px">${t.pickTitle}</div>
 <div style="display:flex;align-items:center;justify-content:space-between;margin:16px 0">
  <span class="rbtn" style="background:var(--s300);box-shadow:none">${icon.minus}</span>
  <div style="display:flex;align-items:center;gap:4px"><div class="seg2 on">20</div><span class="bric" style="font-weight:800;font-size:56px;color:var(--inkM)">:</span><div class="seg2">00</div></div>
  <span class="rbtn" style="background:var(--s300);box-shadow:none">${icon.plus}</span>
 </div>
 <div class="caps">${t.usual}</div>
 <div style="display:flex;gap:8px;flex-wrap:wrap;margin:10px 0 16px">${['18:00', '19:00', '20:00', '21:00', '22:00'].map((h) => `<span class="chip ${h === '20:00' ? 'on' : ''}" style="height:44px">${h}</span>`).join('')}</div>
 <div class="caps" style="margin-bottom:10px">${t.hours}</div>
 <div class="grid">${hours.map((h) => `<div class="${h === 20 ? 'on' : ''}">${h}</div>`).join('')}</div>
 <div class="btn volt" style="margin-top:18px">${t.use}</div>
 <div style="text-align:center;font:600 15px Inst;color:var(--inkM);margin-top:12px">${t.cancel}</div>
</div></div>`;
}

// 6 · A week off, with its reason, in the Match tab
export function weekOff(lang, ios) {
  const t = T[lang];
  return `<div class="scr">${statusBar()}${clubHeader(t, 0)}
<div style="padding:16px;display:flex;flex-direction:column;gap:14px">
 <div class="notice">${icon.alert}<div><div style="font:600 16px Inst">${t.weekOff}</div><div style="font-size:15px;margin-top:2px">${t.reasonLine}</div></div></div>
 <div class="ticket cut fold">
  <div class="caps" style="color:var(--onInvM)">${t.next}</div>
  <div class="bric" style="font-weight:700;font-size:24px;margin-top:6px">${t.nextOne}</div>
  <div style="color:var(--onInvM);margin-top:4px">${t.format}</div>
 </div>
 <div class="card"><div class="caps">${t.called}</div><div style="display:flex;margin-top:12px">${PLAYERS.slice(0, 7).map(([n], i) => `<span style="margin-left:${i ? -10 : 0}px">${avatar(n, 38, i === 1 ? 'me' : '')}</span>`).join('')}<span style="margin-left:10px;align-self:center;color:var(--inkM)">+4</span></div></div>
</div>${nav(t, 1, ios)}</div>`;
}

// 7 · Join a club with a code
export function join(lang, ios) {
  const t = T[lang];
  const cells = t.code.split('').map((c, i) => `${i === 4 ? '<i style="width:10px;height:2px;background:var(--lineS);align-self:center"></i>' : ''}<span style="width:36px;height:50px;border-radius:10px;background:var(--s200);border:1.5px solid ${i === 7 ? 'var(--brand)' : 'var(--lineS)'};display:grid;place-items:center;font:700 22px Bric">${c}</span>`).join('');
  return `<div class="scr">${statusBar()}
<div class="hdr"><span class="rbtn">${icon.back}</span></div>
<div style="padding:10px 24px">
 <div class="crest" style="width:72px;height:72px;font-size:30px;border-radius:18px">A</div>
 <div class="bric" style="font-weight:700;font-size:30px;margin-top:22px">${t.joinTitle}</div>
 <div style="color:var(--inkM);font-size:17px;margin-top:8px;line-height:1.5">${t.joinHint}</div>
 <div style="display:flex;gap:6px;margin-top:28px">${cells}</div>
 <div class="btn volt" style="margin-top:30px">${t.joinCta}</div>
 <div style="text-align:center;color:var(--inkM);margin-top:40px">${t.createHint}</div>
 <div class="btn sec" style="margin-top:12px">${t.createCta}</div>
</div></div>`;
}

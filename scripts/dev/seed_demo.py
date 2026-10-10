#!/usr/bin/env python3
"""
Demo data for a club on the LOCAL backend (spec 018), so every screen of the app can be seen with real data:
a 7-a-side squad, three played matches (results, goals, cards, ratings, form), the next match with the teams
already drawn plus a waitlist, absences and two chats.

Only for a local backend started with `docker compose up` + `bootRun` (dev profile). It talks to the API like the
app, so the backend computes teams, ratings and stats itself; a few dates are moved directly in the local Postgres
(the API cannot create past matches). Never run it against PRE or PRO.

Usage:
  OWNER_EMAIL=… OWNER_PASSWORD=… DEMO_PASSWORD=… python3 scripts/dev/seed_demo.py <clubId>

DEMO_PASSWORD is the password of the demo players (demo.<name>@squadfy.test); pick any valid one
(9+ characters, a digit and an upper-case letter). Running it again reuses the players already created.
"""
import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from datetime import datetime, timedelta, timezone

BASE = os.environ.get("API_BASE", "http://localhost:8080/api/v1")
PG_CONTAINER = os.environ.get("PG_CONTAINER", "squadfy_backend-postgres-1")

# The squad of the mockups: (username, shirt number, position)
TEAM = [
    ("Marcos Peña", 1, "GOALKEEPER"), ("Iván Soto", 4, "DEFENDER"), ("Sergio Mora", 5, "DEFENDER"),
    ("Hugo Prieto", 3, "DEFENDER"), ("Dani Moreno", 8, "MIDFIELDER"), ("Álex Ruiz", 9, "FORWARD"),
    ("Nico Vidal", 13, "GOALKEEPER"), ("Jaime Ortiz", 2, "DEFENDER"), ("Tomás Gallego", 6, "DEFENDER"),
    ("Gabi Sanz", 15, "DEFENDER"), ("Pablo Ruiz", 16, "MIDFIELDER"), ("Nacho León", 14, "MIDFIELDER"),
    ("Leo Vega", 11, "FORWARD"),
]
WAITLIST = [("Rubén Castro", 17, "MIDFIELDER"), ("Kike Lara", 19, "FORWARD")]

# Played matches: (days ago, score A, score B)
HISTORY = [(23, 3, 3), (16, 2, 4), (9, 4, 3)]


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    if data is not None:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            txt = r.read().decode()
            return r.status, (json.loads(txt) if txt else None)
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()


def sql(statement):
    out = subprocess.run(
        ["docker", "exec", PG_CONTAINER, "psql", "-U", "squadfy", "-d", "squadfy", "-At", "-c", statement],
        capture_output=True, text=True, check=True
    )
    return out.stdout.strip()


def login(email, password):
    st, auth = call("POST", "/auth/login", {"email": email, "password": password})
    assert st == 200, (email, st, auth)
    return auth


def player(name, password):
    slug = name.lower().replace(" ", ".").replace("ñ", "n").replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
    email = f"demo.{slug}@squadfy.test"
    st, _ = call("POST", "/auth/register", {"email": email, "username": name, "password": password})
    if st not in (200, 201, 409, 400):
        raise SystemExit(f"register {email}: {st} {_}")
    return login(email, password)


def iso(dt):
    return dt.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def main():
    club_id = sys.argv[1]
    owner = login(os.environ["OWNER_EMAIL"], os.environ["OWNER_PASSWORD"])
    owner_token = owner["accessToken"]
    demo_password = os.environ["DEMO_PASSWORD"]

    st, club = call("GET", f"/clubs/{club_id}", token=owner_token)
    assert st == 200, (st, club)
    code = club["invitationCode"]
    print("club", club["name"], code)

    tokens = {}
    for name, number, position in TEAM + WAITLIST:
        auth = player(name, demo_password)
        tokens[name] = auth["accessToken"]
        st, res = call("POST", "/clubs/join", {"invitationCode": code, "shirtNumber": number, "position": position}, auth["accessToken"])
        print("join", name, st if st != 200 else "ok")

    st, members = call("GET", f"/clubs/{club_id}/members", token=owner_token)
    assert st == 200, (st, members)
    member_of = {m["username"]: m["id"] for m in members}
    owner_member = next(m["id"] for m in members if m["userId"] == owner["user"]["id"])
    squad_tokens = [owner_token] + [tokens[n] for n, _, _ in TEAM]

    def enroll_all(match_id, extra=()):
        st, ann = call("GET", f"/matches/{match_id}/announcement", token=owner_token)
        assert st == 200, (st, ann)
        for token in squad_tokens + [tokens[n] for n in extra]:
            call("POST", f"/announcements/{ann['id']}/enrollment", token=token)
        return ann["id"]

    now = datetime.now(timezone.utc)

    # 1) Played matches: created in the future through the API, filled, then moved to their past date and closed
    already = int(sql(f"select count(*) from match_service.matches where club_id='{club_id}' and status='COMPLETED'") or 0)
    if already >= len(HISTORY):
        print("history already seeded")
    else:
        for index, (days_ago, score_a, score_b) in enumerate(HISTORY):
            st, match = call("POST", f"/clubs/{club_id}/matches", {
                "scheduledAt": iso(now + timedelta(days=30 + index)), "format": "SEVEN_A_SIDE", "durationMinutes": 60
            }, owner_token)
            assert st == 201, (st, match)
            match_id = match["id"]
            sql(f"update match_service.match_announcements set opens_at=now()-interval '1 hour', closes_at=now()+interval '1 day', "
                f"draw_at=now()+interval '1 day', status='OPEN', max_players=14 where match_id='{match_id}'")
            enroll_all(match_id)
            st, teams = call("POST", f"/matches/{match_id}/teams", {"mode": "AUTO"}, owner_token)
            assert st in (200, 201), (st, teams)
            played = (now - timedelta(days=days_ago)).replace(hour=18, minute=0, second=0, microsecond=0)
            sql(f"update match_service.matches set scheduled_at='{iso(played)}' where id='{match_id}'")
            sql(f"update match_service.match_announcements set opens_at='{iso(played - timedelta(days=6))}', "
                f"closes_at='{iso(played - timedelta(hours=20))}', draw_at='{iso(played - timedelta(hours=19))}', "
                f"teams_published_at='{iso(played - timedelta(hours=19))}', status='CLOSED' where match_id='{match_id}'")
            st, detail = call("GET", f"/matches/{match_id}", token=owner_token)
            assert st == 200, (st, detail)
            # MatchDto: teamA / teamB are the clubMemberIds of each team
            team_a, team_b = detail.get("teamA", []), detail.get("teamB", [])
            minute = 6
            for team, goals in ((team_a, score_a), (team_b, score_b)):
                outfield = [m for m in team if m != team[0]] or team
                for g in range(goals):
                    scorer = outfield[(g * 2 + index) % len(outfield)]
                    assist = outfield[(g * 2 + index + 1) % len(outfield)]
                    call("POST", f"/matches/{match_id}/events", {"clubMemberId": scorer, "type": "GOAL", "minute": minute}, owner_token)
                    if assist != scorer:
                        call("POST", f"/matches/{match_id}/events", {"clubMemberId": assist, "type": "ASSIST", "minute": minute}, owner_token)
                    minute += 7
            if team_b:
                call("POST", f"/matches/{match_id}/events", {"clubMemberId": team_b[-1], "type": "YELLOW_CARD", "minute": 38}, owner_token)
            st, done = call("POST", f"/matches/{match_id}/complete", token=owner_token)
            print("played", played.date(), f"{score_a}-{score_b}", st)

    # 2) The next match: 14 called up, a waitlist and the teams already drawn and published
    st, current = call("GET", f"/clubs/{club_id}/announcements/current", token=owner_token)
    assert st == 200 and current, (st, current)
    announcement = current.get("announcement", current)
    match_id = announcement["matchId"]
    sql(f"update match_service.match_announcements set max_players=14, status='OPEN', "
        f"opens_at=least(opens_at, now()-interval '1 hour'), closes_at=greatest(closes_at, now()+interval '1 day') "
        f"where match_id='{match_id}'")
    enroll_all(match_id, extra=[n for n, _, _ in WAITLIST])
    st, teams = call("POST", f"/matches/{match_id}/teams", {"mode": "AUTO"}, owner_token)
    print("next match teams", st)
    # Pre-match state: sign-up closed and teams published (the scheduler will not draw again)
    sql(f"update match_service.match_announcements set status='CLOSED', closes_at=now()-interval '30 minutes', "
        f"draw_at=now()-interval '20 minutes', teams_published_at=now()-interval '20 minutes' where match_id='{match_id}'")

    # 3) Absences of two players (after the next match, so they stay called up)
    for name, start, length, reason in (("Marcos Peña", 12, 7, "Vacaciones"), ("Iván Soto", 20, 14, "Lesión")):
        body = {"fromDate": str((now + timedelta(days=start)).date()), "toDate": str((now + timedelta(days=start + length)).date()), "reason": reason}
        st, _ = call("POST", f"/clubs/{club_id}/members/me/absences", body, tokens[name])
        print("absence", name, st)

    # 4) Two chats with a few messages (messages travel over the WebSocket, so they are written to the local DB)
    users = {m["username"]: m["userId"] for m in members}
    owner_user = owner["user"]["id"]
    for other, lines in (("Álex Ruiz", ["¿Alguien lleva petos el jueves?", "Yo llevo los verdes", "Perfecto, nos vemos a las 20:00"]),
                         ("Dani Moreno", ["Me apunto, pero llego justo", "Sin problema, te guardo sitio"])):
        st, chat = call("POST", "/chats", {"otherUserIds": [users[other]]}, owner_token)
        if st not in (200, 201):
            print("chat", other, st)
            continue
        for i, text in enumerate(lines):
            sender = users[other] if i % 2 == 0 else owner_user
            when = iso(now - timedelta(minutes=30 - i * 3))
            sql(f"insert into chat_service.chat_messages(id, chat_id, sender_id, content, created_at) "
                f"values (gen_random_uuid(), '{chat['id']}', '{sender}', '{text}', '{when}')")
        print("chat", other, "ok")
    print("done")


if __name__ == "__main__":
    main()

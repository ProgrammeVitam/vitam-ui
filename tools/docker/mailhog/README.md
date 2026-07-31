# MailHog

SMTP + web UI sink for local dev. Any service pointing `spring.mail.host=localhost` / `spring.mail.port=1025` will deliver here, and messages show up on <http://localhost:8025>.

Currently used by `api/auth-server` for password-reset emails (chantier #6c).

## Start / stop

```bash
./start_dev.sh
./stop.sh
```

## Verify

```bash
# Send a probe from the CLI
python3 -c '
import smtplib, email.mime.text as t
m = t.MIMEText("probe body")
m["From"] = "probe@vitamui.local"; m["To"] = "user@vitamui.local"; m["Subject"] = "probe"
smtplib.SMTP("localhost", 1025).send_message(m)
'
# Then open http://localhost:8025
```

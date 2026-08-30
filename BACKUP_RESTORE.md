# Database Backup & Restore Runbook

Verified procedure (pre-deployment test pass) — `pg_dump`/`pg_restore` against the app's own
Postgres database, using the same tools already installed alongside the local Postgres server.

## Backup

```bash
PGPASSWORD=<db_password> pg_dump -h <host> -U <db_user> -Fc \
  -f backup_$(date +%Y%m%d_%H%M%S).dump \
  <database_name>
```

- `-Fc` = custom format (compressed, supports selective/parallel restore) — always use this over
  plain SQL dumps for anything beyond a quick manual inspection.
- Run this on a schedule (cron / Cloud Scheduler / your host's managed backup feature) — this
  runbook covers the mechanism, not a schedule. For a Cloud SQL deployment specifically, prefer
  Cloud SQL's built-in automated backups + point-in-time recovery over a manually-scripted
  `pg_dump`, and treat this procedure as the fallback/manual-verification path.

## Restore (into a NEW database — never restore over the live one directly)

```bash
PGPASSWORD=<db_password> createdb -h <host> -U <db_user> <restore_target_db>
PGPASSWORD=<db_password> pg_restore -h <host> -U <db_user> -d <restore_target_db> backup_....dump
```

## Verify after restore, before trusting it

Compare row counts on the business-critical tables between source and restored DB — at minimum:
`users`, `roles`, `customers`, `vehicles`, `job_cards`, `invoices`, `payment_transactions`,
`employee_salary_config`, `salary_payments`. Spot-check the newest row in `invoices` and
`salary_payments` matches exactly (all monetary fields, status).

This was executed end-to-end during the pre-deployment test pass: dumped the dev DB, restored
into `inventorymanagementsystem_restore_test`, confirmed every table's row count and the latest
`salary_payments` row matched byte-for-byte between source and restored copies, then dropped the
temporary restore DB. No data loss or corruption observed.

## Cutting over to a restored DB (disaster recovery)

1. Stop the application (or put it in maintenance mode) — never restore into a DB the app is
   actively writing to.
2. Restore as above into a new DB name.
3. Verify per the checklist above.
4. Point `SPRING_DATASOURCE_URL` at the restored DB (or rename databases at the Postgres level)
   and restart the application.
5. Only after the app is confirmed healthy against the restored DB, decommission the old one.

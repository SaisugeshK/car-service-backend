# Deploying v2 (car-service-v2-backend + car-service-v2-frontend)

One Docker image: the React build is baked into the Spring Boot jar (`car-service-backend/Dockerfile`),
deployed to Cloud Run as `car-service-app` in `asia-south1`.

Production runs `spring.jpa.hibernate.ddl-auto=validate` — **the database must already have every new
table and column before the new backend starts**, or it will refuse to boot. Do the steps in order.

Placeholders: `<PROJECT_ID>`, `<DB_HOST>`, `<DB_USER>`, `<DB_NAME>`, `<IMAGE>` (e.g.
`asia-south1-docker.pkg.dev/<PROJECT_ID>/car-service/car-service-app:v2`).

---

## 1. Back up the production database

```bash
pg_dump -h <DB_HOST> -U <DB_USER> -d <DB_NAME> -Fc -f car-service-before-v2.dump
```

Keep this file until v2 has run cleanly for a few days.

## 2. Check for duplicate registration numbers

v2 makes registration numbers unique (ignoring spaces, dashes and case). Find any duplicates first:

```sql
SELECT UPPER(REPLACE(REPLACE(registration_number,' ',''),'-','')) AS plate, COUNT(*), ARRAY_AGG(vehicle_id)
FROM vehicles WHERE registration_number IS NOT NULL AND registration_number <> ''
GROUP BY 1 HAVING COUNT(*) > 1;
```

If this returns rows, fix them (merge or correct the plate) before step 3 — otherwise the last script
fails on the unique index.

## 3. Run the three migration scripts (in this order)

```bash
cd car-service-backend/src/main/resources/db
psql -h <DB_HOST> -U <DB_USER> -d <DB_NAME> -v ON_ERROR_STOP=1 -f manual-migration-offer-coupons.sql
psql -h <DB_HOST> -U <DB_USER> -d <DB_NAME> -v ON_ERROR_STOP=1 -f manual-migration-expenses.sql
psql -h <DB_HOST> -U <DB_USER> -d <DB_NAME> -v ON_ERROR_STOP=1 -f manual-migration-customer-visits.sql
```

All three are additive (new tables/columns/indexes only) and safe to re-run. Nothing is dropped.

## 4. Users and roles

v2 has two roles: **SUPER_ADMIN** (everything) and **EMPLOYEE** (own work, read-only reference data,
no revenue/salary). The old MANAGER role is gone — a MANAGER user would be refused almost everywhere.

See who is affected:

```sql
SELECT u.user_id, u.username, u.email, r.role_name
FROM users u LEFT JOIN roles r ON r.role_id = u.role_id ORDER BY u.user_id;
```

Decide for each MANAGER user, then:

```sql
-- make someone an admin
UPDATE users SET role_id = (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN') WHERE email = '...';
-- everyone still on MANAGER becomes EMPLOYEE
UPDATE users SET role_id = (SELECT role_id FROM roles WHERE role_name = 'EMPLOYEE')
WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'MANAGER');
DELETE FROM roles WHERE role_name = 'MANAGER';
```

(If you skip this, the backend moves any user whose role no longer exists to EMPLOYEE on startup — but
MANAGER users keep MANAGER until the role row is deleted.)

Anyone logged in keeps their old token until it expires; ask users to log out and back in.

## 5. File storage for receipts and inspection photos

Expense receipts (`app.expense-receipt-dir`) and inspection photos (`app.upload-dir`) are written to
local disk. **Cloud Run's disk is wiped on every restart or redeploy**, so uploads would vanish.
Mount a Cloud Storage bucket and point both folders at it:

```bash
gcloud run services update car-service-app --region asia-south1 \
  --add-volume name=uploads,type=cloud-storage,bucket=<BUCKET> \
  --add-volume-mount volume=uploads,mount-path=/app/uploads \
  --update-env-vars APP_UPLOADDIR=/app/uploads/inspection-photos,APP_EXPENSERECEIPTDIR=/app/uploads/expense-receipts
```

(Or accept the limitation for now and tell staff receipts are temporary.)

## 6. Build and deploy

From the folder that contains **both** `car-service-backend/` and `car-service-frontend/`, each on its
v2 branch. `--no-cache` matters: a cached layer once shipped an old jar (pages 404'd in production).

```bash
git -C car-service-backend  checkout car-service-v2-backend  && git -C car-service-backend  pull
git -C car-service-frontend checkout car-service-v2-frontend && git -C car-service-frontend pull

docker build --no-cache -f car-service-backend/Dockerfile -t <IMAGE> .
docker push <IMAGE>
gcloud run deploy car-service-app --image <IMAGE> --region asia-south1
```

Existing environment variables (database, JWT secret, `SPRING_PROFILES_ACTIVE=prod`, CORS) are kept by
`gcloud run deploy`. The frontend uses `VITE_API_BASE_URL=/api` from `.env.production` — same origin.

On first start the backend also: gives existing offers a coupon code, turns every existing job card into
a visit, and calculates each customer's New/Occasional/Regular status. This is automatic and idempotent.

## 7. Verify

```bash
BASE=https://<your-cloud-run-url>
curl -s $BASE/actuator/health                       # {"status":"UP"}
TOKEN=$(curl -s -X POST $BASE/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"<admin email>","password":"<password>"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
for p in expenses visits offers job-cards; do
  echo "$p -> $(curl -s -o /dev/null -w '%{http_code}' $BASE/api/$p -H "Authorization: Bearer $TOKEN")"
done                                                  # all 200
```

Then, from `car-service-frontend/`, run the **read-only** E2E checks against production (login + every
page + permission checks; they create no data):

```bash
E2E_BASE_URL=$BASE E2E_API_URL=$BASE \
E2E_SA_EMAIL=... E2E_SA_PASSWORD=... E2E_EMP_EMAIL=... E2E_EMP_PASSWORD=... \
npx playwright test e2e/01-auth.spec.js e2e/02-rbac-and-pages.spec.js --project=chromium
```

Do **not** run specs 03–11 against production — they create and delete test records.

Manual spot-check: log in as an employee → no revenue on the dashboard, no Invoices/Offers/Payroll in
the menu; as admin → open a job card, add a service, see the offer, take a payment.

## 8. Rollback

```bash
gcloud run revisions list --service car-service-app --region asia-south1
gcloud run services update-traffic car-service-app --region asia-south1 --to-revisions=<PREVIOUS_REVISION>=100
```

The database changes are additive, so the previous version keeps working on the migrated database —
no need to restore the backup unless data itself went wrong. If you deleted the MANAGER role in step 4
and roll back, re-create it: `INSERT INTO roles (role_name, description, created_at, updated_at) VALUES ('MANAGER', 'Operational access', NOW(), NOW());`

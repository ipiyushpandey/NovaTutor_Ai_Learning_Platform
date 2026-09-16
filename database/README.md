# NovaTutor database

- Production uses `ddl-auto=validate`; schema changes should be applied through an operator-approved migration before deployment.
- Startup avoids destructive curriculum and quiz rebuilds when a database already contains data.
- Before a production migration, take a database backup and test the migration on a staging copy.

The `migrations/` directory is reserved for ordered schema changes when a migration runner is introduced.

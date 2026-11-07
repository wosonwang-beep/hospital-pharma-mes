-- Current maintenance metadata only; legacy rows and frozen evidence remain unchanged.
ALTER TABLE proc_package
  ADD COLUMN package_name VARCHAR(200) NULL,
  ADD COLUMN description VARCHAR(1000) NULL;

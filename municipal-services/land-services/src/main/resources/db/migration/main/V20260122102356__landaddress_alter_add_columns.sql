ALTER TABLE ug_land_address
    ADD COLUMN IF NOT EXISTS same_as_site_address boolean;

ALTER TABLE ug_land_address_audit_details
    ADD COLUMN IF NOT EXISTS same_as_site_address boolean;

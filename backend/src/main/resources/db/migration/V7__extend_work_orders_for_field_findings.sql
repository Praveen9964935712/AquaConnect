ALTER TABLE work_orders
    ADD COLUMN inspection_notes VARCHAR(2000),
    ADD COLUMN observed_condition VARCHAR(1000),
    ADD COLUMN repair_notes VARCHAR(2000);

INSERT INTO brands (name) VALUES
    ('Samsung'),
    ('LG'),
    ('TCL'),
    ('Hisense'),
    ('Philips'),
    ('AOC'),
    ('Philco'),
    ('Toshiba')
ON CONFLICT DO NOTHING;


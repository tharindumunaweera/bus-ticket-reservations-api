--Insert 40 seats
INSERT INTO seats (seat_number, is_bookedad, is_bookedda) VALUES
('1A', FALSE, FALSE), ('1B', FALSE, FALSE), ('1C', FALSE, FALSE), ('1D', FALSE, FALSE),
('2A', FALSE, FALSE), ('2B', FALSE, FALSE), ('2C', FALSE, FALSE), ('2D', FALSE, FALSE),
('3A', FALSE, FALSE), ('3B', FALSE, FALSE), ('3C', FALSE, FALSE), ('3D', FALSE, FALSE),
('4A', FALSE, FALSE), ('4B', FALSE, FALSE), ('4C', FALSE, FALSE), ('4D', FALSE, FALSE),
('5A', FALSE, FALSE), ('5B', FALSE, FALSE), ('5C', FALSE, FALSE), ('5D', FALSE, FALSE),
('6A', FALSE, FALSE), ('6B', FALSE, FALSE), ('6C', FALSE, FALSE), ('6D', FALSE, FALSE),
('7A', FALSE, FALSE), ('7B', FALSE, FALSE), ('7C', FALSE, FALSE), ('7D', FALSE, FALSE),
('8A', FALSE, FALSE), ('8B', FALSE, FALSE), ('8C', FALSE, FALSE), ('8D', FALSE, FALSE),
('9A', FALSE, FALSE), ('9B', FALSE, FALSE), ('9C', FALSE, FALSE), ('9D', FALSE, FALSE),
('10A', FALSE, FALSE), ('10B', FALSE, FALSE), ('10C', FALSE, FALSE), ('10D', FALSE, FALSE);


-- Insert routes with prices
INSERT INTO routes (from_location, to_location, price) VALUES
('A', 'B', 50.00),
('A', 'C', 100.00),
('A', 'D', 150.00),
('B', 'C', 50.00),
('B', 'D', 100.00),
('C', 'D', 50.00),
('B', 'A', 50.00),
('C', 'A', 100.00),
('D', 'A', 150.00),
('C', 'B', 50.00),
('D', 'B', 100.00),
('D', 'C', 50.00);
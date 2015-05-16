Delete from "Sector"; DELETE from "Education";
insert into "Sector" ("name", "country", "year", "averageSalary", "code") VALUES 
('Zemědělství, lesnictví, rybářství', 'cz', '2012', 16436, 'A'), 
('Zemědělství, lesnictví, rybářství', 'cz', '2011', 18622, 'A'), 
('Těžba a dobývání', 'cz', '2012', 32498, 'B'), 
('Těžba a dobývání', 'cz', '2011', 31570, 'B'), 
('Zpracovatelský průmysl', 'cz', '2012', 24572, 'C'), 
('Zpracovatelský průmysl', 'cz', '2011', 23798, 'C'),
('Zemědělství, lesnictví, rybářství', 'sk', '2012', 14436, 'A'), 
('Zemědělství, lesnictví, rybářství', 'sk', '2011', 20622, 'A'), 
('Těžba a dobývání', 'sk', '2012', 28498, 'B'), 
('Těžba a dobývání', 'sk', '2011', 33570, 'B'), 
('Zpracovatelský průmysl', 'sk', '2012', 23572, 'C'), 
('Zpracovatelský průmysl', 'sk', '2011', 25798, 'C');

insert into "Education" ("degree", "country", "year", "averageSalary", "sex") VALUES
('Zakladni', 'sk', '2013', 692, 'muzi'),
('Zakladni', 'sk', '2013', 501, 'zeny'),
('Zakladni', 'sk', '2012', 663, 'muzi'),
('Zakladni', 'sk', '2012', 482, 'zeny'),

('Ucnovske bez maturity', 'sk', '2013', 778, 'muzi'),
('Ucnovske bez maturity', 'sk', '2013', 541, 'zeny'),
('Ucnovske bez maturity', 'sk', '2012', 761, 'muzi'),
('Ucnovske bez maturity', 'sk', '2012', 525, 'zeny'),

('Stredni bez maturity', 'sk', '2013', 758, 'muzi'),
('Stredni bez maturity', 'sk', '2013', 564, 'zeny'),
('Stredni bez maturity', 'sk', '2012', 737, 'muzi'),
('Stredni bez maturity', 'sk', '2012', 540, 'zeny');

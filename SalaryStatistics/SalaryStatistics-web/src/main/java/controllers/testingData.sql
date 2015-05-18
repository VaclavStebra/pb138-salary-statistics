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

insert into "Region" ("name", "country", "year", "averageSalary", "sex") VALUES
('Hlavní město Praha', 'cz', '2012', 38295, 'muzi'),
('Středočeský kraj', 'cz', '2012', 29090, 'muzi'),
('Jihočeský kraj', 'cz', '2012', 26166, 'muzi'),
('Plzeňský kraj', 'cz', '2012', 27437, 'muzi'),
('Hlavní město Praha', 'cz', '2012', 22683, 'zeny'),
('Středočeský kraj', 'cz', '2012', 29780, 'zeny'),
('Jihočeský kraj', 'cz', '2012', 22129, 'zeny'),
('Plzeňský kraj', 'cz', '2012', 20662, 'zeny'),
('Hlavní město Praha', 'cz', '2011', 37346, 'muzi'),
('Středočeský kraj', 'cz', '2011', 28316, 'muzi'),
('Jihočeský kraj', 'cz', '2011', 25721, 'muzi'),
('Plzeňský kraj', 'cz', '2011', 26349, 'muzi'),
('Hlavní město Praha', 'cz', '2011', 28984, 'zeny'),
('Středočeský kraj', 'cz', '2011', 22046, 'zeny'),
('Jihočeský kraj', 'cz', '2011', 20171, 'zeny'),
('Plzeňský kraj', 'cz', '2011', 21264, 'zeny')
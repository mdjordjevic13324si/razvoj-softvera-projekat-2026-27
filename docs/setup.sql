-- Pokrenuti JEDNOM kao root (MySQL Workbench -> Local instance -> nalepiti -> munja).
-- Pravi korisnika kog aplikacija koristi; tabele pravi aplikacija sama pri pokretanju.
CREATE USER IF NOT EXISTS 'studenti_app'@'localhost' IDENTIFIED BY 'studenti123';
GRANT ALL PRIVILEGES ON studenti_baza.* TO 'studenti_app'@'localhost';

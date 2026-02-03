-- =====================================================
-- GESTIONE INGROSSO - Schema Database MySQL
-- =====================================================
-- Eseguire questo script per creare il database completo
-- =====================================================

-- Creazione database (eseguire come root se necessario)
-- CREATE DATABASE IF NOT EXISTS gestione_ingrosso CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE gestione_ingrosso;

-- =====================================================
-- CONFIGURAZIONE AZIENDA
-- =====================================================
CREATE TABLE IF NOT EXISTS config_azienda (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    indirizzo VARCHAR(255),
    citta VARCHAR(100),
    cap VARCHAR(10),
    provincia VARCHAR(2),
    piva VARCHAR(20),
    codice_fiscale VARCHAR(20),
    telefono VARCHAR(50),
    email VARCHAR(100),
    logo MEDIUMBLOB,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- UTENTI E AUTENTICAZIONE
-- =====================================================
CREATE TABLE IF NOT EXISTS utenti (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nome VARCHAR(100),
    cognome VARCHAR(100),
    ruolo ENUM('ADMIN', 'OPERATORE', 'VISUALIZZATORE') DEFAULT 'OPERATORE',
    attivo BOOLEAN DEFAULT TRUE,
    ultimo_accesso DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ANAGRAFICA PRODOTTI
-- =====================================================
CREATE TABLE IF NOT EXISTS categorie (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descrizione TEXT,
    ordine INT DEFAULT 0,
    attiva BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sottocategorie (
    id INT PRIMARY KEY AUTO_INCREMENT,
    categoria_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descrizione TEXT,
    ordine INT DEFAULT 0,
    attiva BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (categoria_id) REFERENCES categorie(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS unita_misura (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL,
    simbolo VARCHAR(10) NOT NULL,
    decimali INT DEFAULT 2,
    attiva BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prodotti (
    id INT PRIMARY KEY AUTO_INCREMENT,
    codice VARCHAR(50) UNIQUE NOT NULL,
    barcode VARCHAR(50),
    nome VARCHAR(255) NOT NULL,
    descrizione TEXT,
    sottocategoria_id INT,
    unita_misura_id INT NOT NULL,
    scorta_minima DECIMAL(15,3) DEFAULT 0,
    scorta_massima DECIMAL(15,3),
    prezzo_acquisto DECIMAL(15,4),
    prezzo_vendita DECIMAL(15,4),
    aliquota_iva DECIMAL(5,2) DEFAULT 22.00,
    gestisce_lotti BOOLEAN DEFAULT FALSE,
    note TEXT,
    attivo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sottocategoria_id) REFERENCES sottocategorie(id) ON DELETE SET NULL,
    FOREIGN KEY (unita_misura_id) REFERENCES unita_misura(id),
    INDEX idx_codice (codice),
    INDEX idx_barcode (barcode),
    INDEX idx_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- MAGAZZINO
-- =====================================================
CREATE TABLE IF NOT EXISTS magazzini (
    id INT PRIMARY KEY AUTO_INCREMENT,
    codice VARCHAR(20) UNIQUE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    indirizzo VARCHAR(255),
    citta VARCHAR(100),
    principale BOOLEAN DEFAULT FALSE,
    attivo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS giacenze (
    id INT PRIMARY KEY AUTO_INCREMENT,
    prodotto_id INT NOT NULL,
    magazzino_id INT NOT NULL,
    quantita DECIMAL(15,3) DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (prodotto_id) REFERENCES prodotti(id) ON DELETE CASCADE,
    FOREIGN KEY (magazzino_id) REFERENCES magazzini(id) ON DELETE CASCADE,
    UNIQUE KEY uk_prodotto_magazzino (prodotto_id, magazzino_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lotti (
    id INT PRIMARY KEY AUTO_INCREMENT,
    prodotto_id INT NOT NULL,
    magazzino_id INT NOT NULL,
    numero_lotto VARCHAR(50) NOT NULL,
    data_produzione DATE,
    data_scadenza DATE,
    quantita DECIMAL(15,3) DEFAULT 0,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prodotto_id) REFERENCES prodotti(id) ON DELETE CASCADE,
    FOREIGN KEY (magazzino_id) REFERENCES magazzini(id) ON DELETE CASCADE,
    INDEX idx_scadenza (data_scadenza)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS movimenti (
    id INT PRIMARY KEY AUTO_INCREMENT,
    prodotto_id INT NOT NULL,
    magazzino_id INT NOT NULL,
    lotto_id INT,
    tipo ENUM('CARICO', 'SCARICO', 'RETTIFICA', 'TRASFERIMENTO') NOT NULL,
    quantita DECIMAL(15,3) NOT NULL,
    quantita_precedente DECIMAL(15,3),
    quantita_successiva DECIMAL(15,3),
    causale VARCHAR(100),
    documento_rif VARCHAR(100),
    magazzino_destinazione_id INT,
    utente_id INT,
    note TEXT,
    data_movimento DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prodotto_id) REFERENCES prodotti(id) ON DELETE CASCADE,
    FOREIGN KEY (magazzino_id) REFERENCES magazzini(id) ON DELETE CASCADE,
    FOREIGN KEY (lotto_id) REFERENCES lotti(id) ON DELETE SET NULL,
    FOREIGN KEY (magazzino_destinazione_id) REFERENCES magazzini(id) ON DELETE SET NULL,
    FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE SET NULL,
    INDEX idx_data (data_movimento),
    INDEX idx_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DDT (Documenti di Trasporto)
-- =====================================================
CREATE TABLE IF NOT EXISTS destinatari (
    id INT PRIMARY KEY AUTO_INCREMENT,
    codice VARCHAR(50),
    ragione_sociale VARCHAR(255) NOT NULL,
    indirizzo VARCHAR(255),
    citta VARCHAR(100),
    cap VARCHAR(10),
    provincia VARCHAR(2),
    piva VARCHAR(20),
    codice_fiscale VARCHAR(20),
    telefono VARCHAR(50),
    email VARCHAR(100),
    note TEXT,
    attivo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ddt (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero INT NOT NULL,
    anno INT NOT NULL,
    data_documento DATE NOT NULL,
    data_trasporto DATETIME,
    destinatario_id INT,
    destinazione_diversa VARCHAR(255),
    magazzino_id INT NOT NULL,
    causale_trasporto VARCHAR(100) DEFAULT 'Vendita',
    aspetto_beni VARCHAR(100),
    colli INT,
    peso_kg DECIMAL(10,2),
    porto VARCHAR(50) DEFAULT 'Franco',
    vettore VARCHAR(100),
    note TEXT,
    utente_id INT,
    stato ENUM('BOZZA', 'EMESSO', 'ANNULLATO') DEFAULT 'BOZZA',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (destinatario_id) REFERENCES destinatari(id) ON DELETE SET NULL,
    FOREIGN KEY (magazzino_id) REFERENCES magazzini(id),
    FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE SET NULL,
    UNIQUE KEY uk_numero_anno (numero, anno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ddt_righe (
    id INT PRIMARY KEY AUTO_INCREMENT,
    ddt_id INT NOT NULL,
    prodotto_id INT NOT NULL,
    lotto_id INT,
    descrizione VARCHAR(255),
    quantita DECIMAL(15,3) NOT NULL,
    unita_misura VARCHAR(10),
    prezzo_unitario DECIMAL(15,4),
    aliquota_iva DECIMAL(5,2),
    ordine INT DEFAULT 0,
    FOREIGN KEY (ddt_id) REFERENCES ddt(id) ON DELETE CASCADE,
    FOREIGN KEY (prodotto_id) REFERENCES prodotti(id),
    FOREIGN KEY (lotto_id) REFERENCES lotti(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- LISTINI
-- =====================================================
CREATE TABLE IF NOT EXISTS listini (
    id INT PRIMARY KEY AUTO_INCREMENT,
    codice VARCHAR(20) UNIQUE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descrizione TEXT,
    tipo ENUM('ACQUISTO', 'VENDITA') NOT NULL,
    data_validita_inizio DATE,
    data_validita_fine DATE,
    principale BOOLEAN DEFAULT FALSE,
    attivo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS listini_prezzi (
    id INT PRIMARY KEY AUTO_INCREMENT,
    listino_id INT NOT NULL,
    prodotto_id INT NOT NULL,
    prezzo DECIMAL(15,4) NOT NULL,
    data_inizio DATE NOT NULL,
    data_fine DATE,
    FOREIGN KEY (listino_id) REFERENCES listini(id) ON DELETE CASCADE,
    FOREIGN KEY (prodotto_id) REFERENCES prodotti(id) ON DELETE CASCADE,
    UNIQUE KEY uk_listino_prodotto_data (listino_id, prodotto_id, data_inizio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS storico_prezzi (
    id INT PRIMARY KEY AUTO_INCREMENT,
    prodotto_id INT NOT NULL,
    tipo ENUM('ACQUISTO', 'VENDITA') NOT NULL,
    prezzo_precedente DECIMAL(15,4),
    prezzo_nuovo DECIMAL(15,4) NOT NULL,
    variazione_percentuale DECIMAL(8,2),
    utente_id INT,
    data_modifica TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prodotto_id) REFERENCES prodotti(id) ON DELETE CASCADE,
    FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- NUMERATORI
-- =====================================================
CREATE TABLE IF NOT EXISTS numeratori (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(50) NOT NULL,
    anno INT NOT NULL,
    ultimo_numero INT DEFAULT 0,
    UNIQUE KEY uk_tipo_anno (tipo, anno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- VISTE
-- =====================================================
CREATE OR REPLACE VIEW v_giacenze_complete AS
SELECT
    p.id as prodotto_id,
    p.codice,
    p.nome as prodotto,
    c.nome as categoria,
    sc.nome as sottocategoria,
    m.id as magazzino_id,
    m.nome as magazzino,
    COALESCE(g.quantita, 0) as giacenza,
    um.simbolo as unita_misura,
    p.scorta_minima,
    p.prezzo_acquisto,
    p.prezzo_vendita,
    COALESCE(g.quantita, 0) * COALESCE(p.prezzo_acquisto, 0) as valore_acquisto,
    COALESCE(g.quantita, 0) * COALESCE(p.prezzo_vendita, 0) as valore_vendita
FROM prodotti p
LEFT JOIN sottocategorie sc ON p.sottocategoria_id = sc.id
LEFT JOIN categorie c ON sc.categoria_id = c.id
LEFT JOIN unita_misura um ON p.unita_misura_id = um.id
CROSS JOIN magazzini m
LEFT JOIN giacenze g ON p.id = g.prodotto_id AND m.id = g.magazzino_id
WHERE p.attivo = TRUE AND m.attivo = TRUE;

CREATE OR REPLACE VIEW v_prodotti_sotto_scorta AS
SELECT * FROM v_giacenze_complete
WHERE giacenza < scorta_minima AND scorta_minima > 0;

CREATE OR REPLACE VIEW v_lotti_in_scadenza AS
SELECT
    l.*,
    p.codice,
    p.nome as prodotto,
    m.nome as magazzino,
    DATEDIFF(l.data_scadenza, CURDATE()) as giorni_a_scadenza
FROM lotti l
JOIN prodotti p ON l.prodotto_id = p.id
JOIN magazzini m ON l.magazzino_id = m.id
WHERE l.quantita > 0
  AND l.data_scadenza IS NOT NULL
  AND l.data_scadenza <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)
ORDER BY l.data_scadenza;

-- =====================================================
-- DATI INIZIALI
-- =====================================================

-- Unita di misura predefinite
INSERT INTO unita_misura (nome, simbolo, decimali, attiva) VALUES
('Chilogrammo', 'Kg', 3, TRUE),
('Grammo', 'g', 0, TRUE),
('Litro', 'Lt', 3, TRUE),
('Millilitro', 'ml', 0, TRUE),
('Pezzo', 'Pz', 0, TRUE),
('Confezione', 'Conf', 0, TRUE),
('Cartone', 'Cart', 0, TRUE),
('Metro', 'm', 2, TRUE),
('Metro quadro', 'm2', 2, TRUE),
('Quintale', 'q', 2, TRUE)
ON DUPLICATE KEY UPDATE nome=nome;

-- Magazzino principale
INSERT INTO magazzini (codice, nome, principale, attivo) VALUES
('MAG01', 'Magazzino Principale', TRUE, TRUE)
ON DUPLICATE KEY UPDATE nome=nome;

-- Utente admin di default (password: admin123)
-- Hash BCrypt per 'admin123'
INSERT INTO utenti (username, password_hash, nome, cognome, ruolo, attivo) VALUES
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.J.JHQO9cNKzMma', 'Amministratore', 'Sistema', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Configurazione azienda di default
INSERT INTO config_azienda (id, nome) VALUES (1, 'La Mia Azienda')
ON DUPLICATE KEY UPDATE id=id;

-- Categorie di esempio
INSERT INTO categorie (nome, descrizione, ordine, attiva) VALUES
('Alimentari', 'Prodotti alimentari', 1, TRUE),
('Bevande', 'Bevande alcoliche e analcoliche', 2, TRUE),
('Casalinghi', 'Prodotti per la casa', 3, TRUE),
('Igiene', 'Prodotti per igiene personale e pulizia', 4, TRUE)
ON DUPLICATE KEY UPDATE nome=nome;

-- Sottocategorie di esempio
INSERT INTO sottocategorie (categoria_id, nome, descrizione, ordine, attiva)
SELECT c.id, 'Pasta', 'Pasta secca e fresca', 1, TRUE FROM categorie c WHERE c.nome = 'Alimentari'
ON DUPLICATE KEY UPDATE nome=nome;

INSERT INTO sottocategorie (categoria_id, nome, descrizione, ordine, attiva)
SELECT c.id, 'Conserve', 'Conserve e sughi', 2, TRUE FROM categorie c WHERE c.nome = 'Alimentari'
ON DUPLICATE KEY UPDATE nome=nome;

INSERT INTO sottocategorie (categoria_id, nome, descrizione, ordine, attiva)
SELECT c.id, 'Acqua', 'Acqua minerale', 1, TRUE FROM categorie c WHERE c.nome = 'Bevande'
ON DUPLICATE KEY UPDATE nome=nome;

INSERT INTO sottocategorie (categoria_id, nome, descrizione, ordine, attiva)
SELECT c.id, 'Bibite', 'Bibite gassate e non', 2, TRUE FROM categorie c WHERE c.nome = 'Bevande'
ON DUPLICATE KEY UPDATE nome=nome;

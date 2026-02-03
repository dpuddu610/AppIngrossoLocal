package com.ingrosso.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Movimento {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty prodottoId = new SimpleIntegerProperty();
    private final IntegerProperty magazzinoId = new SimpleIntegerProperty();
    private final IntegerProperty lottoId = new SimpleIntegerProperty();
    private final ObjectProperty<TipoMovimento> tipo = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> quantita = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> quantitaPrecedente = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> quantitaSuccessiva = new SimpleObjectProperty<>();
    private final StringProperty causale = new SimpleStringProperty();
    private final StringProperty documentoRif = new SimpleStringProperty();
    private final IntegerProperty magazzinoDestinazioneId = new SimpleIntegerProperty();
    private final IntegerProperty utenteId = new SimpleIntegerProperty();
    private final StringProperty note = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> dataMovimento = new SimpleObjectProperty<>();

    // Relazioni
    private final ObjectProperty<Prodotto> prodotto = new SimpleObjectProperty<>();
    private final ObjectProperty<Magazzino> magazzino = new SimpleObjectProperty<>();
    private final ObjectProperty<Magazzino> magazzinoDestinazione = new SimpleObjectProperty<>();
    private final ObjectProperty<Lotto> lotto = new SimpleObjectProperty<>();
    private final ObjectProperty<Utente> utente = new SimpleObjectProperty<>();

    public Movimento() {
        setDataMovimento(LocalDateTime.now());
    }

    public Movimento(int prodottoId, int magazzinoId, TipoMovimento tipo, BigDecimal quantita) {
        this();
        setProdottoId(prodottoId);
        setMagazzinoId(magazzinoId);
        setTipo(tipo);
        setQuantita(quantita);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Prodotto ID
    public int getProdottoId() { return prodottoId.get(); }
    public void setProdottoId(int prodottoId) { this.prodottoId.set(prodottoId); }
    public IntegerProperty prodottoIdProperty() { return prodottoId; }

    // Magazzino ID
    public int getMagazzinoId() { return magazzinoId.get(); }
    public void setMagazzinoId(int magazzinoId) { this.magazzinoId.set(magazzinoId); }
    public IntegerProperty magazzinoIdProperty() { return magazzinoId; }

    // Lotto ID
    public int getLottoId() { return lottoId.get(); }
    public void setLottoId(int lottoId) { this.lottoId.set(lottoId); }
    public IntegerProperty lottoIdProperty() { return lottoId; }

    // Tipo
    public TipoMovimento getTipo() { return tipo.get(); }
    public void setTipo(TipoMovimento tipo) { this.tipo.set(tipo); }
    public ObjectProperty<TipoMovimento> tipoProperty() { return tipo; }

    // Quantita
    public BigDecimal getQuantita() { return quantita.get(); }
    public void setQuantita(BigDecimal quantita) { this.quantita.set(quantita); }
    public ObjectProperty<BigDecimal> quantitaProperty() { return quantita; }

    // Quantita Precedente
    public BigDecimal getQuantitaPrecedente() { return quantitaPrecedente.get(); }
    public void setQuantitaPrecedente(BigDecimal quantitaPrecedente) { this.quantitaPrecedente.set(quantitaPrecedente); }
    public ObjectProperty<BigDecimal> quantitaPrecedenteProperty() { return quantitaPrecedente; }

    // Quantita Successiva
    public BigDecimal getQuantitaSuccessiva() { return quantitaSuccessiva.get(); }
    public void setQuantitaSuccessiva(BigDecimal quantitaSuccessiva) { this.quantitaSuccessiva.set(quantitaSuccessiva); }
    public ObjectProperty<BigDecimal> quantitaSuccessivaProperty() { return quantitaSuccessiva; }

    // Causale
    public String getCausale() { return causale.get(); }
    public void setCausale(String causale) { this.causale.set(causale); }
    public StringProperty causaleProperty() { return causale; }

    // Documento Riferimento
    public String getDocumentoRif() { return documentoRif.get(); }
    public void setDocumentoRif(String documentoRif) { this.documentoRif.set(documentoRif); }
    public StringProperty documentoRifProperty() { return documentoRif; }

    // Magazzino Destinazione ID
    public int getMagazzinoDestinazioneId() { return magazzinoDestinazioneId.get(); }
    public void setMagazzinoDestinazioneId(int magazzinoDestinazioneId) { this.magazzinoDestinazioneId.set(magazzinoDestinazioneId); }
    public IntegerProperty magazzinoDestinazioneIdProperty() { return magazzinoDestinazioneId; }

    // Utente ID
    public int getUtenteId() { return utenteId.get(); }
    public void setUtenteId(int utenteId) { this.utenteId.set(utenteId); }
    public IntegerProperty utenteIdProperty() { return utenteId; }

    // Note
    public String getNote() { return note.get(); }
    public void setNote(String note) { this.note.set(note); }
    public StringProperty noteProperty() { return note; }

    // Data Movimento
    public LocalDateTime getDataMovimento() { return dataMovimento.get(); }
    public void setDataMovimento(LocalDateTime dataMovimento) { this.dataMovimento.set(dataMovimento); }
    public ObjectProperty<LocalDateTime> dataMovimentoProperty() { return dataMovimento; }

    // Prodotto (relazione)
    public Prodotto getProdotto() { return prodotto.get(); }
    public void setProdotto(Prodotto prodotto) { this.prodotto.set(prodotto); }
    public ObjectProperty<Prodotto> prodottoProperty() { return prodotto; }

    // Magazzino (relazione)
    public Magazzino getMagazzino() { return magazzino.get(); }
    public void setMagazzino(Magazzino magazzino) { this.magazzino.set(magazzino); }
    public ObjectProperty<Magazzino> magazzinoProperty() { return magazzino; }

    // Magazzino Destinazione (relazione)
    public Magazzino getMagazzinoDestinazione() { return magazzinoDestinazione.get(); }
    public void setMagazzinoDestinazione(Magazzino magazzinoDestinazione) { this.magazzinoDestinazione.set(magazzinoDestinazione); }
    public ObjectProperty<Magazzino> magazzinoDestinazioneProperty() { return magazzinoDestinazione; }

    // Lotto (relazione)
    public Lotto getLotto() { return lotto.get(); }
    public void setLotto(Lotto lotto) { this.lotto.set(lotto); }
    public ObjectProperty<Lotto> lottoProperty() { return lotto; }

    // Utente (relazione)
    public Utente getUtente() { return utente.get(); }
    public void setUtente(Utente utente) { this.utente.set(utente); }
    public ObjectProperty<Utente> utenteProperty() { return utente; }
}

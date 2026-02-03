package com.ingrosso.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StoricoPrezzo {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty prodottoId = new SimpleIntegerProperty();
    private final ObjectProperty<TipoListino> tipo = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> prezzoPrecedente = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> prezzoNuovo = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> variazionePercentuale = new SimpleObjectProperty<>();
    private final IntegerProperty utenteId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> dataModifica = new SimpleObjectProperty<>();

    // Relazioni
    private final ObjectProperty<Prodotto> prodotto = new SimpleObjectProperty<>();
    private final ObjectProperty<Utente> utente = new SimpleObjectProperty<>();

    public StoricoPrezzo() {}

    public StoricoPrezzo(int prodottoId, TipoListino tipo, BigDecimal prezzoPrecedente, BigDecimal prezzoNuovo) {
        setProdottoId(prodottoId);
        setTipo(tipo);
        setPrezzoPrecedente(prezzoPrecedente);
        setPrezzoNuovo(prezzoNuovo);
        calcolaVariazione();
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Prodotto ID
    public int getProdottoId() { return prodottoId.get(); }
    public void setProdottoId(int prodottoId) { this.prodottoId.set(prodottoId); }
    public IntegerProperty prodottoIdProperty() { return prodottoId; }

    // Tipo
    public TipoListino getTipo() { return tipo.get(); }
    public void setTipo(TipoListino tipo) { this.tipo.set(tipo); }
    public ObjectProperty<TipoListino> tipoProperty() { return tipo; }

    // Prezzo Precedente
    public BigDecimal getPrezzoPrecedente() { return prezzoPrecedente.get(); }
    public void setPrezzoPrecedente(BigDecimal prezzoPrecedente) { this.prezzoPrecedente.set(prezzoPrecedente); }
    public ObjectProperty<BigDecimal> prezzoPrecedenteProperty() { return prezzoPrecedente; }

    // Prezzo Nuovo
    public BigDecimal getPrezzoNuovo() { return prezzoNuovo.get(); }
    public void setPrezzoNuovo(BigDecimal prezzoNuovo) { this.prezzoNuovo.set(prezzoNuovo); }
    public ObjectProperty<BigDecimal> prezzoNuovoProperty() { return prezzoNuovo; }

    // Variazione Percentuale
    public BigDecimal getVariazionePercentuale() { return variazionePercentuale.get(); }
    public void setVariazionePercentuale(BigDecimal variazionePercentuale) { this.variazionePercentuale.set(variazionePercentuale); }
    public ObjectProperty<BigDecimal> variazionePercentualeProperty() { return variazionePercentuale; }

    // Utente ID
    public int getUtenteId() { return utenteId.get(); }
    public void setUtenteId(int utenteId) { this.utenteId.set(utenteId); }
    public IntegerProperty utenteIdProperty() { return utenteId; }

    // Data Modifica
    public LocalDateTime getDataModifica() { return dataModifica.get(); }
    public void setDataModifica(LocalDateTime dataModifica) { this.dataModifica.set(dataModifica); }
    public ObjectProperty<LocalDateTime> dataModificaProperty() { return dataModifica; }

    // Prodotto (relazione)
    public Prodotto getProdotto() { return prodotto.get(); }
    public void setProdotto(Prodotto prodotto) { this.prodotto.set(prodotto); }
    public ObjectProperty<Prodotto> prodottoProperty() { return prodotto; }

    // Utente (relazione)
    public Utente getUtente() { return utente.get(); }
    public void setUtente(Utente utente) { this.utente.set(utente); }
    public ObjectProperty<Utente> utenteProperty() { return utente; }

    public void calcolaVariazione() {
        if (getPrezzoPrecedente() != null && getPrezzoNuovo() != null
                && getPrezzoPrecedente().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal diff = getPrezzoNuovo().subtract(getPrezzoPrecedente());
            BigDecimal variazione = diff.divide(getPrezzoPrecedente(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            setVariazionePercentuale(variazione);
        }
    }
}

package com.ingrosso.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Giacenza {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty prodottoId = new SimpleIntegerProperty();
    private final IntegerProperty magazzinoId = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> quantita = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Relazioni
    private final ObjectProperty<Prodotto> prodotto = new SimpleObjectProperty<>();
    private final ObjectProperty<Magazzino> magazzino = new SimpleObjectProperty<>();

    public Giacenza() {}

    public Giacenza(int prodottoId, int magazzinoId, BigDecimal quantita) {
        setProdottoId(prodottoId);
        setMagazzinoId(magazzinoId);
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

    // Quantita
    public BigDecimal getQuantita() { return quantita.get(); }
    public void setQuantita(BigDecimal quantita) { this.quantita.set(quantita); }
    public ObjectProperty<BigDecimal> quantitaProperty() { return quantita; }

    // Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    // Prodotto (relazione)
    public Prodotto getProdotto() { return prodotto.get(); }
    public void setProdotto(Prodotto prodotto) { this.prodotto.set(prodotto); }
    public ObjectProperty<Prodotto> prodottoProperty() { return prodotto; }

    // Magazzino (relazione)
    public Magazzino getMagazzino() { return magazzino.get(); }
    public void setMagazzino(Magazzino magazzino) { this.magazzino.set(magazzino); }
    public ObjectProperty<Magazzino> magazzinoProperty() { return magazzino; }
}

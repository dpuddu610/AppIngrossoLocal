package com.ingrosso.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ListinoPrezzo {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty listinoId = new SimpleIntegerProperty();
    private final IntegerProperty prodottoId = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> prezzo = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dataInizio = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dataFine = new SimpleObjectProperty<>();

    // Relazioni
    private final ObjectProperty<Listino> listino = new SimpleObjectProperty<>();
    private final ObjectProperty<Prodotto> prodotto = new SimpleObjectProperty<>();

    public ListinoPrezzo() {}

    public ListinoPrezzo(int listinoId, int prodottoId, BigDecimal prezzo, LocalDate dataInizio) {
        setListinoId(listinoId);
        setProdottoId(prodottoId);
        setPrezzo(prezzo);
        setDataInizio(dataInizio);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Listino ID
    public int getListinoId() { return listinoId.get(); }
    public void setListinoId(int listinoId) { this.listinoId.set(listinoId); }
    public IntegerProperty listinoIdProperty() { return listinoId; }

    // Prodotto ID
    public int getProdottoId() { return prodottoId.get(); }
    public void setProdottoId(int prodottoId) { this.prodottoId.set(prodottoId); }
    public IntegerProperty prodottoIdProperty() { return prodottoId; }

    // Prezzo
    public BigDecimal getPrezzo() { return prezzo.get(); }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo.set(prezzo); }
    public ObjectProperty<BigDecimal> prezzoProperty() { return prezzo; }

    // Data Inizio
    public LocalDate getDataInizio() { return dataInizio.get(); }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio.set(dataInizio); }
    public ObjectProperty<LocalDate> dataInizioProperty() { return dataInizio; }

    // Data Fine
    public LocalDate getDataFine() { return dataFine.get(); }
    public void setDataFine(LocalDate dataFine) { this.dataFine.set(dataFine); }
    public ObjectProperty<LocalDate> dataFineProperty() { return dataFine; }

    // Listino (relazione)
    public Listino getListino() { return listino.get(); }
    public void setListino(Listino listino) { this.listino.set(listino); }
    public ObjectProperty<Listino> listinoProperty() { return listino; }

    // Prodotto (relazione)
    public Prodotto getProdotto() { return prodotto.get(); }
    public void setProdotto(Prodotto prodotto) { this.prodotto.set(prodotto); }
    public ObjectProperty<Prodotto> prodottoProperty() { return prodotto; }

    public boolean isValido() {
        LocalDate oggi = LocalDate.now();
        boolean inizioOk = getDataInizio() == null || !oggi.isBefore(getDataInizio());
        boolean fineOk = getDataFine() == null || !oggi.isAfter(getDataFine());
        return inizioOk && fineOk;
    }
}

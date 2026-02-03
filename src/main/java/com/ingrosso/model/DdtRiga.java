package com.ingrosso.model;

import javafx.beans.property.*;
import java.math.BigDecimal;

public class DdtRiga {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty ddtId = new SimpleIntegerProperty();
    private final IntegerProperty prodottoId = new SimpleIntegerProperty();
    private final IntegerProperty lottoId = new SimpleIntegerProperty();
    private final StringProperty descrizione = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> quantita = new SimpleObjectProperty<>();
    private final StringProperty unitaMisura = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> prezzoUnitario = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> aliquotaIva = new SimpleObjectProperty<>();
    private final IntegerProperty ordine = new SimpleIntegerProperty(0);

    // Relazioni
    private final ObjectProperty<Prodotto> prodotto = new SimpleObjectProperty<>();
    private final ObjectProperty<Lotto> lotto = new SimpleObjectProperty<>();

    public DdtRiga() {}

    public DdtRiga(int prodottoId, BigDecimal quantita) {
        setProdottoId(prodottoId);
        setQuantita(quantita);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // DDT ID
    public int getDdtId() { return ddtId.get(); }
    public void setDdtId(int ddtId) { this.ddtId.set(ddtId); }
    public IntegerProperty ddtIdProperty() { return ddtId; }

    // Prodotto ID
    public int getProdottoId() { return prodottoId.get(); }
    public void setProdottoId(int prodottoId) { this.prodottoId.set(prodottoId); }
    public IntegerProperty prodottoIdProperty() { return prodottoId; }

    // Lotto ID
    public int getLottoId() { return lottoId.get(); }
    public void setLottoId(int lottoId) { this.lottoId.set(lottoId); }
    public IntegerProperty lottoIdProperty() { return lottoId; }

    // Descrizione
    public String getDescrizione() { return descrizione.get(); }
    public void setDescrizione(String descrizione) { this.descrizione.set(descrizione); }
    public StringProperty descrizioneProperty() { return descrizione; }

    // Quantita
    public BigDecimal getQuantita() { return quantita.get(); }
    public void setQuantita(BigDecimal quantita) { this.quantita.set(quantita); }
    public ObjectProperty<BigDecimal> quantitaProperty() { return quantita; }

    // Unita Misura
    public String getUnitaMisura() { return unitaMisura.get(); }
    public void setUnitaMisura(String unitaMisura) { this.unitaMisura.set(unitaMisura); }
    public StringProperty unitaMisuraProperty() { return unitaMisura; }

    // Prezzo Unitario
    public BigDecimal getPrezzoUnitario() { return prezzoUnitario.get(); }
    public void setPrezzoUnitario(BigDecimal prezzoUnitario) { this.prezzoUnitario.set(prezzoUnitario); }
    public ObjectProperty<BigDecimal> prezzoUnitarioProperty() { return prezzoUnitario; }

    // Aliquota IVA
    public BigDecimal getAliquotaIva() { return aliquotaIva.get(); }
    public void setAliquotaIva(BigDecimal aliquotaIva) { this.aliquotaIva.set(aliquotaIva); }
    public ObjectProperty<BigDecimal> aliquotaIvaProperty() { return aliquotaIva; }

    // Ordine
    public int getOrdine() { return ordine.get(); }
    public void setOrdine(int ordine) { this.ordine.set(ordine); }
    public IntegerProperty ordineProperty() { return ordine; }

    // Prodotto (relazione)
    public Prodotto getProdotto() { return prodotto.get(); }
    public void setProdotto(Prodotto prodotto) { this.prodotto.set(prodotto); }
    public ObjectProperty<Prodotto> prodottoProperty() { return prodotto; }

    // Lotto (relazione)
    public Lotto getLotto() { return lotto.get(); }
    public void setLotto(Lotto lotto) { this.lotto.set(lotto); }
    public ObjectProperty<Lotto> lottoProperty() { return lotto; }

    public BigDecimal getImportoRiga() {
        if (getQuantita() == null || getPrezzoUnitario() == null) {
            return BigDecimal.ZERO;
        }
        return getQuantita().multiply(getPrezzoUnitario());
    }

    public ObjectProperty<BigDecimal> importoRigaProperty() {
        return new SimpleObjectProperty<>(getImportoRiga());
    }
}

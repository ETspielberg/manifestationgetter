package org.unidue.ub.libintel.getter.model;

import java.util.ArrayList;
import java.util.List;

public class PrimoResponse {

    private List<PrimoData> electronic = new ArrayList<>();

    private List<PrimoData> print = new ArrayList<>();

    public void addPrimoData(PrimoData primoData) {
        if ("Online Resource".equals(primoData.getType()))
            this.electronic.add(primoData);
        else if ("Physical Item".equals(primoData.getType()))
            this.print.add(primoData);
    }

    public List<PrimoData> getElectronic() {
        return electronic;
    }

    public void setElectronic(List<PrimoData> electronic) {
        this.electronic = electronic;
    }

    public List<PrimoData> getPrint() {
        return print;
    }

    public void setPrint(List<PrimoData> print) {
        this.print = print;
    }
}

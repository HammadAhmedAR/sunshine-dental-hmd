package com.sunrise.clinic.model;

import java.util.List;

public record BillPage(List<Bill> bills, int page, boolean hasNext) {
    public List<Bill> getBills() { return bills; }
    public int getPage() { return page; }
    public boolean isHasNext() { return hasNext; }
}

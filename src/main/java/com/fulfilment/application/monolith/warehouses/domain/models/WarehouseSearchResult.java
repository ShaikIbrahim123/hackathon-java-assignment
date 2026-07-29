package com.fulfilment.application.monolith.warehouses.domain.models;

import java.util.List;

public class WarehouseSearchResult {

    public List<Warehouse> warehouses;

    public long total;

    public int page;

    public int pageSize;


    public WarehouseSearchResult(
            List<Warehouse> warehouses,
            long total,
            int page,
            int pageSize) {

        this.warehouses = warehouses;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}

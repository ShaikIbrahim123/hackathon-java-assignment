package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseSearchCriteria;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseSearchResult;

public interface SearchWarehouseOperation {

    WarehouseSearchResult search(
            WarehouseSearchCriteria criteria);
}

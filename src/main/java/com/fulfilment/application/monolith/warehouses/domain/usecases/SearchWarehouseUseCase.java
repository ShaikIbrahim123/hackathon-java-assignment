package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseSearchCriteria;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseSearchResult;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;


@ApplicationScoped
public class SearchWarehouseUseCase implements SearchWarehouseOperation {


    private static final Logger LOG =
            Logger.getLogger(SearchWarehouseUseCase.class);


    private final WarehouseStore warehouseStore;


    public SearchWarehouseUseCase(WarehouseStore warehouseStore) {
        this.warehouseStore = warehouseStore;
    }


    public WarehouseSearchResult search(
            WarehouseSearchCriteria criteria) {


        LOG.infof(
                "Searching warehouses location=%s page=%s size=%s",
                criteria.location,
                criteria.page,
                criteria.pageSize
        );

        if(criteria == null){
            criteria = new WarehouseSearchCriteria();
        }

        validate(criteria);

        return warehouseStore.search(criteria);
    }



    private void validate(WarehouseSearchCriteria criteria) {


        if(criteria.page < 0) {
            throw new IllegalArgumentException(
                    "Page cannot be negative");
        }


        if(criteria.pageSize <=0 ||
                criteria.pageSize >100) {

            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }


        if(criteria.sortBy != null &&
                !criteria.sortBy.equals("createdAt")
                &&
                !criteria.sortBy.equals("capacity")) {

            throw new IllegalArgumentException(
                    "Invalid sort field");
        }


        if(criteria.sortOrder != null &&
                !criteria.sortOrder.equals("asc")
                &&
                !criteria.sortOrder.equals("desc")) {

            throw new IllegalArgumentException(
                    "Invalid sort order");
        }
    }
}
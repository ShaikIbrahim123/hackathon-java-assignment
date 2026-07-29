package com.fulfilment.application.monolith.warehouses.adapters;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseSearchCriteria;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseSearchResult;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
public class WarehouseSearchIT {


    @Inject
    WarehouseRepository warehouseRepository;


    @Inject
    LocationGateway locationGateway;


    @Inject
    EntityManager em;


    private CreateWarehouseUseCase createWarehouseUseCase;



    @BeforeEach
    @Transactional
    void setup() {

        em.createQuery(
                        "DELETE FROM DbWarehouse")
                .executeUpdate();


        createWarehouseUseCase =
                new CreateWarehouseUseCase(
                        warehouseRepository,
                        locationGateway);
    }



    @Test
    @Transactional
    void shouldExcludeArchivedWarehouses() {


        createWarehouse(
                "SEARCH-ACTIVE-001",
                "AMSTERDAM-001",
                100);


        Warehouse archived =
                new Warehouse();

        archived.businessUnitCode =
                "SEARCH-ARCHIVED-001";

        archived.location =
                "AMSTERDAM-001";

        archived.capacity = 200;

        archived.stock = 20;

        archived.archivedAt =
                java.time.LocalDateTime.now();


        warehouseRepository.create(archived);



        WarehouseSearchResult result =
                warehouseRepository.search(
                        new WarehouseSearchCriteria());



        assertEquals(
                1,
                result.total);


        assertEquals(
                "SEARCH-ACTIVE-001",
                result.warehouses.get(0)
                        .businessUnitCode);
    }





    @Test
    @Transactional
    void shouldFilterByLocation() {


        createWarehouse(
                "LOCATION-001",
                "AMSTERDAM-001",
                100);


        createWarehouse(
                "LOCATION-002",
                "ZWOLLE-001",
                200);



        WarehouseSearchCriteria criteria =
                new WarehouseSearchCriteria();


        criteria.location =
                "AMSTERDAM-001";



        WarehouseSearchResult result =
                warehouseRepository.search(criteria);



        assertEquals(
                1,
                result.warehouses.size());


        assertEquals(
                "LOCATION-001",
                result.warehouses.get(0)
                        .businessUnitCode);
    }





    @Test
    @Transactional
    void shouldFilterCapacityRange() {


        createWarehouse(
                "CAPACITY-001",
                "AMSTERDAM-001",
                100);


        createWarehouse(
                "CAPACITY-002",
                "AMSTERDAM-001",
                500);


        createWarehouse(
                "CAPACITY-003",
                "AMSTERDAM-001",
                1000);



        WarehouseSearchCriteria criteria =
                new WarehouseSearchCriteria();


        criteria.minCapacity = 400;

        criteria.maxCapacity = 700;



        WarehouseSearchResult result =
                warehouseRepository.search(criteria);



        assertEquals(
                1,
                result.total);


        assertEquals(
                500,
                result.warehouses.get(0)
                        .capacity);
    }





    @Test
    @Transactional
    void shouldSortByCapacityDescending() {


        createWarehouse(
                "SORT-001",
                "AMSTERDAM-001",
                100);


        createWarehouse(
                "SORT-002",
                "AMSTERDAM-001",
                900);



        WarehouseSearchCriteria criteria =
                new WarehouseSearchCriteria();


        criteria.sortBy =
                "capacity";


        criteria.sortOrder =
                "desc";



        WarehouseSearchResult result =
                warehouseRepository.search(criteria);



        assertEquals(
                900,
                result.warehouses.get(0)
                        .capacity);
    }





    @Test
    @Transactional
    void shouldApplyPagination() {


        for(int i=0;i<5;i++){

            createWarehouse(
                    "PAGE-"+i,
                    "AMSTERDAM-001",
                    100+i);
        }



        WarehouseSearchCriteria criteria =
                new WarehouseSearchCriteria();


        criteria.page = 0;

        criteria.pageSize = 2;



        WarehouseSearchResult result =
                warehouseRepository.search(criteria);



        assertEquals(
                2,
                result.warehouses.size());


        assertEquals(
                5,
                result.total);
    }




    private void createWarehouse(
            String code,
            String location,
            int capacity){


        Warehouse warehouse =
                new Warehouse();


        warehouse.businessUnitCode =
                code;


        warehouse.location =
                location;


        warehouse.capacity =
                capacity;


        warehouse.stock =
                10;



        createWarehouseUseCase.create(
                warehouse);
    }
}
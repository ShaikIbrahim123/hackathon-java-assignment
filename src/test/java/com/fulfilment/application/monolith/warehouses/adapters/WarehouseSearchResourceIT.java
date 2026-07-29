package com.fulfilment.application.monolith.warehouses.adapters;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@QuarkusTest
public class WarehouseSearchResourceIT {


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


        createWarehouse(
                "REST-WH-001",
                "AMSTERDAM-001",
                90);


        createWarehouse(
                "REST-WH-002",
                "ZWOLLE-001",
                40);
    }



    @Test
    void shouldSearchWarehousesSuccessfully() {

        given()
                .when()
                .get("/warehouse/search")

                .then()
                .statusCode(200)
                .body("total", equalTo(2))
                .body("warehouses.size()", equalTo(2));
    }



    @Test
    void shouldFilterWarehouseByLocation() {

        given()
                .queryParam(
                        "location",
                        "AMSTERDAM-001")

                .when()
                .get("/warehouse/search")

                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body(
                        "warehouses[0].businessUnitCode",
                        equalTo("REST-WH-001"));
    }



    @Test
    void shouldFilterWarehouseByCapacity() {

        given()
                .queryParam(
                        "minCapacity",
                        85)

                .when()
                .get("/warehouse/search")

                .then()
                .statusCode(200)
                .body("total", equalTo(1))
                .body(
                        "warehouses[0].capacity",
                        equalTo(90));
    }



    @Test
    void shouldSupportPagination() {

        given()
                .queryParam(
                        "pageSize",
                        1)

                .when()
                .get("/warehouse/search")

                .then()
                .statusCode(200)
                .body(
                        "pageSize",
                        equalTo(1))
                .body(
                        "warehouses.size()",
                        equalTo(1));
    }



    @Test
    @Transactional
    void shouldExcludeArchivedWarehouses() {


        Warehouse archived =
                createWarehouse(
                        "ARCHIVED-WH",
                        "AMSTERDAM-001",
                        50);


        archived.archivedAt =
                java.time.LocalDateTime.now();


        warehouseRepository.update(archived);



        given()

                .when()
                .get("/warehouse/search")

                .then()
                .statusCode(200)
                .body(
                        "warehouses.businessUnitCode",
                        not(hasItem("ARCHIVED-WH")));
    }



    @Test
    void shouldSortByCapacityDescending() {


        createWarehouse(
                "LOW-CAP",
                "AMSTERDAM-001",
                20);


        createWarehouse(
                "HIGH-CAP",
                "AMSTERDAM-001",
                90);


        createWarehouse(
                "MID-CAP",
                "AMSTERDAM-001",
                50);



        given()
                .queryParam("sortBy","capacity")
                .queryParam("sortOrder","desc")

                .when()
                .get("/warehouse/search")

                .then()
                .statusCode(200)
                .body(
                        "warehouses[0].businessUnitCode",
                        equalTo("HIGH-CAP"));
    }



    @Test
    void shouldReturnPagedResults() {


        createWarehouse(
                "PAGE-1",
                "AMSTERDAM-001",
                10);


        createWarehouse(
                "PAGE-2",
                "AMSTERDAM-001",
                20);


        createWarehouse(
                "PAGE-3",
                "AMSTERDAM-001",
                30);



        given()
                .queryParam("page",0)
                .queryParam("pageSize",2)

                .when()
                .get("/warehouse/search")

                .then()
                .statusCode(200)
                .body(
                        "page",
                        equalTo(0))
                .body(
                        "pageSize",
                        equalTo(2))
                .body(
                        "warehouses.size()",
                        equalTo(2));
    }



    /*
       IMPORTANT:
       This method is package-private.
       Do NOT make it private.
       Quarkus needs to intercept @Transactional.
    */
    @Transactional
    Warehouse createWarehouse(
            String code,
            String location,
            int capacity) {


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



        return warehouse;
    }
}
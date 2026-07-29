package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


@QuarkusTest
public class WarehouseEndpointIT {


  @Inject
  EntityManager entityManager;


  @BeforeEach
  @Transactional
  void resetWarehouseData() {

    entityManager
            .createQuery("DELETE FROM DbWarehouse")
            .executeUpdate();


    DbWarehouse warehouse1 = new DbWarehouse();
    warehouse1.businessUnitCode = "MWH.001";
    warehouse1.location = "ZWOLLE-001";
    warehouse1.capacity = 100;
    warehouse1.stock = 10;
    warehouse1.createdAt = java.time.LocalDateTime.now();

    entityManager.persist(warehouse1);


    DbWarehouse warehouse2 = new DbWarehouse();
    warehouse2.businessUnitCode = "MWH.012";
    warehouse2.location = "AMSTERDAM-001";
    warehouse2.capacity = 50;
    warehouse2.stock = 5;
    warehouse2.createdAt = java.time.LocalDateTime.now();

    entityManager.persist(warehouse2);


    DbWarehouse warehouse3 = new DbWarehouse();
    warehouse3.businessUnitCode = "MWH.023";
    warehouse3.location = "TILBURG-001";
    warehouse3.capacity = 30;
    warehouse3.stock = 27;
    warehouse3.createdAt = java.time.LocalDateTime.now();

    entityManager.persist(warehouse3);


    entityManager.flush();
  }

  @Test
  public void testSimpleListWarehouses() {

    final String path = "warehouse";


    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body(
                    containsString("MWH.001"),
                    containsString("MWH.012"),
                    containsString("MWH.023")
            );
  }


  @Test
  public void testSimpleCheckingArchivingWarehouses() {

    // Enable this test after archive endpoint implementation
  }
}
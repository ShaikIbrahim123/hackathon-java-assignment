package com.fulfilment.application.monolith.warehouses.adapters;


import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


import static org.junit.jupiter.api.Assertions.assertEquals;



@QuarkusTest
public class WarehouseConcurrencyIT {


  @Inject
  WarehouseRepository warehouseRepository;


  @Inject
  CreateWarehouseUseCase createWarehouseUseCase;



  @Test
  void testConcurrentWarehouseCreationWithUniqueCodesSucceeds()
          throws Exception {


    int count = 10;


    ExecutorService executor =
            Executors.newFixedThreadPool(count);



    List<Future<Boolean>> futures =
            new ArrayList<>();



    for(int i=0;i<count;i++){


      int index=i;


      futures.add(
              executor.submit(() -> {


                try {


                  Warehouse warehouse =
                          new Warehouse();


                  warehouse.businessUnitCode =
                          "CONCURRENT-"+index;


                  warehouse.location =
                          "AMSTERDAM-001";


                  warehouse.capacity =
                          50;


                  warehouse.stock =
                          10;



                  QuarkusTransaction
                          .requiringNew()
                          .run(() ->
                                  createWarehouseUseCase
                                          .create(warehouse)
                          );


                  return true;


                }catch(Exception e){

                  return false;

                }

              })
      );
    }



    long success =
            futures.stream()
                    .filter(f -> {

                      try {
                        return f.get();
                      }
                      catch(Exception e){
                        return false;
                      }

                    })
                    .count();



    executor.shutdown();



    assertEquals(
            count,
            success);
  }





  @Test
  void testConcurrentWarehouseCreationWithDuplicateCodeFails()
          throws Exception {



    int count = 5;


    ExecutorService executor =
            Executors.newFixedThreadPool(count);



    AtomicInteger success =
            new AtomicInteger();



    String code =
            "DUPLICATE-"+System.currentTimeMillis();




    List<Future<?>> futures =
            new ArrayList<>();



    for(int i=0;i<count;i++){


      futures.add(
              executor.submit(() -> {


                try {


                  Warehouse warehouse =
                          new Warehouse();


                  warehouse.businessUnitCode =
                          code;


                  warehouse.location =
                          "ZWOLLE-001";


                  warehouse.capacity =
                          30;


                  warehouse.stock =
                          5;



                  QuarkusTransaction
                          .requiringNew()
                          .run(() ->
                                  createWarehouseUseCase
                                          .create(warehouse)
                          );



                  success.incrementAndGet();



                }
                catch(Exception ignored){

                }


              })
      );
    }



    for(Future<?> f:futures){
      f.get();
    }



    executor.shutdown();



    assertEquals(
            1,
            success.get());
  }





  @Test
  void testConcurrentReadsAreNonBlocking()
          throws Exception {



    Warehouse warehouse =
            new Warehouse();



    warehouse.businessUnitCode =
            "READ-TEST-001";


    warehouse.location =
            "AMSTERDAM-001";


    warehouse.capacity =
            100;


    warehouse.stock =
            50;



    QuarkusTransaction
            .requiringNew()
            .run(() ->
                    createWarehouseUseCase
                            .create(warehouse));



    int count =20;


    ExecutorService executor =
            Executors.newFixedThreadPool(count);



    AtomicInteger success =
            new AtomicInteger();



    List<Future<?>> futures =
            new ArrayList<>();



    for(int i=0;i<count;i++){


      futures.add(
              executor.submit(() -> {


                try{


                  Warehouse result =
                          QuarkusTransaction
                                  .requiringNew()
                                  .call(() ->
                                          warehouseRepository
                                                  .findByBusinessUnitCode(
                                                          "READ-TEST-001")
                                  );



                  if(result!=null){
                    success.incrementAndGet();
                  }


                }catch(Exception ignored){}


              })
      );
    }



    for(Future<?> f:futures){
      f.get();
    }



    executor.shutdown();



    assertEquals(
            count,
            success.get());
  }
}
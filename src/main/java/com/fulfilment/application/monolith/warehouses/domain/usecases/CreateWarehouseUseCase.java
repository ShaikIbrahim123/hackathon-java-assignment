package com.fulfilment.application.monolith.warehouses.domain.usecases;


import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;



@ApplicationScoped
public class CreateWarehouseUseCase
        implements CreateWarehouseOperation {


  private final WarehouseRepository repository;

  private final LocationGateway locationGateway;



  public CreateWarehouseUseCase(
          WarehouseRepository repository,
          LocationGateway locationGateway) {

    this.repository = repository;
    this.locationGateway = locationGateway;
  }



  @Override
  @Transactional
  public Warehouse create(Warehouse warehouse) {


    validateWarehouse(warehouse);


    repository.create(warehouse);


    return warehouse;
  }




  private void validateWarehouse(Warehouse warehouse) {


    if(warehouse.businessUnitCode == null ||
            warehouse.businessUnitCode.isBlank()) {

      throw new IllegalArgumentException(
              "Business unit code is mandatory");
    }


    if(warehouse.capacity <=0){

      throw new IllegalArgumentException(
              "Capacity must be positive");
    }


    if(warehouse.stock <0){

      throw new IllegalArgumentException(
              "Stock cannot be negative");
    }


  }

}
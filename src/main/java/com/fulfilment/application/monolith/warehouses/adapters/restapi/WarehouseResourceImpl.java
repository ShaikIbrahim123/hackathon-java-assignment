package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseSearchResult;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseSearchResult;
import com.warehouse.api.beans.WarehouseSearchResponse;
import java.math.BigInteger;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject private SearchWarehouseOperation searchWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    // Convert API model to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      // Create warehouse through use case (includes validations)
      createWarehouseOperation.create(domainWarehouse);
      
      // Return the created warehouse
      return toWarehouseResponse(domainWarehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    // Find warehouse by business unit code
    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);
    
    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }
    
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    // Find warehouse by business unit code
    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);

    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }

    try {
      // Archive warehouse through use case (includes validations)
      archiveWarehouseOperation.archive(domainWarehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    // Convert API model to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = businessUnitCode; // Use businessUnitCode from path
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      // Replace warehouse through use case (includes validations)
      replaceWarehouseOperation.replace(domainWarehouse);

      // Return the updated warehouse
      var updated = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
      return toWarehouseResponse(updated);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  @Override
  public WarehouseSearchResponse searchWarehouses(
          String location,
          BigInteger minCapacity,
          BigInteger maxCapacity,
          String sortBy,
          String sortOrder,
          BigInteger page,
          BigInteger pageSize) {


    var criteria =
            new com.fulfilment.application.monolith
                    .warehouses.domain.models
                    .WarehouseSearchCriteria();


    criteria.location = location;


    if(minCapacity != null) {
      criteria.minCapacity = minCapacity.intValue();
    }


    if(maxCapacity != null) {
      criteria.maxCapacity = maxCapacity.intValue();
    }


    if(sortBy != null) {
      criteria.sortBy = sortBy;
    }


    if(sortOrder != null) {
      criteria.sortOrder = sortOrder;
    }


    if(page != null) {
      criteria.page = page.intValue();
    }


    if(pageSize != null) {
      criteria.pageSize = pageSize.intValue();
    }


    try {

      var result =
              searchWarehouseOperation.search(criteria);


      var response =
              new WarehouseSearchResponse();


      response.setTotal(
              (int) result.total);


      response.setPage(
              result.page);


      response.setPageSize(
              result.pageSize);


      response.setWarehouses(
              result.warehouses
                      .stream()
                      .map(this::toWarehouseResponse)
                      .toList()
      );


      return response;


    } catch (IllegalArgumentException e) {

      throw new WebApplicationException(
              e.getMessage(),
              400);
    }
  }
}

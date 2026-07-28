package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse entity = find("businessUnitCode", warehouse.businessUnitCode)
            .firstResult();

    if (entity == null) {
      throw new IllegalArgumentException(
              "Warehouse not found: " + warehouse.businessUnitCode);
    }

    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;

    // Clear persistence context to see updates in subsequent queries
    getEntityManager().flush();
    getEntityManager().clear();
  }

  @Override
  public void remove(Warehouse warehouse) {

    DbWarehouse dbWarehouse =
            find("businessUnitCode", warehouse.businessUnitCode).firstResult();

    if (dbWarehouse != null) {
      delete(dbWarehouse);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }
}

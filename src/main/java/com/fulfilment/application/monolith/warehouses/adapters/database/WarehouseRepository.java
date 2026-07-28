package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.jboss.logging.Logger;


/**
 * Repository implementation responsible for Warehouse persistence operations,Uses Hibernate Panache for database interaction.
 * Optimistic locking is handled through the managed entity (@Version field).
 */
@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOG = Logger.getLogger(WarehouseRepository.class);

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {

    LOG.infof("Creating warehouse with BusinessUnitCode=%s", warehouse.businessUnitCode);
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    this.persist(dbWarehouse);
    LOG.infof("Warehouse %s created successfully.", warehouse.businessUnitCode);
  }

  @Override
  public void update(Warehouse warehouse) {
    LOG.infof("Updating warehouse %s", warehouse.businessUnitCode);

    /*
      Load the managed entity first. This allows Hibernate's optimistic locking mechanism
      to automatically compare entity versions and throw OptimisticLockException when concurrent updates occur.
     */
    DbWarehouse entity = find("businessUnitCode", warehouse.businessUnitCode).firstResult();

    if (entity == null) {
      LOG.warnf("Warehouse %s not found for update.", warehouse.businessUnitCode);
      throw new IllegalArgumentException(
              "Warehouse not found: " + warehouse.businessUnitCode);
    }

    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;

    this.persist(entity);

    LOG.infof("Warehouse %s updated successfully.", warehouse.businessUnitCode);

    // Clear persistence context to see updates in subsequent queries
    getEntityManager().flush();
    getEntityManager().clear();
  }

  @Override
  public void remove(Warehouse warehouse) {

    DbWarehouse dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();

    if (dbWarehouse != null) {
      delete(dbWarehouse);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    LOG.debugf("Searching warehouse %s", buCode);

    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();

    if (dbWarehouse == null) {

      LOG.debugf("Warehouse %s not found", buCode);

      return null;
    }

    LOG.debugf("Warehouse %s found", buCode);

    return dbWarehouse.toWarehouse();

  }
}

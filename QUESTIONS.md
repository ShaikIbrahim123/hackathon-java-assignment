# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```API Specification Approaches:

OpenAPI generated API vs manually coded API:

OpenAPI Advantages
Contract first approach
Frontend/backend teams can work independently
Automatic documentation
Client SDK generation
Less API inconsistency

OpenAPI Disadvantages
Generated code can be difficult to customize
Extra build complexity
Changes require regeneration


Manual API Advantages:
Full developer control
Faster for small APIs
Easy customization

Disadvantages:
Documentation can become outdated
More boilerplate
Higher chance of inconsistency

Note:
For enterprise systems:
Use OpenAPI for public and critical APIs because contract consistency is more important. Use manual coding for internal/simple APIs.

```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```Testing Strategy:
Based on the priority of the business logic:
There are Various testing strategies:
1. Unit Tests:
Business rules
Validations
Use cases

In Our Code:
CreateWarehouseUseCaseTest
ArchiveWarehouseUseCaseTest
ReplaceWarehouseUseCaseTest


2. Integration Tests:
Database interaction
Transactions
REST endpoints

In Our Code:
WarehouseConcurrencyIT
WarehouseTestcontainersIT


3.Concurrency Tests:
Important assignment focuses on:
Optimistic locking
Transaction management

In our Code:
Concurrent archive + stock update


4.Parameterized Tests:
For multiple validation cases:

In Our Code:
capacity > location max
stock > capacity
invalid location
duplicate warehouse

```
## # Implementation Checkpoint

## Completed Work

### 1. Warehouse Domain Implementation

Implemented and validated warehouse business operations:

✅ Create Warehouse
- Validates unique business unit code
- Validates location existence
- Validates warehouse capacity against location limits
- Validates stock does not exceed capacity
- Sets creation timestamp automatically

✅ Replace Warehouse
- Validates warehouse existence
- Prevents modification of archived warehouses
- Validates new location
- Validates capacity constraints
- Validates stock constraints
- Preserves existing warehouse metadata

✅ Archive Warehouse
- Validates warehouse existence
- Prevents duplicate archive operations
- Sets archive timestamp
- Updates warehouse state correctly


---

## 2. Optimistic Locking Implementation

### Problem Identified

The original repository implementation used JPQL bulk update:

### Solution Implemented

### Updated WarehouseRepository.update() to:

Load the managed Hibernate entity
Apply changes on the entity object
Allow Hibernate @Version handling
Trigger optimistic locking checks automatically
Result

### Concurrent warehouse modifications now behave correctly:
- Both updates cannot silently overwrite each other
-  Hibernate detects stale versions
- OptimisticLockException is thrown when concurrent updates conflict
# ControlCards Management System

## Project Description

ControlCards is a Spring Boot web application designed for managing work cards in a production environment. The system facilitates workflow management between workers, technicians, managers, and administrators through a role-based access control system.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.4.0
- **Spring Cloud**: 2024.0.0 (OpenFeign)
- **Build Tool**: Maven
- **Database**: MySQL (Production), H2 (Testing)
- **Frontend**: Spring MVC + Thymeleaf
- **Security**: Spring Security with role-based access control
- **Testing**: JUnit 5, Mockito, Spring Test, H2

## Application Architecture

The solution consists of two independent Spring Boot applications:

1. **ControlCards** (Main Application) - Port 8000
   - Core backend system for managing work cards, users, workshops, and work centers
   - Provides web interface using Thymeleaf templates
   - Integrates with WorkScheduleService via Feign Client

2. **WorkScheduleService** (REST Microservice) - Port 8001
   - Separate Spring Boot application exposing REST API
   - Manages work schedules for production planning
   - Consumed by ControlCards via Feign Client

## Core Features

### User Management
- Role-based authentication and authorization
- Five distinct roles: ADMIN, PRODUCTION_MANAGER, MANAGER, TECHNICIAN, WORKER
- Soft delete mechanism (activation/deactivation instead of deletion)
- User profile management with workshop assignments

### Card Workflow Management
- **WORKER**: Create new cards for assigned workshops
- **TECHNICIAN**: View and extend created cards with detailed descriptions
- **MANAGER/PRODUCTION_MANAGER**: View extended cards and all cards
- **ADMIN/PRODUCTION_MANAGER**: Close cards (ADMIN can close CREATED or EXTENDED, others only EXTENDED)
- Card status workflow: CREATED → EXTENDED → CLOSED (archived)
- Automatic archiving of closed cards

### Workshop and Work Center Management
- Hierarchical structure: Workshops contain Work Centers
- Soft delete mechanism for data integrity
- Only active workshops/work centers available for new card creation
- Full CRUD operations for ADMIN role

### Work Schedule Integration
- Integration with WorkScheduleService via Feign Client
- View, create, edit, and delete work schedules
- Search functionality with filters: work center, date, shift
- Accessible to all authenticated users (view), ADMIN/PRODUCTION_MANAGER (modify)

### Logging and Audit
- Comprehensive activity logging for all user actions
- Automatic log cleanup scheduler (removes logs older than 90 days)
- Admin interface for viewing all logs

### Caching
- Spring Cache implementation using SimpleCacheManager
- Caching for workshops and work centers to improve performance

### Scheduling
- Cron-based scheduled job: Daily log cleanup at midnight
- Fixed delay scheduled job: Log cleanup scheduler

## Domain Entities

### Main Application (ControlCards)
1. **User** - System users with roles and workshop assignments
2. **Workshop** - Production workshops containing work centers
3. **WorkCenter** - Work centers within workshops
4. **Card** - Work cards with status workflow
5. **ArchivedCard** - Closed cards archive
6. **LogEntry** - Activity logging

### REST Microservice (WorkScheduleService)
1. **WorkSchedule** - Production work schedules

## Security and Roles

- **ADMIN**: Full system access, can manage all entities, view all cards, close any card
- **PRODUCTION_MANAGER**: Can view all cards, close cards, manage schedules
- **MANAGER**: Can view extended cards and all cards for assigned workshops
- **TECHNICIAN**: Can view and extend created cards for assigned workshops
- **WORKER**: Can create cards and view cards for assigned workshops

Security features:
- CSRF protection enabled
- Method-level security with `@PreAuthorize`
- Password hashing with BCrypt
- Soft delete prevents data loss while maintaining access control

## Database

- **Main Application**: MySQL database with UUID primary keys
- **REST Microservice**: Separate MySQL database
- **Testing**: H2 in-memory database
- Entity relationships: User ↔ Workshop (Many-to-Many), Workshop ↔ WorkCenter (One-to-Many)

## Functionalities

### Main Application (6+ valid functionalities)
1. Create work card (WORKER)
2. Extend work card (TECHNICIAN)
3. Close work card (ADMIN/PRODUCTION_MANAGER)
4. Create/Edit/Deactivate user (ADMIN)
5. Create/Edit/Deactivate workshop (ADMIN)
6. Create/Edit/Deactivate work center (ADMIN)
7. Create work schedule (ADMIN/PRODUCTION_MANAGER) - via Feign Client
8. Edit work schedule (ADMIN/PRODUCTION_MANAGER) - via Feign Client
9. Delete work schedule (ADMIN/PRODUCTION_MANAGER) - via Feign Client

### REST Microservice (2+ valid functionalities)
1. Create work schedule (POST)
2. Update work schedule (PUT)
3. Delete work schedule (DELETE)



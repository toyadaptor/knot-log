# knotlog

# dev
```bash
lein with-profile dev repl
lein shadow watch app
```
# Knotlog

A knowledge management system built with Clojure and ClojureScript.

## Clean Architecture

This project follows Clean Architecture principles, organizing code by responsibility rather than by technology:

### Domain Layer (src/knotlog/domain)

The core business entities and rules, independent of any external frameworks or technologies:

- **piece.cljc**: Core entity representing a piece of content
- **link.cljc**: Entity representing a link between pieces
- **file.cljc**: Entity representing a file attached to a piece
- **protocols.cljc**: Interfaces defining the boundaries between layers

### Application Layer (src/knotlog/application)

The use cases and business logic, dependent only on the domain layer:

- **piece_service.clj**: Business logic for pieces
- **link_service.clj**: Business logic for links
- **file_service.clj**: Business logic for files

### Interface Layer (src/knotlog/interface)

Adapters that convert data between the application layer and external frameworks:

- **controllers/piece_controller.clj**: Handles HTTP requests and responses
- **repositories/piece_repository.clj**: Implements the repository interfaces for pieces
- **repositories/link_repository.clj**: Implements the repository interfaces for links
- **repositories/file_repository.clj**: Implements the repository interfaces for files
- **routes/router.clj**: Handles HTTP routing

### Infrastructure Layer (src/knotlog/infrastructure)

External frameworks and drivers:

- **config.clj**: Configuration and dependency injection
- **firebase_storage.clj**: Firebase integration for file storage

## Running the Application

```
lein run
```

## Development

```
lein repl
```

Then in the REPL:

```clojure
(require 'knotlog.main)
(knotlog.main/-main)
```

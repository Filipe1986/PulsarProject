



```mermaid
graph TD
    Transport -->|Depends on| Application
    Application -->|Depends on| Domain
    Test-Integration -->|Depends on| Transport
    Test-Integration -->|Depends on| Application
    Test-Integration -->|Depends on| Domain
    Test-Integration -->|Depends on| PulsarFunctions
```
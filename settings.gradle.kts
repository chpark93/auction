rootProject.name = "auction"

// Infrastructure Servers
include("server-discovery")
include("server-gateway")
include("server-config")

// Business Services
include("user-service")
include("auction-service")
include("payment-service")
include("search-service")
include("chat-service")
include("admin-service")
include("product-service")

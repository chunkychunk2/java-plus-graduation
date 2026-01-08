CREATE DATABASE stat_db;
CREATE DATABASE core_event_service_db;
CREATE DATABASE core_user_service_db;
CREATE DATABASE core_request_service_db;
CREATE DATABASE core_comment_service_db;


GRANT ALL PRIVILEGES ON DATABASE stat_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE core_event_service_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE core_user_service_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE core_request_service_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE core_comment_service_db TO postgres;

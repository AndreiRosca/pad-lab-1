create schema broker;
create sequence broker.broker_sequence;

create table broker.message_queues(id bigint primary key default nextval('broker_sequence'),
	queue_name varchar(255) not null unique);

create table broker.messages (id bigint primary key default nextval('broker_sequence'),
	message_payload text default '', queue_id bigint references message_queues(id));
	
create table broker.message_properties (id bigint primary key default nextval('broker_sequence'),
	property_name varchar(255), property_value varchar(255), message_id bigint not null references messages(id));


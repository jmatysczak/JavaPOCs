create table resource (
    id bigint,
    name varchar(100)
);

create table work (
    id bigint,
    title varchar(100),
    date_start datetime,
    date_finish datetime,
    assigned_to_resource_id bigint
);
create table attachments (
    id bigint primary key auto_increment,
    bucket varchar(100) not null,
    object_key varchar(255) not null,
    content_type varchar(100),
    size bigint not null
);

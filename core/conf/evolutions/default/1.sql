# --- !Ups

create table companies (
    name varchar(128) not null,
    peerGroup double not null,
    id bigint not null primary key auto_increment,
    foundingYear int not null
  );

# --- !Downs
--
-- DDL Schema
--
create table if not exists cliente (
    id      int8        generated by default as identity,
    nome    varchar(40) not null,
    limite  int8        not null default (0),
    saldo   int8        not null default (0),
    primary key (id),
    CONSTRAINT saldo_check check ((saldo + limite) >= 0)
);

create table if not exists transacao (
    id            int8        generated by default as identity,
    descricao     varchar(10) not null,
    tipo          varchar(10) not null,
    valor         int8        not null,
    cliente_id    int8        not null,
    realizada_em  timestamp   not null default (now() at time zone 'utc'),
    primary key (id)
);

alter table if exists transacao
       add constraint FK_transacao_cliente_id
       foreign key (cliente_id)
       references cliente ;

--
-- Initial data
--
--INSERT INTO cliente (nome, limite)
--  VALUES
--    ('o barato sai caro'     , 1000 * 100  ),
--    ('zan corp ltda'         , 800 * 100   ),
--    ('les cruders'           , 10000 * 100 ),
--    ('padaria joia de cocaia', 100000 * 100),
--    ('kid mais'              , 5000 * 100  )
--  ;
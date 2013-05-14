# gameweb schema
 
# --- !Ups

CREATE TABLE game (
    id bigint NOT NULL auto_increment,
    which varchar(255) NOT NULL,
    label varchar(255) NOT NULL,
    last_state bigint NOT NULL,
    deleted char(1) NOT NULL DEFAULT 'N',
    primary key (id)
) CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE poker_game (
    game_id bigint NOT NULL references game(id),
    num_players smallint,
    primary key (game_id)
) CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE poker_state (
    state_id bigint NOT NULL auto_increment,
    game_id bigint NOT NULL references poker_game(game_id),
    player varchar(64) NOT NULL,
    deck varchar(255) NOT NULL,
    shared varchar(255) NOT NULL,
    num_shown smallint NOT NULL,
    hands varchar(255) NOT NULL,
    pot smallint NOT NULL,
    current_bet smallint NOT NULL,
    still_in varchar(255) NOT NULL,
    in_fors varchar(255) NOT NULL,
    piles varchar(255) NOT NULL,
    outcome varchar(255),
    event_queues varchar(1024),
    primary key (state_id)
) CHARACTER SET utf8 COLLATE utf8_general_ci;
 
# --- !Downs

DROP TABLE poker_state;
DROP TABLE poker_game;
DROP TABLE game;

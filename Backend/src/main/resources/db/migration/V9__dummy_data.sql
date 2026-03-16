insert into users (user_name, password_hash, description, created_at, updated_at)
values
('alice', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'Backend engineer', now(), now()),
('bob', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'Frontend dev', now(), now()),
('charlie', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'DevOps', now(), now()),
('diana', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'Designer', now(), now()),
('eve', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'Security researcher', now(), now()),
('frank', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'Student', now(), now()),
('grace', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'Product manager', now(), now()),
('heidi', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'QA engineer', now(), now()),
('ivan', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'Cloud architect', now(), now()),
('judy', '$2a$10$42LyxehqiKlNcGUO/.y99Otj8f1haZi9GjQdg56NVS9gYPCAUgl3y', 'Open source contributor', now(), now());


insert into guilds (guild_name, description, created_at, updated_at)
values
('Backend Hub', 'Java and Spring discussions', now(), now()),
('Frontend Space', 'UI/UX and JS', now(), now()),
('Cloud Guild', 'AWS, GCP, Azure', now(), now()),
('Security Lab', 'Pentesting and crypto', now(), now()),
('Open Source', 'OSS collaboration', now(), now());



insert into guild_memberships (guild_id, user_id, role, joined_at)
values
(1,1,'owner',now()), (1,2,'member',now()), (1,3,'member',now()),
(2,2,'owner',now()), (2,4,'member',now()),
(3,9,'owner',now()), (3,3,'member',now()),
(4,5,'owner',now()), (5,10,'owner',now());


insert into channels (guild_id, admin_user_id, channel_name, description, created_at, updated_at)
values
(1,1,'general','General backend talk',now(),now()),
(1,1,'spring','Spring Boot help',now(),now()),
(2,2,'design','Design reviews',now(),now()),
(3,9,'aws','AWS deep dives',now(),now()),
(4,5,'crypto','Cryptography',now(),now());


insert into channel_memberships (channel_id, user_id, role, joined_at)
values
(1,1,'owner',now()), (1,2,'member',now()),
(2,1,'owner',now()), (2,3,'member',now()),
(3,2,'owner',now()),
(4,9,'owner',now()),
(5,5,'owner',now());

insert into messages (channel_id, sender_user_id, content, created_at)
values
(1,1,'Welcome to backend hub',now()),
(1,2,'Happy to be here',now()),
(2,3,'Spring is awesome',now()),
(3,4,'New design uploaded',now()),
(4,9,'AWS pricing is wild',now());

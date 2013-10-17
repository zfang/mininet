INSERT INTO SocialNetwork(name) VALUES ("facebook");
INSERT INTO SocialNetwork(name) VALUES ("google");
INSERT INTO SocialNetwork(name) VALUES ("linkedin");

INSERT INTO Interest(name) VALUES ("basketball");
INSERT INTO Interest(name) VALUES ("music");
INSERT INTO Interest(name) VALUES ("fashion");
INSERT INTO Interest(name) VALUES ("math");

INSERT INTO Person VALUES("testtest", "felix", SHA("123456"), COMPRESS("asdfalsdfjalksdj.png"), "Male", "2000-12-12", COMPRESS("felixfangzh@gmail.com"));
INSERT INTO PersonSocialNetwork VALUES("testtest", 1, "testtesttest");
INSERT INTO PersonInterest VALUES("testtest", 1);

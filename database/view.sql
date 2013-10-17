CREATE VIEW PersonInfoWithPassword AS
SELECT id, username, password, CONVERT(UNCOMPRESS(avatar) USING utf8) AS avatar, gender, birthdate, CONVERT(UNCOMPRESS(email) USING utf8) AS email FROM Person;

CREATE VIEW PersonInfo AS
SELECT id, username, avatar, gender, birthdate, email FROM PersonInfoWithPassword;

CREATE VIEW PersonId AS
SELECT id, username FROM Person;

CREATE VIEW PersonBasic AS
SELECT id, avatar, gender, birthdate, email FROM PersonInfo;

CREATE VIEW PersonDetailed AS
SELECT PersonInterest.pid AS id, Interest.name AS interest FROM PersonInterest, Interest
WHERE PersonInterest.iid = Interest.id;

CREATE VIEW PersonSocialNetworkWithName AS
SELECT PersonSocialNetwork.pid AS id, SocialNetwork.name AS socialnetwork, PersonSocialNetwork.snuserid FROM PersonSocialNetwork, SocialNetwork
WHERE PersonSocialNetwork.snid = SocialNetwork.id;

CREATE VIEW GCMRegistrationWithId AS
SELECT pid AS id, CONVERT(UNCOMPRESS(regid) USING utf8) AS regid FROM GCMRegistration;

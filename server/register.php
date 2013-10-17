<?

if (isset($_POST['gcm']) && isset($_POST['id'])) {
   foreach($_POST as $k => $v) $$k = $v;
   require_once __DIR__ . '/db/db_connect.php';
   $db = new DB_CONNECT();

   $result = mysql_query("INSERT INTO GCMRegistration VALUES('$id', COMPRESS('$gcm'))
      ON DUPLICATE KEY UPDATE regid=VALUES(regid);");

   if (!$result) {
      return;
   }

   require_once __DIR__ . '/db/db_util.php';
   sendLocations($id, $gcm);
   sendInterests($id, $gcm);
   return;
}

if (isset($_POST['profile'])) {
   $profiles = json_decode(utf8_encode($_POST['profile']), true);
   $profile = $profiles[0];
   if (isset($profile) 
      && isset($profile['id']) 
      && isset($profile['username']) 
      && (isset($profile['password']) || isset($profile['socialnetworks'])) 
      && isset($profile['avatar']) 
      && isset($profile['gender']) 
      && isset($profile['birthdate']) 
      && isset($profile['email'])) {
         foreach($profile as $k => $v) $$k = $v;
         // Mininet Uesr registration
         if (isset($profile['password'])) {
            require_once __DIR__ . '/db/db_connect.php';
            $db = new DB_CONNECT();

            $result = mysql_query("INSERT INTO Person VALUES( 
               '$id', '$username', SHA('$password'), COMPRESS('$avatar'), '$gender', '$birthdate', COMPRESS('$email'));"); 
            if ($result) {
               require_once __DIR__ . '/db/db_util.php';
               populateInterests($id, $interests);
               echo json_encode('{}');
               return;
            }
         }
         // SN registration
         else {
            $snuserid = null;
            $snname = null;
            foreach ($socialnetworks as $sn) {
               $snuserid = $sn['id'];
               $snname = $sn['name'];
               break;
            }
            if (isset($snuserid) && isset($snname)) {
               require_once __DIR__ . '/db/db_connect.php';
               $db = new DB_CONNECT();

               mysql_query("INSERT INTO SocialNetwork(name) VALUES('$snname');"); 
               $result = mysql_query("SELECT id FROM SocialNetwork WHERE name = '$snname';");
               if (mysql_num_rows($result) > 0) {
                  $row = mysql_fetch_assoc($result);
                  $snid = $row['id'];
                  $result = mysql_query("INSERT INTO Person VALUES(
                     '$id', '$username', NULL, COMPRESS('$avatar'), '$gender', '$birthdate', COMPRESS('$email'));");
                  $result &= mysql_query("INSERT INTO PersonSocialNetwork VALUES('$id', $snid, '$snuserid');"); 
                  if ($result) {
                     require_once __DIR__ . '/db/db_util.php';
                     populateInterests($id, $interests);
                     echo json_encode('{}');
                     return;
                  }
               }
            }
         }
      }
}

$response = array();
$response['error'] = "Incomplete fields";
echo json_encode($response);
?>

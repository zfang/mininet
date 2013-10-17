<?

function addMoreInfo($result) {
   $data = mysql_fetch_assoc($result);
   if (isset($_POST['privacy'])) {
      return $data;
   }

   $data = queryInterests( $data );
   $id = $data['id'];
   $result = mysql_query("SELECT socialnetwork AS name, snuserid AS id FROM PersonSocialNetworkWithName WHERE id = '$id';");
   if (mysql_num_rows($result) > 0) {
      $data['socialnetworks'] = array();
      while ($row = mysql_fetch_assoc($result)) {
         $data['socialnetworks'][] = $row;
      }
   }

   $result = mysql_query("SELECT regid FROM GCMRegistrationWithId WHERE id = '$id';");
   if (mysql_num_rows($result) > 0) {
      $row = mysql_fetch_assoc($result);
      require_once __DIR__ . '/db/db_util.php';
      sendLocations($id, $row['regid']);
   }
   return $data;
}

$query = isset($_POST['privacy']) ? 
   "SELECT id, level, basic, detailed, location FROM
   (SELECT P.*, level, basic, detailed, location FROM Privacy, (SELECT id, password FROM Person) AS P WHERE Privacy.pid = P.id) AS T"
   : "SELECT DISTINCT id, username, avatar, gender, birthdate, email FROM PersonInfoWithPassword AS T";

if (isset($_POST['id']) && isset($_POST['password'])) {
   $id = $_POST['id'];
   $password = $_POST['password'];

   require_once __DIR__ . '/db/db_connect.php';
   require_once __DIR__ . '/db/db_util.php';
   $db = new DB_CONNECT();

   $result = mysql_query($query
      . " WHERE T.id = '$id' 
      AND T.password != NULL
      AND T.password = SHA( '$password' );");

   if (mysql_num_rows($result) > 0) {
      echo '['.json_encode(addMoreInfo($result)).']';
      return;
   } 
}
else if (isset($_POST['id']) && isset($_POST['network'])) {
   $id = $_POST['id'];
   $network = $_POST['network'];

   require_once __DIR__ . '/db/db_connect.php';
   require_once __DIR__ . '/db/db_util.php';
   $db = new DB_CONNECT();

   $result = mysql_query($query
      . " WHERE EXISTS (SELECT 1 FROM PersonSocialNetworkWithName P
      WHERE T.id = P.id
      AND P.snuserid = '$id' 
      AND P.socialnetwork = '$network');");

   if (mysql_num_rows($result) > 0) {
      echo '['.json_encode(addMoreInfo($result)).']';
      return;
   } 
}

?>

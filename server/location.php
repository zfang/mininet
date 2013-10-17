<?
if (isset($_POST['location'])) {
   $location = json_decode(utf8_encode($_POST['location']), true);
   if (isset($location) 
      && isset($location['pid']) 
      && isset($location['longitude']) 
      && isset($location['latitude'])) {
      } 
   foreach($location as $k=>$v) $$k = $v;
   require_once __DIR__ . '/db/db_connect.php';
   $db = new DB_CONNECT();
   $query = "INSERT INTO PersonLocation VALUES('$pid', '$longitude', '$latitude')
      ON DUPLICATE KEY UPDATE longitude=VALUES(longitude), latitude=VALUES(latitude);";
   $result = mysql_query($query);
   if ($result) {
      $data = array();
      $data['location'] = array();
      $data['location'][] = $location;
      $query = "SELECT regid FROM GCMRegistrationWithId
         WHERE EXISTS (SELECT 1 FROM Friend 
         WHERE Friend.pid2 = '$pid'
         AND Friend.pid1 = GCMRegistrationWithId.id)
         AND EXISTS (SELECT 1 FROM Privacy 
         WHERE Privacy.pid = GCMRegistrationWithId.id 
         AND Privacy.level = 'Everyone' 
         AND Privacy.location = TRUE);";
      $result = mysql_query($query);
      if (mysql_num_rows($result) > 0) {
         require_once __DIR__ . '/util/gcm.php';
         $regIds = array();
         while ($row = mysql_fetch_assoc($result)) {
            $regIds[] = $row['regid'];
         }
         sendToGCM($regIds, $data);
      }
      return;
   }
}

$response = array();
$response['error'] = "Update failed";
echo json_encode($response);
?>

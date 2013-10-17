<?

function queryInterests( $data ) {
   $id = $data['id'];
   $result = mysql_query("SELECT interest FROM PersonDetailed WHERE PersonDetailed.id = '$id';");
   if (mysql_num_rows($result) > 0) {
      $data['interests'] = array();
      while ($row = mysql_fetch_assoc($result)) {
         $data['interests'][] = $row['interest'];
      }
   }


   return $data;
}

function populateInterests( $id, $interests ) {
   foreach($interests as $interest) {
      mysql_query("INSERT INTO Interest(name) VALUES('$interest')");
   }
   $interest_str = implode("','", $interests);
   $result = mysql_query("SELECT id FROM Interest 
      WHERE name in ('$interest_str')
      AND EXISTS (SELECT 1 FROM Privacy
      WHERE Privacy.pid = '$id'
      AND Privacy.detailed = TRUE
      AND Privacy.level in ('Everyone'));");
   if (mysql_num_rows($result) > 0) {
      while ($row = mysql_fetch_assoc($result)) {
         $iid = $row['id'];
         mysql_query("INSERT INTO PersonInterest VALUES('$id', '$iid')");
      }
      return True;
   }
   return $result;
}

function sendLocations( $id, $regid ) {

   $query = "SELECT * FROM PersonLocation
      WHERE EXISTS (SELECT 1 FROM Friend 
      WHERE Friend.pid1 = '$id'
      AND Friend.pid2 = PersonLocation.pid)
      AND EXISTS (SELECT 1 FROM Privacy 
      WHERE Privacy.pid = PersonLocation.pid 
      AND Privacy.level = 'Everyone' 
      AND Privacy.location = TRUE);";
   $result = mysql_query($query);
   if (mysql_num_rows($result) > 0) {
      require_once __DIR__ . '/../util/gcm.php';
      $location = array();
      $location['location'] = array();
      while ($row = mysql_fetch_assoc($result)) {
         $location['location'][] = $row;
      }
      sendToGCM(array($regid), $location);
   }
}

function sendInterests( $id, $regid ) {

   $query = "SELECT name FROM Interest";
   $result = mysql_query($query);
   if (mysql_num_rows($result) > 0) {
      require_once __DIR__ . '/../util/gcm.php';
      $interests = array();
      $interests['interests'] = array();
      while ($row = mysql_fetch_assoc($result)) {
         $interests['interests'][] = $row['name'];
      }
      sendToGCM(array($regid), $interests);
   }
}
?>
